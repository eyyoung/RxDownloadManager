package com.nd.android.sdp.dm.observer;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.nd.android.sdp.dm.pojo.BaseDownloadInfo;
import com.nd.android.sdp.dm.provider.downloads.DownloadsColumns;
import com.nd.android.sdp.dm.provider.downloads.DownloadsCursor;
import com.nd.android.sdp.dm.state.State;

import java.util.HashSet;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
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
    final private ArrayMap<String, PublishSubject<BaseDownloadInfo>> mSubjectMap = new ArrayMap<>();

    public void init(ContentResolver pContentResolver) {
        if (mContentResolver == null) {
            mContentResolver = pContentResolver;
            this.mContentResolver.registerContentObserver(
                    DownloadsColumns.CONTENT_URI, true, mContentObserver);
        }
    }

    final private ContentObserver mContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Cursor cursor = mContentResolver.query(uri, DownloadsColumns.ALL_COLUMNS, null, null, null);
            final DownloadsCursor downloadsCursor = new DownloadsCursor(cursor);
            if (downloadsCursor.getCount() == 0) {
                return;
            }
            downloadsCursor.moveToFirst();
            BaseDownloadInfo downloadInfoInner = new BaseDownloadInfo(downloadsCursor);
            String url = downloadsCursor.getUrl();
            PublishSubject<BaseDownloadInfo> subject = mSubjectMap.get(url);
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
    private PublishSubject<BaseDownloadInfo> subscribeDownloadListener() {
        final PublishSubject<BaseDownloadInfo> subject = PublishSubject.create();
        subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pDownloadInfo -> {
                    for (OnDownloadLisener pAction : mProgressAction) {
                        switch (pDownloadInfo.state) {
                            case DOWNLOADING:
                                if (pDownloadInfo.totalSize > 0 && pDownloadInfo.currentSize > 0) {
                                    pAction.onProgress(pDownloadInfo.url,
                                            pDownloadInfo.currentSize,
                                            pDownloadInfo.totalSize);
                                }
                                break;
                            case PAUSING:
                                pAction.onPause(pDownloadInfo.url);
                                break;
                            case CANCEL:
                                pAction.onCancel(pDownloadInfo.url);
                                break;
                            case FINISHED:
                                pAction.onComplete(pDownloadInfo.url);
                                break;
                            case ERROR:
                                pAction.onError(pDownloadInfo.url, pDownloadInfo.getHttpState());
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

        void onError(String pUrl, int httpState);
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

}
