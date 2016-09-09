package com.udacity.nanodegree.nghianja.capstone.util;

import android.content.ContentValues;
import android.util.Log;
import android.util.Xml;

import com.udacity.nanodegree.nghianja.capstone.data.DataContract;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling RSS/XML feed.
 * <p/>
 * References:
 * [1] https://androidresearch.wordpress.com/2012/01/21/creating-a-simple-rss-application-in-android/
 * [2] http://www.vogella.com/tutorials/AndroidXML/article.html
 * [3] http://www.tutorialspoint.com/android/android_rss_reader.htm
 * [4] http://www.ibm.com/developerworks/opensource/library/x-android/
 * [5] http://stackoverflow.com/questions/12152718/not-able-to-set-the-default-namespace-in-android-xmlserializer
 * [6] http://stackoverflow.com/questions/27270041/writing-an-xml-using-xmlserializer-and-change-prefix-n0-to-ns1
 */
public class RssFeed {

    private static final String TAG = RssFeed.class.getSimpleName();

    public static List<ContentValues> handleLibraryXml(XmlPullParser xpp)
            throws XmlPullParserException, IOException {
        // Create a new map of values, where column names are the keys
        List<ContentValues> valuesList = new ArrayList<>();
        ContentValues values = new ContentValues();

        // Returns the type of current event: START_TAG, END_TAG, etc..
        int eventType = xpp.getEventType();
        boolean insideItem = false;
        String text = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = xpp.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (name.equals("item")) {
                        insideItem = true;
                        values = new ContentValues();
                    }
                    break;
                case XmlPullParser.TEXT:
                    text = xpp.getText();
                    break;
                case XmlPullParser.CDSECT:
                    text = xpp.getText();
                    break;
                case XmlPullParser.END_TAG:
                    if (insideItem) {
                        switch (name) {
                            case "title":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry.COLUMN_TITLE, text);
                                break;
                            case "description":
                                Log.d(TAG, name + " : " + text.length());
                                values.put(DataContract.LibraryEntry.COLUMN_DESC, text);
                                break;
                            case "branchCode":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry._ID, text);
                                break;
                            case "libraryImage":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry.COLUMN_LIBRARY_IMAGE, text);
                                break;
                            case "address":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry.COLUMN_ADDRESS, text);
                                break;
                            case "point":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry.COLUMN_GEO_POINT, text);
                                break;
                            case "telNumber":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry.COLUMN_TEL_NUMBER, text);
                                break;
                            case "faxNumber":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry.COLUMN_FAX_NUMBER, text);
                                break;
                            case "operatingHours":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry.COLUMN_OPERATING, text);
                                break;
                            case "guide":
                                Log.d(TAG, name + " : " + text);
                                values.put(DataContract.LibraryEntry.COLUMN_GUIDE, text);
                                break;
                            case "item":
                                insideItem = false;
                                valuesList.add(values);
                                break;
                        }
                    }
                    break;
            }
            eventType = xpp.nextToken(); //move to next element
        }
        return valuesList;
    }

    public static String writeXml(String apiKey, String ean) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            serializer.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
            serializer.setPrefix("soap", "http://schemas.xmlsoap.org/soap/envelope/");
            serializer.startTag("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
            serializer.startTag("http://schemas.xmlsoap.org/soap/envelope/", "Body");
            serializer.startTag("", "GetAvailabilityInfoRequest");
            serializer.attribute("", "xmlns", "http://www.nlb.gov.sg/ws/CatalogueService");
            serializer.startTag("", "APIKey");
            serializer.text(apiKey);
            serializer.endTag("", "APIKey");
            serializer.startTag("", "ISBN");
            serializer.text(ean);
            serializer.endTag("", "ISBN");
            serializer.startTag("", "Modifiers");
            serializer.endTag("", "Modifiers");
            serializer.endTag("", "GetAvailabilityInfoRequest");
            serializer.endTag("http://schemas.xmlsoap.org/soap/envelope/", "Body");
            serializer.endTag("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
