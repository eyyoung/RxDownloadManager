package com.nd.android.sdp.dm.provider.downloads;

import com.nd.android.sdp.dm.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * ND SDP Downlods Manager
 */
public interface DownloadsModel extends BaseModel {

    /**
     * Downlods Task ID
     * Can be {@code null}.
     */
    @Nullable
    String getUrl();

    /**
     * Local File Path
     * Can be {@code null}.
     */
    @Nullable
    String getFilepath();

    /**
     * Get the {@code state} value.
     * Can be {@code null}.
     */
    @Nullable
    Integer getState();

    /**
     * Function Segment
     * Can be {@code null}.
     */
    @Nullable
    String getModuleName();

    /**
     * Current Size
     * Can be {@code null}.
     */
    @Nullable
    Long getCurrentSize();

    /**
     * Get the {@code total_size} value.
     * Can be {@code null}.
     */
    @Nullable
    Long getTotalSize();

    /**
     * Get the {@code create_time} value.
     * Can be {@code null}.
     */
    @Nullable
    Date getCreateTime();
}
