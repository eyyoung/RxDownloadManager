package com.nd.android.sdp.dm.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;

import com.nd.android.sdp.dm.R;
import com.nd.android.sdp.dm.observer.DownloadObserver;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.service.presenter.DownloadPresenter;


/**
 * 下载服务
 *
 * @author Young
 */
public class DownloadService extends Service implements DownloadObserver.OnDownloadLisener {

    public static final String PARAM_URL = "url";
    public static final String PARAM_OPTIONS = "options";
    public static final String PARAM_MD5 = "md5";
    private static final String PARAM_OPER = "oper";

    private DownloadPresenter mDownloadPresenter;
    private NotificationManager mNotifyManager;
    private final ArrayMap<String, NotificationCompat.Builder> mNotificationHashMap = new ArrayMap<>();

    private enum OPER {
        START,
        CANCEL,
        PAUSE
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloadPresenter = new DownloadPresenter(getContentResolver());
        DownloadObserver.INSTANCE.init(getContentResolver());
        DownloadObserver.INSTANCE.registerProgressListener(this);
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 启动一次服务
     *
     * @param context          the context
     * @param pUrl             the p url
     * @param pDownloadOptions the p download options
     * @author Young
     */
    public static void start(Context context, String pUrl, DownloadOptions pDownloadOptions) {
        start(context, pUrl, null, pDownloadOptions);
    }

    /**
     * 启动一次服务
     *
     * @param context          the context
     * @param pUrl             the p url
     * @param md5
     * @param pDownloadOptions the p download options  @author Young
     */
    public static void start(Context context, String pUrl, String md5, DownloadOptions pDownloadOptions) {
        Intent starter = new Intent(context, DownloadService.class);
        starter.putExtra(PARAM_URL, pUrl);
        starter.putExtra(PARAM_OPTIONS, pDownloadOptions);
        starter.putExtra(PARAM_OPER, OPER.START);
        starter.putExtra(PARAM_MD5, md5);
        context.startService(starter);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent pIntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra(PARAM_URL);
        DownloadOptions downloadOptions = (DownloadOptions) intent.getSerializableExtra(PARAM_OPTIONS);
        OPER oper = (OPER) intent.getSerializableExtra(PARAM_OPER);
        switch (oper) {
            case START:
                startTask(intent, url, downloadOptions);
                break;
            case CANCEL:
                mDownloadPresenter.cancelDownload(url);
                break;
            case PAUSE:
                mDownloadPresenter.pauseDownload(url);
                break;
        }
        return START_NOT_STICKY;
    }

    private void startTask(Intent intent, String url, DownloadOptions downloadOptions) {
        String md5 = intent.getStringExtra(PARAM_MD5);
        mDownloadPresenter.addTask(url, md5, downloadOptions);
        if (downloadOptions.isNeedNotificationBar()) {
            makeNotification(downloadOptions.getFileName(), url);
        }
    }

    private void makeNotification(String pFileName, String pUrl) {
        Intent dismissIntent = new Intent(this, DownloadService.class);
        dismissIntent.putExtra(PARAM_URL, pUrl);
        dismissIntent.putExtra(PARAM_OPER, OPER.CANCEL);
        PendingIntent piCancel = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.downloadmanager_ic_download_noti)
                        .setContentTitle(getString(R.string.downloadmanager_downloading, pFileName))
                        .setProgress(100, 0, false)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.downloadmanager_downloading, pFileName)))
                        .addAction(R.drawable.downloadmanager_ic_download_noti,
                                getString(R.string.downloadmanager_cancel), piCancel);
        Notification notification = builder.build();
        mNotifyManager.notify(pUrl.hashCode(), notification);
        mNotificationHashMap.put(pUrl, builder);
    }

    public static void cancel(Context pContext, String pUrl) {
        Intent starter = new Intent(pContext, DownloadService.class);
        starter.putExtra(PARAM_URL, pUrl);
        starter.putExtra(PARAM_OPER, OPER.CANCEL);
        pContext.startService(starter);
    }

    public static void pause(Context pContext, String pUrl) {
        Intent starter = new Intent(pContext, DownloadService.class);
        starter.putExtra(PARAM_URL, pUrl);
        starter.putExtra(PARAM_OPER, OPER.PAUSE);
        pContext.startService(starter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadPresenter.cancelAll();
        mDownloadPresenter.onDestroy();
        mDownloadPresenter = null;
        DownloadObserver.INSTANCE.unregisterProgressListener(this);
        mNotificationHashMap.clear();
    }

    @Override
    public void onPause(String pUrl) {
        cancelNotify(pUrl);
    }

    private void cancelNotify(String pUrl) {
        mNotifyManager.cancel(pUrl.hashCode());
        mNotificationHashMap.remove(pUrl);
    }

    @Override
    public void onComplete(String pUrl) {
        cancelNotify(pUrl);
    }

    @Override
    public void onProgress(String pUrl, long current, long total) {
        NotificationCompat.Builder builder = mNotificationHashMap.get(pUrl);
        if (builder != null) {
            if (total != 0) {
                builder.setProgress(100, (int) (current * 100 / total), false);
            } else {
                builder.setProgress(0, 0, true);
            }
        }
        mNotifyManager.notify(pUrl.hashCode(), builder.build());
    }

    @Override
    public void onCancel(String pUrl) {
        cancelNotify(pUrl);
    }
}
