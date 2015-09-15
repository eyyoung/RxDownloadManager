package com.nd.android.sdp.dm.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public final class IoUtils {


    /**
     * {@value}
     */
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 KB
    /**
     * {@value}
     */
    public static final int DEFAULT_FILE_TOTAL_SIZE = 500 * 1024; // 500 Kb
    /**
     * {@value}
     */
    public static final int CONTINUE_LOADING_PERCENTAGE = 75;


    private IoUtils() {
    }


    /**
     * Copies stream, fires progress events by listener, can be interrupted by listener. Uses buffer size =
     * {@value #DEFAULT_BUFFER_SIZE} bytes.
     *
     * @param is       Input stream
     * @param os       Output stream
     * @param listener null-ok; Listener of copying progress and controller of copying interrupting
     * @return <b>true</b> - if stream copied successfully; <b>false</b> - if copying was interrupted by listener
     * @throws IOException
     */
    public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener) throws IOException {
        return copyStream(is, os, listener, DEFAULT_BUFFER_SIZE);
    }


    /**
     * Copies stream, fires progress events by listener, can be interrupted by listener.
     *
     * @param is         Input stream
     * @param os         Output stream
     * @param listener   null-ok; Listener of copying progress and controller of copying interrupting
     * @param bufferSize Buffer size for copying, also represents a step for firing progress listener callback, i.e.
     *                   progress event will be fired after every copied <b>bufferSize</b> bytes
     * @return <b>true</b> - if stream copied successfully; <b>false</b> - if copying was interrupted by listener
     * @throws IOException
     */
    public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener, int bufferSize)
            throws IOException {
        int current = 0;
        int total = is.available();
        if (total <= 0) {
            total = DEFAULT_FILE_TOTAL_SIZE;
        }


        final byte[] bytes = new byte[bufferSize];
        int count;
        if (shouldStopLoading(listener, current, total)) return false;
        while ((count = is.read(bytes, 0, bufferSize)) != -1) {
            os.write(bytes, 0, count);
            current += count;
            if (shouldStopLoading(listener, current, total)) return false;
        }
        os.flush();
        return true;
    }


    private static boolean shouldStopLoading(CopyListener listener, long current, long total) {
        if (listener != null) {
            boolean shouldContinue = listener.onBytesCopied(current, total);
            return !shouldContinue;
//            if (!shouldContinue) {
//                if (100 * current / total < CONTINUE_LOADING_PERCENTAGE) {
//                    Log.d("IoUtils", "true");
//                    return true; // if loaded more than 75% then continue loading anyway
//                }
//            }
        }
        return false;
    }


    /**
     * Reads all data from stream and close it silently
     *
     * @param is Input stream
     */
    public static void readAndCloseStream(InputStream is) {
        final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
        try {
            while (is.read(bytes, 0, DEFAULT_BUFFER_SIZE) != -1) ;
        } catch (IOException ignored) {
        } finally {
            closeSilently(is);
        }
    }


    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }


    /**
     * Listener and controller for copy process
     */
    public static interface CopyListener {
        /**
         * @param current Loaded bytes
         * @param total   Total bytes for loading
         * @return <b>true</b> - if copying should be continued; <b>false</b> - if copying should be interrupted
         */
        boolean onBytesCopied(long current, long total);
    }

    /**
     * 用于断点续传拷贝文件流
     *
     * @param is         输入流
     * @param pFile      文件名
     * @param bufferSize 缓冲区大小
     * @param current    起始位置
     * @param total      此次流长度
     * @param pListener  监听器
     * @return
     * @throws IOException
     */
    public static boolean copyStreamToFile(InputStream is,
                                           File pFile,
                                           int bufferSize, long current, long total,
                                           CopyListener pListener) throws IOException {

        try {
            ensureParentFile(pFile);
            RandomAccessFile os = new RandomAccessFile(pFile, "rw");
            long startOffset = current;
            try {
                os.seek(current);
                byte[] bytes = new byte[bufferSize];
                if (total <= 0L) {
                    total = (long) is.available();
                }

                int count;
                if (shouldStopLoading(pListener, current, startOffset + total)) {
                    return false;
                } else {
                    do {
                        count = is.read(bytes, 0, bufferSize);
                        if (count == -1) {
                            return true;
                        }

                        current += (long) count;
                        os.write(bytes, 0, count);
                    } while (!shouldStopLoading(pListener, current, startOffset + total));
                    return false;
                }
            } finally {
                closeSilently(os);
            }
        } finally {
            closeSilently(is);
        }
    }

    /**
     * 确保父级目录存在
     *
     * @param pFile
     */
    private static void ensureParentFile(File pFile) {
        // Fix Issue #2
        final File parentFile = pFile.getParentFile();
        parentFile.mkdirs();
    }

    public static void copyFile(File pSrcFile, File pDestFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            ensureParentFile(pDestFile);
            in = new FileInputStream(pSrcFile);
            out = new FileOutputStream(pDestFile);
            copyStream(in, out, null);
        } finally {
            closeSilently(in);
            closeSilently(out);
        }
    }
} 