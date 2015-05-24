package com.potd.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.potd.models.PicDetailTable;

import java.io.ByteArrayOutputStream;
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
    public static final String TABLE_NAME = "pic_details";
    private static SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");


    public InternalDBHelper(Context context) {
        InternalDBManager internalDBManager = new InternalDBManager(context);
        database = internalDBManager.getWritableDatabase();
//        context.deleteDatabase(InternalDBManager.DATABASE_NAME);
//        database.execSQL(InternalDBManager.TABLE_DELETE_QUERY);
//        database.execSQL(InternalDBManager.PIC_DETAIL_TABLE_CREATE_QUERY);
    }

    public void insert(PicDetailTable picDetailTable) {
        logger.info("Insert pic details in internal storage : " + picDetailTable.getSubject());
        if (getImage(picDetailTable.getLink()) != null) {
            logger.info("Image already present in local database");
            return;
        }
        try {
            ContentValues values = new ContentValues();
            values.put("subject", picDetailTable.getSubject());
            values.put("link", picDetailTable.getLink());
            Bitmap bitmap = picDetailTable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            values.put("picture", stream.toByteArray());
            values.put("description", picDetailTable.getDescription());
            values.put("photographer", picDetailTable.getPhotographer());
            values.put("date", df1.format(picDetailTable.getDate()));
            database.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            logger.info("Failed to store image in local database");
        }
    }

    public void deleteImage(String link) {
        logger.info("Deleting image for link : " + link);
        int delete = database.delete(TABLE_NAME, "link=?", new String[]{link});
        logger.info("Return Value : " + delete);
    }

    public PicDetailTable getImage(String link) {
        logger.info("Get Image from Internal DB");
        Cursor cursor = database.query(TABLE_NAME, new String[]{"subject", "description", "date", "link", "picture", "photographer"}, "link=?", new String[]{link}, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            logger.info("Image Found in Local DB");
            PicDetailTable picDetailTable = parsePicDetailTable(cursor);
            cursor.close();
            return picDetailTable;
        }
        return null;
    }

    public List<PicDetailTable> getByDate(Date date, int size) {
        logger.info("Get All Images from Local Database from date " + date);
        List<PicDetailTable> list = new ArrayList<>();
        size--;
        try {
            String query = "SELECT subject, description, date, link, photographer FROM " + TABLE_NAME + " " +
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
                PicDetailTable picDetailTable = new PicDetailTable(cursor.getString(0), cursor.getString(1), df1.parse(cursor.getString(2)), cursor.getString(3), null, cursor.getString(4));
                list.add(picDetailTable);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            logger.info("Failed to get all images from local DB " + e.getMessage());
        }
        return list;
    }


    public List<PicDetailTable> getAll(int start, int size) {
        logger.info("Get All Images from Local Database...");
        List<PicDetailTable> list = new ArrayList<>();
        try {
            Cursor cursor = database.query(TABLE_NAME, new String[]{"subject", "description", "date", "link", "photographer"}, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                PicDetailTable picDetailTable = new PicDetailTable(cursor.getString(0), cursor.getString(1), df1.parse(cursor.getString(2)), cursor.getString(3), null, cursor.getString(4));
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
            Bitmap bmp = BitmapFactory.decodeByteArray(cursor.getBlob(4), 0, cursor.getBlob(4).length);
            return new PicDetailTable(cursor.getString(0), cursor.getString(1), df1.parse(cursor.getString(2)), cursor.getString(3), bmp, cursor.getString(5));
        } catch (ParseException e) {
            logger.info("Failed to parse date : " + e.getMessage());
            return null;
        }
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
