package com.nd.sdp.android.downloader;

import com.nd.android.sdp.dm.downloader.BaseDownloader;
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
    public void testGetStream() throws IOException {
        final BaseDownloader baseDownloader = new BaseDownloader();
        final InputStream stream = baseDownloader.getStream("http://apps.pba.cn/apk/Hardware_release.apk", null);
        byte[] bytes = new byte[DownloadPresenter.DEFAULT_BUFFER_SIZE];
        stream.read(bytes, 0, DownloadPresenter.DEFAULT_BUFFER_SIZE);
        stream.close();
        assertTrue(bytes[0] != 0);
    }

}
