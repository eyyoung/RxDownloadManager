package com.nd.android.sdp.dm.provider.downloads;

import java.util.Date;

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nd.android.sdp.dm.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code downloads} table.
 */
public class DownloadsContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return DownloadsColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable DownloadsSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable DownloadsSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Downlods Task ID
     */
    public DownloadsContentValues putUrl(@Nullable String value) {
        mContentValues.put(DownloadsColumns.URL, value);
        return this;
    }

    public DownloadsContentValues putUrlNull() {
        mContentValues.putNull(DownloadsColumns.URL);
        return this;
    }

    /**
     * Local File Path
     */
    public DownloadsContentValues putFilepath(@Nullable String value) {
        mContentValues.put(DownloadsColumns.FILEPATH, value);
        return this;
    }

    public DownloadsContentValues putFilepathNull() {
        mContentValues.putNull(DownloadsColumns.FILEPATH);
        return this;
    }

    public DownloadsContentValues putState(@Nullable Integer value) {
        mContentValues.put(DownloadsColumns.STATE, value);
        return this;
    }

    public DownloadsContentValues putStateNull() {
        mContentValues.putNull(DownloadsColumns.STATE);
        return this;
    }

    /**
     * Function Segment
     */
    public DownloadsContentValues putModuleName(@Nullable String value) {
        mContentValues.put(DownloadsColumns.MODULE_NAME, value);
        return this;
    }

    public DownloadsContentValues putModuleNameNull() {
        mContentValues.putNull(DownloadsColumns.MODULE_NAME);
        return this;
    }

    /**
     * Current Size
     */
    public DownloadsContentValues putCurrentSize(@Nullable Long value) {
        mContentValues.put(DownloadsColumns.CURRENT_SIZE, value);
        return this;
    }

    public DownloadsContentValues putCurrentSizeNull() {
        mContentValues.putNull(DownloadsColumns.CURRENT_SIZE);
        return this;
    }

    public DownloadsContentValues putTotalSize(@Nullable Long value) {
        mContentValues.put(DownloadsColumns.TOTAL_SIZE, value);
        return this;
    }

    public DownloadsContentValues putTotalSizeNull() {
        mContentValues.putNull(DownloadsColumns.TOTAL_SIZE);
        return this;
    }

    public DownloadsContentValues putCreateTime(@Nullable Date value) {
        mContentValues.put(DownloadsColumns.CREATE_TIME, value == null ? null : value.getTime());
        return this;
    }

    public DownloadsContentValues putCreateTimeNull() {
        mContentValues.putNull(DownloadsColumns.CREATE_TIME);
        return this;
    }

    public DownloadsContentValues putCreateTime(@Nullable Long value) {
        mContentValues.put(DownloadsColumns.CREATE_TIME, value);
        return this;
    }
}
