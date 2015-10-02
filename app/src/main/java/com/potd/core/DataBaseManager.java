package com.potd.core;

import android.util.Log;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.potd.ApiException;
import com.potd.Configuration;
import com.potd.models.PicDetailTable;

import org.bson.Document;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class DataBaseManager {

    private MongoDatabase database;
    private Connection connection;
    private static final Logger logger = Logger.getLogger("DataBaseManager");

    public DataBaseManager() throws ApiException{
        init();
    }

    public String getURL() {
        return "mongodb://" + Configuration.db_user + ":" + Configuration.db_pass + "@ds035750.mongolab.com:35750/" + Configuration.database;
    }

    /*public void init() throws ApiException {
        try {
            java.security.Security.addProvider(new GnuSasl());
            logger.info("Connecting to Mongo Database...");
            String url = getURL();
            logger.info("URL : " + url);
            MongoCredential credential = MongoCredential.createCredential(Configuration.db_user, Configuration.database, Configuration.db_pass.toCharArray());
            ServerAddress serverAddress = new ServerAddress("ds035750.mongolab.com", 35750);
            MongoClient mongoClient = new MongoClient(serverAddress, Arrays.asList(credential));
            database = mongoClient.getDatabase(Configuration.database);
            logger.info("authenticated");
        } catch (MongoException e) {
            logger.log(Level.ALL, "MongoException : Failed to initialize Database Connection!!!!!\n" + e.getMessage(), e);
            throw new ApiException(400, "Failed to initialize Database" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : Failed to initialize Database Connection!!!!!\n" + e.getMessage(), e);
            throw new ApiException(500, "Failed to initialize Database" + e.getMessage());
        }
    }*/

    private void init() {
        try {
            logger.info("Connecting to MYSQL POTD Server...");
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/" + Configuration.mysql_database, Configuration.mysql_db_user, Configuration.mysql_db_pass);
            if (connection == null) {
                logger.log(Level.ALL, "Failed to initialize mysql server");
            } else {
                logger.info("connected");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQLException:Failed to initialize mysql server", e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "ClassNotFoundException:Failed to initialize mysql server", e);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "InstantiationException:Failed to initialize mysql server", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "IllegalAccessException:Failed to initialize mysql server", e);
        }
    }


    public List<PicDetailTable> getLatestImages(Date date) throws SQLException {
        List<PicDetailTable> list = new ArrayList<>();
        boolean queryFailed = true;
        int retryCount = 3;
        PreparedStatement statement = null;
        while (queryFailed && retryCount > 0) {
            try {
                Log.i("Info", "MYSQL: Get Images meta data from table after date : " + date);
                /*MongoCollection<Document> collection = database.getCollection(Configuration.main_table);
                DBObject condition = new BasicDBObject();
                condition.put("$gt", date);
                condition.put("$lte", new Date());
                BasicDBObject sortByDate = new BasicDBObject();
                sortByDate.put("date", -1);
                FindIterable<Document> dbObjects = collection.find(new BasicDBObject("date", condition)).sort(sortByDate);
                MongoCursor<Document> iterator = dbObjects.iterator();

                while (iterator.hasNext()) {
                    PicDetailTable pdt = new PicDetailTable();
                    Document dbObject = iterator.next();
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
                }*/

                String query = "SELECT * from " + Configuration.main_table + " WHERE pic_date > ? order by pic_date desc";
                statement = connection.prepareStatement(query);
                statement.setTimestamp(1, new Timestamp(date.getTime()));
                logger.info("Query: " + statement);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        PicDetailTable pdt = new PicDetailTable();
                        pdt.setDate(resultSet.getDate("pic_date"));
                        pdt.setDescription(resultSet.getString("description"));
                        if (resultSet.getString("photographer") != null)
                            pdt.setPhotographer(resultSet.getString("photographer"));
                        if (resultSet.getString("picture_link") != null)
                            pdt.setLink(resultSet.getString("picture_link"));
                        if (resultSet.getString("subject") != null)
                            pdt.setSubject(resultSet.getString("subject"));
                        list.add(pdt);
                    }
                }

                queryFailed = false;
            } catch(Exception e){
                logger.log(Level.ALL, "Exception : Failed to add entry\n" + e.getMessage(), e);
                Log.i("Info", "Trying again...");
                retryCount--;
            } finally {
                if (statement != null)
                    statement.close();
            }
        }
        return list;
    }

    public List<PicDetailTable> getAllImages(int offset, int size) throws SQLException {
        List<PicDetailTable> list = new ArrayList<>();
        boolean queryFailed = true;
        int retryCount = 3;
        PreparedStatement statement = null;
        while (queryFailed && retryCount > 0) {
            try {
                Log.i("Info", "MYSQL: Get Images meta data from table, offset " + offset + ", Size " + size);
                /*MongoCollection<Document> collection = database.getCollection(Configuration.main_table);
                BasicDBObject sortByDate = new BasicDBObject();
                sortByDate.put("date", -1);

                FindIterable<Document> dbObjects = collection.find().skip(offset).limit(size).sort(sortByDate);
                MongoCursor<Document> iterator = dbObjects.iterator();
                while (iterator.hasNext()) {
                    PicDetailTable pdt = new PicDetailTable();
                    Document dbObject = iterator.next();
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
                }*/

                String query = "SELECT * from " + Configuration.main_table + " order by pic_date desc LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, size);
                statement.setInt(2, offset);
                logger.info("Query: " + statement);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        PicDetailTable pdt = new PicDetailTable();
                        pdt.setDate(resultSet.getDate("pic_date"));
                        pdt.setDescription(resultSet.getString("description"));
                        if (resultSet.getString("photographer") != null)
                            pdt.setPhotographer(resultSet.getString("photographer"));
                        if (resultSet.getString("picture_link") != null)
                            pdt.setLink(resultSet.getString("picture_link"));
                        if (resultSet.getString("subject") != null)
                            pdt.setSubject(resultSet.getString("subject"));
                        list.add(pdt);
                    }
                }

                queryFailed = false;

            } catch(Exception e){
                logger.log(Level.ALL, "Exception : Failed to get entry\n" + e.getMessage(), e);
                Log.i("Info", "Trying again..." + e.getMessage());
                retryCount--;
            } finally {
                if (statement != null)
                    statement.close();
            }
        }
        return list;
    }

    public Map<String, String> getColor() {
        int color = 0;
        Map<String, String> map = new Hashtable<>();
        MongoCollection<Document> collection = database.getCollection("hackday");
        FindIterable<Document> dbObjects = collection.find();
        MongoCursor<Document> iterator = dbObjects.iterator();
        while (iterator.hasNext()) {
            Document dbObject = iterator.next();
            String id = dbObject.get("id").toString();
            String status = dbObject.get("status").toString();
            map.put(id, status);
        }
        return map;
    }

    public void insertHack() {
        MongoCollection<Document> table = database.getCollection("hackday");
        Document document = new Document();
        document.put("id", "1");
        document.put("status", true);
        table.insertOne(document);

        document.put("id", "2");
        document.put("status", true);
        table.insertOne(document);

        document.put("id", "3");
        document.put("status", true);
        table.insertOne(document);

    }

    public void insertInMainTable(PicDetailTable picDetailTable){
        try {
            logger.info("Adding new entry");
            MongoCollection<Document> table = database.getCollection(Configuration.main_table);
            Document document = new Document();
            document.put("subject", picDetailTable.getSubject());
            document.put("description", picDetailTable.getDescription());
            document.put("link", picDetailTable.getLink());
            document.put("date", picDetailTable.getDate());
            table.insertOne(document);
            logger.info("Inserted.");
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : Failed to get entry\n" + e.getMessage(), e);
        }
    }

    public PicDetailTable getPicByDate(Date date) throws SQLException {
        List<PicDetailTable> list = new ArrayList<>();
        boolean queryFailed = true;
        int retryCount = 3;
        PreparedStatement statement = null;
        while (queryFailed && retryCount > 0) {
            try {
                Log.i("Info", "MYSQL: Get Images meta data from table for date : " + date);
                /*MongoCollection<Document> collection = database.getCollection(Configuration.main_table);
                DBObject condition = new BasicDBObject();
                condition.put("$eq", date);
                FindIterable<Document> dbObjects = collection.find(new BasicDBObject("date", condition));
                MongoCursor<Document> iterator = dbObjects.iterator();
                while (iterator.hasNext()) {
                    PicDetailTable pdt = new PicDetailTable();
                    Document dbObject = iterator.next();
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
                }*/

                String query = "SELECT * from " + Configuration.main_table + " WHERE pic_date=?";
                statement = connection.prepareStatement(query);
                statement.setTimestamp(1, new Timestamp(date.getTime()));
                logger.info("Query: " + statement);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        PicDetailTable pdt = new PicDetailTable();
                        pdt.setDate(resultSet.getDate("pic_date"));
                        pdt.setDescription(resultSet.getString("description"));
                        if (resultSet.getString("photographer") != null)
                            pdt.setPhotographer(resultSet.getString("photographer"));
                        if (resultSet.getString("picture_link") != null)
                            pdt.setLink(resultSet.getString("picture_link"));
                        if (resultSet.getString("subject") != null)
                            pdt.setSubject(resultSet.getString("subject"));
                        list.add(pdt);
                    }
                }

                queryFailed = false;
            } catch(Exception e){
                logger.log(Level.ALL, "Exception : Failed to get entry\n" + e.getMessage(), e);
                Log.i("Info", "Trying again..." + e.getMessage());
                retryCount--;
            } finally {
                if (statement != null)
                    statement.close();
            }
        }
        if (list.isEmpty())
            return null;
        else
            return list.get(0);
    }
}
