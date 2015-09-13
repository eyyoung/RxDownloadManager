package com.nd.android.sdp.dm.provider.downloads;

import android.support.annotation.Nullable;

import com.nd.android.sdp.dm.provider.base.BaseModel;

import java.util.Date;

/**
 * A human being which is part of a team.
 */
public interface DownloadsModel extends BaseModel {

    /**
     * 下载地址 URL
     * Can be {@code null}.
     */
    @Nullable
    String getUrl();

    /**
     * 本地存放路径
     * Can be {@code null}.
     */
    @Nullable
    String getFilepath();

    /**
     * 文件md5，用于查询是否已经下载过，秒下
     * Can be {@code null}.
     */
    @Nullable
    String getMd5();

    /**
     * 下载状态
     * Can be {@code null}.
     */
    @Nullable
    Integer getState();

    /**
     * 调用模块名称（区分是哪个模块调用的下载）
     * Can be {@code null}.
     */
    @Nullable
    String getModuleName();

    /**
     * 当前已下载大小
     * Can be {@code null}.
     */
    @Nullable
    Long getCurrentSize();

    /**
     * 文件大小
     * Can be {@code null}.
     */
    @Nullable
    Long getTotalSize();

    /**
     * 任务创建时间
     * Can be {@code null}.
     */
    @Nullable
    Date getCreateTime();
}
