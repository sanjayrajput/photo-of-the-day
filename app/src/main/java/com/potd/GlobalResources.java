package com.potd;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.potd.models.PicDetailTable;

import java.util.List;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class GlobalResources {

    private static DBManager dbManager;
    private static List<PicDetailTable> picDetailList;
    private static LruCache<String, Bitmap> images;

    public static DBManager getDbManager() {
        return dbManager;
    }

    public static void setDbManager(DBManager dbManager) {
        GlobalResources.dbManager = dbManager;
    }

    public static List<PicDetailTable> getPicDetailList() {
        return picDetailList;
    }

    public static void setPicDetailList(List<PicDetailTable> picDetailList) {
        GlobalResources.picDetailList = picDetailList;
    }

    public static LruCache<String, Bitmap> getImages() {
        return images;
    }

    public static void setImages(LruCache<String, Bitmap> images) {
        GlobalResources.images = images;
    }
}
