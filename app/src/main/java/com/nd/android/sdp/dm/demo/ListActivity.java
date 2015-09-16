package com.nd.android.sdp.dm.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nd.android.sdp.dm.DownloadManager;
import com.nd.android.sdp.dm.observer.DownloadObserver;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.options.DownloadOptionsBuilder;
import com.nd.android.sdp.dm.pojo.BaseDownloadInfo;
import com.nd.android.sdp.dm.pojo.IDownloadInfo;
import com.nd.android.sdp.dm.state.State;

public class ListActivity extends AppCompatActivity implements DownloadObserver.OnDownloadLisener {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mListView = (ListView) findViewById(R.id.lvData);
        mListView.setAdapter(new DownloadAdapter());
        DownloadManager.INSTANCE.registerDownloadListener(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(String pUrl) {
//        Log.e("ListActivity", "onPause" + pUrl);
    }

    @Override
    public void onComplete(String pUrl) {

    }

    @Override
    public void onProgress(String pUrl, long current, long total) {
        Log.d("ListActivity", pUrl);
        Log.d("ListActivity", "current:" + current);
        Log.d("ListActivity", "total:" + total);
    }

    @Override
    public void onCancel(String pUrl) {

    }

    @Override
    public void onError(String pUrl, int httpState) {

    }

    private class DownloadAdapter extends BaseAdapter implements View.OnClickListener {

        private String[] downloadUrls = new String[]{
                "http://ww1.sinaimg.cn/bmiddle/d54a1fa7jw1ew39p7sl90g20ak05sqv6.gif",
                "http://ww2.sinaimg.cn/bmiddle/bea3a845gw1ew47xye2csg206p088ha6.gif",
                "http://ww2.sinaimg.cn/bmiddle/a71cfe56gw1ew49kdx66lg208c07nx1i.gif",
                "http://www.texts.io/Texts-0.23.5.msi",
                "http://down.360safe.com/360ap/360freeap_whole_setup_5.3.0.3010.exe",
                "http://betacs.101.com/v0.1/download?dentryId=0c14337f-439a-4ee5-8b42-e8f5cb374f38",
                "http://betacs.101.com/v0.1/download?dentryId=60d34463-af82-4292-8f78-aa59e4d6e431&session=cc76fc38-d59b-41fb-82ca-07086cd06349"
        };

        @Override
        public int getCount() {
            return downloadUrls.length;
        }

        @Override
        public Object getItem(int i) {
            return downloadUrls[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View pView, ViewGroup pViewGroup) {
            if (pView == null) {
                pView = LayoutInflater.from(ListActivity.this).inflate(R.layout.item_download, pViewGroup, false);
            }
            ((TextView) pView.findViewById(R.id.tvUrl)).setText(((String) getItem(i)));
            pView.findViewById(R.id.btnOper).setOnClickListener(this);
            pView.findViewById(R.id.btnOper).setTag(i);
            return pView;
        }

        @Override
        public void onClick(View pView) {
            final Object tag = pView.getTag();
            if (tag == null) {
                return;
            }
            final int index = (int) tag;
            try {
                final ArrayMap<String, IDownloadInfo> downloadInfos = DownloadManager.INSTANCE.getDownloadInfos(ListActivity.this, BaseDownloadInfo.class, downloadUrls[index]);
                if (downloadInfos.size() == 0 ||
                        downloadInfos.get(downloadUrls[index]).getState() != State.DOWNLOADING) {
                    DownloadOptions downloadOptions = new DownloadOptionsBuilder()
                            .fileName("file" + index + ".test")
                            .needNotificationBar(true)
                            .parentDirPath("/sdcard/test/")
                            .build();
                    DownloadManager.INSTANCE.start(ListActivity.this, downloadUrls[index], downloadOptions);
                } else {
                    DownloadManager.INSTANCE.pause(ListActivity.this, downloadUrls[index]);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ListActivity.class);
        context.startActivity(starter);
    }


}
