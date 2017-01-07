package com.potd.core;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;
import com.potd.ApiException;
import com.potd.GlobalResources;
import com.potd.models.PicDetailTable;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by sanjay.rajput on 10/01/16.
 */
public class GoogleSpreadSheetAdapter {

    private static final Logger logger = Logger.getLogger("GoogleSpreadSheetAdapter");
    private static final String CLIENT_ID = "photooftheday@potd-118507.iam.gserviceaccount.com";
    private static final List<String> SCOPES = Arrays.asList("https://spreadsheets.google.com/feeds https://docs.google.com/feeds https://www.googleapis.com/auth/drive");
    public static final String P12FILE = "potd-72686a5f5387.p12";
    private GoogleCredential credentials = null;
    private SpreadsheetFeed spreadsheet = null;
    private SpreadsheetService service = null;
    URL worksheetUrl = null;

    public GoogleSpreadSheetAdapter(InputStream inputStream) {
        init(inputStream);
    }

    public void init(InputStream inputStream) {
        logger.info("initializing spreadsheet adapter");
        try {
            loadCredentials(inputStream);
            logger.info("initializing spreadsheet adapter");
            this.service = new SpreadsheetService("Photo of the day");
            this.service.setOAuth2Credentials(this.credentials);
            worksheetUrl = new URL("https://spreadsheets.google.com/feeds/worksheets/1HDoLhoIDfTPolo1ARPIksoSJR45tVpdLnwQeBxecch0/private/full");

        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "MalformedURLException: ", e);
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, "GeneralSecurityException: ", e);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "URISyntaxException: ", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException: ", e);
//        } catch (AuthenticationException e) {
//            logger.log(Level.SEVERE, "AuthenticationException: ", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e);
        }
        logger.info("initialized.");
    }

    public void loadCredentials(InputStream inputStream) throws GeneralSecurityException, IOException, URISyntaxException {
        logger.info("fetching token");
        JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport = new ApacheHttpTransport();

        URL fileUrl = GoogleSpreadSheetAdapter.class.getResource(P12FILE);
        File file = new File(GlobalResources.getP12AuthKeyFilePath());
        this.credentials = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(CLIENT_ID)
                .setServiceAccountPrivateKeyFromP12File(file)
                .setServiceAccountScopes(SCOPES)
//                .setServiceAccountUser("sanjay.rajputcse@gmail.com")
                .build();
        this.credentials.refreshToken();
        logger.info("token generated");
    }

    public boolean isEntryExistInSheet(SpreadsheetEntry spreadsheet, PicDetailTable potdMeta) throws IOException, ServiceException, ParseException {
        SimpleDateFormat smf = new SimpleDateFormat("MM/dd/yyyy");
        List<WorksheetEntry> worksheets = spreadsheet.getWorksheets();
        for (WorksheetEntry worksheet : worksheets) {
            if (smf.format(smf.parse(worksheet.getTitle().getPlainText())).equalsIgnoreCase(smf.format(potdMeta.getDate()))) {
                return true;
            }
        }
        return false;
    }

    public List<PicDetailTable> get(int start, int size) throws ApiException {
        logger.log(Level.INFO, "Fetching images from google sheet offset: " + start + ", size: " + size);
        List<PicDetailTable> list = new ArrayList<>();
        try {
            List<SpreadsheetEntry> sheetList = null;
            try {
                if (this.spreadsheet == null) {
                    logger.info("fetching spreadsheet");
                    this.spreadsheet = this.service.getFeed(worksheetUrl, SpreadsheetFeed.class);
                }
                logger.info("fetching spreadsheet entries");
                sheetList = this.spreadsheet.getEntries();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to get entries");
                logger.info("fetching spreadsheet");
                this.spreadsheet = this.service.getFeed(worksheetUrl, SpreadsheetFeed.class);
                sheetList = this.spreadsheet.getEntries();
            }
            if (sheetList != null && !sheetList.isEmpty()) {
                SpreadsheetEntry potdSheet = sheetList.get(0);
                List<WorksheetEntry> worksheets = potdSheet.getWorksheets();
                sort(worksheets);
                if (worksheets.size() > start) {
                    ListIterator<WorksheetEntry> iterator = worksheets.listIterator(start);
                    while (iterator.hasNext() && size > 0) {
                        WorksheetEntry worksheet = iterator.next();
                        list.add(parseXML(worksheet.getXmlBlob().getBlob()));
                        size--;
                    }
                }
            }
        } catch (ServiceException e) {
            logger.log(Level.SEVERE, "ServiceException: Failed to get rows from spreadsheet", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException: Failed to get rows from spreadsheet", e);
        }
        return list;
    }

    public List<PicDetailTable> getLatestImages(Date date) throws ApiException {
        logger.log(Level.INFO, "Fetching latest images after date: " + date);
        List<PicDetailTable> list = new ArrayList<>();
        SimpleDateFormat smf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            this.spreadsheet = this.service.getFeed(worksheetUrl, SpreadsheetFeed.class);
            List<SpreadsheetEntry> sheetList = this.spreadsheet.getEntries();
            if (sheetList != null && !sheetList.isEmpty()) {
                SpreadsheetEntry potdSheet = sheetList.get(0);
                List<WorksheetEntry> worksheets = potdSheet.getWorksheets();
                sort(worksheets);
                for (WorksheetEntry worksheet : worksheets) {
                    if (smf.format(smf.parse(worksheet.getTitle().getPlainText())).equalsIgnoreCase(smf.format(date))) {
                        return list;
                    }
                    list.add(parseXML(worksheet.getXmlBlob().getBlob()));
                }
            }
        } catch (ServiceException e) {
            logger.log(Level.SEVERE, "ServiceException: Failed to get rows from spreadsheet", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException: Failed to get rows from spreadsheet", e);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "IOException: Failed to parse date", e);
        }
        return null;
    }

    public PicDetailTable parseXML(String blobContent) {
        try {
            blobContent = blobContent.replaceAll("gsx:", "");
            blobContent = "<?xml version=\"1.0\"?>" +
                    "<picmeta>" + blobContent +
                    "</picmeta>";
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            XMLBlobHandler xmlBlobHandler = new XMLBlobHandler();
            saxParser.parse(new InputSource(new StringReader(blobContent)), xmlBlobHandler);
            return xmlBlobHandler.getPicDetailTable();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to parse XML content" + e.getMessage(), e);
        }
        return null;
    }

    private void sort(final List<WorksheetEntry> worksheets) {
        Collections.sort(worksheets, new Comparator<WorksheetEntry>() {
            private SimpleDateFormat smf = new SimpleDateFormat("MM/dd/yyyy");
            @Override
            public int compare(WorksheetEntry lhs, WorksheetEntry rhs) {
                try {
                    return smf.parse(rhs.getTitle().getPlainText()).compareTo(smf.parse(lhs.getTitle().getPlainText()));
                } catch (ParseException e) {
                    logger.log(Level.SEVERE, "Failed to sort worksheets", e);
                }
                return 0;
            }
        });
    }

    public class XMLBlobHandler extends DefaultHandler {

        private PicDetailTable picDetailTable;
        private SimpleDateFormat smf = new SimpleDateFormat("MM/dd/yyyy");

        boolean date = false;
        boolean subject = false;
        boolean photographer = false;
        boolean imageUrl = false;
        boolean description = false;
        boolean height = false;
        boolean width = false;
        boolean alt = false;
        boolean prev = false;

        public XMLBlobHandler() {
            picDetailTable = new PicDetailTable();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("SUBJECT")) {
                subject = true;
            } else if (qName.equalsIgnoreCase("DATE")) {
                date = true;
            } else if (qName.equalsIgnoreCase("IMAGEURL")) {
                imageUrl = true;
            } else if (qName.equalsIgnoreCase("PHOTOGRAPHER")) {
                photographer = true;
            } else if (qName.equalsIgnoreCase("DESCRIPTION")) {
                description = true;
            } else if (qName.equalsIgnoreCase("HEIGHT")) {
                height = true;
            } else if (qName.equalsIgnoreCase("WIDTH")) {
                width = true;
            } else if (qName.equalsIgnoreCase("ALT")) {
                alt = true;
            } else if (qName.equalsIgnoreCase("PREV")) {
                prev = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String data = new String(ch, start, length);
            if (subject) {
                picDetailTable.setSubject(data);
                subject = false;
            } else if (date) {
                try {picDetailTable.setDate(smf.parse(data));} catch (ParseException e) {logger.log(Level.WARNING, "INVALID Date");}
                date = false;
            } else if (imageUrl) {
                picDetailTable.setLink(data);
                imageUrl = false;
            } else if (photographer) {
                picDetailTable.setPhotographer(data);
                photographer = false;
            } else if (description) {
                picDetailTable.setDescription(data);
                description = false;
            } else if (height) {
                height = false;
            } else if (width) {
                width = false;
            } else if (alt) {
                alt = false;
            } else if (prev) {
                prev = false;
            } else {
                logger.log(Level.WARNING, "Unknown column");
            }
        }

        public PicDetailTable getPicDetailTable() {
            return picDetailTable;
        }

        public void setPicDetailTable(PicDetailTable picDetailTable) {
            this.picDetailTable = picDetailTable;
        }
    }


    /*public String convertInXML(POTDMeta potdMeta) {
        SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd");
        String xml = "<gsx:date>" + smf.format(potdMeta.getDate()) + "</gsx:date>" +
                "<gsx:subject>" + potdMeta.getSubject() + "</gsx:subject>" +
                "<gsx:imageurl>" + potdMeta.getSource() + "</gsx:imageurl>" +
                "<gsx:photographer>" + potdMeta.getPhotographer() + "</gsx:photographer>" +
                "<gsx:description>" + potdMeta.getDescription() + "</gsx:description>" +
                "<gsx:height>" + potdMeta.getHeight() + "</gsx:height>" +
                "<gsx:width>" + potdMeta.getWidth() + "</gsx:width>" +
                "<gsx:alt>" + potdMeta.getAlt() + "</gsx:alt>" +
                "<gsx:prev>" + potdMeta.getPrev() + "</gsx:prev>";
        return xml;
    }*/


}
