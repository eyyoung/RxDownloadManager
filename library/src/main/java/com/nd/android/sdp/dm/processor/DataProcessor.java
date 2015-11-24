package com.nd.android.sdp.dm.processor;

import com.nd.android.sdp.dm.downloader.Downloader;

import java.io.File;
import java.util.Map;

/**
 * 数据处理层
 * Url下载到File
 *
 * @author Young
 */
public interface DataProcessor {

    boolean processData(String remoteUrl,
                        final File tmpFile,
                        Class<? extends Downloader> downloaderClass,
                        Map<String, String> extraForDownloader,
                     DataProcessorListener dataProcessorListener) throws Throwable;

}
