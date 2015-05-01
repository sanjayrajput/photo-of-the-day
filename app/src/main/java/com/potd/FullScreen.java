package com.potd;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.LruCache;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import com.potd.core.ImageDownloaderTask;
import com.potd.models.PicDetailTable;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 02/05/15.
 */
public class FullScreen extends Activity {

    private static final Logger logger = Logger.getLogger("FullScreen");

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_helper);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        final ImageView fullscreenImage = (ImageView) findViewById(R.id.imgDisplay);
        final ImageView loadingImage = (ImageView) findViewById(R.id.pageLoading);

        loadingImage.setBackgroundResource(R.drawable.loading_animation);
        AnimationDrawable animation = (AnimationDrawable) loadingImage.getBackground();
        animation.start();

        List<PicDetailTable> picDetailList = GlobalResources.getPicDetailList();
        final PicDetailTable picDetailTable = picDetailList.get(position);

        LruCache<String, Bitmap> images = GlobalResources.getImages();
        Bitmap bitmap = images.get(picDetailTable.getLink());

        if (bitmap != null) {

            logger.info("Found image in cache");
            animation.stop();
            loadingImage.setBackgroundResource(0);

            fullscreenImage.setImageBitmap(bitmap);

        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    new ImageDownloaderTask(fullscreenImage, loadingImage, "FullScreen").execute(picDetailTable.getLink());
                }
            });
            thread.start();
//                Picasso.with(context).load(picDetailTable.getLink()).into(image); TODO : Explore Picasso
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
