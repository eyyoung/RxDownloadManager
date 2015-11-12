package com.nd.android.sdp.dm.service.presenter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.nd.android.sdp.dm.downloader.BaseDownloader;
import com.nd.android.sdp.dm.downloader.Downloader;
import com.nd.android.sdp.dm.exception.DownloadHttpException;
import com.nd.android.sdp.dm.options.ConflictStragedy;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.options.TempFileNameStragedy;
import com.nd.android.sdp.dm.pojo.BaseDownloadInfo;
import com.nd.android.sdp.dm.pojo.IDownloadInfo;
import com.nd.android.sdp.dm.provider.downloads.DownloadsColumns;
import com.nd.android.sdp.dm.provider.downloads.DownloadsContentValues;
import com.nd.android.sdp.dm.provider.downloads.DownloadsCursor;
import com.nd.android.sdp.dm.provider.downloads.DownloadsSelection;
import com.nd.android.sdp.dm.state.State;
import com.nd.android.sdp.dm.utils.IoUtils;
import com.nd.android.sdp.dm.utils.MD5Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * 下载Presenter，负责数据的下载与下载状态的维护
 * Created by young on 2015/9/13.
 */
public class DownloadPresenter {

    private static final int DOWNLOAD_TIMEOUT = 20;  // 下载无数据传输超时
    private static final int RETRY_COUNT = 5; // 重试次数
    private static final int RETRY_TIME_OUT = 2000; // 重试超时
    final protected ContentResolver mContentResolver;

    final private Map<String, Subscription> mUriSubscriptionMap = new HashMap<>();

    public static Class<? extends Downloader> DEFAULT_DOWNLOADER = BaseDownloader.class;
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 100;

    public DownloadPresenter(ContentResolver pContentResolver) {
        mContentResolver = pContentResolver;
    }

    public DownloadsCursor query(@NonNull String url) {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.url(url);
        downloadsSelection.orderById(true);
        return downloadsSelection.query(mContentResolver, DownloadsColumns.ALL_COLUMNS);
    }

    private long insertOrUpdate(@NonNull String pUrl,
                                @NonNull String pFilePath,
                                @Nullable String pMd5,
                                @NonNull String pModuleName,
                                State state,
                                long pCurrentSize,
                                long pTotalsize) {
        DownloadsContentValues contentValues = new DownloadsContentValues();
        contentValues.putUrl(pUrl);
        contentValues.putFilepath(pFilePath);
        contentValues.putModuleName(pModuleName);
        contentValues.putTotalSize(pTotalsize);
        contentValues.putState(state.getValue());
        contentValues.putCurrentSize(pCurrentSize);
        contentValues.putMd5(pMd5);
        contentValues.putCreateTime(new Date());
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.url(pUrl);
        if (downloadsSelection.count(mContentResolver) > 0) {
            return contentValues.update(mContentResolver, downloadsSelection);
        } else {
            final Uri insert = contentValues.insert(mContentResolver);
            return ContentUris.parseId(insert);
        }
    }

    private void updateState(String pUrl, State pState) {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.url(pUrl);
        final DownloadsCursor cursor = downloadsSelection.query(mContentResolver, DownloadsColumns.ALL_COLUMNS);
        if (cursor.getCount() == 0) {
            return;
        }
        cursor.moveToFirst();
        DownloadsContentValues contentValues = new DownloadsContentValues();
        contentValues.putUrl(pUrl);
        contentValues.putFilepath(cursor.getFilepath());
        contentValues.putModuleName(cursor.getModuleName());
        contentValues.putTotalSize(cursor.getTotalSize());
        contentValues.putState(pState.getValue());
        contentValues.putCurrentSize(cursor.getCurrentSize());
        contentValues.putCreateTime(cursor.getCreateTime());
        contentValues.update(mContentResolver, downloadsSelection);
        cursor.close();
    }

    /**
     * 添加任务
     *
     * @param pUrl             下载地址
     * @param md5              文件md5(用于秒下)
     * @param pDownloadOptions the download options
     * @return 是否添加成功
     */
    public boolean addTask(@NonNull final String pUrl,
                           @Nullable String md5,
                           @NonNull final DownloadOptions pDownloadOptions) {
        final Subscription cachedSubscription = mUriSubscriptionMap.get(pUrl);
        if (cachedSubscription != null && !cachedSubscription.isUnsubscribed()) {
            // 已经在做，不做
            return false;
        }
        if (checkExists(pUrl)) {
            insertOrUpdate(pUrl,
                    new File(pDownloadOptions.getParentDirPath(), pDownloadOptions.getFileName()).getAbsolutePath(),
                    md5,
                    pDownloadOptions.getModuleName(),
                    State.FINISHED,
                    0,
                    0);
            return false;
        }
        // 添加任务
        insertOrUpdate(pUrl,
                new File(pDownloadOptions.getParentDirPath(), pDownloadOptions.getFileName()).getAbsolutePath(),
                md5,
                pDownloadOptions.getModuleName(),
                State.DOWNLOADING,
                0,
                0);
        final Subscription subscription = getTaskStream(pUrl, md5, pDownloadOptions)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<BaseDownloadInfo>() {

                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onCompleted() {
                        mUriSubscriptionMap.remove(pUrl);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof DownloadHttpException) {
                            DownloadsSelection downloadsSelection = new DownloadsSelection();
                            downloadsSelection.url(pUrl);
                            // HTTP请求错误
                            final IDownloadInfo downloadInfo = ((DownloadHttpException) e).getDownloadInfo();
                            DownloadsContentValues contentValues = new DownloadsContentValues();
                            contentValues.putUrl(downloadInfo.getUrl());
                            contentValues.putFilepath(downloadInfo.getFilePath());
                            contentValues.putModuleName(pDownloadOptions.getModuleName());
                            contentValues.putTotalSize(downloadInfo.getTotalSize());
                            contentValues.putState(downloadInfo.getState().getValue());
                            contentValues.putCurrentSize(downloadInfo.getCurrentSize());
                            contentValues.putHttpState(downloadInfo.getHttpState());
                            contentValues.putCreateTime(new Date());
                            contentValues.update(mContentResolver, downloadsSelection);
                        } else {
                            updateState(pUrl, State.ERROR);
                        }
                        e.printStackTrace();
                        mUriSubscriptionMap.remove(pUrl);
                    }

                    @Override
                    public void onNext(BaseDownloadInfo downloadInfoInner) {

                    }
                });
        mUriSubscriptionMap.put(pUrl, subscription);
        return true;
    }

    /**
     * 判断任务是否存在
     *
     * @param pUrl url
     */
    private boolean checkExists(@NonNull String pUrl) {
        final DownloadsCursor query = query(pUrl);
        try {
            final int count = query.getCount();
            if (count > 0) {
                query.moveToFirst();
                // 如果是后台被Kill掉，有可能是Downloading，这个时候也应该执行下载操作
                // 在队列中的任务应该由前置任务判断HashMap中的Subsctiption
                // 如果是暂停状态和取消状态，认为不存在，重新下载
                // 综上，只有在任务已经完成的情况下，才不需要执行下载操作
                // 需加上判断文件是否存在
                if (query.getState() == State.FINISHED.getValue()
                        && new File(query.getFilepath()).exists()) {
                    return true;
                }
            }
            return false;
        } finally {
            query.close();
        }
    }

    /**
     * Add task.
     *
     * @author Young
     */
    private Observable<BaseDownloadInfo> getTaskStream(@NonNull final String pUrl, final String md5, @NonNull final DownloadOptions pDownloadOptions) {
        return Observable
                .just(md5)
                .flatMap(pMd5 -> judgeMd5Exist(pUrl, pMd5, pDownloadOptions))
                .flatMap(s -> getDownloadInfoStream(pUrl, pDownloadOptions))
                .retry(judgeRetry())
                .buffer(1000, TimeUnit.MILLISECONDS)
                .filter(downloadInfoInners -> downloadInfoInners != null && downloadInfoInners.size() > 0)
                .map(downloadInfoInners -> downloadInfoInners.get(downloadInfoInners.size() - 1))
                .timeout(DOWNLOAD_TIMEOUT, TimeUnit.SECONDS)
                .map(writeStateToDb(pDownloadOptions));
    }

    @NonNull
    private Func2<Integer, Throwable, Boolean> judgeRetry() {
        return (integer, throwable) -> {
            if (integer > RETRY_COUNT) {
                return false;
            }
            if (throwable instanceof IOException) {
                try {
                    Thread.sleep(RETRY_TIME_OUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        };
    }

    /**
     * 写入数据库
     *
     * @param pDownloadOptions download options
     * @return Func1
     */
    @NonNull
    private Func1<BaseDownloadInfo, BaseDownloadInfo> writeStateToDb(@NonNull DownloadOptions pDownloadOptions) {
        return downloadInfoInner -> {
            insertOrUpdate(downloadInfoInner.url,
                    downloadInfoInner.filePath,
                    downloadInfoInner.md5,
                    pDownloadOptions.getModuleName(),
                    downloadInfoInner.state,
                    downloadInfoInner.currentSize,
                    downloadInfoInner.totalSize);
            return downloadInfoInner;
        };
    }

    /**
     * 判断md5是否存在
     *
     * @return
     */
    @NonNull
    private Observable<String> judgeMd5Exist(String pUrl,
                                             String pMd5,
                                             DownloadOptions pDownloadOptions) {
        return Observable.create(subscriber -> {
            if (!TextUtils.isEmpty(pMd5)) {
                DownloadsCursor cursor = null;
                try {
                    DownloadsSelection downloadsSelection = new DownloadsSelection();
                    downloadsSelection.addRaw("md5=? and state=?", pMd5.toLowerCase(), State.FINISHED.getValue());
                    downloadsSelection.orderById(true);
                    cursor = downloadsSelection.query(mContentResolver, DownloadsColumns.ALL_COLUMNS);
                    if (cursor.getCount() != 0) {
                        // 将该任务直接索引到旧任务的相同文件路径
                        cursor.moveToFirst();
                        // 判断文件是否存在
                        String filepath = cursor.getFilepath();
                        File file = new File(filepath);
                        final File destFile = new File(pDownloadOptions.getParentDirPath(), pDownloadOptions.getFileName());
                        ConflictStragedy conflictStragedy = pDownloadOptions.getConflictStragedy();
                        File realDownloadFile = conflictStragedy.getRepeatFileName(destFile);
                        if (file.exists()) {
                            // TODO: 2015/9/15 监听拷贝，传递进度
                            IoUtils.copyFile(file, realDownloadFile);
                            insertOrUpdate(pUrl,
                                    realDownloadFile.getAbsolutePath(),
                                    pMd5,
                                    pDownloadOptions.getModuleName(),
                                    State.FINISHED,
                                    realDownloadFile.length(),
                                    realDownloadFile.length());
                            subscriber.onCompleted();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                // 不存在，需要向下传递
            }
            subscriber.onNext(pMd5);
            subscriber.onCompleted();
        });
    }

    /**
     * 下载进度流
     *
     * @param pUrl
     * @param pDownloadOptions
     * @return
     */
    @NonNull
    private Observable<BaseDownloadInfo> getDownloadInfoStream(@NonNull final String pUrl,
                                                               @NonNull final DownloadOptions pDownloadOptions) {
        return Observable.create(new Observable.OnSubscribe<BaseDownloadInfo>() {
            @Override
            public void call(final Subscriber<? super BaseDownloadInfo> pSubscriber) {
                // 先拼出假的，最终重命名会判重处理
                File downloadFile = new File(pDownloadOptions.getParentDirPath(), pDownloadOptions.getFileName());
                final TempFileNameStragedy tempFileNameStragedy = pDownloadOptions.getTempFileStragedy();
                String tmpKeyName = tempFileNameStragedy.getTempFileName(pUrl);
                final File tmpFile = new File(pDownloadOptions.getParentDirPath(), tmpKeyName);
                long currentSize = 0;
                HashMap<String, String> extraForDownloader = pDownloadOptions.getExtraForDownloader();
                if (tmpFile.exists()) {
                    currentSize = tmpFile.length();
                    if (extraForDownloader == null) {
                        extraForDownloader = new HashMap<>();
                    }
//                    extraForDownloader.put("RANGE", "bytes=" + currentSize + "-");
                    extraForDownloader.put("Range", "bytes=" + currentSize + "-");
                }
                // 开始下载
                Class<? extends Downloader> downloaderClass = pDownloadOptions.getDownloader();
                if (downloaderClass == null) {
                    downloaderClass = DEFAULT_DOWNLOADER;
                }
                final Downloader downloader;
                try {
                    downloader = downloaderClass.newInstance();
                    // 拼凑真实下载路径
                    String downloadUrl = getDownloadUrl(pUrl, pDownloadOptions);
                    final InputStream downloaderStream = downloader.getStream(downloadUrl, extraForDownloader);
                    boolean loaded = false;
                    try {
                        loaded = IoUtils.copyStreamToFile(downloaderStream, tmpFile, DEFAULT_BUFFER_SIZE, currentSize, downloader.getContentLength(), (current, total) -> {
                            final boolean isCanceled = pSubscriber.isUnsubscribed();
                            if (!isCanceled) {
                                BaseDownloadInfo downloadInfoInner = new BaseDownloadInfo(pUrl,
                                        State.DOWNLOADING,
                                        null,
                                        downloadFile.getAbsolutePath(),
                                        current,
                                        total);
                                pSubscriber.onNext(downloadInfoInner);
                            }
                            return !isCanceled;
                        });
                    } finally {
                        // 真实判重冲突重命名处理
                        ConflictStragedy conflictStragedy = pDownloadOptions.getConflictStragedy();
                        File realDownloadFile = conflictStragedy.getRepeatFileName(downloadFile);
                        final String filePath = realDownloadFile.getAbsolutePath();
                        if (loaded && tmpFile.renameTo(realDownloadFile)) {
                            String fileMd5 = null;
                            try {
                                fileMd5 = MD5Utils.getFileMd5(filePath);
                            } catch (NoSuchAlgorithmException e) {
                                // 计算失败并不影响下载
                                e.printStackTrace();
                            }
                            BaseDownloadInfo downloadInfoInner = new BaseDownloadInfo(pUrl,
                                    State.FINISHED,
                                    fileMd5,
                                    filePath,
                                    currentSize + downloader.getContentLength(),
                                    currentSize + downloader.getContentLength());
                            pSubscriber.onNext(downloadInfoInner);
                        }else{
                            throw new IOException();
                        }
                    }
                    pSubscriber.onCompleted();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    pSubscriber.onError(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    pSubscriber.onError(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    pSubscriber.onError(e);
                } catch (DownloadHttpException e) {
                    e.printStackTrace();
                    BaseDownloadInfo downloadInfoInner = new BaseDownloadInfo(pUrl,
                            State.ERROR,
                            null,
                            downloadFile.getAbsolutePath(),
                            0,
                            0);
                    downloadInfoInner.httpState = e.getHttpCode();
                    DownloadHttpException downloadHttpException = new DownloadHttpException(downloadInfoInner);
                    pSubscriber.onError(downloadHttpException);
                }
            }
        });
    }

    /**
     * 拼凑出真实下载地址
     *
     * @param pUrl
     * @param pDownloadOptions
     * @return
     */
    public static String getDownloadUrl(@NonNull String pUrl, @NonNull DownloadOptions pDownloadOptions) {
        final HashMap<String, String> urlParams = pDownloadOptions.getUrlParams();
        if (urlParams != null && urlParams.size() > 0) {
            Uri.Builder b = Uri.parse(pUrl).buildUpon();
            for (String key : urlParams.keySet()) {
                b.appendQueryParameter(key, urlParams.get(key));
            }
            return b.build().toString();
        }
        return pUrl;
    }

    /**
     * 暂停下载
     *
     * @param pUrl
     */
    public void pauseDownload(@NonNull String pUrl) {
        final Subscription subscription = mUriSubscriptionMap.get(pUrl);
        if (mUriSubscriptionMap.containsKey(pUrl) && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            Log.d("DownloadPresenter", "pause");
        }
        final DownloadsCursor query = query(pUrl);
        query.moveToFirst();
        if (query.getCount() > 0 && query.getState() == State.DOWNLOADING.getValue()) {
            updateState(pUrl, State.PAUSING);
        }
        if (query != null) {
            query.close();
        }
    }

    /**
     * 取消下载（删除临时文件）
     *
     * @param pUrl
     */
    public void cancelDownload(@NonNull String pUrl) {
        final Subscription subscription = mUriSubscriptionMap.get(pUrl);
        if (mUriSubscriptionMap.containsKey(pUrl) && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            final DownloadsCursor query = query(pUrl);
            query.moveToFirst();
            final File file = new File(query.getFilepath());
            query.close();
            file.delete();
            final File tmpFile = new File(file.getAbsolutePath() + ".tmp");
            tmpFile.delete();
        }
        updateState(pUrl, State.CANCEL);
    }

    /**
     * 取消所有任务
     */
    public void pauseAll() {
        final Iterator<String> iterator = mUriSubscriptionMap.keySet().iterator();
        while (iterator.hasNext()) {
            final String pUrl = iterator.next();
            final Subscription subscription = mUriSubscriptionMap.get(pUrl);
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
            updateState(pUrl, State.PAUSING);
        }
        mUriSubscriptionMap.clear();
    }

    public void onDestroy() {
        pauseAll();
    }

    public static void setDefaultDownloader(Class<? extends Downloader> pDefaultDownloader) {
        DEFAULT_DOWNLOADER = pDefaultDownloader;
    }
}
