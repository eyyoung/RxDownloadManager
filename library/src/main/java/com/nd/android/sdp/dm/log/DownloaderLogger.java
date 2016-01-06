package com.nd.android.sdp.dm.log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志记录器
 */
public interface DownloaderLogger extends Serializable{

    /**
     * 记录重试次数与重试信息
     *
     * @param url
     * @param headerMap
     * @param throwable
     * @param retryCount
     */
    void logRetry(String url,
                  Map<String, String> headerMap,
                  Throwable throwable,
                  int retryCount);

    /**
     * 记录重试成功
     *
     * @param downloadUrl
     * @param extraForDownloader
     * @param retryCount
     */
    void logRetrySuccess(String downloadUrl, HashMap<String, String> extraForDownloader, int retryCount);
}
