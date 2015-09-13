package com.nd.android.sdp.dm.options;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认重名防冲突策略
 * orig($d)
 * Created by young on 2015/9/13.
 */
public class DefaultConflictStragedy implements ConflictStragedy {
    @Override
    public File getRepeatFileName(File pOrigFile) {
        while (pOrigFile.exists()) {
            String origPath = pOrigFile.getAbsolutePath();
            pOrigFile = new File(getNewName(origPath));
        }
        return pOrigFile;
    }

    public String getNewName(String origPath) {
        StringBuilder stringBuilder = new StringBuilder(origPath);
        Pattern pattern = Pattern.compile("\\(\\d+\\)");
        Matcher matcher = pattern.matcher(origPath);
        if (matcher.find()) {
            // 最后一次匹配索引
            int lastIndexStart;
            int lastIndexEnd;
            do {
                lastIndexStart = matcher.start();
                lastIndexEnd = matcher.end();
            } while (matcher.find());
            String numStr = origPath.substring(lastIndexStart, lastIndexEnd);
            Pattern pattern2 = Pattern.compile("\\d+");
            Matcher matcher2 = pattern2.matcher(numStr);
            matcher2.find();
            String num = matcher2.group();
            stringBuilder.replace(lastIndexStart, lastIndexEnd, "(" + (Integer.parseInt(num) + 1) + ")");
        } else {
            int lastIndexOf = origPath.lastIndexOf(".");
            if (lastIndexOf >= 0) {
                stringBuilder.replace(lastIndexOf, lastIndexOf + 1, "(1).");
            } else {
                stringBuilder.append("(1)");
            }
        }
        return stringBuilder.toString();
    }
}
