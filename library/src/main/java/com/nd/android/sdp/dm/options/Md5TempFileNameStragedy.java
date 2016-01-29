package com.nd.android.sdp.dm.options;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 默认临时文件命名策略
 * Created by Administrator on 2015/9/18.
 */
public class Md5TempFileNameStragedy implements TempFileNameStragedy, Serializable {

    private static final String HASH_ALGORITHM = "MD5";
    private static final int RADIX = 36;

    public Md5TempFileNameStragedy() {
    }

    public String getTempFileName(String imageUri) {
        byte[] md5 = this.getMD5(imageUri.getBytes());
        BigInteger bi = (new BigInteger(md5)).abs();
        return bi.toString(RADIX);
    }

    private byte[] getMD5(byte[] data) {
        byte[] hash = null;

        try {
            MessageDigest e = MessageDigest.getInstance(HASH_ALGORITHM);
            e.update(data);
            hash = e.digest();
        } catch (NoSuchAlgorithmException var4) {
            var4.printStackTrace();
        }

        return hash;
    }
}
