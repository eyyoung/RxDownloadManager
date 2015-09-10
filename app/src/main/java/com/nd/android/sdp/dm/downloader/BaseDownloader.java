package com.nd.android.sdp.dm.downloader;

import com.nd.android.okhttp.OkHttpClient;
import com.nd.android.okhttp.Request;
import com.nd.android.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The Base downloader.
 *
 * @author Young
 */
public class BaseDownloader implements Downloader {

    private Response mResponse;

    @Override
    public InputStream getStream(String imageUri, Object extra) throws IOException {
        HashMap<String, String> header = null;
        if (extra instanceof HashMap) {
            header = (HashMap<String, String>) extra;
        }
        OkHttpClient client = new OkHttpClient();
        final Request.Builder builder = new Request.Builder().url(imageUri);
        if (header != null) {
            final Iterator<String> iterator = header.keySet().iterator();
            while (iterator.hasNext()) {
                final String key = iterator.next();
                builder.addHeader(key, header.get(key));
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
