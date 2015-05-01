package com.potd;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.potd.core.DBManager;
import com.potd.models.PicDetailTable;

import java.util.List;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class GlobalResources {

    private static DBManager dbManager;
    private static List<PicDetailTable> picDetailList;
    private static LruCache<String, Bitmap> images;
    private static ProgressDialog loadingDialog;
    private static float scale = 0;

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

    public static ProgressDialog getLoadingDialog() {
        return loadingDialog;
    }

    public static void setLoadingDialog(ProgressDialog loadingDialog) {
        GlobalResources.loadingDialog = loadingDialog;
    }

    public static float getScale() {
        return scale;
    }

    public static void setScale(float scale) {
        GlobalResources.scale = scale;
    }
}
