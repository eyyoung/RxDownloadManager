package com.nd.android.sdp.dm.downloader;

import com.nd.android.sdp.dm.exception.DownloadHttpException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/9/10.
 */
public interface Downloader {

    /**
     * Retrieves {@link InputStream} of image by URI.
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to {@link com.nd.android.sdp.dm.options.DownloadOptionsBuilder#extraForDownloader(HashMap<String,String>)}
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException                   if some I/O error occurs during getting image stream
     * @throws UnsupportedOperationException if image URI has unsupported scheme(protocol)
     */
    InputStream getStream(String imageUri, Map<String, String> extra) throws IOException, DownloadHttpException;

    /**
     * 获取总长度
     *
     * @return the content length
     */
    long getContentLength();

}
