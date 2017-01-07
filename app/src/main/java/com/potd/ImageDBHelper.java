package com.potd;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.potd.core.InternalDBHelper;
import com.potd.models.PicDetailTable;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 03/05/15.
 */
public class ImageDBHelper {

    private static final Logger logger = Logger.getLogger("PicDetailsAdapter");

    public static Bitmap getImage(String link, Date date) {
        Bitmap bmp = null;
        //--------- Looking in cache --------
        bmp = getFromCache(link);
        //-------- Looking in Internal Database --------
        if (bmp == null)
            bmp = getFromInternalStorage(link, date);
        return bmp;
    }

    public static Bitmap getFromCache(String link) {
        LruCache<String, Bitmap> imageCache = GlobalResources.getImageCache();
        Bitmap bitmap = imageCache.get(link);
        if (bitmap != null) {
            logger.info("Found image in **** CACHE ***");
            return bitmap;
        }
        return null;
    }

    public static Bitmap getFromInternalStorage(String link, Date date) {
        InternalDBHelper internalDBHelper = GlobalResources.getInternalDBHelper();
        LruCache<String, Bitmap> imageCache = GlobalResources.getImageCache();
        PicDetailTable image = internalDBHelper.getImageByDate(date);
        if (image != null) {
            if (image.getBitmap() != null) {
                logger.info("Found image in **** LOCAL DB ***");
                imageCache.put(link, image.getBitmap());
            }
            return image.getBitmap();
        }
        return null;
    }
}
