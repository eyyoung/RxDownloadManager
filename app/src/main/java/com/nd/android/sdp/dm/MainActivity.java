package com.nd.android.sdp.dm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.options.DownloadOptionsBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                .fileName("test.exe")
                .parentDirPath("/sdcard")
                .build();
        DownloadManager.INSTANCE.start(this, "http://org.101.com/v2/app/down?appid=10007&product=new99u_win", options);
    }

    public void onStop(View view) {
        DownloadManager.INSTANCE.cancel(this, "http://org.101.com/v2/app/down?appid=10007&product=new99u_win");
    }
}
