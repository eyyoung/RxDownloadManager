package com.nd.android.sdp.dm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

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
    public void start(Context pContext, String url, String pMd5, DownloadOptions pDownloadOptions) {
        DownloadService.start(pContext, url, pMd5, pDownloadOptions);
    }

    /**
     * 注册下载监听
     *
     * @param pLisener the lisener
     * @author Young
     */
    public void registerDownloadListener(Context pContext, DownloadObserver.OnDownloadLisener pLisener) {
        DownloadObserver.INSTANCE.init(pContext.getContentResolver());
        DownloadObserver.INSTANCE.registerProgressListener(pLisener);
    }

    /**
     * 反注册观察者
     *
     * @param pLisener the lisener
     * @author Young
     */
    public void unregisterDownloadListener(DownloadObserver.OnDownloadLisener pLisener) {
        DownloadObserver.INSTANCE.unregisterProgressListener(pLisener);
    }

    /**
     * 取消下载
     *
     * @param url the url
     * @author Young
     */
    public void cancel(Context pContext, String url) {
        DownloadService.cancel(pContext, url);
    }

    /**
     * 暂停下载
     *
     * @param url the url
     * @author Young
     */
    public void pause(Context pContext, @NonNull String url) {
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
    public File getDownloadedFile(Context pContext, String pMd5) {
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
                File file = new File(filepath);
                if (file.exists()) {
                    return file;
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
    public ArrayMap<String, IDownloadInfo> getDownloadInfos(Context pContext,
                                                            Class<? extends IDownloadInfo> pClass,
                                                            String... url) throws IllegalAccessException, InstantiationException {
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

}
