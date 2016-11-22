package com.udacity.nanodegree.nghianja.capstone.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for Book and Library schemas.
 *
 * References:
 * [1] http://stackoverflow.com/questions/16754443/contenturis-format-for-id-as-string
 */
public class DataContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.udacity.nanodegree.nghianja.capstone";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_BOOKS = "books";
    public static final String PATH_LIBRARIES = "libraries";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DataContract() {
    }

    /* Inner class that defines the table contents for Book */
    public static final class BookEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String TABLE_NAME = "books";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SUBTITLE = "subtitle";
        public static final String COLUMN_DESC = "description";
        public static final String COLUMN_IMAGE_URL = "imageUrl";
        public static final String COLUMN_STATUS_CODE = "statusCode";
        public static final String COLUMN_LIBRARY_ID = "libraryId";
        public static final String COLUMN_LAST_UPDATE = "lastUpdate";

        public static Uri buildBookUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents for Library */
    public static final class LibraryEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIBRARIES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIBRARIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIBRARIES;
        public static final String TABLE_NAME = "libraries";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESC = "description";
        public static final String COLUMN_LIBRARY_IMAGE= "libraryImage";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_GEO_POINT = "geoPoint";
        public static final String COLUMN_TEL_NUMBER = "telNumber";
        public static final String COLUMN_FAX_NUMBER = "faxNumber";
        public static final String COLUMN_OPERATING = "operatingHours";
        public static final String COLUMN_GUIDE = "guide";

        public static Uri buildLibraryUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
    }
}
