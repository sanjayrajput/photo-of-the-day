package com.potd.core;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.potd.ApiException;
import com.potd.Configuration;
import com.potd.models.PicDetailTable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class MongoDBManager {

    private DB database;
    private static final Logger logger = Logger.getLogger("MongoDBManager");

    public MongoDBManager() {
    }

    public String getURL() {
        return "mongodb://" + Configuration.db_user + ":" + Configuration.db_pass + "@ds035750.mongolab.com:35750/" + Configuration.database;
    }

    public void init() throws ApiException {
        try {
            logger.info("Connecting to Mongo Database...");
            String url = getURL();
            logger.info("URL : " + url);
            MongoClientURI uri = new MongoClientURI(url);
            MongoClient client = new MongoClient(uri);
            database = client.getDB(Configuration.database);
            logger.info("connected");
            logger.info("Authenticating database...");
            database.authenticate(Configuration.db_user, Configuration.db_pass.toCharArray());
            logger.info("authenticated");
        } catch (MongoException e) {
            logger.log(Level.ALL, "MongoException : Failed to initialize Database Connection!!!!!\n" + e.getMessage(), e);
            throw new ApiException(400, "Failed to initialize Database Connection");
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : Failed to initialize Database Connection!!!!!\n" + e.getMessage(), e);
            throw new ApiException(500, e.getMessage());
        }
    }

    public List<PicDetailTable> getLatestImages(Date date) {
        List<PicDetailTable> list = new ArrayList<>();
        boolean queryFailed = true;
        int retryCount = 3;
        while (queryFailed && retryCount > 0) {
            try {
                Log.i("Info", "Get Images meta data from table after date : " + date);
                DBCollection collection = database.getCollection(Configuration.main_table);
                DBObject condition = new BasicDBObject();
                condition.put("$gt", date);
                condition.put("$lte", new Date());
                DBObject sortByDate = new BasicDBObject();
                sortByDate.put("date", -1);
                DBCursor dbObjects = collection.find(new BasicDBObject("date", condition)).sort(sortByDate);

                while (dbObjects.hasNext()) {
                    PicDetailTable pdt = new PicDetailTable();
                    DBObject dbObject = dbObjects.next();
                    Object subject = dbObject.get("subject");
                    if (subject != null)
                        pdt.setSubject(subject.toString());
                    Object description = dbObject.get("description");
                    if (description != null)
                        pdt.setDescription(description.toString());
                    Object link = dbObject.get("link");
                    if (link != null)
                        pdt.setLink(link.toString());
                    Object d = dbObject.get("date");
                    if (d != null)
                        pdt.setDate((Date) d);
                    Object photographer = dbObject.get("photographer");
                    if (photographer != null)
                        pdt.setPhotographer(photographer.toString());
                    list.add(pdt);
                }
                queryFailed = false;
            } catch(Exception e){
                logger.log(Level.ALL, "Exception : Failed to add entry\n" + e.getMessage(), e);
                Log.i("Info", "Trying again...");
                retryCount--;
            }
        }
        return list;
    }

    public List<PicDetailTable> getAllImages(int offset, int size) {
        List<PicDetailTable> list = new ArrayList<>();
        boolean queryFailed = true;
        int retryCount = 3;
        while (queryFailed && retryCount > 0) {
            try {
                Log.i("Info", "Get Images meta data from table, offset " + offset + ", Size " + size);
                DBCollection collection = database.getCollection(Configuration.main_table);
                DBObject sortByDate = new BasicDBObject();
                sortByDate.put("date", -1);

                DBCursor dbObjects = collection.find().skip(offset).limit(size).sort(sortByDate);
                while (dbObjects.hasNext()) {
                    PicDetailTable pdt = new PicDetailTable();
                    DBObject dbObject = dbObjects.next();
                    Object subject = dbObject.get("subject");
                    if (subject != null)
                        pdt.setSubject(subject.toString());
                    Object description = dbObject.get("description");
                    if (description != null)
                        pdt.setDescription(description.toString());
                    Object link = dbObject.get("link");
                    if (link != null)
                        pdt.setLink(link.toString());
                    Object date = dbObject.get("date");
                    if (date != null)
                        pdt.setDate((Date) date);
                    Object photographer = dbObject.get("photographer");
                    if (photographer != null)
                        pdt.setPhotographer(photographer.toString());
                    list.add(pdt);
                }
                queryFailed = false;
            } catch(Exception e){
                logger.log(Level.ALL, "Exception : Failed to add entry\n" + e.getMessage(), e);
                Log.i("Info", "Trying again...");
                retryCount--;
            }
        }
        return list;
    }

    public void insertInMainTable(PicDetailTable picDetailTable){
        try {
            logger.info("Adding new entry");
            DBCollection table = database.getCollection(Configuration.main_table);
            BasicDBObject document = new BasicDBObject();
            document.put("subject", picDetailTable.getSubject());
            document.put("description", picDetailTable.getDescription());
            document.put("link", picDetailTable.getLink());
            document.put("date", picDetailTable.getDate());
            table.insert(document);
            logger.info("Inserted.");
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : Failed to add entry\n" + e.getMessage(), e);
        }
    }
}
