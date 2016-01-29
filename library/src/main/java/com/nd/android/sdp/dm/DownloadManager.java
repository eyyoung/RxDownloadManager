package com.nd.android.sdp.dm;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.nd.android.sdp.dm.observer.DownloadObserver;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.pojo.IDownloadInfo;
import com.nd.android.sdp.dm.processor.DataProcessor;
import com.nd.android.sdp.dm.processor.DefaultDataProcessor;
import com.nd.android.sdp.dm.provider.DownloadProvider;
import com.nd.android.sdp.dm.provider.downloads.DownloadsColumns;
import com.nd.android.sdp.dm.provider.downloads.DownloadsCursor;
import com.nd.android.sdp.dm.provider.downloads.DownloadsSelection;
import com.nd.android.sdp.dm.service.DownloadService;
import com.nd.android.sdp.dm.state.State;

import java.io.File;

/**
 * The Download manager.
 *
 * @author Young
 */
public enum DownloadManager {

    INSTANCE;

    private Class<? extends DataProcessor> mDataProcessor;

    public void init(Context context) {
        init(context, null);
    }

    public void init(Context pContext, Class<? extends DataProcessor> dataProcessor) {
        DownloadProvider.init(pContext.getApplicationContext());
        DownloadsColumns.CONTENT_URI = Uri.parse(DownloadProvider.CONTENT_URI_BASE + "/" + DownloadsColumns.TABLE_NAME);
        if (dataProcessor != null) {
            mDataProcessor = dataProcessor;
        }
    }

    /**
     * 开始下载
     *
     * @param pContext         the context
     * @param url              the url
     * @param pDownloadOptions the download options
     */
    public void start(Context pContext, String url, DownloadOptions pDownloadOptions) {
        start(pContext, url, null, pDownloadOptions);
    }

    /**
     * 开始下载
     *
     * @param pContext         the context
     * @param url              the url
     * @param pDownloadOptions the download options
     * @param pMd5             md5
     */
    public void start(@NonNull Context pContext,
                      @NonNull String url,
                      @Nullable String pMd5,
                      @NonNull DownloadOptions pDownloadOptions) {
        // TODO: 2015/9/17 需要增加文件已经下载完成的判断逻辑
        if (TextUtils.isEmpty(url)) {
            return;
        }
        DownloadService.start(pContext, url, pMd5, pDownloadOptions);
    }

    /**
     * 注册下载监听
     *
     * @param pLisener the lisener
     */
    public void registerDownloadListener(@NonNull Context pContext, @NonNull DownloadObserver.OnDownloadLisener pLisener) {
        DownloadObserver.INSTANCE.init(pContext.getApplicationContext().getContentResolver());
        DownloadObserver.INSTANCE.registerProgressListener(pLisener);
    }

    /**
     * 反注册观察者
     *
     * @param pLisener the lisener
     */
    public void unregisterDownloadListener(@NonNull DownloadObserver.OnDownloadLisener pLisener) {
        DownloadObserver.INSTANCE.unregisterProgressListener(pLisener);
    }

    /**
     * 取消下载
     *
     * @param url the url
     */
    public void cancel(@NonNull Context pContext, @NonNull String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        DownloadService.cancel(pContext, url);
    }

    /**
     * 暂停下载
     *
     * @param url the url
     */
    public void pause(@NonNull Context pContext, @NonNull String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        DownloadService.pause(pContext, url);
    }

    /**
     * 根据md5获取已经下载的文件
     *
     * @param pContext pContext
     * @param pMd5     Md5
     * @return 返回空为无此md5文件
     */
    @Nullable
    public File getDownloadedFile(@NonNull Context pContext, @NonNull String pMd5) {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.md5(pMd5);
        downloadsSelection.orderById(true);
        final DownloadsCursor cursor = downloadsSelection.query(pContext, DownloadsColumns.ALL_COLUMNS);
        if (cursor.getCount() != 0) {
            // 将该任务直接索引到旧任务的相同文件路径
            cursor.moveToFirst();
            final Long currentSize = cursor.getCurrentSize();
            final Long totalSize = cursor.getTotalSize();
            if (currentSize != null && currentSize.equals(totalSize)) {
                // 判断文件是否存在
                String filepath = cursor.getFilepath();
                if (!TextUtils.isEmpty(filepath)) {
                    File file = new File(filepath);
                    if (file.exists()) {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据URL获取下载信息
     *
     * @param pContext context
     * @param url      url
     * @return 下载信息列表
     */
    @NonNull
    public ArrayMap<String, IDownloadInfo> getDownloadInfos(@NonNull Context pContext,
                                                            @NonNull Class<? extends IDownloadInfo> pClass,
                                                            @NonNull String... url) throws IllegalAccessException, InstantiationException {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.urlContains(url);
        downloadsSelection.orderById(true);
        ArrayMap<String, IDownloadInfo> downloadInfos = new ArrayMap<>();
        final DownloadsCursor cursor = downloadsSelection.query(pContext, DownloadsColumns.ALL_COLUMNS);
        if (cursor.getCount() == 0) {
            return downloadInfos;
        }
        while (cursor.moveToNext()) {
            IDownloadInfo downloadInfo = cursorToDownloadInfo(pClass, cursor);
            downloadInfos.put(cursor.getUrl(), downloadInfo);
        }
        cursor.close();
        return downloadInfos;
    }

    /**
     * 获取某个Url的下载信息
     *
     * @param pContext context
     * @param pClass   pClass
     * @param url      url
     * @return the download info
     * @throws IllegalAccessException the illegal access exception
     * @throws InstantiationException the instantiation exception
     */
    @Nullable
    public IDownloadInfo getDownloadInfo(@NonNull Context pContext,
                                         @NonNull Class<? extends IDownloadInfo> pClass,
                                         @NonNull String url) throws IllegalAccessException, InstantiationException {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.urlContains(url);
        downloadsSelection.orderById(true);
        final DownloadsCursor cursor = downloadsSelection.query(pContext, DownloadsColumns.ALL_COLUMNS);
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();
        IDownloadInfo downloadInfo = cursorToDownloadInfo(pClass, cursor);
        cursor.close();
        return downloadInfo;
    }

    @NonNull
    private IDownloadInfo cursorToDownloadInfo(@NonNull Class<? extends IDownloadInfo> pClass,
                                               @NonNull DownloadsCursor cursor) throws InstantiationException, IllegalAccessException {
        IDownloadInfo downloadInfo = pClass.newInstance();
        final Long currentSize = cursor.getCurrentSize();
        if (currentSize != null) {
            downloadInfo.setCurrentSize(currentSize);
        }
        final Long totalSize = cursor.getTotalSize();
        if (totalSize != null) {
            downloadInfo.setTotalSize(totalSize);
        }
        downloadInfo.setFilePath(cursor.getFilepath());
        if (cursor.getHttpState() != null) {
            downloadInfo.setHttpState(cursor.getHttpState());
        }
        downloadInfo.setMd5(cursor.getMd5());
        final Integer state = cursor.getState();
        if (state != null) {
            State state1 = State.fromInt(state);
            final String filePath = downloadInfo.getFilePath();
            if (state1 == State.FINISHED
                    && filePath != null
                    && !new File(filePath).exists()) {
                state1 = State.CANCEL;
            }
            downloadInfo.setState(state1);
        } else {
            downloadInfo.setState(State.CANCEL);
        }
        downloadInfo.setUrl(cursor.getUrl());
        return downloadInfo;
    }

    /**
     * Pause all.
     *
     * @param pContext the p context
     */
    public void pauseAll(@NonNull Context pContext) {
        DownloadService.pauseAll(pContext);
    }

    public DataProcessor getDataProcessor() throws IllegalAccessException, InstantiationException {
        if (mDataProcessor == null) {
            return new DefaultDataProcessor();
        }
        return mDataProcessor.newInstance();
    }
}
