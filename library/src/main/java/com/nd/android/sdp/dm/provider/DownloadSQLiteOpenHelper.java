package com.nd.android.sdp.dm.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.nd.android.sdp.dm.BuildConfig;
import com.nd.android.sdp.dm.provider.downloads.DownloadsColumns;

public class DownloadSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = DownloadSQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "sdp_downloads.db";
    private static final int DATABASE_VERSION = 1;
    private static DownloadSQLiteOpenHelper sInstance;
    private final Context mContext;
    private final DownloadSQLiteOpenHelperCallbacks mOpenHelperCallbacks;

    // @formatter:off
    public static final String SQL_CREATE_TABLE_DOWNLOADS = "CREATE TABLE IF NOT EXISTS "
            + DownloadsColumns.TABLE_NAME + " ( "
            + DownloadsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DownloadsColumns.URL + " TEXT, "
            + DownloadsColumns.FILEPATH + " TEXT, "
            + DownloadsColumns.MD5 + " TEXT, "
            + DownloadsColumns.STATE + " INTEGER, "
            + DownloadsColumns.HTTP_STATE + " INTEGER, "
            + DownloadsColumns.MODULE_NAME + " TEXT DEFAULT 'sdp_common', "
            + DownloadsColumns.CURRENT_SIZE + " INTEGER, "
            + DownloadsColumns.TOTAL_SIZE + " INTEGER, "
            + DownloadsColumns.CREATE_TIME + " INTEGER "
            + ", CONSTRAINT unique_name UNIQUE (url) ON CONFLICT REPLACE"
            + " );";

    // @formatter:on

    public static DownloadSQLiteOpenHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = newInstance(context.getApplicationContext());
        }
        return sInstance;
    }

    private static DownloadSQLiteOpenHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */
    private static DownloadSQLiteOpenHelper newInstancePreHoneycomb(Context context) {
        return new DownloadSQLiteOpenHelper(context);
    }

    private DownloadSQLiteOpenHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mOpenHelperCallbacks = new DownloadSQLiteOpenHelperCallbacks();
    }


    /*
     * Post Honeycomb.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static DownloadSQLiteOpenHelper newInstancePostHoneycomb(Context context) {
        return new DownloadSQLiteOpenHelper(context, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private DownloadSQLiteOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, errorHandler);
        mContext = context;
        mOpenHelperCallbacks = new DownloadSQLiteOpenHelperCallbacks();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mOpenHelperCallbacks.onPreCreate(mContext, db);
        db.execSQL(SQL_CREATE_TABLE_DOWNLOADS);
        mOpenHelperCallbacks.onPostCreate(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        mOpenHelperCallbacks.onOpen(mContext, db);
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mOpenHelperCallbacks.onUpgrade(mContext, db, oldVersion, newVersion);
    }
}
