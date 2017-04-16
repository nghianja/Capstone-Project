package com.udacity.nanodegree.nghianja.capstone.background;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.udacity.nanodegree.nghianja.capstone.MasterActivity;
import com.udacity.nanodegree.nghianja.capstone.R;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract;
import com.udacity.nanodegree.nghianja.capstone.util.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * References:
 * [1] http://karanbalkar.com/2013/12/displaying-toast-message-in-an-intentservice/
 * [2] http://stackoverflow.com/questions/27358134/how-to-display-toast-from-a-service-sending-message-to-a-handler-on-a-dead-thre
 * [3] http://stackoverflow.com/questions/33889229/android-service-extends-resultreceiver-for-intentservice-how-to-implement-creat
 */
public class BookIntentService extends IntentService {

    private static final String TAG = BookIntentService.class.getSimpleName();

    public static final String RECEIVER = "receiver";
    public static final String FETCH_BOOK = "fetch";
    public static final String DELETE_BOOK = "delete";
    public static final String EAN = "ean";

    public BookIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                /* Sending running status back to fragment */
                receiver.send(Receiver.STATUS_RUNNING, Bundle.EMPTY);

                final String ean = intent.getStringExtra(EAN);
                fetchBook(ean, receiver);

                /* Sending finished status back to fragment */
                receiver.send(Receiver.STATUS_FINISHED, Bundle.EMPTY);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean, receiver);
            }
        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided parameters.
     */
    private void fetchBook(String ean, ResultReceiver receiver) {
        if (ean.length() != 13) {
            return;
        }

        Cursor bookEntry = getContentResolver().query(
                DataContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (bookEntry != null) {
            bookEntry.close();
            if (bookEntry.getCount() > 0) {
                return;
            }
        }

        // Checks for an internet connection.
        if (!Network.isNetworkAvailable(this)) {
            Log.i(TAG, "No internet connection!");
            Intent messageIntent = new Intent(MasterActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MasterActivity.MESSAGE_KEY, "No internet connection!");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
            return;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";
            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }

            if (builder.length() == 0) {
                return;
            }
            bookJsonString = builder.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if (bookJson.has(ITEMS)) {
                bookArray = bookJson.getJSONArray(ITEMS);
            } else {
                Intent messageIntent = new Intent(MasterActivity.MESSAGE_EVENT);
                messageIntent.putExtra(MasterActivity.MESSAGE_KEY, getResources().getString(R.string.not_found));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                return;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            StringBuilder authors = new StringBuilder();
            if (bookInfo.has(AUTHORS)) {
                JSONArray jsonArray = bookInfo.getJSONArray(AUTHORS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (i > 0) authors.append(", ");
                    authors.append(jsonArray.getString(i));
                }
            }
            String title = bookInfo.getString(TITLE);
            String subtitle = "";
            if (bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }
            String desc = "";
            if (bookInfo.has(DESC)) {
                desc = bookInfo.getString(DESC);
            }
            String imgUrl = "";
            if (bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            writeBackBook(ean, authors.toString(), title, subtitle, desc, imgUrl);
        } catch (JSONException e) {
            Log.e(TAG, "Error ", e);
        }
    }

    private void writeBackBook(String ean, String author, String title,
                               String subtitle, String desc, String imgUrl) {
        ContentValues values = new ContentValues();
        values.put(DataContract.BookEntry._ID, ean);
        values.put(DataContract.BookEntry.COLUMN_AUTHOR, author);
        values.put(DataContract.BookEntry.COLUMN_TITLE, title);
        values.put(DataContract.BookEntry.COLUMN_SUBTITLE, subtitle);
        values.put(DataContract.BookEntry.COLUMN_DESC, desc);
        values.put(DataContract.BookEntry.COLUMN_IMAGE_URL, imgUrl);
        values.put(DataContract.BookEntry.COLUMN_STATUS_CODE, "");
        values.put(DataContract.BookEntry.COLUMN_LIBRARY_ID, "");
        values.put(DataContract.BookEntry.COLUMN_LAST_UPDATE, System.currentTimeMillis());
        getContentResolver().insert(DataContract.BookEntry.CONTENT_URI, values);
    }

    /**
     * Handle action deleteBook in the provided background thread with the provided parameters.
     */
    private void deleteBook(String ean, ResultReceiver receiver) {
        if (ean != null) {
            getContentResolver().delete(
                    DataContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }
}
