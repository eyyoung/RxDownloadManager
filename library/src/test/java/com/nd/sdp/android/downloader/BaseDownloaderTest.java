package com.nd.sdp.android.downloader;

import com.nd.android.sdp.dm.downloader.BaseDownloader;
import com.nd.android.sdp.dm.exception.DownloadHttpException;
import com.nd.android.sdp.dm.service.presenter.DownloadPresenter;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Administrator on 2015/9/15.
 */
public class BaseDownloaderTest {

    @Test
    public void testGetStream() throws IOException, DownloadHttpException {
        final BaseDownloader baseDownloader = new BaseDownloader();
        final InputStream stream = baseDownloader.getStream("http://apps.pba.cn/apk/Hardware_release.apk", null);
        byte[] bytes = new byte[DownloadPresenter.DEFAULT_BUFFER_SIZE];
        stream.read(bytes, 0, DownloadPresenter.DEFAULT_BUFFER_SIZE);
        stream.close();
        assertTrue(bytes[0] != 0);
    }

    /**
     * 测试返回码是否正确
     */
    @Test
    public void testDownloadFailed() {

    }

    /**
     * 测试断点续传获取长度
     */
    @Test
    public void testDownloadResume() {

    }

}
