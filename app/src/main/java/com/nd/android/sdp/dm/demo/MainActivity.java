package com.nd.android.sdp.dm.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nd.android.sdp.dm.DownloadManager;
import com.nd.android.sdp.dm.observer.DownloadObserver;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.options.DownloadOptionsBuilder;

import java.io.File;

public class MainActivity extends AppCompatActivity implements DownloadObserver.OnDownloadLisener {

    private static final String URL1 = "http://apps.pba.cn/apk/Hardware_release.apk";
    private static final String URL2 = "http://apps.pba.cn/apk/Hardware_release2.apk";
    private static final String URL3 = "http://down.sandai.net/thunderspeed/ThunderSpeed1.0.31.338.exe";
    private ProgressBar mPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPb = (ProgressBar) findViewById(R.id.pb);
        DownloadManager.INSTANCE.init(this);
        DownloadManager.INSTANCE.registerDownloadListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void onDownload(View view) {
        final DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test.apk")
                .parentDirPath("/sdcard")
                .openAction((pContext, filePath) -> {
                    File file = new File(filePath);
                    Toast.makeText(pContext, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                })
                .needNotificationBar(true)
                .build();
        DownloadManager.INSTANCE.start(this, URL1, options);
    }

    public void onStop(View view) {
        DownloadManager.INSTANCE.cancel(this, URL1);
    }

    @Override
    public void onPause(String pUrl) {

    }

    @Override
    public void onComplete(String pUrl) {
        if (pUrl.equals(URL1)) {
            Toast.makeText(this, "Complete", Toast.LENGTH_SHORT).show();
        } else if (pUrl.equals(URL2)) {
            Toast.makeText(this, "Md5 Complete", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProgress(String pUrl, long current, long total) {
        if (pUrl.equals(URL1)) {
            mPb.setProgress((int) (current * 100 / total));
        }
        Log.d("MainActivity", (current + " " + total));
    }

    @Override
    public void onCancel(String pUrl) {
        Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
    }

    public void onPause(View view) {
        DownloadManager.INSTANCE.pause(this, URL1);
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    }

    public void onRepeatMd5(View view) {
        final DownloadOptions options = new DownloadOptionsBuilder()
                .fileName("test2.apk")
                .parentDirPath("/sdcard")
                .build();
        DownloadManager.INSTANCE.start(this, URL2, "d41d8cd98f00b204e9800998ecf8427e", options);
        DownloadManager.INSTANCE.registerDownloadListener(this);
    }

    public void onCheckMd5(View view) {
        File file = DownloadManager.INSTANCE.getDownloadedFile(this, "d41d8cd98f00b204e9800998ecf8427e");
        Toast.makeText(this, "file.exists():" + file.exists(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadManager.INSTANCE.unregisterDownloadListener(this);
    }
}
