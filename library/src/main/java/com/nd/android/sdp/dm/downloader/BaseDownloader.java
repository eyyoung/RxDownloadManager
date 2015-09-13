package com.nd.android.sdp.dm.downloader;

import android.support.v4.util.ArrayMap;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * The Base downloader.
 *
 * @author Young
 */
public class BaseDownloader implements Downloader {

    private Response mResponse;

    @Override
    public InputStream getStream(String imageUri, ArrayMap<String,String> extra) throws IOException {
        OkHttpClient client = new OkHttpClient();
        final Request.Builder builder = new Request.Builder().url(imageUri);
        if (extra != null) {
            final Iterator<String> iterator = extra.keySet().iterator();
            while (iterator.hasNext()) {
                final String key = iterator.next();
                builder.addHeader(key, extra.get(key));
            }
        }
        final Request request = builder.build();
        mResponse = client.newCall(request).execute();
        return mResponse.body().byteStream();
    }

    @Override
    public long getContentLength() {
        return Long.parseLong(mResponse.header("Content-Length"));
    }
}
