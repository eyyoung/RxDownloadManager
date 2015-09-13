package com.nd.android.sdp.dm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by young on 2015/9/13.
 */
public class MD5Utils {

    public static String getFileMd5(String filePath) throws NoSuchAlgorithmException, IOException {
        DigestInputStream dis = null;
        FileInputStream inputStream = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            inputStream = new FileInputStream(new File(filePath));
            dis = new DigestInputStream(inputStream, md);
            byte[] digest = md.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xFF & digest[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } finally {
            IoUtils.closeSilently(inputStream);
            IoUtils.closeSilently(dis);
        }
    }

}
