package com.nd.android.sdp.dm.store;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;

import com.nd.android.sdp.dm.downloader.BaseDownloader;
import com.nd.android.sdp.dm.downloader.Downloader;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.provider.downloads.DownloadsColumns;
import com.nd.android.sdp.dm.provider.downloads.DownloadsContentValues;
import com.nd.android.sdp.dm.provider.downloads.DownloadsCursor;
import com.nd.android.sdp.dm.provider.downloads.DownloadsSelection;
import com.nd.android.sdp.dm.state.State;
import com.nd.android.sdp.dm.utils.IoUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * 数据仓库层
 * 负责变更数据库并且通知各观察者
 *
 * @author Young
 */
public class DownloadStore {

    final protected ContentResolver mContentResolver;

    final private Map<String, Subscription> mUriSubscriptionMap = new HashMap<>();
    final private static Set<OnDownloadLisener> mProgressAction = new HashSet<>();
    final private ArrayMap<String, Subject<DownloadInfoInner, DownloadInfoInner>> mSubjectMap = new ArrayMap<>();

    public static final Class<? extends Downloader> DEFAULT_DOWNLOADER = BaseDownloader.class;
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 50;

    public DownloadStore(ContentResolver pContentResolver) {
        mContentResolver = pContentResolver;
        this.mContentResolver.registerContentObserver(
                DownloadsColumns.CONTENT_URI, true, mContentObserver);
    }

    final private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Cursor cursor = mContentResolver.query(uri, DownloadsColumns.ALL_COLUMNS, null, null, null);
            final DownloadsCursor downloadsCursor = new DownloadsCursor(cursor);
            if (downloadsCursor.getCount() == 0) {
                return;
            }
            downloadsCursor.moveToFirst();
            DownloadInfoInner downloadInfoInner = new DownloadInfoInner(downloadsCursor);
            final Subject<DownloadInfoInner, DownloadInfoInner> subject = mSubjectMap.get(downloadsCursor.getUrl());
            subject.onNext(downloadInfoInner);
            // 被取消，被暂停的情况，结束流
            if (downloadInfoInner.state != State.DOWNLOADING) {
                subject.onCompleted();
            }
            downloadsCursor.close();
        }
    };

    private DownloadsCursor query(String url) {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.url(url);
        downloadsSelection.orderById(true);
        final DownloadsCursor query = downloadsSelection.query(mContentResolver, DownloadsColumns.ALL_COLUMNS);
        return query;
    }

    private long insertOrUpdate(@NonNull String pUrl,
                                @NonNull String pFilePath,
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
     * @param pUrl             the p url
     * @param pDownloadOptions the p download options
     * @author Young
     */
    public void addTask(@NonNull final String pUrl, @NonNull final DownloadOptions pDownloadOptions) {
        final Subscription cachedSubscription = mUriSubscriptionMap.get(pUrl);
        if (cachedSubscription != null && !cachedSubscription.isUnsubscribed()) {
            // 已经在做，不做
            return;
        }
        final Subscription subscription = getTaskStream(pUrl, pDownloadOptions)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Pair<Long, Long>>() {

                    @Override
                    public void onStart() {
                        super.onStart();
                        final PublishSubject<DownloadInfoInner> subject = subscribeDownloadListener();
                        mSubjectMap.put(pUrl, subject);
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Pair<Long, Long> t) {

                    }
                });
        mUriSubscriptionMap.put(pUrl, subscription);
    }

    @NonNull
    private PublishSubject<DownloadInfoInner> subscribeDownloadListener() {
        final PublishSubject<DownloadInfoInner> subject = PublishSubject.create();
        subject
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DownloadInfoInner>() {
                    @Override
                    public void call(DownloadInfoInner pDownloadInfoInner) {
                        for (OnDownloadLisener pAction : mProgressAction) {
                            switch (pDownloadInfoInner.state) {
                                case DOWNLOADING:
                                    if (pDownloadInfoInner.totalSize > 0 && pDownloadInfoInner.currentSize > 0) {
                                        pAction.onProgress(pDownloadInfoInner.url,
                                                pDownloadInfoInner.currentSize,
                                                pDownloadInfoInner.totalSize);
                                    }
                                    break;
                                case PAUSING:
                                    pAction.onPause(pDownloadInfoInner.url);
                                    break;
                                case CANCEL:
                                    pAction.onCancel(pDownloadInfoInner.url);
                                    break;
                                case FINISHED:
                                    pAction.onComplete(pDownloadInfoInner.url);
                                    break;
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {

                    }
                });
        return subject;
    }

    /**
     * Add task.
     *
     * @author Young
     */
    public Observable<Pair<Long, Long>> getTaskStream(@NonNull final String pUrl, @NonNull final DownloadOptions pDownloadOptions) {
        return Observable.create(new Observable.OnSubscribe<Pair<Long, Long>>() {
            @Override
            public void call(final Subscriber<? super Pair<Long, Long>> pSubscriber) {
                // 写到数据库
                final File downloadFile = new File(pDownloadOptions.getParentDirPath(), pDownloadOptions.getFileName());
                final File tmpFile = new File(downloadFile.getAbsolutePath() + ".tmp");
                final String filePath = downloadFile.getAbsolutePath();
                long currentSize = 0;
                ArrayMap<String, String> extraForDownloader = pDownloadOptions.getExtraForDownloader();
                if (tmpFile.exists()) {
                    currentSize = tmpFile.length();
                    if (extraForDownloader == null) {
                        extraForDownloader = new ArrayMap<String, String>();
                    }
                    extraForDownloader.put("Ranges", "bytes=" + currentSize + "-");
                }
                insertOrUpdate(pUrl, filePath, pDownloadOptions.getModuleName(), State.DOWNLOADING, currentSize, 0);
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
                        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), DEFAULT_BUFFER_SIZE);
                        try {
                            loaded = IoUtils.copyStream(downloaderStream, os, new IoUtils.CopyListener() {
                                @Override
                                public boolean onBytesCopied(int current, int total) {
                                    final boolean isCanceled = pSubscriber.isUnsubscribed();
                                    if (!isCanceled) {
                                        insertOrUpdate(pUrl, filePath, pDownloadOptions.getModuleName(), State.DOWNLOADING, current, downloader.getContentLength());
                                    }
                                    return !isCanceled;
                                }
                            }, DEFAULT_BUFFER_SIZE);
                        } finally {
                            IoUtils.closeSilently(os);
                        }
                    } finally {
                        if (loaded && tmpFile.renameTo(downloadFile)) {
                            insertOrUpdate(pUrl, filePath, pDownloadOptions.getModuleName(), State.FINISHED, downloader.getContentLength(), downloader.getContentLength());
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

    public void pauseDownload(String pUrl) {
        final Subscription cacheSubscriber = mUriSubscriptionMap.get(pUrl);
        if (!mUriSubscriptionMap.containsKey(pUrl) || cacheSubscriber.isUnsubscribed()) {

        } else {
            cacheSubscriber.unsubscribe();
            updateState(pUrl, State.PAUSING);
        }
    }

    public interface OnDownloadLisener {
        void onPause(String pUrl);

        void onComplete(String pUrl);

        void onProgress(String pUrl, long current, long total);

        void onCancel(String pUrl);
    }

    /**
     * 注册观察者
     *
     * @param pLisener the p lisener
     * @author Young
     */
    public static void registerProgressListener(OnDownloadLisener pLisener) {
        mProgressAction.add(pLisener);
    }

    /**
     * 反注册观察者
     *
     * @param pLisener the p lisener
     * @author Young
     */
    public static void unregisterProgressListener(OnDownloadLisener pLisener) {
        mProgressAction.remove(pLisener);
    }

    public void cancelDownload(String pUrl) {
        final Subscription cacheSubscriber = mUriSubscriptionMap.get(pUrl);
        if (!mUriSubscriptionMap.containsKey(pUrl) || cacheSubscriber.isUnsubscribed()) {

        } else {
            cacheSubscriber.unsubscribe();
            updateState(pUrl, State.CANCEL);
            final DownloadsCursor query = query(pUrl);
            query.moveToFirst();
            final File file = new File(query.getFilepath());
            file.delete();
        }
    }

    public void cancelAll() {
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
        long totalSize;

        public DownloadInfoInner(DownloadsCursor pDownloadsCursor) {
            url = pDownloadsCursor.getUrl();
            state = State.fromInt(pDownloadsCursor.getState());
            currentSize = pDownloadsCursor.getCurrentSize();
            totalSize = pDownloadsCursor.getTotalSize();
        }
    }

    public void onDestroy() {
        final Iterator<String> iterator = mSubjectMap.keySet().iterator();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            final Subject<DownloadInfoInner, DownloadInfoInner> subject = mSubjectMap.get(key);
            if (subject != null) {
                subject.onCompleted();
            }
            iterator.remove();
        }
    }
}
