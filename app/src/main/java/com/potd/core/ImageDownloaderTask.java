package com.potd.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.potd.GlobalResources;

import java.io.InputStream;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView imageView;
    private ImageView loadingAnimation;
    private String activity;

    public ImageDownloaderTask(ImageView view, ImageView animation, String activity) {
        this.imageView = view;
        this.loadingAnimation = animation;
        this.activity = activity;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String imageUrl = params[0];

        if (GlobalResources.getDownloadingImages().contains(imageUrl) && activity != null && !activity.equalsIgnoreCase("FullScreen")) {
            Log.i("ImageDownloaderTask", "Already downloading image for url : " + imageUrl);
            return null;
        }
        GlobalResources.getDownloadingImages().add(imageUrl);
        Bitmap imageBitmap = null;
        boolean imageDownloaded = false;
        int retryCount = 5;
        while (!imageDownloaded && retryCount > 0) {
            try {
                Log.i("ImageDownloaderTask", "Downloading image from url : " + imageUrl);
                InputStream in = new java.net.URL(imageUrl).openStream();
                imageBitmap = BitmapFactory.decodeStream(in);

                LruCache<String, Bitmap> images = GlobalResources.getImages();
                Log.i("ImageDownloaderTask", "Image Size : " + imageBitmap.getRowBytes() / 1000.0 + " KB");
                Log.i("ImageDownloaderTask", "Putting image in cache for url - " + imageUrl);
                images.put(imageUrl, imageBitmap);

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
