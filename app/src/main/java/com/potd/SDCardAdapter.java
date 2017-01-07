package com.potd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 19/09/16.
 */
public class SDCardAdapter {

    private static final Logger logger = Logger.getLogger("SDCardAdapter");
    public static final String BASE_DIRECTORY = "/PhotoOfTheDay";

    public static String store(String fileName,
                               Context context,
                               Bitmap bitmap,
                               boolean displayAlreadyExistToast,
                               boolean displaySavingToast,
                               boolean displayFailedSavingToast) {
        logger.info("storing " + fileName + " in **SD CARD**");
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        String filePath = sdCardDirectory.getAbsolutePath() + BASE_DIRECTORY;
        boolean created = new File(filePath).mkdirs();
        filePath += "/" + fileName;
        logger.info("Path : " + filePath);
        File image = new File(filePath);
        if (image.exists()) {
            if (displayAlreadyExistToast)
                Toast.makeText(context, "Image already exist with same name at " + filePath,
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
                Toast.makeText(context,  "Image saved at " + filePath,
                        Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            logger.info("File Not Found");
        } catch (Exception e) {
            if (displayFailedSavingToast)
            Toast.makeText(context, "Failed to save image",
                    Toast.LENGTH_LONG).show();
            logger.log(Level.SEVERE, "Failed to save image: " + e.getMessage());
        }
        return filePath;
    }

//    public static Bitmap getImage(String path) {
//        if (path == null)
//            return null;
//        logger.info("fetching image from SD Card located at: " + path);
//        File file = new File(path);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        return BitmapFactory.decodeFile(path, options);
//    }

    public static Bitmap getImage(String path) {
        if (path == null)
            return null;
        return Utils.decodeSampledBitmapFromResource(path, 4096, 4096);
    }
}
