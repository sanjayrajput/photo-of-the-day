package com.potd.layout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.potd.GlobalResources;
import com.potd.R;
import com.potd.core.AlarmReceiver;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 08/01/17.
 */
public class Preferences extends PreferenceActivity {

    private static final Logger logger = Logger.getLogger("Preferences");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        final Context context  = this;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                handler(prefs, key, true, context);
            }
        });
        handler(defaultSharedPreferences, "set_wallpaper_aut", false, context);
        handler(defaultSharedPreferences, "offline_mode", false, context);
    }

    public static void handler(SharedPreferences prefs, String key, boolean action, Context context) {
        if (key.equalsIgnoreCase("set_wallpaper_aut")) {
            boolean set_wallpaper_aut = prefs.getBoolean("set_wallpaper_aut", false);
//            GlobalResources.getInternalDBHelper().insertInConfig("setAutWallPaper", String.valueOf(set_wallpaper_aut));

            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            if (action) {
                if (set_wallpaper_aut) {
                    logger.info("setting alarm for setting wallpaper automatically: " + set_wallpaper_aut);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis(),
                            10 * 60 * 1000,
                            pendingIntent);
                } else {
                    if (alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                    }
                }
            }
        }
        if (key.equalsIgnoreCase("offline_mode")) {
            boolean offline_mode = prefs.getBoolean("offline_mode", false);
//            GlobalResources.getInternalDBHelper().insertInConfig("offlineMode", String.valueOf(offline_mode));
            logger.info("setting offline mode: " + offline_mode);
            GlobalResources.setStorePicInSDCard(offline_mode);
        }
    }
}
