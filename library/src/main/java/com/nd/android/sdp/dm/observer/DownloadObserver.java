package com.nd.android.sdp.dm.observer;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.nd.android.sdp.dm.provider.downloads.DownloadsColumns;
import com.nd.android.sdp.dm.provider.downloads.DownloadsCursor;
import com.nd.android.sdp.dm.state.State;

import java.util.HashSet;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * 数据仓库层
 * 负责变更数据库并且通知各观察者
 *
 * @author Young
 */
public enum DownloadObserver {

    INSTANCE;

    protected ContentResolver mContentResolver;

    final private Set<OnDownloadLisener> mProgressAction = new HashSet<>();
    final private ArrayMap<String, PublishSubject<DownloadInfoInner>> mSubjectMap = new ArrayMap<>();

    public void init(ContentResolver pContentResolver) {
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
            String url = downloadsCursor.getUrl();
            PublishSubject<DownloadInfoInner> subject = mSubjectMap.get(url);
            // 缓存中不存在
            if (subject == null) {
                subject = subscribeDownloadListener();
                mSubjectMap.put(url, subject);
            }
            subject.onNext(downloadInfoInner);
            // 被取消，被暂停的情况，结束流
            if (downloadInfoInner.state != State.DOWNLOADING) {
                subject.onCompleted();
                mSubjectMap.remove(url);
            }
            downloadsCursor.close();
        }
    };

    @NonNull
    private PublishSubject<DownloadInfoInner> subscribeDownloadListener() {
        final PublishSubject<DownloadInfoInner> subject = PublishSubject.create();
        subject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pDownloadInfoInner -> {
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
                }, t -> {
                    t.printStackTrace();
                });
        return subject;
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
    public void registerProgressListener(OnDownloadLisener pLisener) {
        mProgressAction.add(pLisener);
    }

    /**
     * 反注册观察者
     *
     * @param pLisener the p lisener
     * @author Young
     */
    public void unregisterProgressListener(OnDownloadLisener pLisener) {
        mProgressAction.remove(pLisener);
    }

    private class DownloadInfoInner {
        String url;
        State state;
        long currentSize;
        String filePath;
        long totalSize;
        String md5;

        public DownloadInfoInner(DownloadsCursor pDownloadsCursor) {
            url = pDownloadsCursor.getUrl();
            filePath = pDownloadsCursor.getFilepath();
            state = State.fromInt(pDownloadsCursor.getState());
            currentSize = pDownloadsCursor.getCurrentSize();
            totalSize = pDownloadsCursor.getTotalSize();
            md5 = pDownloadsCursor.getMd5();
        }
    }

}
