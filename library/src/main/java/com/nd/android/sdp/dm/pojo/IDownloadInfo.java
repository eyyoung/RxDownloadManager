package com.nd.android.sdp.dm.pojo;

import com.nd.android.sdp.dm.state.State;

/**
 * Created by Administrator on 2015/9/15.
 */
public interface IDownloadInfo {
    String getUrl();

    void setUrl(String pUrl);

    State getState();

    void setState(State pState);

    long getCurrentSize();

    void setCurrentSize(long pCurrentSize);

    String getFilePath();

    void setFilePath(String pFilePath);

    long getTotalSize();

    void setTotalSize(long pTotalSize);

    String getMd5();

    void setMd5(String pMd5);
}
