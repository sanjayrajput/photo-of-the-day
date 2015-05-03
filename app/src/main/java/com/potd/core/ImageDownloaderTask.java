package com.potd.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.potd.GlobalResources;
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

    public ImageDownloaderTask(ImageView view, ImageView animation, String activity, PicDetailTable picDetailTable) {
        this.imageView = view;
        this.loadingAnimation = animation;
        this.activity = activity;
        this.picDetailTable = picDetailTable;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String imageUrl = picDetailTable.getLink();

        if (GlobalResources.getDownloadingImages().contains(imageUrl) && activity != null && !activity.equalsIgnoreCase("FullScreen")) {
            Log.i("ImageDownloaderTask", "Already downloading image for url : " + imageUrl);
            return null;
        }

        GlobalResources.getDownloadingImages().add(imageUrl);
        Bitmap imageBitmap = null;
        boolean imageDownloaded = false;
        int retryCount = 2;
        while (!imageDownloaded && retryCount > 0) {
            try {
                Log.i("ImageDownloaderTask", "Downloading image from url : " + imageUrl);
                InputStream in = new java.net.URL(imageUrl).openStream();
                imageBitmap = BitmapFactory.decodeStream(in);
                picDetailTable.setBitmap(imageBitmap);

                //------- Cache ----------
                LruCache<String, Bitmap> images = GlobalResources.getImageCache();
                Log.i("ImageDownloaderTask", "Image Size : " + imageBitmap.getRowBytes() / 1000.0 + " KB");
                Log.i("ImageDownloaderTask", "Putting image in cache for url - " + imageUrl);
                images.put(imageUrl, imageBitmap);

                //------- Internal Storage ----------
                InternalDBHelper internalDBHelper = GlobalResources.getInternalDBHelper();
                if (picDetailTable != null)
                    internalDBHelper.insert(picDetailTable);

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
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                imageView.setLayoutParams(layoutParams);
                imageView.requestLayout();
            }
        }
    }
}
