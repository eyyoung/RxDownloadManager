package com.nd.android.sdp.dm;

import com.nd.android.sdp.dm.observer.DownloadObserver;

/**
 * The type Download listener adapter.
 *
 * @author Young
 */
public class DownloadListenerAdapter implements DownloadObserver.OnDownloadLisener {
    @Override
    public void onPause(String pUrl) {

    }

    @Override
    public void onComplete(String pUrl) {

    }

    @Override
    public void onProgress(String pUrl, long current, long total) {

    }

    @Override
    public void onCancel(String pUrl) {

    }

    @Override
    public void onError(String pUrl, int httpState) {

    }
}
