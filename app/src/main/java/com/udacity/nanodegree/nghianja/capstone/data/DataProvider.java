package com.udacity.nanodegree.nghianja.capstone.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Content provider for books and libraries.
 */
public class DataProvider extends ContentProvider {

    private static final int BOOK = 100;
    private static final int BOOK_ID = 101;

    private static final int LIBRARY = 200;
    private static final int LIBRARY_ID = 201;

    // The URI Matcher used by this content provider.
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DataContract.PATH_BOOKS, BOOK);
        matcher.addURI(authority, DataContract.PATH_LIBRARIES, LIBRARY);
        matcher.addURI(authority, DataContract.PATH_BOOKS + "/#", BOOK_ID);
        matcher.addURI(authority, DataContract.PATH_LIBRARIES + "/*", LIBRARY_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);

        switch (match) {
            case BOOK:
                return DataContract.BookEntry.CONTENT_TYPE;
            case LIBRARY:
                return DataContract.LibraryEntry.CONTENT_TYPE;
            case BOOK_ID:
                return DataContract.BookEntry.CONTENT_ITEM_TYPE;
            case LIBRARY_ID:
                return DataContract.LibraryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (uriMatcher.match(uri)) {
            case BOOK:
                retCursor = databaseHelper.getReadableDatabase().query(
                        DataContract.BookEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder
                );
                break;
            case LIBRARY:
                retCursor = databaseHelper.getReadableDatabase().query(
                        DataContract.LibraryEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder
                );
                break;
            case BOOK_ID:
                retCursor = databaseHelper.getReadableDatabase().query(
                        DataContract.BookEntry.TABLE_NAME,
                        projection,
                        DataContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs, null, null, sortOrder
                );
                break;
            case LIBRARY_ID:
                retCursor = databaseHelper.getReadableDatabase().query(
                        DataContract.LibraryEntry.TABLE_NAME,
                        projection,
                        DataContract.LibraryEntry._ID + " = '" + uri.getLastPathSegment() + "'",
                        selectionArgs, null, null, sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return retCursor;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] valuesArray) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case LIBRARY:
                sqLiteDatabase.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues values : valuesArray) {
                        long _id = sqLiteDatabase.insert(
                                DataContract.LibraryEntry.TABLE_NAME, null, values
                        );
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                return returnCount;
            default:
                return super.bulkInsert(uri, valuesArray);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case BOOK: {
                long _id = sqLiteDatabase.insert(
                        DataContract.BookEntry.TABLE_NAME, null, values
                );
                if (_id > 0) {
                    returnUri = DataContract.BookEntry.buildBookUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case LIBRARY: {
                long _id = sqLiteDatabase.insert(
                        DataContract.LibraryEntry.TABLE_NAME, null, values
                );
                if (_id > 0) {
                    returnUri = DataContract.LibraryEntry.buildLibraryUri(values.getAsString(DataContract.LibraryEntry._ID));
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case BOOK:
                rowsDeleted = sqLiteDatabase.delete(
                        DataContract.BookEntry.TABLE_NAME, selection, selectionArgs
                );
                break;
            case LIBRARY:
                rowsDeleted = sqLiteDatabase.delete(
                        DataContract.LibraryEntry.TABLE_NAME, selection, selectionArgs
                );
                break;
            case BOOK_ID:
                rowsDeleted = sqLiteDatabase.delete(
                        DataContract.BookEntry.TABLE_NAME,
                        DataContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case BOOK:
                rowsUpdated = sqLiteDatabase.update(
                        DataContract.BookEntry.TABLE_NAME, values, selection, selectionArgs
                );
                break;
            case LIBRARY:
                rowsUpdated = sqLiteDatabase.update(
                        DataContract.LibraryEntry.TABLE_NAME, values, selection, selectionArgs
                );
                break;
            case BOOK_ID:
                rowsUpdated = sqLiteDatabase.update(
                        DataContract.BookEntry.TABLE_NAME,
                        values,
                        DataContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsUpdated;
    }
}
