package com.nd.android.sdp.dm.options;

import java.io.File;

/**
 * 默认临时文件命名策略
 * Created by Administrator on 2015/9/18.
 */
public class DefaultTempFileNameStragedy implements TempFileNameStragedy{

    @Override
    public File getTempFileName(File origFile) {
        StringBuffer stringBuffer =new StringBuffer(origFile.getAbsolutePath());
        stringBuffer.append(".tmp");
        return new File(stringBuffer.toString());
    }
}
