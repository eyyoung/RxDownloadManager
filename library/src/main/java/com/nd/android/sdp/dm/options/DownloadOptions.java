package com.nd.android.sdp.dm.options;

import android.support.annotation.NonNull;

import com.nd.android.sdp.dm.downloader.Downloader;
import com.nd.android.sdp.dm.processor.DataProcessor;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The Download options.
 *
 * @author Young
 */
public class DownloadOptions implements Serializable {

    private static DefaultConflictStragedy sDefaultConflictStragedy = new DefaultConflictStragedy();

    private static Md5TempFileNameStragedy sDefaultTempFileNameStragedy = new Md5TempFileNameStragedy();

    private HashMap<String, String> mExtraForDownloader;

    private Class<? extends Downloader> mDownloader;

    private String mFileName;

    private String mParentDirPath;

    private String mModuleName;

    private boolean mNeedNotificationBar;

    private ConflictStragedy mConflictStragedy = sDefaultConflictStragedy;

    private TempFileNameStragedy mTempFileStragedy = sDefaultTempFileNameStragedy;

    private Class<? extends OpenAction> mOpenAction;

    private HashMap<String, String> mUrlParams;

    private DataProcessor mDataProcessor;

    DownloadOptions(HashMap<String, String> pExtraForDownloader,
                    Class<? extends Downloader> pDownloader,
                    String pFileName,
                    String pParentDirPath,
                    String pModuleName,
                    DataProcessor dataProcessor,
                    Class<? extends OpenAction> pOpenAction,
                    HashMap<String, String> pUrlParams,
                    boolean pNeedNotificationBar) {
        mExtraForDownloader = pExtraForDownloader;
        mDownloader = pDownloader;
        mFileName = pFileName;
        mParentDirPath = pParentDirPath;
        mModuleName = pModuleName;
        mDataProcessor = dataProcessor;
        mOpenAction = pOpenAction;
        mNeedNotificationBar = pNeedNotificationBar;
        mUrlParams = pUrlParams;
    }

    public DataProcessor getDataProcessor() {
        return mDataProcessor;
    }

    public HashMap<String, String> getExtraForDownloader() {
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

    public Class<? extends OpenAction> getOpenAction() {
        return mOpenAction;
    }

    @NonNull
    public HashMap<String, String> getUrlParams() {
        return mUrlParams;
    }

    public ConflictStragedy getConflictStragedy() {
        return mConflictStragedy;
    }

    public TempFileNameStragedy getTempFileStragedy() {
        return mTempFileStragedy;
    }
}
