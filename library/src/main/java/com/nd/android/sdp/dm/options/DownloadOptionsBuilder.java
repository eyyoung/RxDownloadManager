package com.nd.android.sdp.dm.options;

import com.nd.android.sdp.dm.downloader.Downloader;
import com.nd.android.sdp.dm.log.DownloaderLogger;
import com.nd.android.sdp.dm.processor.DataProcessor;

import java.util.HashMap;

public class DownloadOptionsBuilder {
    private HashMap<String, String> mExtraForDownloader = null;
    private Class<? extends Downloader> mDownloader = null;
    private String mParentDirPath;
    private String mModuleName = "sdp_common";
    private boolean mNeedNotificationBar = false;
    private String mFileName;
    private boolean mForceOverride;
    private Class<? extends OpenAction> mOpenAction;
    private DataProcessor mDataProcessor;
    private HashMap<String, String> mUrlParams = new HashMap<>();
    private DownloaderLogger mDownloaderLogger;
    private TempFileNameStragedy mTempFileNameStragedy;

    /**
     * 下载头
     *
     * @param pExtraForDownloader 下载头
     * @return
     */
    public DownloadOptionsBuilder extraForDownloader(HashMap<String, String> pExtraForDownloader) {
        mExtraForDownloader = pExtraForDownloader;
        return this;
    }

    /**
     * 下载数据处理器
     *
     * @param dataProcessor the data processor
     * @return the download options builder
     * @author Young
     */
    public DownloadOptionsBuilder dataProcessor(DataProcessor dataProcessor) {
        mDataProcessor = dataProcessor;
        return this;
    }

    /**
     * 下载器
     *
     * @param pDownloader 下载器
     * @return
     */
    public DownloadOptionsBuilder downloader(Class<? extends Downloader> pDownloader) {
        mDownloader = pDownloader;
        return this;
    }

    /**
     * 文件路径
     *
     * @param pParentDirPath 父级文件夹路径
     * @return
     */
    public DownloadOptionsBuilder parentDirPath(String pParentDirPath) {
        mParentDirPath = pParentDirPath;
        return this;
    }

    /**
     * 模块名称（用户区分各模块下载数据）
     *
     * @param pModuleName 模块名称
     * @return
     */
    public DownloadOptionsBuilder moduleName(String pModuleName) {
        mModuleName = pModuleName;
        return this;
    }

    /**
     * 是否需要标题栏
     *
     * @param pNeedNotificationBar
     * @return
     */
    public DownloadOptionsBuilder needNotificationBar(boolean pNeedNotificationBar) {
        mNeedNotificationBar = pNeedNotificationBar;
        return this;
    }

    /**
     * 文件名
     *
     * @param pFileName
     * @return
     */
    public DownloadOptionsBuilder fileName(String pFileName) {
        mFileName = pFileName;
        return this;
    }

    /**
     * 任务栏打开操作<br/>
     * 需要与com.nd.android.sdp.dm.options.DownloadOptionsBuilder#needNotificationBar(boolean)搭配使用
     *
     * @param pOpenAction
     * @return
     */
    public DownloadOptionsBuilder openAction(Class<? extends OpenAction> pOpenAction) {
        mOpenAction = pOpenAction;
        return this;
    }

    /**
     * Url参数
     *
     * @param paramKey   Param Key
     * @param paramValue Param Value
     * @return download options builder
     * @author Young
     */
    public DownloadOptionsBuilder urlParam(String paramKey, String paramValue) {
        mUrlParams.put(paramKey, paramValue);
        return this;
    }

    public DownloadOptionsBuilder downloadLogger(DownloaderLogger downloaderLogger) {
        mDownloaderLogger = downloaderLogger;
        return this;
    }

    /**
     * 强制覆盖
     *
     * @param force
     */
    public DownloadOptionsBuilder forceOverride(boolean force) {
        mForceOverride = force;
        return this;
    }

    public DownloadOptionsBuilder tempFileNameStragedy(TempFileNameStragedy tempFileNameStragedy) {
        mTempFileNameStragedy = tempFileNameStragedy;
        return this;
    }

    public DownloadOptions build() {
        return new DownloadOptions(mExtraForDownloader,
                mDownloader,
                mFileName,
                mParentDirPath,
                mModuleName,
                mDataProcessor,
                mOpenAction,
                mUrlParams,
                mForceOverride,
                mNeedNotificationBar,
                mTempFileNameStragedy,
                mDownloaderLogger);
    }
}