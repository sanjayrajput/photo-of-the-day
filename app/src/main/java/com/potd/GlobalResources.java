package com.potd;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.LruCache;

import com.potd.core.AWSDBManager;
import com.potd.core.DataBaseManager;
import com.potd.core.EndlessScrollListener;
import com.potd.core.GoogleSpreadSheetAdapter;
import com.potd.core.InternalDBHelper;
import com.potd.models.PicDetailTable;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class GlobalResources {

    private static DataBaseManager dataBaseManager;
    private static List<PicDetailTable> picDetailList;
    private static LruCache<String, Bitmap> imageCache;
    private static Set<String> downloadingImages;
    private static ProgressDialog loadingDialog;
    private static float scale = 0;
    private static InternalDBHelper internalDBHelper;
    private static boolean doneLoadingLocally;
    private static int indexToStart = 0;
    private static AWSDBManager awsdbManager;
    private static EndlessScrollListener endlessScrollListener;
    private static GoogleSpreadSheetAdapter googleSpreadSheetAdapter;
    private static InputStream p12AuthKeyFile;
    private static String p12AuthKeyFilePath;
    private static ExecutorService executorService;
    private static boolean storePicInSDCard = false;

    public static DataBaseManager getDataBaseManager() {
        return dataBaseManager;
    }

    public static void setDataBaseManager(DataBaseManager dataBaseManager) {
        GlobalResources.dataBaseManager = dataBaseManager;
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

    public static boolean isDoneLoadingLocally() {
        return doneLoadingLocally;
    }

    public static void setDoneLoadingLocally(boolean doneLoadingLocally) {
        GlobalResources.doneLoadingLocally = doneLoadingLocally;
    }

    public static int getIndexToStart() {
        return indexToStart;
    }

    public static void setIndexToStart(int indexToStart) {
        GlobalResources.indexToStart = indexToStart;
    }

    public static AWSDBManager getAwsdbManager() {
        return awsdbManager;
    }

    public static void setAwsdbManager(AWSDBManager awsdbManager) {
        GlobalResources.awsdbManager = awsdbManager;
    }

    public static EndlessScrollListener getEndlessScrollListener() {
        return endlessScrollListener;
    }

    public static void setEndlessScrollListener(EndlessScrollListener endlessScrollListener) {
        GlobalResources.endlessScrollListener = endlessScrollListener;
    }

    public static GoogleSpreadSheetAdapter getGoogleSpreadSheetAdapter() {
        return googleSpreadSheetAdapter;
    }

    public static void setGoogleSpreadSheetAdapter(GoogleSpreadSheetAdapter googleSpreadSheetAdapter) {
        GlobalResources.googleSpreadSheetAdapter = googleSpreadSheetAdapter;
    }

    public static InputStream getP12AuthKeyFile() {
        return p12AuthKeyFile;
    }

    public static void setP12AuthKeyFile(InputStream p12AuthKeyFile) {
        GlobalResources.p12AuthKeyFile = p12AuthKeyFile;
    }

    public static void setP12AuthKeyFilePath(String p12AuthKeyFilePath) {
        GlobalResources.p12AuthKeyFilePath = p12AuthKeyFilePath;
    }

    public static String getP12AuthKeyFilePath() {
        return p12AuthKeyFilePath;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static void setExecutorService(ExecutorService executorService) {
        GlobalResources.executorService = executorService;
    }

    public static boolean isStorePicInSDCard() {
        return storePicInSDCard;
    }

    public static void setStorePicInSDCard(boolean storePicInSDCard) {
        GlobalResources.storePicInSDCard = storePicInSDCard;
    }
}
