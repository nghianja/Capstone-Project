package com.udacity.nanodegree.nghianja.capstone.util;

import android.content.ContentValues;
import android.util.Log;

import com.udacity.nanodegree.nghianja.capstone.data.DataContract;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling RSS/XML feed.
 * <p/>
 * References:
 * [1] https://androidresearch.wordpress.com/2012/01/21/creating-a-simple-rss-application-in-android/
 * [2] http://www.vogella.com/tutorials/AndroidXML/article.html
 * [3] http://www.tutorialspoint.com/android/android_rss_reader.htm
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
}
