package com.potd.core;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.potd.Configuration;
import com.potd.GlobalResources;
import com.potd.models.PicDetailTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                    }catch (Exception e) {
                        logger.log(Level.ALL, "Exception : " + e.getMessage());
                    }
                }
            });
            thread.start();
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
