package com.nd.android.sdp.dm.provider.downloads;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.nd.android.sdp.dm.provider.base.AbstractCursor;

import java.util.Date;

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
     * 下载地址 URL
     * Can be {@code null}.
     */
    @Nullable
    public String getUrl() {
        return getStringOrNull(DownloadsColumns.URL);
    }

    /**
     * 本地存放路径
     * Can be {@code null}.
     */
    @Nullable
    public String getFilepath() {
        return getStringOrNull(DownloadsColumns.FILEPATH);
    }

    /**
     * 文件md5，用于查询是否已经下载过，秒下
     * Can be {@code null}.
     */
    @Nullable
    public String getMd5() {
        return getStringOrNull(DownloadsColumns.MD5);
    }

    /**
     * 下载状态
     * Can be {@code null}.
     */
    @Nullable
    public Integer getState() {
        return getIntegerOrNull(DownloadsColumns.STATE);
    }

    @Nullable
    public Integer getHttpState() {
        return getIntegerOrNull(DownloadsColumns.HTTP_STATE);
    }

    /**
     * 调用模块名称（区分是哪个模块调用的下载）
     * Can be {@code null}.
     */
    @Nullable
    public String getModuleName() {
        return getStringOrNull(DownloadsColumns.MODULE_NAME);
    }

    /**
     * 当前已下载大小
     * Can be {@code null}.
     */
    @Nullable
    public Long getCurrentSize() {
        return getLongOrNull(DownloadsColumns.CURRENT_SIZE);
    }

    /**
     * 文件大小
     * Can be {@code null}.
     */
    @Nullable
    public Long getTotalSize() {
        return getLongOrNull(DownloadsColumns.TOTAL_SIZE);
    }

    /**
     * 任务创建时间
     * Can be {@code null}.
     */
    @Nullable
    public Date getCreateTime() {
        return getDateOrNull(DownloadsColumns.CREATE_TIME);
    }
}
