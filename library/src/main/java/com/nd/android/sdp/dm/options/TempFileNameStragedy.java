package com.nd.android.sdp.dm.options;

import java.io.Serializable;

/**
 * 临时文件命名策略
 * Created by Administrator on 2015/9/18.
 */
public interface TempFileNameStragedy extends Serializable {
    /**
     * 获取临时文件名
     *
     * @param url the url
     * @return temp file name
     */
    String getTempFileName(String url);
}
