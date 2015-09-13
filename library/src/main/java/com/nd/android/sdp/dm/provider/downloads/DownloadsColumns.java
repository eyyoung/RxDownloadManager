package com.nd.android.sdp.dm.provider.downloads;

import android.net.Uri;
import android.provider.BaseColumns;

import com.nd.android.sdp.dm.provider.DownloadProvider;

/**
 * A human being which is part of a team.
 */
public class DownloadsColumns implements BaseColumns {
    public static final String TABLE_NAME = "downloads";
    public static final Uri CONTENT_URI = Uri.parse(DownloadProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * 下载地址 URL
     */
    public static final String URL = "url";

    /**
     * 本地存放路径
     */
    public static final String FILEPATH = "filepath";

    /**
     * 文件md5，用于查询是否已经下载过，秒下
     */
    public static final String MD5 = "md5";

    /**
     * 下载状态
     */
    public static final String STATE = "state";

    /**
     * 调用模块名称（区分是哪个模块调用的下载）
     */
    public static final String MODULE_NAME = "module_name";

    /**
     * 当前已下载大小
     */
    public static final String CURRENT_SIZE = "current_size";

    /**
     * 文件大小
     */
    public static final String TOTAL_SIZE = "total_size";

    /**
     * 任务创建时间
     */
    public static final String CREATE_TIME = "create_time";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            URL,
            FILEPATH,
            MD5,
            STATE,
            MODULE_NAME,
            CURRENT_SIZE,
            TOTAL_SIZE,
            CREATE_TIME
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(URL) || c.contains("." + URL)) return true;
            if (c.equals(FILEPATH) || c.contains("." + FILEPATH)) return true;
            if (c.equals(MD5) || c.contains("." + MD5)) return true;
            if (c.equals(STATE) || c.contains("." + STATE)) return true;
            if (c.equals(MODULE_NAME) || c.contains("." + MODULE_NAME)) return true;
            if (c.equals(CURRENT_SIZE) || c.contains("." + CURRENT_SIZE)) return true;
            if (c.equals(TOTAL_SIZE) || c.contains("." + TOTAL_SIZE)) return true;
            if (c.equals(CREATE_TIME) || c.contains("." + CREATE_TIME)) return true;
        }
        return false;
    }

}
