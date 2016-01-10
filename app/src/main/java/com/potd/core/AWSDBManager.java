package com.potd.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 06/05/15.
 */
public class AWSDBManager {

    private static final Logger logger = Logger.getLogger("AWSDBManager");
    private Connection connection;

    public AWSDBManager() {
        init();
    }

    public void init() {

        String database = "android_apps";
        String dbUrl = "android.cln8vbqb5d2i.us-west-2.rds.amazonaws.com:3306";
        String dbUser = "sanjay";
        String dbPass = "sanjaysu91";
        try {
            logger.info("Connecting to AWS...");
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + dbUrl +"/" + database, dbUser, dbPass);
            logger.info("connected");
        } catch (Exception e) {
            logger.info("Failed to connect to AWS Database - " + e.getMessage());
        }
    }

    public Bitmap getImage(String source) {
        try {
            String query2 = "SELECT image FROM potd_pic_details WHERE link=?";
            PreparedStatement psmt2 = connection.prepareStatement(query2);
            psmt2.setString(1, source);
            ResultSet resultSet = psmt2.executeQuery();
            if (resultSet.next()) {
                Blob image = resultSet.getBlob("image");
                return BitmapFactory.decodeStream(image.getBinaryStream());
            }
        } catch (Exception e) {
            logger.info("Failed to run query on AWS Database - " + e.getMessage());
        }
        return null;
    }

}
