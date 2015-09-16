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
        String res = getStringOrNull(DownloadsColumns.URL);
        return res;
    }

    /**
     * 本地存放路径
     * Can be {@code null}.
     */
    @Nullable
    public String getFilepath() {
        String res = getStringOrNull(DownloadsColumns.FILEPATH);
        return res;
    }

    /**
     * 文件md5，用于查询是否已经下载过，秒下
     * Can be {@code null}.
     */
    @Nullable
    public String getMd5() {
        String res = getStringOrNull(DownloadsColumns.MD5);
        return res;
    }

    /**
     * 下载状态
     * Can be {@code null}.
     */
    @Nullable
    public Integer getState() {
        Integer res = getIntegerOrNull(DownloadsColumns.STATE);
        return res;
    }

    @Nullable
    public Integer getHttpState() {
        Integer res = getIntegerOrNull(DownloadsColumns.HTTP_STATE);
        return res;
    }

    /**
     * 调用模块名称（区分是哪个模块调用的下载）
     * Can be {@code null}.
     */
    @Nullable
    public String getModuleName() {
        String res = getStringOrNull(DownloadsColumns.MODULE_NAME);
        return res;
    }

    /**
     * 当前已下载大小
     * Can be {@code null}.
     */
    @Nullable
    public Long getCurrentSize() {
        Long res = getLongOrNull(DownloadsColumns.CURRENT_SIZE);
        return res;
    }

    /**
     * 文件大小
     * Can be {@code null}.
     */
    @Nullable
    public Long getTotalSize() {
        Long res = getLongOrNull(DownloadsColumns.TOTAL_SIZE);
        return res;
    }

    /**
     * 任务创建时间
     * Can be {@code null}.
     */
    @Nullable
    public Date getCreateTime() {
        Date res = getDateOrNull(DownloadsColumns.CREATE_TIME);
        return res;
    }
}
