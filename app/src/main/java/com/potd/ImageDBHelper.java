package com.potd;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.potd.core.InternalDBHelper;
import com.potd.models.PicDetailTable;

import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 03/05/15.
 */
public class ImageDBHelper {

    private static final Logger logger = Logger.getLogger("PicDetailsAdapter");

    public static Bitmap getImage(String link) {
        Bitmap bmp = null;
        //--------- Looking in cache --------
        bmp = getFromCache(link);
        //-------- Looking in Internal Database --------
        if (bmp == null)
            bmp = getFromInternalStorage(link);
        return bmp;
    }

    public static Bitmap getFromCache(String link) {
        LruCache<String, Bitmap> imageCache = GlobalResources.getImageCache();
        Bitmap bitmap = imageCache.get(link);
        if (bitmap != null) {
            logger.info("Found image in cache");
            return bitmap;
        }
        return null;
    }

    public static Bitmap getFromInternalStorage(String link) {
        InternalDBHelper internalDBHelper = GlobalResources.getInternalDBHelper();
        LruCache<String, Bitmap> imageCache = GlobalResources.getImageCache();
        PicDetailTable image = internalDBHelper.getImage(link);
        if (image != null) {
            logger.info("Found image in local database");
            imageCache.put(link, image.getBitmap());
            return image.getBitmap();
        }
        return null;
    }
}
