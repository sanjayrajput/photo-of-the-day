package com.potd.models;

import android.graphics.Bitmap;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class PicDetails {

    private String heading;
    private String description;
    private Bitmap image;

    public PicDetails(String heading, String description, Bitmap image) {
        this.heading = heading;
        this.description = description;
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
