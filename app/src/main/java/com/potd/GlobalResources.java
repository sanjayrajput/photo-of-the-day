package com.potd;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.LruCache;

import com.potd.core.InternalDBHelper;
import com.potd.core.MongoDBManager;
import com.potd.models.PicDetailTable;

import java.util.List;
import java.util.Set;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class GlobalResources {

    private static MongoDBManager mongoDbManager;
    private static List<PicDetailTable> picDetailList;
    private static LruCache<String, Bitmap> imageCache;
    private static Set<String> downloadingImages;
    private static ProgressDialog loadingDialog;
    private static float scale = 0;
    private static InternalDBHelper internalDBHelper;

    public static MongoDBManager getMongoDbManager() {
        return mongoDbManager;
    }

    public static void setMongoDbManager(MongoDBManager mongoDbManager) {
        GlobalResources.mongoDbManager = mongoDbManager;
    }

    public static List<PicDetailTable> getPicDetailList() {
        return picDetailList;
    }

    public static void setPicDetailList(List<PicDetailTable> picDetailList) {
        GlobalResources.picDetailList = picDetailList;
    }

    public static InternalDBHelper getInternalDBHelper() {
        return internalDBHelper;
    }

    public static void setInternalDBHelper(InternalDBHelper internalDBHelper) {
        GlobalResources.internalDBHelper = internalDBHelper;
    }

    public static LruCache<String, Bitmap> getImageCache() {
        return imageCache;
    }

    public static void setImageCache(LruCache<String, Bitmap> imageCache) {
        GlobalResources.imageCache = imageCache;
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

    public static Set<String> getDownloadingImages() {
        return downloadingImages;
    }

    public static void setDownloadingImages(Set<String> downloadingImages) {
        GlobalResources.downloadingImages = downloadingImages;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return network != null;
    }
}
