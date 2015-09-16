package com.nd.android.sdp.dm.utils;

import android.support.test.runner.AndroidJUnit4;

import com.nd.android.sdp.dm.downloader.BaseDownloader;
import com.nd.android.sdp.dm.exception.DownloadHttpException;
import com.nd.android.sdp.dm.service.presenter.DownloadPresenter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Administrator on 2015/9/15.
 */
@RunWith(AndroidJUnit4.class)
public class IoUtilsTest {

    @Test
    public void testCopyStreamToFile() throws IOException, DownloadHttpException {
        BaseDownloader baseDownloader = new BaseDownloader();
        final InputStream stream = baseDownloader.getStream("http://ww2.sinaimg.cn/bmiddle/820f43e8gw1ew35dqu4y3g206e06ex6p.gif", null);
        File file = new File("/sdcard/test.test");
        IoUtils.copyStreamToFile(stream, file, DownloadPresenter.DEFAULT_BUFFER_SIZE, 0, baseDownloader.getContentLength(),
                (current, total) -> {
                    assertTrue(total != 0);
                    return true;
                });
        assertTrue(file.length() > 0);
        file.delete();
    }

    @Test
    public void testCopyStreamToFileWhenParentNotExist() throws IOException, DownloadHttpException {
        BaseDownloader baseDownloader = new BaseDownloader();
        final InputStream stream = baseDownloader.getStream("http://apps.pba.cn/apk/Hardware_release.apk", null);
        File file = new File("/sdcard/test/test/test.test");
        final IoUtils.CopyListener copyListener = (current, total) -> {
            assertTrue(total != 0);
            return true;
        };
        IoUtils.copyStreamToFile(stream, file,
                DownloadPresenter.DEFAULT_BUFFER_SIZE, 0,
                baseDownloader.getContentLength(),
                copyListener);
        assertTrue(file.length() > 0);
        file.delete();
    }

}
