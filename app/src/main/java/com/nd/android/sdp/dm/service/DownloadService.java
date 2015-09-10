package com.nd.android.sdp.dm.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.store.DownloadStore;

/**
 * 下载服务
 *
 * @author Young
 */
public class DownloadService extends Service implements DownloadStore.OnDownloadLisener {

    public static final String PARAM_URL = "url";
    public static final String PARAM_OPTIONS = "options";
    private static final String PARAM_OPER = "oper";

    private DownloadStore mDownloadStore;

    private enum OPER {
        START,
        CANCEL,
        PAUSE
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloadStore = new DownloadStore(getContentResolver());
        mDownloadStore.registerProgressListener(this);
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
        Intent starter = new Intent(context, DownloadService.class);
        starter.putExtra(PARAM_URL, pUrl);
        starter.putExtra(PARAM_OPTIONS, pDownloadOptions);
        starter.putExtra(PARAM_OPER, OPER.START);
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
                mDownloadStore.addTask(url, downloadOptions);
                break;
            case CANCEL:
                mDownloadStore.cancelDownload(url);
                break;
            case PAUSE:
                break;
        }
        return START_STICKY_COMPATIBILITY;
    }

    public static void cancel(Context pContext, String pUrl) {
        Intent starter = new Intent(pContext, DownloadService.class);
        starter.putExtra(PARAM_URL, pUrl);
        starter.putExtra(PARAM_OPER, OPER.CANCEL);
        pContext.startService(starter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadStore.cancelAll();
        mDownloadStore = null;
        mDownloadStore.unregisterProgressListener(this);
    }

    @Override
    public void onPause(String pUrl) {

    }

    @Override
    public void onComplete(String pUrl) {

    }

    @Override
    public void onProgress(String pUrl, long current, long total) {
        Log.e("DownloadService", pUrl + " " + current + " " + total);
    }

    @Override
    public void onCancel(String pUrl) {

    }
}
