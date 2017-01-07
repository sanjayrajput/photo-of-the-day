package com.potd.models;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.potd.SDCardAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class PicDetailTable {

    private String _id;
    private String subject;
    private String description;
    private Date date;
    private String link;
    private String sdCardImageLocation;
    private Bitmap bitmap;
    private String photographer;
    private String name;

    public PicDetailTable(String subject, String description, Date date, String link, String sdCardImageLocation, Bitmap bitmap, String photographer) {
        this.subject = subject;
        this.description = description;
        this.date = date;
        this.link = link;
        this.sdCardImageLocation = sdCardImageLocation;
        this.name = getName(link);
        this.bitmap = bitmap;
        setPhotographer(photographer);
    }

    public PicDetailTable() {
    }

    public PicDetailTable(Cursor cursor) throws ParseException {
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = -1;

        index = cursor.getColumnIndex("subject");
        if (index >= 0)
            this.subject = cursor.getString(index);

        index = cursor.getColumnIndex("link");
        if (index >= 0)
            this.link = cursor.getString(index);

//        index = cursor.getColumnIndex("picture");
//        if (index >= 0) {
//            this.bitmap = BitmapFactory.decodeByteArray(cursor.getBlob(index), 0, cursor.getBlob(index).length);
//        }

        index = cursor.getColumnIndex("sdCardImageLocation");
        if (index >= 0) {
            this.sdCardImageLocation = cursor.getString(index);
            if (sdCardImageLocation != null)
                this.bitmap = SDCardAdapter.getImage(sdCardImageLocation);
        }

        index = cursor.getColumnIndex("description");
        if (index >= 0)
            this.description = cursor.getString(index);

        index = cursor.getColumnIndex("photographer");
        if (index >= 0) {
            setPhotographer(cursor.getString(index));
        }

        index = cursor.getColumnIndex("date");
        if (index >= 0) {
            this.date = df1.parse(cursor.getString(index));
        }
    }

    public String getPhotographer() {
        return photographer;
    }

    public void setPhotographer(String photographer) {
        if (photographer != null) {
            this.photographer = photographer;
            if (this.photographer.startsWith("Photograph by ")) {
                this.photographer = this.photographer.substring(13);
            }
            if (this.photographer.endsWith(", National Geographic Your Shot")) {
                this.photographer = this.photographer.replace(", National Geographic Your Shot", "");
            }
        }
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
        this.name = getName(link);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(String link) {
        return link.substring(link.lastIndexOf('/') + 1);
    }

    public String getSdCardImageLocation() {
        return sdCardImageLocation;
    }

    public void setSdCardImageLocation(String sdCardImageLocation) {
        this.sdCardImageLocation = sdCardImageLocation;
    }
}
