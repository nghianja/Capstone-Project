package com.udacity.nanodegree.nghianja.capstone.data;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Manages a local database for data.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "capstone.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_BOOK_TABLE = "CREATE TABLE " +
                DataContract.BookEntry.TABLE_NAME + " (" +
                DataContract.BookEntry._ID + " INTEGER PRIMARY KEY, " +         //ISBN
                DataContract.BookEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                DataContract.BookEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                DataContract.BookEntry.COLUMN_SUBTITLE + " TEXT, " +
                DataContract.BookEntry.COLUMN_DESC + " TEXT, " +
                DataContract.BookEntry.COLUMN_IMAGE_URL + " TEXT, " +
                DataContract.BookEntry.COLUMN_LIBRARY_ID + " TEXT, " +
                DataContract.BookEntry.COLUMN_LAST_UPDATE + " INTEGER, " +
                "UNIQUE (" + DataContract.BookEntry._ID + ") ON CONFLICT IGNORE)";

        final String SQL_CREATE_LIBRARY_TABLE = "CREATE TABLE " +
                DataContract.LibraryEntry.TABLE_NAME + " (" +
                DataContract.LibraryEntry._ID + " TEXT PRIMARY KEY, " +      //BRANCH CODE
                DataContract.LibraryEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                DataContract.LibraryEntry.COLUMN_DESC + " TEXT, " +
                DataContract.LibraryEntry.COLUMN_LIBRARY_IMAGE + " TEXT, " +
                DataContract.LibraryEntry.COLUMN_ADDRESS + " TEXT, " +
                DataContract.LibraryEntry.COLUMN_GEO_POINT + " TEXT, " +
                DataContract.LibraryEntry.COLUMN_TEL_NUMBER + " TEXT, " +
                DataContract.LibraryEntry.COLUMN_FAX_NUMBER + " TEXT, " +
                DataContract.LibraryEntry.COLUMN_OPERATING + " TEXT, " +
                DataContract.LibraryEntry.COLUMN_GUIDE + " TEXT, " +
                "UNIQUE (" + DataContract.LibraryEntry._ID + ") ON CONFLICT IGNORE)";

        Log.d(DataContract.BookEntry.TABLE_NAME, SQL_CREATE_BOOK_TABLE);
        Log.d(DataContract.LibraryEntry.TABLE_NAME, SQL_CREATE_LIBRARY_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_BOOK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_LIBRARY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.BookEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.LibraryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * To view database for debugging. To be remove along with AndroidDatabaseManager
     *
     * References:
     * [1] https://github.com/sanathp/DatabaseManager_For_Android
     */
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {
                alc.set(0,c);
                c.moveToFirst();
                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}
