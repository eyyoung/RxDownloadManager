package com.nd.android.sdp.dm.store;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.nd.android.sdp.dm.downloader.BaseDownloader;
import com.nd.android.sdp.dm.downloader.Downloader;
import com.nd.android.sdp.dm.exception.DownloadException;
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

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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

    public static final Class<? extends Downloader> DEFAULT_DOWNLOADER = BaseDownloader.class;
    public static final int DEFAULT_BUFFER_SIZE = 32768;

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
            if (downloadsCursor.getCount() > 0 || downloadsCursor.getUrl().isEmpty()) {
                return;
            }
            Observable.from(mProgressAction)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<OnDownloadLisener>() {
                        @Override
                        public void call(OnDownloadLisener pAction) {
                            State state = State.fromInt(downloadsCursor.getState());
                            switch (state) {
                                case DOWNLOADING:
                                    pAction.onProgress(downloadsCursor.getUrl(), downloadsCursor.getCurrentSize(), downloadsCursor.getTotalSize());
                                    break;
                                case PAUSING:
                                    pAction.onPause(downloadsCursor.getUrl());
                                    break;
                                case CANCEL:
                                    pAction.onCancel(downloadsCursor.getUrl());
                                    break;
                                case FINISHED:
                                    pAction.onComplete(downloadsCursor.getUrl());
                                    break;
                            }
                        }
                    });
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
        Uri uri = contentValues.insert(mContentResolver);
        mContentResolver.notifyChange(uri, null);
        return ContentUris.parseId(uri);
    }

    private void delete(String url) {
        DownloadsSelection downloadsSelection = new DownloadsSelection();
        downloadsSelection.url(url);
        downloadsSelection.delete(mContentResolver);
    }

    /**
     * 添加任务
     *
     * @param pUrl             the p url
     * @param pDownloadOptions the p download options
     * @author Young
     */
    public void addTask(@NonNull final String pUrl, @NonNull final DownloadOptions pDownloadOptions) {
        final Subscription subscription = getTaskStream(pUrl, pDownloadOptions)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Pair<Long, Long>>() {
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

    /**
     * Add task.
     *
     * @author Young
     */
    public Observable<Pair<Long, Long>> getTaskStream(@NonNull final String pUrl, @NonNull final DownloadOptions pDownloadOptions) {
        return Observable.create(new Observable.OnSubscribe<Pair<Long, Long>>() {
            @Override
            public void call(final Subscriber<? super Pair<Long, Long>> pSubscriber) {
                final Subscription subscription = mUriSubscriptionMap.get(pUrl);
                if (subscription != null && subscription.isUnsubscribed()) {
                    // 已经在做，不做
                    pSubscriber.onError(new DownloadException());
                    return;
                }
                // 写到数据库
                final String filePath = new File(pDownloadOptions.getParentDirPath(), pDownloadOptions.getFileName()).getAbsolutePath();
                insertOrUpdate(pUrl, filePath, pDownloadOptions.getModuleName(), State.DOWNLOADING, 0, 0);
                // 开始下载
                Class<? extends Downloader> downloaderClass = pDownloadOptions.getDownloader();
                if (downloaderClass == null) {
                    downloaderClass = DEFAULT_DOWNLOADER;
                }
                final Downloader downloader;
                try {
                    downloader = downloaderClass.newInstance();
                    final InputStream downloaderStream = downloader.getStream(pUrl, pDownloadOptions.getExtraForDownloader());
                    File downloadFile = new File(pDownloadOptions.getParentDirPath(), pDownloadOptions.getFileName());
                    File tmpFile = new File(downloadFile.getAbsolutePath() + ".tmp");
                    boolean loaded = false;
                    try {
                        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), DEFAULT_BUFFER_SIZE);
                        try {
                            loaded = IoUtils.copyStream(downloaderStream, os, new IoUtils.CopyListener() {
                                @Override
                                public boolean onBytesCopied(int current, int total) {
                                    final boolean isCanceled = pSubscriber.isUnsubscribed();
                                    State state;
                                    if (isCanceled) {
                                        state = State.CANCEL;
                                    } else {
                                        // 更新进度
                                        state = State.DOWNLOADING;
                                    }
                                    insertOrUpdate(pUrl, filePath, pDownloadOptions.getModuleName(), state, current, total);
                                    return !isCanceled;
                                }
                            }, DEFAULT_BUFFER_SIZE);
                        } finally {
                            IoUtils.closeSilently(os);
                        }
                    } finally {
                        if (loaded && !tmpFile.renameTo(downloadFile)) {
                            loaded = false;
                        }
                        if (!loaded) {
                            tmpFile.delete();
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
}
