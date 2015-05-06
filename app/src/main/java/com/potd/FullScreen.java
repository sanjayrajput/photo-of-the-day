package com.potd;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.potd.core.ImageDownloaderTask;
import com.potd.models.PicDetailTable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
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
            saveImageInSDCard(true, true);
            return true;
        } else if (id == R.id.action_set_as_wallpaper) {
            setImageAsWallpaper();
            return true;
        } else if (id == R.id.view_in_gallery) {
            openImageInGallery();
            return true;
        } else if (id == R.id.image_share) {
            shareImage();
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


        final ImageView fullscreenImage = (ImageView) findViewById(R.id.imgDisplay);
        final ImageView loadingImage = (ImageView) findViewById(R.id.pageLoading);

        loadingImage.setBackgroundResource(R.drawable.loading_animation);
        AnimationDrawable animation = (AnimationDrawable) loadingImage.getBackground();
        animation.start();

        List<PicDetailTable> picDetailList = GlobalResources.getPicDetailList();
        final PicDetailTable picDetailTable = picDetailList.get(position);

        Bitmap bitmap = ImageDBHelper.getImage(picDetailTable.getLink());

        if (bitmap != null) {

            logger.info("Found image in cache");
            animation.stop();
            loadingImage.setBackgroundResource(0);

            fullscreenImage.setImageBitmap(bitmap);

        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    new ImageDownloaderTask(fullscreenImage, loadingImage, "FullScreen", picDetailTable).execute();
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
        Bitmap bitmap = picDetailTable.getBitmap();
        String fileName = picDetailTable.getName();
        String filePath = null;

        if (fileName == null) {
            Random rand = new Random();
            fileName = "Nat_Geo_Photo_Of_The_Day_" + rand.nextInt() + ".jpg";
        }
        if (bitmap != null) {
            File sdCardDirectory = Environment.getExternalStorageDirectory();
            filePath = sdCardDirectory.getAbsolutePath() + "/Download";
            boolean created = new File(filePath).mkdirs();
            filePath += "/" + fileName;
            logger.info("Path : " + filePath);
            File image = new File(filePath);
            if (image.exists()) {
                if (displayAlreadyExistToast)
                    Toast.makeText(getApplicationContext(),  "Image already exist with same name at " + filePath,
                        Toast.LENGTH_LONG).show();
                logger.info("Image already exist");
                return filePath;
            }
            FileOutputStream outStream;
            try {
                outStream = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
                logger.info("Image saved at " + filePath);
                if (displaySavingToast)
                    Toast.makeText(getApplicationContext(),  "Image saved at " + filePath,
                        Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                logger.info("File Not Found");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to save image",
                        Toast.LENGTH_LONG).show();
            }
        }
        return filePath;
    }

    public void setImageAsWallpaper() {
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(getApplicationContext());
        try {
            myWallpaperManager.setBitmap(getCurrentPic().getBitmap());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Failed to set wallpaper",
                    Toast.LENGTH_LONG).show();
            logger.info("Failed to set background");
        }
    }

    public void openImageInGallery() {
        String filePath = saveImageInSDCard(false, true);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri fileUri = Uri.parse("file://" + filePath);
        intent.setDataAndType(fileUri, "image/*");
        startActivity(intent);
    }

    public void shareImage() {
        String filePath = saveImageInSDCard(false, false);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        Uri fileUri = Uri.parse("file://" + filePath);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setType("image/jpeg");
        startActivity(intent);
    }

    public PicDetailTable getCurrentPic() {
        List<PicDetailTable> picDetailList = GlobalResources.getPicDetailList();
        return picDetailList.get(position);
    }
}
