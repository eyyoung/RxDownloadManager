package com.nd.android.sdp.dm.downloader;

import android.support.v4.util.ArrayMap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2015/9/10.
 */
public interface Downloader {

    /**
     * Retrieves {@link InputStream} of image by URI.
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to {@link com.nd.android.sdp.dm.options.DownloadOptionsBuilder#extraForDownloader(ArrayMap<String,String>)}
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException                   if some I/O error occurs during getting image stream
     * @throws UnsupportedOperationException if image URI has unsupported scheme(protocol)
     */
    InputStream getStream(String imageUri, ArrayMap<String,String> extra) throws IOException;

    /**
     * 获取总长度
     *
     * @return the content length
     */
    long getContentLength();

}
