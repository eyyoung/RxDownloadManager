package com.nd.android.sdp.dm.options;

import android.support.v4.util.ArrayMap;

import com.nd.android.sdp.dm.downloader.Downloader;

import java.io.Serializable;

/**
 * The Download options.
 *
 * @author Young
 */
public class DownloadOptions implements Serializable {

    private static DefaultConflictStragedy sDefaultConflictStragedy = new DefaultConflictStragedy();

    private ArrayMap<String,String> mExtraForDownloader;

    private Class<? extends Downloader> mDownloader;

    private String mFileName;

    private String mParentDirPath;

    private String mModuleName;

    private boolean mNeedNotificationBar;

    private ConflictStragedy mConflictStragedy = sDefaultConflictStragedy;

    public DownloadOptions(ArrayMap<String,String> pExtraForDownloader,
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

    public ArrayMap<String,String> getExtraForDownloader() {
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

    public ConflictStragedy getConflictStragedy() {
        return mConflictStragedy;
    }
}
