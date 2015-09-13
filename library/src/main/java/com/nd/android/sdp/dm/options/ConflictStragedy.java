package com.nd.android.sdp.dm.options;

import java.io.File;
import java.io.Serializable;

/**
 * 重名文件防冲突策略
 */
public interface ConflictStragedy extends Serializable {

    /**
     * 获取重命名文件名
     *
     * @param origFile 原文件名
     * @return
     */
    File getRepeatFileName(File origFile);

}
