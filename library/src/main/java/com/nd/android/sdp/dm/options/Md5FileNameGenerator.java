package com.nd.android.sdp.dm.options;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5FileNameGenerator implements TmpFileNameGenerator {
    private static final String HASH_ALGORITHM = "MD5";
    private static final int RADIX = 36;

    public Md5FileNameGenerator() {
    }

    public String generate(String imageUri) {
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
        }

        return hash;
    }
}