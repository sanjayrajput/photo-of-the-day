package com.potd.models;

import android.graphics.Bitmap;

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
    private Bitmap bitmap;
    private String photographer;
    private String name;

    public PicDetailTable(String subject, String description, Date date, String link, Bitmap bitmap, String photographer) {
        this.subject = subject;
        this.description = description;
        this.date = date;
        this.link = link;
        this.name = getName(link);
        this.bitmap = bitmap;
        this.photographer = photographer;
    }

    public PicDetailTable() {
    }

    public String getPhotographer() {
        return photographer;
    }

    public void setPhotographer(String photographer) {
        this.photographer = photographer;
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
}
