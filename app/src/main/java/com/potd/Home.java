package com.potd;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.potd.adapters.PicDetailsAdapter;
import com.potd.models.PicDetailTable;
import com.potd.models.PicDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Home extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isNetworkConnected()) {
            Toast.makeText(this, "No Internet Connection",
                    Toast.LENGTH_LONG).show();
            return;
        }
        setContentView(R.layout.activity_home);
        final ListView picList = (ListView) findViewById(R.id.picsList);
        GlobalResources.setImages(new LruCache<String, Bitmap>(Configuration.cacheSize));
        GlobalResources.setPicDetailList(new ArrayList<PicDetailTable>());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AsyncTask<Object, Void, List<PicDetailTable>> task = new InitApplication(picList, getApplicationContext()).execute(picList);
            }
        });

//        PicDetailsAdapter adapter = new PicDetailsAdapter(this, R.layout.main_list, GlobalResources.getPicDetailList());
//        picList.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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
    public void onBackPressed() {
        finish();
    }


    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return network != null;
    }
}
