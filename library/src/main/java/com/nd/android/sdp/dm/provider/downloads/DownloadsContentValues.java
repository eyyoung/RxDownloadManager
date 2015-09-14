package com.nd.android.sdp.dm.provider.downloads;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.nd.android.sdp.dm.provider.base.AbstractContentValues;

import java.util.Date;

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
     * @param where           The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable DownloadsSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where           The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable DownloadsSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * 下载地址 URL
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
     * 本地存放路径
     */
    public DownloadsContentValues putFilepath(@Nullable String value) {
        mContentValues.put(DownloadsColumns.FILEPATH, value);
        return this;
    }

    public DownloadsContentValues putFilepathNull() {
        mContentValues.putNull(DownloadsColumns.FILEPATH);
        return this;
    }

    /**
     * 文件md5，用于查询是否已经下载过，秒下
     */
    public DownloadsContentValues putMd5(@Nullable String value) {
        if (!TextUtils.isEmpty(value)) {
            value = value.toLowerCase();
        }
        mContentValues.put(DownloadsColumns.MD5, value);
        return this;
    }

    public DownloadsContentValues putMd5Null() {
        mContentValues.putNull(DownloadsColumns.MD5);
        return this;
    }

    /**
     * 下载状态
     */
    public DownloadsContentValues putState(@Nullable Integer value) {
        mContentValues.put(DownloadsColumns.STATE, value);
        return this;
    }

    public DownloadsContentValues putStateNull() {
        mContentValues.putNull(DownloadsColumns.STATE);
        return this;
    }

    /**
     * 调用模块名称（区分是哪个模块调用的下载）
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
     * 当前已下载大小
     */
    public DownloadsContentValues putCurrentSize(@Nullable Long value) {
        mContentValues.put(DownloadsColumns.CURRENT_SIZE, value);
        return this;
    }

    public DownloadsContentValues putCurrentSizeNull() {
        mContentValues.putNull(DownloadsColumns.CURRENT_SIZE);
        return this;
    }

    /**
     * 文件大小
     */
    public DownloadsContentValues putTotalSize(@Nullable Long value) {
        mContentValues.put(DownloadsColumns.TOTAL_SIZE, value);
        return this;
    }

    public DownloadsContentValues putTotalSizeNull() {
        mContentValues.putNull(DownloadsColumns.TOTAL_SIZE);
        return this;
    }

    /**
     * 任务创建时间
     */
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
