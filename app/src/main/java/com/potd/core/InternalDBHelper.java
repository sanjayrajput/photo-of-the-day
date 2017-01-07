package com.potd.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.potd.GlobalResources;
import com.potd.SDCardAdapter;
import com.potd.Utils;
import com.potd.models.PicDetailTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 03/05/15.
 */
public class InternalDBHelper {
    private static final Logger logger = Logger.getLogger("InternalDBHelper");

    private SQLiteDatabase database;
    public static final String TABLE_NAME = "potd_details";
    public static final String CONFIG_TABLE_NAME = "potd_configs";
    private static SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");


    public InternalDBHelper(Context context) {
        InternalDBManager internalDBManager = new InternalDBManager(context);
        logger.info("get all internal databases");
        database = internalDBManager.getWritableDatabase();
//        context.deleteDatabase(InternalDBManager.DATABASE_NAME);
//        database.execSQL(InternalDBManager.TABLE_DELETE_QUERY);
//        database.execSQL(InternalDBManager.PIC_DETAIL_TABLE_CREATE_QUERY);
    }

    public void insert(PicDetailTable picDetailTable, Context applicationContext) throws ParseException {
        logger.info("Insert pic details in internal storage : " + picDetailTable.getSubject());
        if (getImageByDate(picDetailTable.getDate()) != null) {
            logger.info("Record already present in local database, updating BitMap");
            if (picDetailTable.getBitmap() != null && GlobalResources.isStorePicInSDCard()) {
                ContentValues values = new ContentValues();
                String location = SDCardAdapter.store(Utils.replaceSpaces(picDetailTable.getSubject(), "-"),
                        applicationContext, picDetailTable.getBitmap(), false, false, false);   // because of large file size which can not be stored in DB
                values.put("sdCardImageLocation", location);
                String dateString = df1.format(df2.parse(df2.format(picDetailTable.getDate())));
                database.update(TABLE_NAME, values, "date=?", new String[]{dateString});
                picDetailTable.setSdCardImageLocation(location);
            }
            return;
        }
        try {
            ContentValues values = new ContentValues();
            values.put("subject", picDetailTable.getSubject());
            values.put("link", picDetailTable.getLink());
//            Bitmap bitmap = picDetailTable.getBitmap();
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            values.put("picture", stream.toByteArray());
            values.put("description", picDetailTable.getDescription());
            values.put("photographer", picDetailTable.getPhotographer());
            values.put("date", df1.format(picDetailTable.getDate()));
            if (picDetailTable.getBitmap() != null && GlobalResources.isStorePicInSDCard()) {
                String location = SDCardAdapter.store(Utils.replaceSpaces(picDetailTable.getSubject(), "-"),
                        applicationContext, picDetailTable.getBitmap(), false, false, false);   // because of large file size which can not be stored in DB
                values.put("sdCardImageLocation", location);
                picDetailTable.setSdCardImageLocation(location);
            }
            database.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            logger.info("Failed to store image in local database");
        }
    }

    public void insertInConfig(String key, String value) {
        logger.info("Insert config in internal storage key: " + key + ", value: " + value);
        if (getConfigValue(key) != null) {
            logger.info("Record already present in local database, updating value");
            ContentValues values = new ContentValues();
            values.put("config_value", value);
            database.update(CONFIG_TABLE_NAME, values, "config_key=?", new String[]{key});
            return;
        }
        ContentValues values = new ContentValues();
        values.put("config_key", key);
        values.put("config_value", value);
        database.insert(CONFIG_TABLE_NAME, null, values);
    }

    public String getConfigValue(String key) {
        logger.info("Get config value from Internal DB for key: " + key);
        String value = null;
        Cursor cursor = database.query(CONFIG_TABLE_NAME, new String[]{"config_key, config_value"}, "config_key=?", new String[]{key}, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            logger.info("Config Found in Local DB");
            int index = -1;
            index = cursor.getColumnIndex("config_value");
            if (index >= 0) {
                value = cursor.getString(index);
                logger.info("value: " + value);
            }
            cursor.close();
        }
        return value;
    }


    public void deleteImage(String link) {
        logger.info("Deleting image for link : " + link);
        int delete = database.delete(TABLE_NAME, "link=?", new String[]{link});
        logger.info("Return Value : " + delete);
    }

    public PicDetailTable getImage(String link) {
        logger.info("Get Image from Internal DB");
        Cursor cursor = database.query(TABLE_NAME, new String[]{"subject", "description", "date", "link", "picture", "photographer", "sdCardImageLocation"}, "link=?", new String[]{link}, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            logger.info("Image Found in Local DB");
            PicDetailTable picDetailTable = parsePicDetailTable(cursor);
            cursor.close();
            return picDetailTable;
        }
        return null;
    }

    public PicDetailTable getImageByDate(Date date) {
        logger.info("Get Image from Internal DB for Date: " + date);
        try {
            String dateString = df1.format(df2.parse(df2.format(date)));
//            Cursor cursor = database.query(TABLE_NAME, new String[]{"subject", "description", "date", "link", "picture", "photographer"}, "date=?", new String[]{dateString}, null, null, null);
            Cursor cursor = database.rawQuery("SELECT subject, description, date, link, " +
                    "photographer, picture, sdCardImageLocation FROM " + TABLE_NAME + " " +
                    "WHERE date=?", new String[]{dateString});
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                logger.info(">>>>>> Image Found in Local DB ");
                PicDetailTable picDetailTable = parsePicDetailTable(cursor);
                cursor.close();
                return picDetailTable;
            }
        } catch (Exception e) {
            logger.info("Failed to get image from local DB: " + e.getMessage());
        }
        logger.info("Image NOT FOUND in Local DB");
        return null;
    }

    public List<PicDetailTable> getByDate(Date date, int size) {
        logger.info("Get All Images from Local Database from date " + date);
        List<PicDetailTable> list = new ArrayList<>();
        size--;
        try {
            String query = "SELECT subject, description, date, link, photographer, sdCardImageLocation FROM " + TABLE_NAME + " " +
                    "WHERE date BETWEEN ? AND ? ORDER BY date DESC";
            String date1 = df1.format(df2.parse(df2.format(date)));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, -size);
            date = cal.getTime();
            String date2 = df1.format(df2.parse(df2.format(date)));

            Cursor cursor = database.rawQuery(query, new String[]{date2, date1});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                PicDetailTable picDetailTable = new PicDetailTable(cursor);
//                PicDetailTable picDetailTable = new PicDetailTable(cursor.getString(0), cursor.getString(1), df1.parse(cursor.getString(2)), cursor.getString(3), null, null, cursor.getString(4));
                list.add(picDetailTable);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            logger.info("Failed to get all images from local DB " + e.getMessage());
        }
        return list;
    }


    public PicDetailTable getLatestRecord() {
        logger.info("getting latest record from internal DB");
        PicDetailTable picDetailTable = null;
        try {
            String query = "SELECT subject, description, date, link, photographer, sdCardImageLocation FROM " + TABLE_NAME + " " +
                    "ORDER BY date DESC limit 1";
            Cursor cursor = database.rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                picDetailTable = new PicDetailTable(cursor);
                logger.info("Latest Pic- Subject: " + picDetailTable.getSubject() +
                        ", Photographer: " + picDetailTable.getPhotographer() +
                ", Date: " + picDetailTable.getDate() + ", Link: " + picDetailTable.getLink());
                break;
            }
            cursor.close();
        } catch (Exception e) {
            logger.info("Failed to get all images from local DB " + e.getMessage());
        }
        return picDetailTable;
    }

    public List<PicDetailTable> getAll(int start, int size) {
        logger.info("Get All Images from Local Database...");
        List<PicDetailTable> list = new ArrayList<>();
        try {
            Cursor cursor = database.query(TABLE_NAME, new String[]{"subject", "description", "date", "link", "photographer"}, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                PicDetailTable picDetailTable = new PicDetailTable(cursor.getString(0), cursor.getString(1), df1.parse(cursor.getString(2)), cursor.getString(3), null, null, cursor.getString(4));
                list.add(picDetailTable);
                cursor.moveToNext();
            }
            cursor.close();
            sortDataByDate(list);
        } catch (Exception e) {
            logger.info("Failed to get all images from local DB " + e.getMessage());
        }
        if (list.isEmpty())
            return list;
        if (start > list.size())
            return list;
        int end = start + size;
        if (end > list.size())
            end = list.size();
        return list.subList(start, end);
    }

    public PicDetailTable parsePicDetailTable(Cursor cursor) {
        try {
            return new PicDetailTable(cursor);
//            Bitmap bmp = BitmapFactory.decodeByteArray(cursor.getBlob(4), 0, cursor.getBlob(4).length);
//            if (true)
//                return null;
//            Bitmap bmp = null;
//            return new PicDetailTable(cursor.getString(0), cursor.getString(1), df1.parse(cursor.getString(2)), cursor.getString(3), bmp, cursor.getString(5));
        } catch (ParseException e) {
            logger.info("Failed to parse date : " + e.getMessage());
        }
        return null;
    }

    public void sortDataByDate(List<PicDetailTable> list) {
        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<PicDetailTable>() {
                @Override
                public int compare(final PicDetailTable object1, final PicDetailTable object2) {
                    return object2.getDate().compareTo(object1.getDate());
                }
            });
        }
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }

}
