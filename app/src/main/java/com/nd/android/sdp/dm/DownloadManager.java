package com.nd.android.sdp.dm;

import android.content.Context;

import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.service.DownloadService;

/**
 * The Download manager.
 *
 * @author Young
 */
public enum DownloadManager {

    INSTANCE;

    /**
     * 开始下载
     *
     * @param pContext         the context
     * @param url              the url
     * @param pDownloadOptions the download options
     * @author Young
     */
    public void start(Context pContext, String url, DownloadOptions pDownloadOptions) {
        DownloadService.start(pContext, url, pDownloadOptions);
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
    public void pause(String url) {

    }

}
