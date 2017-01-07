package com.potd.layout;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.potd.GlobalResources;
import com.potd.R;
import com.potd.core.AlarmReceiver;
import com.potd.core.EndlessScrollListener;
import com.potd.core.GoogleSpreadSheetAdapter;
import com.potd.core.InitApplication;
import com.potd.core.UpdateLatestTask;
import com.potd.models.PicDetailTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Home extends Activity {

    public static Context appContext;
    private static final Logger logger = Logger.getLogger("Home");
    public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    public static boolean isMovedToBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        appContext = this;

        final float scale = this.getResources().getDisplayMetrics().density;
        GlobalResources.setScale(scale);
        saveP12KeyFile();

//        int setWallAutTick = R.id.setWallAutTick;
//        View set = (View) findViewById(settings);
//        CheckBox setWallAutCheckbox = (CheckBox) findViewById(setWallAutTick);
//        String setAutWallPaper = GlobalResources.getInternalDBHelper().getConfigValue("setAutWallPaper");


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
                    AsyncTask<Object, Void, List<PicDetailTable>> task = new InitApplication(picList, getApplicationContext(), appContext).execute(0, false);
                }
            }
        });
        if (picList != null) {
            EndlessScrollListener endlessScrollListener = new EndlessScrollListener(getApplicationContext(), appContext, picList);
            GlobalResources.setEndlessScrollListener(endlessScrollListener);
            picList.setOnScrollListener(endlessScrollListener);
        }
    }


    @Override
    protected void onResume() {
        if (!isMovedToBack) {
            isMovedToBack = false;
            super.onResume();
            return;
        }
        isMovedToBack = false;
        checkForUpdate(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            checkForUpdate(true);
            return true;
        } else if (id == R.id.action_rate_app) {
            rateTheApp();
            return true;
//        } else if (id == R.id.action_skip_to_date) {
//            skipToDate();
//            return true;
        } else if (id == R.id.action_skip_to_top) {
            skipToTop();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Settings.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
            this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void skipToTop() {
        final ListView picList = (ListView) findViewById(R.id.picsList);
//        picList.smoothScrollToPosition(0);
        picList.setSelectionAfterHeaderView();
    }

    private void skipToDate() {

    }

    @Override
    public void onBackPressed() {
        isMovedToBack = true;
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

    public void checkForUpdate(boolean upToDateToast) {
        List<PicDetailTable> currentList = GlobalResources.getPicDetailList();
        if (currentList != null && !currentList.isEmpty()) {
            PicDetailTable topItem = currentList.get(0);
            final ListView picList = (ListView) findViewById(R.id.picsList);
            try {
                Date date = topItem.getDate();
                Date topPicDate = df.parse(df.format(date));
                Date today = df.parse(df.format(new Date()));
                if (!topPicDate.equals(today)) {
                    if (GlobalResources.isNetworkConnected(getApplicationContext())) {
                        Toast.makeText(this, "Fetching latest photographs...",
                                Toast.LENGTH_LONG).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new UpdateLatestTask(picList, getApplicationContext()).execute();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Failed to update. No Internet Connection.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (upToDateToast) {
                        Toast.makeText(this, "Content is up to date",
                                Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                logger.info("Failed to compare Dates...");
            }
        }
    }

    public void rateTheApp() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void saveP12KeyFile() {
        try {
            String filePath = "";
            File sdCardDirectory = Environment.getExternalStorageDirectory();
            filePath = sdCardDirectory.getAbsolutePath() + "/PhotoOfTheDay";
            boolean created = new File(filePath).mkdirs();
            filePath += "/" + GoogleSpreadSheetAdapter.P12FILE;
            logger.info("Path : " + filePath);
            File keyFile = new File(filePath);
            if (!keyFile.exists()) {
                InputStream inputStream = getAssets().open(GoogleSpreadSheetAdapter.P12FILE);
                FileOutputStream outputStream = new FileOutputStream(keyFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                inputStream.close();
                outputStream.close();
            }
            GlobalResources.setP12AuthKeyFilePath(filePath);
        } catch (IOException e) {
            logger.log(Level.ALL, "Failed to save " + e.getMessage(), e);
        }
    }
}
