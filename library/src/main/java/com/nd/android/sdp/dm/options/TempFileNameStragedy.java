package com.nd.android.sdp.dm.options;

import java.io.File;

/**
 * 临时文件命名策略
 * Created by Administrator on 2015/9/18.
 */
public interface TempFileNameStragedy {
    /**
     * 获取临时文件名
     *
     * @param origFile 原文件名
     * @return
     */
    File getTempFileName(File origFile);
}
