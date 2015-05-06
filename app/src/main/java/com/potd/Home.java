package com.potd;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.potd.adapters.PicDetailsAdapter;
import com.potd.core.EndlessScrollListener;
import com.potd.core.InitApplication;
import com.potd.models.PicDetailTable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


public class Home extends Activity {

    public static Context appContext;
    private static final Logger logger = Logger.getLogger("Home");
    public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        appContext = this;

        final float scale = this.getResources().getDisplayMetrics().density;
        GlobalResources.setScale(scale);

        if (!GlobalResources.isNetworkConnected(appContext)) {
            Toast.makeText(this, "No Internet Connection",
                    Toast.LENGTH_LONG).show();
            ProgressDialog loading = ProgressDialog.show(this, "Please Wait...", "loading", false);
            loading.hide();
            GlobalResources.setLoadingDialog(loading);
            GlobalResources.setDoneLoadingLocally(true);
        } else {
            ProgressDialog pd = ProgressDialog.show(this, "Please Wait...", "loading", true);
            GlobalResources.setLoadingDialog(pd);
        }

        final ListView picList = (ListView) findViewById(R.id.picsList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (picList != null) {
                    AsyncTask<Object, Void, List<PicDetailTable>> task = new InitApplication(picList, getApplicationContext(), appContext).execute(0);
                }
            }
        });
        if (picList != null) {
            picList.setOnScrollListener(new EndlessScrollListener(getApplicationContext(), appContext, picList));
        }
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
        if (id == R.id.action_refresh) {
            List<PicDetailTable> currentList = GlobalResources.getPicDetailList();
            if (currentList != null && !currentList.isEmpty()) {
                try {
                    Date date = currentList.get(0).getDate();
                    Date topPicDate = df.parse(df.format(date));
                    Date today = df.parse(df.format(new Date()));
                    if (topPicDate.equals(today)) {
                        Toast.makeText(this, "Content is up to date",
                                Toast.LENGTH_LONG).show();
                        return true;
                    }
                } catch (ParseException e) {
                    logger.info("Failed to compare dates...");
                }
            }
            ArrayList<PicDetailTable> newList = new ArrayList<>();
            GlobalResources.setPicDetailList(newList);
            final ListView picList = (ListView) findViewById(R.id.picsList);
            ((PicDetailsAdapter)picList.getAdapter()).updateList(newList);
            new InitApplication(picList, getApplicationContext(), appContext).execute(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
//        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl1);
        ImageView imageView = (ImageView) findViewById(R.id.picofday);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            imageView.requestLayout();
//            rl.getLayoutParams().height = (int) (1000 * scale + 0.5f);
//            rl.requestLayout();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.requestLayout();
//            rl.getLayoutParams().height = (int) (570 * scale + 0.5f);
        }
    }

}
