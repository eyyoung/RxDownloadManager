package com.nd.android.sdp.dm.provider.downloads;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nd.android.sdp.dm.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code downloads} table.
 */
public class DownloadsCursor extends AbstractCursor implements DownloadsModel {
    public DownloadsCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(DownloadsColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Downlods Task ID
     * Can be {@code null}.
     */
    @Nullable
    public String getUrl() {
        String res = getStringOrNull(DownloadsColumns.URL);
        return res;
    }

    /**
     * Local File Path
     * Can be {@code null}.
     */
    @Nullable
    public String getFilepath() {
        String res = getStringOrNull(DownloadsColumns.FILEPATH);
        return res;
    }

    /**
     * Get the {@code state} value.
     * Can be {@code null}.
     */
    @Nullable
    public Integer getState() {
        Integer res = getIntegerOrNull(DownloadsColumns.STATE);
        return res;
    }

    /**
     * Function Segment
     * Can be {@code null}.
     */
    @Nullable
    public String getModuleName() {
        String res = getStringOrNull(DownloadsColumns.MODULE_NAME);
        return res;
    }

    /**
     * Current Size
     * Can be {@code null}.
     */
    @Nullable
    public Long getCurrentSize() {
        Long res = getLongOrNull(DownloadsColumns.CURRENT_SIZE);
        return res;
    }

    /**
     * Get the {@code total_size} value.
     * Can be {@code null}.
     */
    @Nullable
    public Long getTotalSize() {
        Long res = getLongOrNull(DownloadsColumns.TOTAL_SIZE);
        return res;
    }

    /**
     * Get the {@code create_time} value.
     * Can be {@code null}.
     */
    @Nullable
    public Date getCreateTime() {
        Date res = getDateOrNull(DownloadsColumns.CREATE_TIME);
        return res;
    }
}
