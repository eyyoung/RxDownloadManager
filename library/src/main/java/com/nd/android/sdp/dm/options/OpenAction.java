package com.nd.android.sdp.dm.options;

import android.content.Context;

import java.io.Serializable;

/**
 * 打开操作接口
 *
 * @author Young
 */
public interface OpenAction extends Serializable {

    /**
     * 打开操作
     *
     * @param filePath the file path
     * @author Young
     */
    void open(Context pContext, String filePath);

}
