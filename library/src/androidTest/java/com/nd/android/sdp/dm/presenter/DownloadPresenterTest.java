package com.nd.android.sdp.dm.presenter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import com.nd.android.sdp.dm.DownloadListenerAdapter;
import com.nd.android.sdp.dm.DownloadManager;
import com.nd.android.sdp.dm.observer.DownloadObserver;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.options.DownloadOptionsBuilder;
import com.nd.android.sdp.dm.pojo.BaseDownloadInfo;
import com.nd.android.sdp.dm.pojo.IDownloadInfo;
import com.nd.android.sdp.dm.provider.DownloadSQLiteOpenHelper;
import com.nd.android.sdp.dm.provider.downloads.DownloadsColumns;
import com.nd.android.sdp.dm.service.presenter.DownloadPresenter;
import com.nd.android.sdp.dm.state.State;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.UUID;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
            "http://ww2.sinaimg.cn/bmiddle/a71cfe56gw1ew49kdx66lg208c07nx1i.gif",//小文件
            "http://www.texts.io/Texts-0.23.5.msi",
            "http://down.360safe.com/360ap/360freeap_whole_setup_5.3.0.3010.exe",
            "http://betacs.101.com/v0.1/download?dentryId=0c14337f-439a-4ee5-8b42-e8f5cb374f38"
    };

    public static final String WRONG_URL = "http://www.texts.io/Texts-0.23.53.msi";


    private Context mContext;
    private DownloadPresenter mPresenter;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DownloadManager.INSTANCE.init(mContext, null);
        mPresenter = new DownloadPresenter(mContext.getContentResolver());
    }

    /**
     * 测试添加任务
     * {@link DownloadPresenter#addTask(String, String, DownloadOptions)}
     */
    @Test
    public void testAddTask() throws InstantiationException, IllegalAccessException, InterruptedException {
        // 测试有没有添加到数据库来确认有添加任务
        clearDataBase();
        final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
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
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        downloadLisener.getPublishSubject().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertCompleted();
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
    public void testAddTaskRepeat() throws InterruptedException {
        clearDataBase();
        final TestOnDownloadLisener downloadLisener = new TestOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        DownloadOptions downloadOptions = new DownloadOptionsBuilder()
                .fileName("test.test")
                .parentDirPath("/sdcard/test/unittest")
                .build();
        mPresenter.addTask(DOWNLOAD_URLS[0], null, downloadOptions);
        //等待2秒再次添加任务
        Thread.sleep(2000);
        boolean isDown = mPresenter.addTask(DOWNLOAD_URLS[0], null, downloadOptions);
        assertEquals(isDown, false);
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
    }

    @Test
    public void testAddTaskRepeatWhenFileExist() throws InterruptedException {
        clearDataBase();
        final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        DownloadOptions downloadOptions = new DownloadOptionsBuilder()
                .fileName("test.test")
                .parentDirPath("/sdcard/test/unittest")
                .build();
        mPresenter.addTask(DOWNLOAD_URLS[2], null, downloadOptions);
        //等待5秒再次添加任务
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        downloadLisener.getPublishSubject().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        // 有下载
        assertTrue(testSubscriber.getOnNextEvents().size() > 0);
        // 确保下载完成
        testSubscriber.assertCompleted();
        boolean isDown = mPresenter.addTask(DOWNLOAD_URLS[2], null, downloadOptions);
        assertEquals(isDown, false);
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
    }

    private void clearDataBase() {
        mPresenter.pauseAll();
        // 清楚数据库方法，保证测试数据的一致性
        final DownloadSQLiteOpenHelper downloadSQLiteOpenHelper = DownloadSQLiteOpenHelper.getInstance(mContext);
        final SQLiteDatabase writableDatabase = downloadSQLiteOpenHelper.getWritableDatabase();
        writableDatabase.execSQL("delete from " + DownloadsColumns.TABLE_NAME);
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
//        testMd5IfMd5NotExist();
//        testMd5IfMd5ExistButFileDelete();
        testMd5ExistAndFileExist();
    }

    private void clearTestDir() throws Exception {
        deleteDir(new File("/sdcard/test"));
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // http://stackoverflow.com/questions/11539657/open-failed-ebusy-device-or-resource-busy
        dir.renameTo(new File(dir.getParent(), UUID.randomUUID().toString()));
        return dir.delete();
    }

    /**
     * Test md 5 if md 5 not exist.
     *
     * @throws Exception the exception
     * @author Young
     */
    public void testMd5IfMd5NotExist() throws Exception {
        clearDataBase();
        // 清空文件夹
        clearTestDir();
        final TestOnDownloadLisener downloadLisener = new TestOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test.test2")
                .parentDirPath("/sdcard/test/unittest3")
                .build();
        //传入的MD5不存在
        final boolean addTask = mPresenter.addTask(DOWNLOAD_URLS[0], "12345678990a", options);
        assertTrue(addTask);
        Thread.sleep(2000);
        // 有进度
        assertTrue(downloadLisener.hasProgress);
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
    }

    /**
     * MD5存在，但是文件被删除
     * 预期：重新开始下载
     *
     * @throws Exception the exception
     * @author Young
     */
    public void testMd5IfMd5ExistButFileDelete() throws Exception {
        clearDataBase();
        // 清空文件夹
        clearTestDir();
        {
            final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
            DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
            // 先下载一个文件
            DownloadOptions options = new DownloadOptionsBuilder()
                    .fileName("test.test2")
                    .parentDirPath("/sdcard/test/unittest3")
                    .build();
            mPresenter.addTask(DOWNLOAD_URLS[2], "12345678990aa", options);
            TestSubscriber<String> testSubscriber = new TestSubscriber<>();
            downloadLisener.getPublishSubject().subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoErrors();
            // 有下载
            assertTrue(testSubscriber.getOnNextEvents().size() > 0);
            // 确保下载完成
            testSubscriber.assertCompleted();
            DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
            // 删除
            new File(options.getParentDirPath(), options.getFileName()).delete();
        }
        // 下载第二个文件
        // 数据库读取MD5
        {
            final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[2]);
            if (downloadInfo == null) {
                throw new Exception("Url Error");
            }
            String md5 = downloadInfo.getMd5();
            final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
            DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
            // 添加该MD5任务
            DownloadOptions options = new DownloadOptionsBuilder()
                    .fileName("test.test2")
                    .parentDirPath("/sdcard/test/unittest3")
                    .build();
            mPresenter.addTask(DOWNLOAD_URLS[1], md5, options);
            TestSubscriber<String> testSubscriber = new TestSubscriber<>();
            downloadLisener.getPublishSubject().subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoErrors();
            // 有下载
            assertTrue(testSubscriber.getOnNextEvents().size() > 0);
            // 确保下载完成
            testSubscriber.assertCompleted();
            DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
        }
    }

    public void testMd5ExistAndFileExist() throws Exception {
        clearDataBase();
        // 清空文件夹
        clearTestDir();
        {
            final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
            DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
            // 先下载一个文件
            DownloadOptions options = new DownloadOptionsBuilder()
                    .fileName("test.test2")
                    .parentDirPath("/sdcard/test/unittest3")
                    .build();
            mPresenter.addTask(DOWNLOAD_URLS[1], "12345678990aa", options);
            TestSubscriber<String> testSubscriber = new TestSubscriber<>();
            downloadLisener.getPublishSubject().subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoErrors();
            // 有下载
            assertTrue(testSubscriber.getOnNextEvents().size() > 0);
            // 确保下载完成
            testSubscriber.assertCompleted();
            DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
        }
        // 下载第二个文件
        // 数据库读取MD5
        {
            final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[1]);
            if (downloadInfo == null) {
                throw new Exception("Url Error");
            }
            String md5 = downloadInfo.getMd5();
            final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
            DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
            // 添加该MD5任务
            DownloadOptions options = new DownloadOptionsBuilder()
                    .fileName("test.test3")
                    .parentDirPath("/sdcard/test/unittest3")
                    .build();
            mPresenter.addTask(DOWNLOAD_URLS[3], md5, options);
            TestSubscriber<String> testSubscriber = new TestSubscriber<>();
            downloadLisener.getPublishSubject().subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoErrors();
            // 没下载
            // 会通知已经下载完了
            testSubscriber.assertValueCount(1);
            // 确保下载完成
            testSubscriber.assertCompleted();
            DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
            final IDownloadInfo downloadInfo2 = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[3]);
            // 拷贝完成任务，长度一直
            assertEquals(new File(downloadInfo2.getFilePath()).length(), new File(downloadInfo.getFilePath()).length());
        }
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
        clearDataBase();
        clearTestDir();
        {
            final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
            DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
            DownloadOptions options = new DownloadOptionsBuilder()
                    .fileName("test.test2")
                    .parentDirPath("/sdcard/test/unittest3")
                    .build();
            mPresenter.addTask(DOWNLOAD_URLS[0], null, options);
            TestSubscriber<String> testSubscriber = new TestSubscriber<>();
            downloadLisener.getPublishSubject().subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertCompleted();
            DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
        }
        {
            final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
            DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
            DownloadOptions options = new DownloadOptionsBuilder()
                    .fileName("test.test2")
                    .parentDirPath("/sdcard/test/unittest3")
                    .build();
            mPresenter.addTask(DOWNLOAD_URLS[1], null, options);
            TestSubscriber<String> testSubscriber = new TestSubscriber<>();
            downloadLisener.getPublishSubject().subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertCompleted();
            final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[1]);
            assertEquals(downloadInfo.getState(), State.FINISHED);
            String finishName = new File(downloadInfo.getFilePath()).getName();
            assertFalse(options.getFileName().equals(finishName));
            DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
        }
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
        clearDataBase();
        clearTestDir();
        final TestOnDownloadLisener downloadLisener = new TestOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test.test2")
                .parentDirPath("/sdcard/test/unittest2")
                .build();
        //添加一个下载任务
        mPresenter.addTask(DOWNLOAD_URLS[4], null, options);
        //让其下载2.5秒
        Thread.sleep(2500);
        //暂停任务
        mPresenter.pauseDownload(DOWNLOAD_URLS[4]);
        Thread.sleep(2000);
        //暂停0.5秒后保存
        long beforePauseProgress = downloadLisener.progress;
        assertTrue(downloadLisener.isOnPause);
        final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[4]);
        final State state = downloadInfo.getState();
        assertEquals(state, State.PAUSING);

        Thread.sleep(1000);
        mPresenter.addTask(DOWNLOAD_URLS[4], null, options);
        Thread.sleep(1500);
        final IDownloadInfo afterDownloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[4]);
        final State afterState = afterDownloadInfo.getState();
        assertEquals(afterState, State.DOWNLOADING);
        long afterPauseProgress = downloadLisener.progress;
        assertTrue(downloadLisener.isOnProgress);
        assertTrue(afterPauseProgress > beforePauseProgress);
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
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
        clearDataBase();
        clearTestDir();
        final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test.test2")
                .parentDirPath("/sdcard/test/unittest3")
                .build();

        TestSubscriber testSubscriber = new TestSubscriber();
        downloadLisener.getPublishSubject().subscribe(testSubscriber);
        mPresenter.addTask(DOWNLOAD_URLS[1], null, options);
        testSubscriber.awaitTerminalEvent();
        final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[1]);
        assertEquals(downloadInfo.getState(), State.FINISHED);
        String md5 = downloadInfo.getMd5();
        Log.i("MD5:", md5);
        assertTrue(md5 != null && !md5.equals(""));
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
    }

    /**
     * 测试URL错误的情况下是否会抛出onError
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testAddTaskWrongUrl() throws Exception {
        clearDataBase();
        clearTestDir();
        // 观察者是否出发onError
        final TestCompleteOnDownloadLisener downloadLisener = new TestCompleteOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test.test2")
                .parentDirPath("/sdcard/test/unittest2")
                .build();

        mPresenter.addTask(WRONG_URL, null, options);
        TestSubscriber testSubscriber = new TestSubscriber();
        downloadLisener.getPublishSubject().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertError(Throwable.class);
        final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, WRONG_URL);
        final State state = downloadInfo.getState();
        assertEquals(state, State.ERROR);
        assertEquals(downloadInfo.getHttpState(), 404);
        // onError的httpState是否正常？
//        Log.i("ErrorState",downloadLisener.errorState+"");
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
    }

    /**
     * 测试拼凑的Url
     * {@link DownloadPresenter#getDownloadUrl(String, DownloadOptions)}
     *
     * @author Young
     */
    @Test
    public void testGetDownloadUrl() {
        final TestOnDownloadLisener downloadLisener = new TestOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test.test2")
                .parentDirPath("/sdcard/test/unittest2")
                .build();
        mPresenter.addTask(DOWNLOAD_URLS[4], null, options);
        String url = DownloadPresenter.getDownloadUrl(DOWNLOAD_URLS[4], options);
        Log.i("DownloadUrl", url);
        assertTrue(url != null && !url.equals(""));
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
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
        // 触发onPause后不再触发onProgress
        clearDataBase();
        clearTestDir();
        final TestOnDownloadLisener downloadLisener = new TestOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        //初始化下载信息
        DownloadOptions downloadOptions = new DownloadOptionsBuilder()
                .fileName("test.test2")//.tmp
                .parentDirPath("/sdcard/test/unittest2")
                .build();
        //添加任务
        mPresenter.addTask(DOWNLOAD_URLS[4], null, downloadOptions);
        //让其下载1.3秒
        Thread.sleep(1300);
        //是否有进行下载
        assertTrue(downloadLisener.hasProgress);
        //调用暂停方法
        mPresenter.pauseDownload(DOWNLOAD_URLS[4]);
        //获取下载信息
        final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[4]);
        final State state = downloadInfo.getState();
        //是否是暂停状态
        assertEquals(state, State.PAUSING);
        //是否调用了onPause
        assertTrue(downloadLisener.isOnPause);
        //1秒后查看调用了onPause以后是否还会再调用onProgress
        Thread.sleep(2000);
        assertFalse(downloadLisener.isOnProgress);
        // 移除监听
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
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
        clearDataBase();
        clearTestDir();
        final TestOnDownloadLisener downloadLisener = new TestOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, downloadLisener);
        //初始化下载信息
        DownloadOptions downloadOptions = new DownloadOptionsBuilder()
                .fileName("test.test2")//.tmp
                .parentDirPath("/sdcard/test/unittest2")
                .build();
        //添加任务
        mPresenter.addTask(DOWNLOAD_URLS[4], null, downloadOptions);
        //让其下载1.3秒
        Thread.sleep(1300);
        //是否有进行下载
        assertTrue(downloadLisener.hasProgress);
        //取消任务
        mPresenter.cancelDownload(DOWNLOAD_URLS[4]);
        Thread.sleep(1300);
        //是否调用onCancel
        assertTrue(downloadLisener.isCancel);
        //是否还在触发onProgress
        assertFalse(downloadLisener.isOnProgress);
        final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[4]);
        final State state = downloadInfo.getState();
        //是否是取消状态
        assertEquals(state, State.CANCEL);
        DownloadManager.INSTANCE.unregisterDownloadListener(downloadLisener);
    }

    /**
     * 测试方法 {@link DownloadPresenter#pauseAll()}
     *
     * @throws Exception the exception
     * @author Young
     */
    @Test
    public void testPauseAll() throws Exception {
        clearDataBase();
        clearTestDir();
        final TestOnDownloadLisener onDownloadLisener = new TestOnDownloadLisener();
        DownloadManager.INSTANCE.registerDownloadListener(mContext, onDownloadLisener);

        DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test.test2")
                .parentDirPath("/sdcard/test/unittest2")
                .build();
        mPresenter.addTask(DOWNLOAD_URLS[4], null, options);
        mPresenter.addTask(DOWNLOAD_URLS[3], null, options);

        Thread.sleep(3300);
        assertTrue(onDownloadLisener.hasProgress);
        mPresenter.pauseAll();
        final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[4]);
        final State state = downloadInfo.getState();
        //第一个url是否是暂停状态
        assertEquals(state, State.PAUSING);
        final IDownloadInfo downloadInfo2 = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[3]);
        final State state2 = downloadInfo2.getState();
        //第二个url是否是暂停状态
        assertEquals(state2, State.PAUSING);

        //是否还在触发onProgress
        assertFalse(onDownloadLisener.isOnProgress);
        DownloadManager.INSTANCE.unregisterDownloadListener(onDownloadLisener);

    }

    /**
     * 同事开启两个具有相同文件名的任务
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartTwoTaskWithSameName() throws Exception {
        clearTestDir();
        clearDataBase();
        TestCompleteOnDownloadLisener testOnDownloadLisener = new TestCompleteOnDownloadLisener(DOWNLOAD_URLS[1]);
        TestCompleteOnDownloadLisener testOnDownloadLisener2 = new TestCompleteOnDownloadLisener(DOWNLOAD_URLS[0]);
        DownloadManager.INSTANCE.registerDownloadListener(mContext, testOnDownloadLisener);
        DownloadManager.INSTANCE.registerDownloadListener(mContext, testOnDownloadLisener2);
        DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test.test2")
                .parentDirPath("/sdcard/test/unittest2")
                .build();
        mPresenter.addTask(DOWNLOAD_URLS[1], null, options);
        mPresenter.addTask(DOWNLOAD_URLS[0], null, options);
        TestSubscriber testSubscriber = new TestSubscriber();
        testOnDownloadLisener.getPublishSubject().subscribe(testSubscriber);
        TestSubscriber testSubscriber2 = new TestSubscriber();
        testOnDownloadLisener2.getPublishSubject().subscribe(testSubscriber2);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertCompleted();
        testSubscriber2.awaitTerminalEvent();
        testSubscriber2.assertCompleted();
        final IDownloadInfo downloadInfo = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[1]);
        final IDownloadInfo downloadInfo2 = DownloadManager.INSTANCE.getDownloadInfo(mContext, BaseDownloadInfo.class, DOWNLOAD_URLS[0]);
        assertNotEquals(downloadInfo.getFilePath(), downloadInfo2.getFilePath());
        DownloadManager.INSTANCE.unregisterDownloadListener(testOnDownloadLisener);
        DownloadManager.INSTANCE.unregisterDownloadListener(testOnDownloadLisener2);
    }

    private static class TestOnDownloadLisener implements DownloadObserver.OnDownloadLisener {
        boolean hasProgress = false;// 用于确认是否出发onProgress
        boolean isOnPause = false;
        boolean isOnProgress = false;
        boolean isCancel = false;
        boolean isOnError = false;
        int errorState = -999;
        long progress;
        boolean isComplete = false;

        @Override
        public void onPause(String pUrl) {
            isOnPause = true;
            isOnProgress = false;
        }

        @Override
        public void onComplete(String pUrl) {
            isComplete = true;
        }

        @Override
        public void onProgress(String pUrl, long current, long total) {
            hasProgress = current != 0;
            isOnProgress = true;
            progress = current;
            isOnPause = false;
        }

        @Override
        public void onCancel(String pUrl) {
            isCancel = true;
            isOnProgress = false;
        }

        @Override
        public void onError(String pUrl, int httpState) {
            isOnError = true;
            errorState = httpState;
        }
    }

    private static class TestCompleteOnDownloadLisener extends DownloadListenerAdapter {

        PublishSubject<String> mPublishSubject = PublishSubject.create();
        private String mUrl;

        public TestCompleteOnDownloadLisener(String url) {
            mUrl = url;
        }

        public TestCompleteOnDownloadLisener() {
        }

        @Override
        public void onError(String pUrl, int httpState) {
            super.onError(pUrl, httpState);
            if (TextUtils.isEmpty(mUrl)) {
                mPublishSubject.onError(new Exception());
            } else {
                if (pUrl.equals(mUrl)) {
                    mPublishSubject.onError(new Exception());
                }
            }
        }

        @Override
        public void onProgress(String pUrl, long current, long total) {
            if (TextUtils.isEmpty(mUrl)) {
                mPublishSubject.onNext(pUrl);
            } else {
                if (pUrl.equals(mUrl)) {
                    mPublishSubject.onNext(pUrl);
                }
            }
        }

        @Override
        public void onComplete(String pUrl) {
            if (TextUtils.isEmpty(mUrl)) {
                mPublishSubject.onCompleted();
            } else {
                if (pUrl.equals(mUrl)) {
                    mPublishSubject.onCompleted();
                }
            }
        }

        public PublishSubject<String> getPublishSubject() {
            return mPublishSubject;
        }
    }
}
