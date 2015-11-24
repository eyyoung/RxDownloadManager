package com.nd.android.sdp.dm.processor;

/**
 * 数据处理器监听器
 *
 * @author Young
 */
public interface DataProcessorListener {

    void onNotifyProgress(long progress, long total);

    boolean isCanceled();

}
