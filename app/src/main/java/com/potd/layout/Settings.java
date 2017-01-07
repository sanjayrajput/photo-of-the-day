package com.potd.layout;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.potd.GlobalResources;
import com.potd.R;
import com.potd.core.AlarmReceiver;
import com.potd.core.GoogleSpreadSheetAdapter;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 17/01/16.
 */
public class Settings extends Activity {

    private static final Logger logger = Logger.getLogger("Settings");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        int setWallAutTick = R.id.setWallAutTick;
        CheckBox setWallAutCheckbox = (CheckBox) findViewById(setWallAutTick);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        String setAutWallPaper = GlobalResources.getInternalDBHelper().getConfigValue("setAutWallPaper");
        if (setAutWallPaper != null) {
            if (setAutWallPaper.equalsIgnoreCase("true")) {
                setWallAutCheckbox.setChecked(true);
            } else {
                setWallAutCheckbox.setChecked(false);
            }
        }
        setWallAutCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        logger.info("set wallpaper automatically checkbox clicked : " + isChecked);
                        if (isChecked) {
                            logger.info("setting alarm for setting wallpaper automatically");
                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                    System.currentTimeMillis(),
                                    10 * 60 * 1000,
                                    pendingIntent);
                            GlobalResources.getInternalDBHelper().insertInConfig("setAutWallPaper", "true");
                        } else {
                            GlobalResources.getInternalDBHelper().insertInConfig("setAutWallPaper", "false");
                            logger.info("cancelling alarm for setting wallpaper automatically");
                            if (alarmManager!= null) {
                                alarmManager.cancel(pendingIntent);
                            }
                        }
                    }
                }
        );


        int offlineSupportTick = R.id.offlineSupportTick;
        CheckBox offlineSupportCheckbox = (CheckBox) findViewById(offlineSupportTick);
        String offlineMode = GlobalResources.getInternalDBHelper().getConfigValue("offlineMode");
        if (offlineMode != null) {
            if (offlineMode.equalsIgnoreCase("true")) {
                offlineSupportCheckbox.setChecked(true);
                GlobalResources.setStorePicInSDCard(true);
            } else {
                offlineSupportCheckbox.setChecked(false);
                GlobalResources.setStorePicInSDCard(false);
            }
        }
        offlineSupportCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        logger.info("offline mode checkbox clicked : " + isChecked);
                        if (isChecked) {
                            logger.info("setting offline mode");
                            GlobalResources.setStorePicInSDCard(true);
                            GlobalResources.getInternalDBHelper().insertInConfig("offlineMode", "true");
                        } else {
                            logger.info("cancelling offline mode ");
                            GlobalResources.setStorePicInSDCard(false);
                            GlobalResources.getInternalDBHelper().insertInConfig("offlineMode", "false");
                        }
                    }
                }
        );
    }
}