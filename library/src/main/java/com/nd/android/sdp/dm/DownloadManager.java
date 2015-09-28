package com.nd.android.sdp.dm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.nd.android.sdp.dm.observer.DownloadObserver;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.pojo.IDownloadInfo;
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
 * @date $date
 */
public enum DownloadManager {

    INSTANCE;

    public void init(Context pContext) {
        DownloadProvider.init(pContext.getApplicationContext());
    }

    /**
     * 开始下载
     *
     * @param pContext         the context
     * @param url              the url
     * @param pDownloadOptions the download options
     * @author Young
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
     * @param pMd5
     * @author Young
     */
    public void start(@NonNull Context pContext,
                      @NonNull String url,
                      @Nullable String pMd5,
                      @NonNull DownloadOptions pDownloadOptions) {
        // TODO: 2015/9/17 需要增加文件已经下载完成的判断逻辑
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (pDownloadOptions == null) {
            return;
        }
        DownloadService.start(pContext, url, pMd5, pDownloadOptions);
    }

    /**
     * 注册下载监听
     *
     * @param pLisener the lisener
     * @author Young
     */
    public void registerDownloadListener(@NonNull Context pContext, @NonNull DownloadObserver.OnDownloadLisener pLisener) {
        if (pContext == null) {
            throw new IllegalArgumentException();
        }
        if (pLisener == null) {
            throw new IllegalArgumentException();
        }
        DownloadObserver.INSTANCE.init(pContext.getApplicationContext().getContentResolver());
        DownloadObserver.INSTANCE.registerProgressListener(pLisener);
    }

    /**
     * 反注册观察者
     *
     * @param pLisener the lisener
     * @author Young
     */
    public void unregisterDownloadListener(@NonNull DownloadObserver.OnDownloadLisener pLisener) {
        DownloadObserver.INSTANCE.unregisterProgressListener(pLisener);
    }

    /**
     * 取消下载
     *
     * @param url the url
     * @author Young
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
     * @author Young
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
            if (cursor.getCurrentSize().equals(cursor.getTotalSize())) {
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
     * @param pContext
     * @param url
     * @return
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
            IDownloadInfo downloadInfo = pClass.newInstance();
            downloadInfo.setCurrentSize(cursor.getCurrentSize());
            downloadInfo.setTotalSize(cursor.getTotalSize());
            downloadInfo.setFilePath(cursor.getFilepath());
            downloadInfo.setMd5(cursor.getMd5());
            downloadInfo.setState(State.fromInt(cursor.getState()));
            downloadInfo.setUrl(cursor.getUrl());
            downloadInfos.put(cursor.getUrl(), downloadInfo);
        }
        cursor.close();
        return downloadInfos;
    }

    /**
     * 获取某个Url的下载信息
     *
     * @param pContext context
     * @param pClass
     * @param url
     * @return the download info
     * @throws IllegalAccessException the illegal access exception
     * @throws InstantiationException the instantiation exception
     */
    public IDownloadInfo getDownloadInfo(@NonNull Context pContext,
                                         @NonNull Class<? extends IDownloadInfo> pClass,
                                         @NonNull String url) throws IllegalAccessException, InstantiationException {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.urlContains(url);
        downloadsSelection.orderById(true);
        IDownloadInfo downloadInfo = pClass.newInstance();
        final DownloadsCursor cursor = downloadsSelection.query(pContext, DownloadsColumns.ALL_COLUMNS);
        if (cursor.getCount() == 0) {
            return downloadInfo;
        }
        cursor.moveToFirst();
        downloadInfo.setCurrentSize(cursor.getCurrentSize());
        downloadInfo.setTotalSize(cursor.getTotalSize());
        downloadInfo.setFilePath(cursor.getFilepath());
        if (cursor.getHttpState() != null) {
            downloadInfo.setHttpState(cursor.getHttpState());
        }
        downloadInfo.setMd5(cursor.getMd5());
        downloadInfo.setState(State.fromInt(cursor.getState()));
        downloadInfo.setUrl(cursor.getUrl());
        cursor.close();
        return downloadInfo;
    }

    /**
     * Pause all.
     *
     * @param pContext the p context
     * @author Young
     */
    public void pauseAll(@NonNull Context pContext) {
        DownloadService.pauseAll(pContext);
    }

}
