package com.nd.android.sdp.dm.processor;

import com.nd.android.sdp.dm.downloader.BaseDownloader;
import com.nd.android.sdp.dm.downloader.Downloader;
import com.nd.android.sdp.dm.utils.IoUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认下载处理器，支持断点续传
 *
 * @author Young
 */
public class DefaultDataProcessor implements DataProcessor {

    public static Class<? extends Downloader> DEFAULT_DOWNLOADER = BaseDownloader.class;

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 100;

    public DefaultDataProcessor() {
    }

    @Override
    public boolean processData(String remoteUrl,
                               final File tmpFile,
                               Class<? extends Downloader> downloaderClass,
                               Map<String, String> extraForDownloader,
                               DataProcessorListener dataProcessorListener) throws Throwable {
        long currentSize = 0;
        if (tmpFile.exists()) {
            currentSize = tmpFile.length();
            if (extraForDownloader == null) {
                extraForDownloader = new HashMap<>();
            }
            extraForDownloader.put("Range", "bytes=" + currentSize + "-");
        }
        // 开始下载
        if (downloaderClass == null) {
            downloaderClass = DEFAULT_DOWNLOADER;
        }
        final Downloader downloader;
        boolean loaded = false;
        downloader = downloaderClass.newInstance();
        // 拼凑真实下载路径
        final InputStream downloaderStream = downloader.getStream(remoteUrl, extraForDownloader);
        loaded = IoUtils.copyStreamToFile(downloaderStream, tmpFile, DEFAULT_BUFFER_SIZE, currentSize, downloader.getContentLength(), (current, total) -> {
            final boolean isCanceled = dataProcessorListener.isCanceled();
            if (!isCanceled) {
                dataProcessorListener.onNotifyProgress(current, total);
            }
            return !isCanceled;
        });
        return loaded;
    }

}
