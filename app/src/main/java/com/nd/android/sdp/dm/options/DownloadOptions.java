package com.nd.android.sdp.dm.options;

import com.nd.android.sdp.dm.downloader.Downloader;

import java.io.Serializable;

/**
 * The Download options.
 *
 * @author Young
 */
public class DownloadOptions implements Serializable {

    private Object mExtraForDownloader;

    private Class<? extends Downloader> mDownloader;

    private String mFileName;

    private String mParentDirPath;

    private String mModuleName;

    private boolean mNeedNotificationBar;

    public DownloadOptions(Object pExtraForDownloader,
                           Class<? extends Downloader> pDownloader,
                           String pFileName,
                           String pParentDirPath,
                           String pModuleName,
                           boolean pNeedNotificationBar) {
        mExtraForDownloader = pExtraForDownloader;
        mDownloader = pDownloader;
        mFileName = pFileName;
        mParentDirPath = pParentDirPath;
        mModuleName = pModuleName;
        mNeedNotificationBar = pNeedNotificationBar;
    }

    public Object getExtraForDownloader() {
        return mExtraForDownloader;
    }

    public Class<? extends Downloader> getDownloader() {
        return mDownloader;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getParentDirPath() {
        return mParentDirPath;
    }

    public String getModuleName() {
        return mModuleName;
    }

    public boolean isNeedNotificationBar() {
        return mNeedNotificationBar;
    }
}
