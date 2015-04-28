package com.potd;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView imageView;
    private ImageView loadingAnimation;

    public ImageDownloaderTask(ImageView view, ImageView animation) {
        this.imageView = view;
        this.loadingAnimation = animation;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String imageUrl = params[0];
        Bitmap imageBitmap = null;
        try {
            Log.i("ImageDownloaderTask", "Downloading image from url : " + imageUrl);
            InputStream in = new java.net.URL(imageUrl).openStream();
            imageBitmap = BitmapFactory.decodeStream(in);

            LruCache<String, Bitmap> images = GlobalResources.getImages();
            Log.i("ImageDownloaderTask", "Putting image in cache for url - " + imageUrl);
            images.put(imageUrl, imageBitmap);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return imageBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        loadingAnimation.setBackgroundResource(0);
        imageView.setImageBitmap(bitmap);
    }
}
