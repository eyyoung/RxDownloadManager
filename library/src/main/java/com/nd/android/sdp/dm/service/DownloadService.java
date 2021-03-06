package com.nd.android.sdp.dm.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.nd.android.sdp.dm.DownloadManager;
import com.nd.android.sdp.dm.R;
import com.nd.android.sdp.dm.observer.DownloadObserver;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.options.OpenAction;
import com.nd.android.sdp.dm.provider.downloads.DownloadsCursor;
import com.nd.android.sdp.dm.service.presenter.DownloadPresenter;

import java.io.File;
import java.io.Serializable;


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
    private static final String PARAM_OPEN_ACTION = "open_action";

    private DownloadPresenter mDownloadPresenter;
    private NotificationManager mNotifyManager;
    private final ArrayMap<String, NotificationCompat.Builder> mNotificationHashMap = new ArrayMap<>();
    private final ArrayMap<String, Class<? extends OpenAction>> mOpenActionArrayMap = new ArrayMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.INSTANCE.init(this);
        mDownloadPresenter = new DownloadPresenter(getContentResolver());
        DownloadObserver.INSTANCE.init(getContentResolver());
        DownloadObserver.INSTANCE.registerProgressListener(this);
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private enum OPER {
        START,
        CANCEL,
        PAUSE,
        OPEN,
        PAUSE_ALL
    }

    /**
     * 启动一次服务
     *
     * @param context          the context
     * @param pUrl             the p url
     * @param md5              md5
     * @param pDownloadOptions the p download options  @author Young
     */
    public static void start(@NonNull Context context,
                             @NonNull String pUrl,
                             @Nullable String md5,
                             @NonNull DownloadOptions pDownloadOptions) {
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
        if (intent == null) {
            return START_NOT_STICKY;
        }
        String url = intent.getStringExtra(PARAM_URL);
        OPER oper = (OPER) intent.getSerializableExtra(PARAM_OPER);
        if (oper == null) {
            return START_NOT_STICKY;
        }
        switch (oper) {
            case START:
                if (TextUtils.isEmpty(url)) {
                    return START_NOT_STICKY;
                }
                DownloadOptions downloadOptions = (DownloadOptions) intent.getSerializableExtra(PARAM_OPTIONS);
                startTask(intent, url, downloadOptions);
                break;
            case CANCEL:
                if (TextUtils.isEmpty(url)) {
                    return START_NOT_STICKY;
                }
                mDownloadPresenter.cancelDownload(url);
                break;
            case PAUSE:
                if (TextUtils.isEmpty(url)) {
                    return START_NOT_STICKY;
                }
                mDownloadPresenter.pauseDownload(url);
                break;
            case PAUSE_ALL:
                mDownloadPresenter.pauseAll();
                break;
            case OPEN:
                if (TextUtils.isEmpty(url)) {
                    return START_NOT_STICKY;
                }
                cancelNotify(url);
                final Serializable actionExtra = intent.getSerializableExtra(PARAM_OPEN_ACTION);
                if (actionExtra != null) {
                    try {
                        final Class<? extends OpenAction> openActionClass = (Class<? extends OpenAction>) actionExtra;
                        final OpenAction openAction = openActionClass.newInstance();
                        final DownloadsCursor query = mDownloadPresenter.query(url);
                        query.moveToFirst();
                        openAction.open(this, query.getFilepath());
                        query.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        return START_NOT_STICKY;
    }

    private void startTask(Intent intent, String url, DownloadOptions downloadOptions) {
        String md5 = intent.getStringExtra(PARAM_MD5);
        final boolean addTask = mDownloadPresenter.addTask(url, md5, downloadOptions);
        if (addTask && downloadOptions.isNeedNotificationBar()) {
            makeProgressNotification(downloadOptions.getFileName(), url);
            if (downloadOptions.getOpenAction() != null) {
                mOpenActionArrayMap.put(url, downloadOptions.getOpenAction());
            }
        }
    }

    private void makeProgressNotification(String pFileName, String pUrl) {
        try {
            Intent cancelIntent = new Intent(this, DownloadService.class);
            cancelIntent.putExtra(PARAM_URL, pUrl);
            cancelIntent.putExtra(PARAM_OPER, OPER.CANCEL);
            PendingIntent piCancel = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Intent pauseIntent = new Intent(this, DownloadService.class);
            pauseIntent.putExtra(PARAM_URL, pUrl);
            pauseIntent.putExtra(PARAM_OPER, OPER.PAUSE);
            PendingIntent piPause = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.drawable.stat_sys_download)
                            .setContentTitle(getString(R.string.downloadmanager_downloading, pFileName))
                            .setProgress(100, 0, true)
                            .addAction(R.drawable.downloadmanager_ic_block,
                                    getString(R.string.downloadmanager_cancel), piCancel)
                            .setOngoing(true)
                            .addAction(R.drawable.downloadmanager_ic_pause,
                                    getString(R.string.downloadmanager_pause), piPause);
            Notification notification = builder.build();
            mNotifyManager.notify(Math.abs(pUrl.hashCode()), notification);
            mNotificationHashMap.put(pUrl, builder);
        } catch (Exception e) {
            Log.w(getClass().getName(), "Notify Error");
        }
    }

    public static void cancel(@NonNull Context pContext, @NonNull String pUrl) {
        Intent starter = new Intent(pContext, DownloadService.class);
        starter.putExtra(PARAM_URL, pUrl);
        starter.putExtra(PARAM_OPER, OPER.CANCEL);
        pContext.startService(starter);
    }

    public static void pause(@NonNull Context pContext, @NonNull String pUrl) {
        Intent starter = new Intent(pContext, DownloadService.class);
        starter.putExtra(PARAM_URL, pUrl);
        starter.putExtra(PARAM_OPER, OPER.PAUSE);
        pContext.startService(starter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadPresenter.pauseAll();
        mDownloadPresenter.onDestroy();
        mDownloadPresenter = null;
        DownloadObserver.INSTANCE.unregisterProgressListener(this);
        mNotificationHashMap.clear();
        mOpenActionArrayMap.clear();
    }

    @Override
    public void onPause(String pUrl) {
        cancelNotify(pUrl);
        mOpenActionArrayMap.remove(pUrl);
    }

    private void cancelNotify(String pUrl) {
        mNotifyManager.cancel(Math.abs(pUrl.hashCode()));
        mNotificationHashMap.remove(pUrl);
    }

    @Override
    public void onComplete(String pUrl) {
        if (mNotificationHashMap.containsKey(pUrl)) {
            cancelNotify(pUrl);
            final DownloadsCursor query = mDownloadPresenter.query(pUrl);
            query.moveToFirst();
            File file = new File(query.getFilepath());
            Intent openIntent = new Intent(this, DownloadService.class);
            openIntent.putExtra(PARAM_URL, pUrl);
            openIntent.putExtra(PARAM_OPER, OPER.OPEN);
            openIntent.putExtra(PARAM_OPEN_ACTION, mOpenActionArrayMap.get(pUrl));
            PendingIntent piOpen = PendingIntent.getService(this, 3, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setContentIntent(piOpen)
                            .setContentTitle(getString(R.string.downloadmanager_download_complete_title, file.getName()))
                            .setContentText(getString(R.string.downloadmanager_download_complete_content, file.getAbsolutePath()));
            Notification notification = builder.build();
            mNotifyManager.notify(pUrl.hashCode(), notification);
            query.close();
        }
        mOpenActionArrayMap.remove(pUrl);
    }

    @Override
    public void onProgress(String pUrl, long current, long total) {
        NotificationCompat.Builder builder = mNotificationHashMap.get(pUrl);
        if (builder != null) {
            if (total != 0) {
                builder.setProgress(100, (int) (current * 100 / total), false);
            }
            mNotifyManager.notify(Math.abs(pUrl.hashCode()), builder.build());
        }
    }

    @Override
    public void onCancel(String pUrl) {
        cancelNotify(pUrl);
        mOpenActionArrayMap.remove(pUrl);
    }

    @Override
    public void onError(String pUrl, int httpState) {
        if (!mNotificationHashMap.containsKey(pUrl)) {
            return;
        }
        final DownloadsCursor query = mDownloadPresenter.query(pUrl);
        if (query == null || query.getCount() == 0) {
            return;
        }
        query.moveToFirst();
        if (query.getFilepath() == null) {
            return;
        }
        File file = new File(query.getFilepath());
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setContentTitle(getString(R.string.downloadmanager_download_failed_title, file.getName()))
                        .setContentText(getString(R.string.downloadmanager_download_failed_content, file.getName()));
        Notification notification = builder.build();
        mNotifyManager.notify(Math.abs(pUrl.hashCode()), notification);
        query.close();
    }

    public static void pauseAll(@NonNull Context context) {
        Intent starter = new Intent(context, DownloadService.class);
        starter.putExtra(PARAM_OPER, OPER.PAUSE_ALL);
        context.startService(starter);
    }
}
