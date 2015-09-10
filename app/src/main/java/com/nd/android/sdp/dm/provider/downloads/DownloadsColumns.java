package com.nd.android.sdp.dm.provider.downloads;

import android.net.Uri;
import android.provider.BaseColumns;

import com.nd.android.sdp.dm.provider.DownloadProvider;
import com.nd.android.sdp.dm.provider.downloads.DownloadsColumns;

/**
 * ND SDP Downlods Manager
 */
public class DownloadsColumns implements BaseColumns {
    public static final String TABLE_NAME = "downloads";
    public static final Uri CONTENT_URI = Uri.parse(DownloadProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Downlods Task ID
     */
    public static final String URL = "url";

    /**
     * Local File Path
     */
    public static final String FILEPATH = "filepath";

    public static final String STATE = "state";

    /**
     * Function Segment
     */
    public static final String MODULE_NAME = "module_name";

    /**
     * Current Size
     */
    public static final String CURRENT_SIZE = "current_size";

    public static final String TOTAL_SIZE = "total_size";

    public static final String CREATE_TIME = "create_time";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            URL,
            FILEPATH,
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
            if (c.equals(STATE) || c.contains("." + STATE)) return true;
            if (c.equals(MODULE_NAME) || c.contains("." + MODULE_NAME)) return true;
            if (c.equals(CURRENT_SIZE) || c.contains("." + CURRENT_SIZE)) return true;
            if (c.equals(TOTAL_SIZE) || c.contains("." + TOTAL_SIZE)) return true;
            if (c.equals(CREATE_TIME) || c.contains("." + CREATE_TIME)) return true;
        }
        return false;
    }

}
