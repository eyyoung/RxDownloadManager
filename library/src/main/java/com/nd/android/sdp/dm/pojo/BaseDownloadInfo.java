package com.nd.android.sdp.dm.pojo;

import com.nd.android.sdp.dm.provider.downloads.DownloadsCursor;
import com.nd.android.sdp.dm.state.State;

/**
 *
 */
public class BaseDownloadInfo implements IDownloadInfo {
    public String url;
    public State state;
    public long currentSize;
    public String filePath;
    public long totalSize;
    public String md5;
    public long speed;// 仅供界面展示使用，数据库不存储

    public BaseDownloadInfo() {
    }

    public BaseDownloadInfo(DownloadsCursor pDownloadsCursor) {
        url = pDownloadsCursor.getUrl();
        filePath = pDownloadsCursor.getFilepath();
        state = State.fromInt(pDownloadsCursor.getState());
        currentSize = pDownloadsCursor.getCurrentSize();
        totalSize = pDownloadsCursor.getTotalSize();
        md5 = pDownloadsCursor.getMd5();
    }

    public BaseDownloadInfo(String url,
                            State state,
                            String md5,
                            String pFilePath,
                            long currentSize,
                            long totalSize) {
        this.url = url;
        this.state = state;
        this.currentSize = currentSize;
        this.md5 = md5;
        this.filePath = pFilePath;
        this.totalSize = totalSize;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String pUrl) {
        url = pUrl;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State pState) {
        state = pState;
    }

    @Override
    public long getCurrentSize() {
        return currentSize;
    }

    @Override
    public void setCurrentSize(long pCurrentSize) {
        currentSize = pCurrentSize;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public void setFilePath(String pFilePath) {
        filePath = pFilePath;
    }

    @Override
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public void setTotalSize(long pTotalSize) {
        totalSize = pTotalSize;
    }

    @Override
    public String getMd5() {
        return md5;
    }

    @Override
    public void setMd5(String pMd5) {
        md5 = pMd5;
    }
}
