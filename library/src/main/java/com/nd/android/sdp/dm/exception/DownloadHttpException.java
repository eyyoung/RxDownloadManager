package com.nd.android.sdp.dm.exception;

import com.nd.android.sdp.dm.pojo.IDownloadInfo;

/**
 * Created by Administrator on 2015/9/10.
 */
public class DownloadHttpException extends Throwable {

    private final int mHttpCode;
    private final IDownloadInfo mDownloadInfo;

    public DownloadHttpException(int pHttpCode) {
        mHttpCode = pHttpCode;
        mDownloadInfo = null;
    }
    public DownloadHttpException(IDownloadInfo pDownloadInfo) {
        mHttpCode = pDownloadInfo.getHttpState();
        mDownloadInfo = pDownloadInfo;
    }

    public int getHttpCode() {
        return mHttpCode;
    }

    public IDownloadInfo getDownloadInfo() {
        return mDownloadInfo;
    }
}
