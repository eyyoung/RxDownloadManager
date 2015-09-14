package com.nd.android.sdp.dm.pojo;

import com.nd.android.sdp.dm.provider.downloads.DownloadsCursor;
import com.nd.android.sdp.dm.state.State;

/**
 *
 */
public class DownloadInfo {
    public String url;
    public State state;
    public long currentSize;
    public String filePath;
    public long totalSize;
    public String md5;

    public DownloadInfo(DownloadsCursor pDownloadsCursor) {
        url = pDownloadsCursor.getUrl();
        filePath = pDownloadsCursor.getFilepath();
        state = State.fromInt(pDownloadsCursor.getState());
        currentSize = pDownloadsCursor.getCurrentSize();
        totalSize = pDownloadsCursor.getTotalSize();
        md5 = pDownloadsCursor.getMd5();
    }

    public DownloadInfo(String url,
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
}
