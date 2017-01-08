package com.potd.core;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.LruCache;
import android.widget.CheckBox;

import com.potd.Configuration;
import com.potd.GlobalResources;
import com.potd.R;
import com.potd.layout.Preferences;
import com.potd.models.PicDetailTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 29/04/15.
 */
public class POTDApplication extends Application {

    private static final Logger logger = Logger.getLogger("InitApplication");

    @Override
    public void onCreate() {
        super.onCreate();
        logger.info("Starting application...");
        try {
            GlobalResources.setImageCache(new LruCache<String, Bitmap>(Configuration.cacheSize));
            GlobalResources.setPicDetailList(new ArrayList<PicDetailTable>());
            GlobalResources.setDownloadingImages(new HashSet<String>());
            GlobalResources.setInternalDBHelper(new InternalDBHelper(getApplicationContext()));
            GlobalResources.setDoneLoadingLocally(false);
//            GlobalResources.setStorePicInSDCard(true);
            GlobalResources.setP12AuthKeyFile(getAssets().open(GoogleSpreadSheetAdapter.P12FILE));
            GlobalResources.setExecutorService(new ThreadPoolExecutor(2, 5, 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1, true), new ThreadPoolExecutor.DiscardPolicy()));

            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Preferences.handler(defaultSharedPreferences, "set_wallpaper_aut", true, this);
            Preferences.handler(defaultSharedPreferences, "offline_mode", true, this);

//            String offlineMode = GlobalResources.getInternalDBHelper().getConfigValue("offlineMode");
//            if (offlineMode != null && offlineMode.equalsIgnoreCase("true")) {
//                logger.info("INIT: setting offline mode");
//                GlobalResources.setStorePicInSDCard(true);
//            }
//
//            String setAutWallPaper = GlobalResources.getInternalDBHelper().getConfigValue("setAutWallPaper");
//            if (setAutWallPaper != null && setAutWallPaper.equalsIgnoreCase("true")) {
//                Intent alarmIntent = new Intent(this, AlarmReceiver.class);
//                final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
//                final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                logger.info("INIT: setting alarm for setting wallpaper automatically");
//                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
//                        System.currentTimeMillis(),
//                        10 * 60 * 1000,
//                        pendingIntent);
//            }
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : " + e.getMessage());
        }

    }

    public void sortResults(List<PicDetailTable> list) {
        if (list.size() > 0) {
            Collections.sort(list, new Comparator<PicDetailTable>() {
                @Override
                public int compare(final PicDetailTable object1, final PicDetailTable object2) {
                    return object2.getDate().compareTo(object1.getDate());
                }
            });
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        logger.info("Terminating application...");
    }
}
