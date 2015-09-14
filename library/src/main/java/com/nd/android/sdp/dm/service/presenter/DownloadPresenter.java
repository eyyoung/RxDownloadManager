package com.nd.android.sdp.dm.service.presenter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.nd.android.sdp.dm.downloader.BaseDownloader;
import com.nd.android.sdp.dm.downloader.Downloader;
import com.nd.android.sdp.dm.options.ConflictStragedy;
import com.nd.android.sdp.dm.options.DownloadOptions;
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
import rx.schedulers.Schedulers;

/**
 * 下载Presenter，负责数据的下载与下载状态的维护
 * Created by young on 2015/9/13.
 */
public class DownloadPresenter {

    final protected ContentResolver mContentResolver;

    final private Map<String, Subscription> mUriSubscriptionMap = new HashMap<>();

    public static final Class<? extends Downloader> DEFAULT_DOWNLOADER = BaseDownloader.class;
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 100;

    public DownloadPresenter(ContentResolver pContentResolver) {
        mContentResolver = pContentResolver;
    }

    public DownloadsCursor query(String url) {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.url(url);
        downloadsSelection.orderById(true);
        final DownloadsCursor query = downloadsSelection.query(mContentResolver, DownloadsColumns.ALL_COLUMNS);
        return query;
    }

    private long insertOrUpdate(@NonNull String pUrl,
                                @NonNull String pFilePath,
                                @NonNull String pMd5,
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

    private void delete(String url) {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.url(url);
        downloadsSelection.delete(mContentResolver);
    }

    private void updateState(String pUrl, State pState) {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.url(pUrl);
        final DownloadsCursor cursor = downloadsSelection.query(mContentResolver, DownloadsColumns.ALL_COLUMNS);
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
     * @author Young
     */
    public void addTask(@NonNull final String pUrl, String md5, @NonNull final DownloadOptions pDownloadOptions) {
        final Subscription cachedSubscription = mUriSubscriptionMap.get(pUrl);
        if (cachedSubscription != null && !cachedSubscription.isUnsubscribed()) {
            // 已经在做，不做
            return;
        }
        final Subscription subscription = getTaskStream(pUrl, md5, pDownloadOptions)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<DownloadInfoInner>() {

                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(DownloadInfoInner downloadInfoInner) {

                    }
                });
        mUriSubscriptionMap.put(pUrl, subscription);
    }

    /**
     * Add task.
     *
     * @author Young
     */
    public Observable<DownloadInfoInner> getTaskStream(@NonNull final String pUrl, final String md5, @NonNull final DownloadOptions pDownloadOptions) {
        return Observable
                .just(md5)
                .flatMap(pMd5 -> judgeMd5Exist(pUrl, pMd5, pDownloadOptions.getModuleName()))
                .flatMap(s -> getDownloadInfoStream(pUrl, pDownloadOptions))
                .buffer(1000, TimeUnit.MILLISECONDS)
                .map(downloadInfoInners -> downloadInfoInners.get(downloadInfoInners.size() - 1))
                .map(writeStateToDb(pDownloadOptions));
    }

    /**
     * 写入数据库
     *
     * @param pDownloadOptions
     * @return
     */
    @NonNull
    private Func1<DownloadInfoInner, DownloadInfoInner> writeStateToDb(@NonNull DownloadOptions pDownloadOptions) {
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
                                             String moduleName) {
        return Observable.create(subscriber -> {
            if (!TextUtils.isEmpty(pMd5)) {
                DownloadsSelection downloadsSelection = new DownloadsSelection();
                downloadsSelection.md5(pMd5);
                downloadsSelection.orderById(true);
                final DownloadsCursor cursor = downloadsSelection.query(mContentResolver, DownloadsColumns.ALL_COLUMNS);
                if (cursor.getCount() != 0) {
                    // 将该任务直接索引到旧任务的相同文件路径
                    cursor.moveToFirst();
                    if (cursor.getState() == State.FINISHED.getValue()
                            && cursor.getCurrentSize().equals(cursor.getTotalSize())) {
                        // 判断文件是否存在
                        String filepath = cursor.getFilepath();
                        File file = new File(filepath);
                        if (file.exists()) {
                            DownloadsContentValues contentValues = new DownloadsContentValues();
                            contentValues.putMd5(pMd5);
                            contentValues.putUrl(pUrl);
                            contentValues.putFilepath(filepath);
                            contentValues.putCreateTime(new Date());
                            contentValues.putCurrentSize(cursor.getCurrentSize());
                            contentValues.putTotalSize(cursor.getTotalSize());
                            contentValues.putModuleName(moduleName);
                            contentValues.putState(State.FINISHED.getValue());
                            contentValues.insert(mContentResolver);
                            subscriber.onCompleted();
                            return;
                        }
                    }
                }
                // 不存在，需要向下传递
                cursor.close();
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
    private Observable<DownloadInfoInner> getDownloadInfoStream(@NonNull final String pUrl,
                                                                @NonNull final DownloadOptions pDownloadOptions) {
        return Observable.create(new Observable.OnSubscribe<DownloadInfoInner>() {
            @Override
            public void call(final Subscriber<? super DownloadInfoInner> pSubscriber) {
                // 写到数据库
                File downloadFile = new File(pDownloadOptions.getParentDirPath(), pDownloadOptions.getFileName());
                // md5如果相同的话不会走到这里
                ConflictStragedy conflictStragedy = pDownloadOptions.getConflictStragedy();
                downloadFile = conflictStragedy.getRepeatFileName(downloadFile);
                final File tmpFile = new File(downloadFile.getAbsolutePath() + ".tmp");
                final String filePath = downloadFile.getAbsolutePath();
                long currentSize = 0;
                ArrayMap<String, String> extraForDownloader = pDownloadOptions.getExtraForDownloader();
                if (tmpFile.exists()) {
                    currentSize = tmpFile.length();
                    if (extraForDownloader == null) {
                        extraForDownloader = new ArrayMap<>();
                    }
                    extraForDownloader.put("RANGE", "bytes=" + currentSize + "-");
                }
                // 添加任务
                {
                    DownloadInfoInner downloadInfoInner = new DownloadInfoInner(pUrl,
                            State.DOWNLOADING,
                            null,
                            filePath,
                            currentSize, 0);
                    pSubscriber.onNext(downloadInfoInner);
                }
                // 开始下载
                Class<? extends Downloader> downloaderClass = pDownloadOptions.getDownloader();
                if (downloaderClass == null) {
                    downloaderClass = DEFAULT_DOWNLOADER;
                }
                final Downloader downloader;
                try {
                    downloader = downloaderClass.newInstance();
                    final InputStream downloaderStream = downloader.getStream(pUrl, extraForDownloader);
                    boolean loaded = false;
                    try {
                        loaded = IoUtils.copyStreamToFile(downloaderStream, tmpFile, (current, total) -> {
                            final boolean isCanceled = pSubscriber.isUnsubscribed();
                            if (!isCanceled) {
                                DownloadInfoInner downloadInfoInner = new DownloadInfoInner(pUrl,
                                        State.DOWNLOADING,
                                        null,
                                        filePath,
                                        current,
                                        total);
                                pSubscriber.onNext(downloadInfoInner);
                            }
                            return !isCanceled;
                        }, DEFAULT_BUFFER_SIZE, currentSize, downloader.getContentLength());
                    } finally {
                        if (loaded && tmpFile.renameTo(downloadFile)) {
                            String fileMd5 = null;
                            try {
                                fileMd5 = MD5Utils.getFileMd5(filePath);
                            } catch (NoSuchAlgorithmException e) {
                                // 计算失败并不影响下载
                                e.printStackTrace();
                            }
                            DownloadInfoInner downloadInfoInner = new DownloadInfoInner(pUrl,
                                    State.FINISHED,
                                    fileMd5,
                                    filePath,
                                    currentSize + downloader.getContentLength(),
                                    currentSize + downloader.getContentLength());
                            pSubscriber.onNext(downloadInfoInner);
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
                }
            }
        });
    }

    /**
     * 暂停下载
     *
     * @param pUrl
     */
    public void pauseDownload(String pUrl) {
        final Subscription cacheSubscriber = mUriSubscriptionMap.get(pUrl);
        if (mUriSubscriptionMap.containsKey(pUrl) && !cacheSubscriber.isUnsubscribed()) {
            cacheSubscriber.unsubscribe();
            updateState(pUrl, State.PAUSING);
        }
    }

    /**
     * 取消下载（删除临时文件）
     *
     * @param pUrl
     */
    public void cancelDownload(String pUrl) {
        final Subscription cacheSubscriber = mUriSubscriptionMap.get(pUrl);
        if (mUriSubscriptionMap.containsKey(pUrl) && !cacheSubscriber.isUnsubscribed()) {
            cacheSubscriber.unsubscribe();
            updateState(pUrl, State.CANCEL);
            final DownloadsCursor query = query(pUrl);
            query.moveToFirst();
            final File file = new File(query.getFilepath());
            query.close();
            file.delete();
            final File tmpFile = new File(file.getAbsolutePath() + ".tmp");
            tmpFile.delete();
        }
    }

    /**
     * 取消所有任务
     */
    public void pauseAll() {
        final Iterator<String> iterator = mUriSubscriptionMap.keySet().iterator();
        while (iterator.hasNext()) {
            final Subscription subscription = mUriSubscriptionMap.get(iterator.next());
            if (subscription != null && subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }

    private class DownloadInfoInner {
        String url;
        State state;
        long currentSize;
        String filePath;
        long totalSize;
        String md5;

        public DownloadInfoInner(String url,
                                 State state,
                                 String md5,
                                 String pFilePath,
                                 long currentSize,
                                 long totalSize) {
            this.url = url;
            this.state = state;
            this.currentSize = currentSize;
            this.md5 = md5;
            this.filePath = pFilePath;
            this.totalSize = totalSize;
        }

    }

    public void onDestroy() {
        pauseAll();
    }

}
