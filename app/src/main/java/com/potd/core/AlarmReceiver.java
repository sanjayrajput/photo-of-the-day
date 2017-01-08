package com.potd.core;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import com.potd.ApiException;
import com.potd.GlobalResources;
import com.potd.SDCardAdapter;
import com.potd.models.PicDetailTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 17/01/16.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final Logger logger = Logger.getLogger("AlarmReceiver");

    @Override
    public void onReceive(final Context context, Intent intent) {
        logger.info("setting wallpaper now");
        initPrerequisites(context, intent);
//        Toast.makeText(context, "Setting Wallpaper Now...",
//                Toast.LENGTH_LONG).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                        Looper.prepare();
                    PicDetailTable latestPicture = getLatestPictureFromDB();
                    WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
                    if (latestPicture == null) {
                        if (GlobalResources.isNetworkConnected(context)) {
                            File sdCardDirectory = Environment.getExternalStorageDirectory();
                            String filePath = sdCardDirectory.getAbsolutePath() + SDCardAdapter.BASE_DIRECTORY + "/" + GoogleSpreadSheetAdapter.P12FILE;
                            logger.info("ALARM: file" + filePath);
                            GlobalResources.setP12AuthKeyFile(new FileInputStream(filePath));
                            GoogleSpreadSheetAdapter sheetAdapter = GlobalResources.getGoogleSpreadSheetAdapter();
                            if (sheetAdapter == null) {
                                logger.info("ALARM: initializing google sheet adapter");
                                sheetAdapter = new GoogleSpreadSheetAdapter(GlobalResources.getP12AuthKeyFile());
                                GlobalResources.setGoogleSpreadSheetAdapter(sheetAdapter);
                            }
                            if (GlobalResources.getGoogleSpreadSheetAdapter() != null) {
                                try {
                                    logger.info("fetching from google spreadsheet");
                                    List<PicDetailTable> picDetailTables = GlobalResources.getGoogleSpreadSheetAdapter().get(0, 1);
                                    if (picDetailTables != null && !picDetailTables.isEmpty()) {
                                        latestPicture = picDetailTables.get(0);
                                        logger.info("Latest Pic - Subject: " + latestPicture.getSubject() +
                                                ", Photographer: " + latestPicture.getPhotographer() +
                                                ", Date: " + latestPicture.getDate() + ", Link: " + latestPicture.getLink());
                                    }
                                } catch (ApiException e) {
                                }
                            }
                        } else {
                            logger.info("ALARM: No internet connection");
                        }
                    }
                    if (latestPicture == null)
                        return;
                    Bitmap bitmap = downloadBitMap(latestPicture.getLink());
                    if (bitmap != null) {
                        myWallpaperManager.setBitmap(bitmap);
//                            Toast.makeText(context, "Wallpaper " + latestPicture.getSubject() + " Set Successfully!!!", Toast.LENGTH_LONG).show();
                    }

                } catch (IOException e) {
//                        Toast.makeText(context, "Failed to set wallpaper", Toast.LENGTH_LONG).show();
                    logger.log(Level.SEVERE, "Failed to set background: " + e.getMessage());
                }
            }
        }).start();
    }

    private void initPrerequisites(Context context, Intent intent) {
        if (GlobalResources.getInternalDBHelper() == null) {
            logger.info("initializing internal DB");
            GlobalResources.setInternalDBHelper(new InternalDBHelper(context));
        }
    }

    public PicDetailTable getLatestPictureFromDB() {
        if (GlobalResources.getInternalDBHelper() != null) {
            logger.info("fetching from internal DB");
            PicDetailTable latestRecord = null;
            latestRecord = GlobalResources.getInternalDBHelper().getLatestRecord();
            if (latestRecord != null && latestRecord.getLink() != null &&
                    latestRecord.getDate() != null && latestRecord.getDate().getDay() == new Date().getDay()) {
                return latestRecord;
            }
        }
        return null;
    }

    public Bitmap downloadBitMap(String imageUrl) {
        logger.info("downloading image: " + imageUrl);
        InputStream in = null;
        try {
            in = new java.net.URL(imageUrl).openStream();
            return BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            logger.info("failed to download image");
        }
        return null;
    }
}
