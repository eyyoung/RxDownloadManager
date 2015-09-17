package com.nd.android.sdp.dm.presenter;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.nd.android.sdp.dm.DownloadManager;
import com.nd.android.sdp.dm.observer.DownloadObserver;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.options.DownloadOptionsBuilder;
import com.nd.android.sdp.dm.pojo.BaseDownloadInfo;
import com.nd.android.sdp.dm.pojo.IDownloadInfo;
import com.nd.android.sdp.dm.service.presenter.DownloadPresenter;
import com.nd.android.sdp.dm.state.State;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 下载Presenter测试
 * {@link DownloadPresenter}
 *
 * @author Young
 */
@RunWith(AndroidJUnit4.class)
public class DownloadPresenterTest {

    private static String[] DOWNLOAD_URLS = new String[]{
            "http://ww1.sinaimg.cn/bmiddle/d54a1fa7jw1ew39p7sl90g20ak05sqv6.gif",
            "http://ww2.sinaimg.cn/bmiddle/bea3a845gw1ew47xye2csg206p088ha6.gif",
            "http://ww2.sinaimg.cn/bmiddle/a71cfe56gw1ew49kdx66lg208c07nx1i.gif",
            "http://www.texts.io/Texts-0.23.5.msi",
            "http://down.360safe.com/360ap/360freeap_whole_setup_5.3.0.3010.exe",
            "http://betacs.101.com/v0.1/download?dentryId=0c14337f-439a-4ee5-8b42-e8f5cb374f38"
    };

    private Context mContext;
    private DownloadPresenter mPresenter;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DownloadManager.INSTANCE.init(mContext);
        mPresenter = new DownloadPresenter(mContext.getContentResolver());
    }

    /**
     * 测试添加任务
     * {@link DownloadPresenter#addTask(String, String, DownloadOptions)}
     *
     * @author Young
     */
    @Test
    public void testAddTask() throws InstantiationException, IllegalAccessException, InterruptedException {
        // 测试有没有添加到数据库来确认有添加任务
        final TestOnDownloadLisener downloadLisener = new TestOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        DownloadOptions downloadOptions = new DownloadOptionsBuilder()
                .fileName("test.test")
                .parentDirPath("/sdcard/test/unittest")
                .build();
        mPresenter.addTask(DOWNLOAD_URLS[0], null, downloadOptions);
        final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[0]);
        final State state = downloadInfo.getState();
        assertEquals(state, State.DOWNLOADING);
        // 等待2秒触发onProgress
        Thread.sleep(2000);
        assertTrue(downloadLisener.hasProgress);
        // 移除监听
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
    }

    /**
     * 测试重复添加任务
     * {@link DownloadPresenter#addTask(String, String, DownloadOptions)}
     * A. 文件下载完成 B.正在下载中
     * 预期：不添加任务
     *
     * @author Young
     */
    @Test
    public void testAddTaskRepeat() {

    }

    private void clearDataBase() {
        // 清楚数据库方法，保证测试数据的一致性
    }

    /**
     * 测试Md5
     * A.传入的MD5不存在 预期：任务正常开始
     * B.传入的MD5存在文件不存在 预期：任务正常开始
     * C.传入的MD5为空 预期：任务正常开始
     * D.传入的MD5未下载完成 预期：任务正常开始
     * E.传入的MD5下载完成 预期：任务通过拷贝文件开始
     * 通过注入观察者来观察是否走onProgress来确定是否有走下载进度
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testAddTaskMd5() throws Exception {


    }

    /**
     * 测试当文件名冲突的时候是否自动重命名
     * 通过增加任务，任务结束时onComplete判断传入的文件名是否变动
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testAddTaskCoflictName() throws Exception {

    }

    /**
     * 测试是否断点续传
     * 先下载，后暂停，在下载，通过onProgress观察第一次onPogress是否大于已经存在的文件大小
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testAddTaskForResume() throws Exception {

    }

    /**
     * 测试是否计算MD5
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testAddTaskForCalMd5() throws Exception {
        // 下载完成文件是否有MD5值
    }

    /**
     * 测试URL错误的情况下是否会抛出onError
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testAddTaskWrongUrl() throws Exception {
        // 观察者是否出发onError
        // onError的httpState是否正常？
    }

    /**
     * 测试拼凑的Url
     * {@link DownloadPresenter#getDownloadUrl(String, DownloadOptions)}
     *
     * @author Young
     */
    @Test
    public void testGetDownloadUrl() {

    }

    /**
     * 测试是否暂停任务
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testPauseTask() throws Exception {
        // 检查数据库是否onPause
        // 检查onPause是否触发
        // 出发onPause后不再出发onProgress
    }

    /**
     * 测试是否取消任务
     * {@link DownloadPresenter#cancelDownload(String)}
     * 调用完后检查数据库是否{@link State#CANCEL}
     * 检查是否触发 {@link DownloadObserver.OnDownloadLisener#onProgress(String, long, long)}
     * 触发 {@link DownloadObserver.OnDownloadLisener#onCancel(String)} (String, long, long)}后不再触发 {@link DownloadObserver.OnDownloadLisener#onProgress(String, long, long)}
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testCancelTask() throws Exception {

    }

    /**
     * 测试方法 {@link DownloadPresenter#pauseAll()}
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testPauseAll() throws Exception {

    }

    private static class TestOnDownloadLisener implements DownloadObserver.OnDownloadLisener {
        boolean hasProgress = false;// 用于确认是否出发onProgress

        @Override
        public void onPause(String pUrl) {

        }

        @Override
        public void onComplete(String pUrl) {

        }

        @Override
        public void onProgress(String pUrl, long current, long total) {
            hasProgress = current != 0;
        }

        @Override
        public void onCancel(String pUrl) {

        }

        @Override
        public void onError(String pUrl, int httpState) {

        }
    }
}
