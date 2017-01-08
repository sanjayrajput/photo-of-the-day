package com.potd.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 03/05/15.
 */
public class InternalDBManager extends SQLiteOpenHelper {

    private static final Logger logger = Logger.getLogger("InternalDBManager");

    public static final String DATABASE_NAME = "photo_of_the_day";
    private static final int DATABASE_VERSION = 4;
    public static final String TABLE_DELETE_QUERY = "DROP TABLE " + InternalDBHelper.TABLE_NAME;
    public static final String PIC_DETAIL_TABLE_CREATE_QUERY = "CREATE TABLE " + InternalDBHelper.TABLE_NAME +
            "( subject VARCHAR(256) DEFAULT NULL," +
            "link VARCHAR(256) DEFAULT NULL," +
            "description TEXT," +
            "sdCardImageLocation TEXT," +
            "picture TEXT," +
            "alt VARCHAR(256) DEFAULT NULL," +
            "photographer VARCHAR(256) DEFAULT NULL," +
            "date VARCHAR(256) NOT NULL PRIMARY KEY)";

    public static final String CONFIG_TABLE_CREATE_QUERY = "CREATE TABLE " + InternalDBHelper.CONFIG_TABLE_NAME +
            "( config_value VARCHAR(256) DEFAULT NULL," +
            "config_key VARCHAR(256) NOT NULL PRIMARY KEY)";

    public InternalDBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        logger.info("Creating Table In DB : " + InternalDBHelper.TABLE_NAME);
        db.execSQL(PIC_DETAIL_TABLE_CREATE_QUERY);
//        logger.info("Creating Table In DB : " + InternalDBHelper.CONFIG_TABLE_NAME);
//        db.execSQL(CONFIG_TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TABLE_DELETE_QUERY);
        logger.info("Creating Table In DB : " + InternalDBHelper.TABLE_NAME);
        db.execSQL(PIC_DETAIL_TABLE_CREATE_QUERY);
//        logger.info("Creating Table In DB : " + InternalDBHelper.CONFIG_TABLE_NAME);
//        db.execSQL(CONFIG_TABLE_CREATE_QUERY);
    }
}
