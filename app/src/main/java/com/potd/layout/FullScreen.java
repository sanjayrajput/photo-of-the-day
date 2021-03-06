package com.potd.layout;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.potd.GlobalResources;
import com.potd.ImageDBHelper;
import com.potd.R;
import com.potd.SDCardAdapter;
import com.potd.Utils;
import com.potd.core.ImageDownloaderTask;
import com.potd.gesture.view.main.src.com.polites.android.GestureImageView;
import com.potd.models.PicDetailTable;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 02/05/15.
 */
public class FullScreen extends Activity {

    private static final Logger logger = Logger.getLogger("FullScreen");
    private int position;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.full_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_image) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    saveImageInSDCard(true, true);
                }
            });
            return true;
        } else if (id == R.id.action_set_as_wallpaper) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setImageAsWallpaper();
                }
            });
            return true;
        } else if (id == R.id.view_in_gallery) {
            openImageInGallery();
            return true;
        } else if (id == R.id.image_share) {
            shareImage();
            return true;
        } else if (id == R.id.action_rate_app) {
            rateTheApp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pager_helper);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);


        final GestureImageView fullscreenImage = (GestureImageView) findViewById(R.id.imgDisplay);
        final ImageView loadingImage = (ImageView) findViewById(R.id.pageLoading);

        loadingImage.setBackgroundResource(R.drawable.loading_animation);
        AnimationDrawable animation = (AnimationDrawable) loadingImage.getBackground();
        animation.start();

        List<PicDetailTable> picDetailList = GlobalResources.getPicDetailList();
        final PicDetailTable picDetailTable = picDetailList.get(position);

        Bitmap bitmap = ImageDBHelper.getImage(picDetailTable.getLink(), picDetailTable.getDate());

        if (bitmap != null) {

            logger.info("Found image in cache");
            animation.stop();
            loadingImage.setBackgroundResource(0);

            fullscreenImage.setImageBitmap(bitmap);

        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    new ImageDownloaderTask(fullscreenImage, loadingImage, "FullScreen", picDetailTable, getApplicationContext()).execute();
                }
            });
            thread.start();
        }

        fullscreenImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullscreenImage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });

        fullscreenImage.setLongClickable(true);

        fullscreenImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openOptionsMenu();
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public String saveImageInSDCard(boolean displayAlreadyExistToast, boolean displaySavingToast) {
        logger.info("Saving file in SD Card");
        PicDetailTable picDetailTable = getCurrentPic();
        if (picDetailTable == null)
            return null;
        Bitmap bitmap = picDetailTable.getBitmap();
        String fileName = picDetailTable.getSubject();
        if (fileName == null) {
            Random rand = new Random();
            fileName = "Nat_Geo_Photo_Of_The_Day_" + rand.nextInt() + ".jpg";
        } else {
            fileName = Utils.replaceSpaces(fileName, "-") + ".jpg";
        }
        String filePath = null;
        if (bitmap != null) {
            filePath = SDCardAdapter.store(fileName, getApplicationContext(), bitmap, displayAlreadyExistToast, displaySavingToast, true);
        }
        return filePath;
    }

    public void setImageAsWallpaper() {
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(getApplicationContext());
        try {
            PicDetailTable currentPic = getCurrentPic();
            if (currentPic != null && currentPic.getBitmap() != null) {
                myWallpaperManager.setBitmap(getCurrentPic().getBitmap());
                Toast.makeText(getApplicationContext(), "Wallpaper Set Successfully!!!",
                        Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Failed to set wallpaper - image not found",
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Failed to set wallpaper",
                    Toast.LENGTH_LONG).show();
            logger.log(Level.SEVERE, "Failed to set background: " + e.getMessage());
        }
    }

    public void openImageInGallery() {
        String filePath = saveImageInSDCard(false, true);
        if (filePath == null) {
            Toast.makeText(getApplicationContext(), "Failed to open Image in Gallery - null file path",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri fileUri = Uri.parse("file://" + filePath);
        intent.setDataAndType(fileUri, "image/*");
        startActivity(intent);
    }

    public void shareImage() {
        String filePath = saveImageInSDCard(false, false);
        if (filePath == null) {
            Toast.makeText(getApplicationContext(), "Failed to share image - couldn't locate image in SDCard",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        Uri fileUri = Uri.parse("file://" + filePath);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setType("image/jpeg");
        startActivity(intent);
    }

    public PicDetailTable getCurrentPic() {
        List<PicDetailTable> picDetailList = GlobalResources.getPicDetailList();
        if (position < picDetailList.size())
            return picDetailList.get(position);
        return null;
    }

    public void rateTheApp() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
