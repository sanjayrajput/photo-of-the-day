package com.potd.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.potd.GlobalResources;
import com.potd.ImageDBHelper;
import com.potd.Utils;
import com.potd.models.PicDetailTable;

import java.io.InputStream;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView imageView;
    private ImageView loadingAnimation;
    private String activity;
    private PicDetailTable picDetailTable;
    private Context applicationContext;

    public ImageDownloaderTask(ImageView view, ImageView animation, String activity, PicDetailTable picDetailTable, Context applicationContext) {
        this.imageView = view;
        this.loadingAnimation = animation;
        this.activity = activity;
        this.picDetailTable = picDetailTable;
        this.applicationContext = applicationContext;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String imageUrl = picDetailTable.getLink();

        if (GlobalResources.getDownloadingImages().contains(imageUrl) && activity != null && !activity.equalsIgnoreCase("FullScreen")) {
            Log.i("ImageDownloaderTask", "Already downloading Image : " + picDetailTable.getSubject());
            return null;
        }
        Bitmap bitmap = ImageDBHelper.getFromInternalStorage(imageUrl, picDetailTable.getDate());
        if (bitmap != null) {
            picDetailTable.setBitmap(bitmap);
            return bitmap;
        }

        GlobalResources.getDownloadingImages().add(imageUrl);
        Bitmap imageBitmap = null;
        boolean imageDownloaded = false;
        int retryCount = 2;
        while (!imageDownloaded && retryCount > 0) {
            try {
                Log.i("ImageDownloaderTask", "Downloading Image : " + picDetailTable.getSubject());
                Log.i("ImageDownloaderTask", "image url : " + imageUrl);
                InputStream in = new java.net.URL(imageUrl).openStream();
                imageBitmap = BitmapFactory.decodeStream(in);
//                imageBitmap = Utils.decodeSampledBitmapFromResource(in, 4096, 4096);
                picDetailTable.setBitmap(imageBitmap);

               /* //---- Code to download image from Amazon DB -----
                AWSDBManager awsdbManager = GlobalResources.getAwsdbManager();
                if (awsdbManager == null) {
                    awsdbManager = new AWSDBManager();
                    GlobalResources.setAwsdbManager(awsdbManager);
                }
                imageBitmap = awsdbManager.getImage(imageUrl);
                */

                //------- Cache ----------
                LruCache<String, Bitmap> images = GlobalResources.getImageCache();
                Log.i("ImageDownloaderTask", "Image Size : " + imageBitmap.getRowBytes() / 1000.0 + " KB");
                Log.i("ImageDownloaderTask", "Putting image in cache: " + picDetailTable.getSubject());
                images.put(imageUrl, imageBitmap);

                //------- Internal Storage ----------
                InternalDBHelper internalDBHelper = GlobalResources.getInternalDBHelper();
                Log.i("ImageDownloaderTask", "Putting image in internal database: " + picDetailTable.getSubject());
                if (picDetailTable != null)
                    internalDBHelper.insert(picDetailTable, applicationContext);

                imageDownloaded = true;
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                Log.i("Info", "Downloading again...");
                retryCount--;
            }
        }
        return imageBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {
            loadingAnimation.setBackgroundResource(0);
            imageView.setImageBitmap(bitmap);

            if (activity.equalsIgnoreCase("Home")) {
                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
//                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                imageView.setLayoutParams(layoutParams);
                imageView.requestLayout();
            }
        }
    }
}
