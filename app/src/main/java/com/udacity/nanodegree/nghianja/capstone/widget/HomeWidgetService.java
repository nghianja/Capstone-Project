package com.udacity.nanodegree.nghianja.capstone.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.udacity.nanodegree.nghianja.capstone.R;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * References:
 * [1] http://www.vogella.com/tutorials/AndroidWidgets/article.html
 * [2] http://www.worldbestlearningcenter.com/answers/1524/using-listview-in-home-screen-widget-in-android
 * [3] http://www.programcreek.com/java-api-examples/index.php?api=android.widget.RemoteViewsService
 * [4] https://github.com/codepath/android_guides/wiki/Populating-a-ListView-with-a-CursorAdapter
 * [5] http://stackoverflow.com/questions/28068888/how-to-set-an-image-from-url-to-imageview-on-widget
 */
public class HomeWidgetService extends RemoteViewsService {

    private static final String TAG = "HomeWidgetService";

    private static final String[] LIBRARY_COLUMNS = {
            DataContract.BookEntry._ID,
            DataContract.BookEntry.COLUMN_IMAGE_URL,
            DataContract.BookEntry.COLUMN_LIBRARY_ID,
            DataContract.BookEntry.COLUMN_LAST_UPDATE
    };

    private static final int INDEX_ID = 0;
    private static final int INDEX_IMAGE_URL = 1;
    private static final int INDEX_LIBRARY_ID = 2;
    private static final int INDEX_LAST_UPDATE = 3;

    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new HomeWidgetFactory(this.getApplicationContext(), intent);
    }

    private class HomeWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context context;
        private Cursor cursor;
        private Uri uri;
        private String appWidgetId;

        public HomeWidgetFactory(Context context, Intent intent) {
            this.context = context;
            this.uri = intent.getData();
            this.appWidgetId = intent.getStringExtra("appWidgetId");
            Log.d(TAG, "appWidgetId=" + this.appWidgetId);
        }

        @Override
        public int getCount() {
            return cursor == null ? 0 : cursor.getCount();
        }

        @Override
        public long getItemId(final int position) {
            final int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
            if (cursor.moveToPosition(position))
                return cursor.getLong(idColumnIndex);
            return position;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(position))
                return null;

            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_item);

            Bitmap bookCoverImage = null;
            try {
                bookCoverImage = Glide.with(HomeWidgetService.this)
                        .load(cursor.getString(INDEX_IMAGE_URL))
                        .asBitmap()
                        .error(R.mipmap.ic_launcher)
                        .into(40, 40).get();
            } catch (InterruptedException | ExecutionException error) {
                Log.e(TAG, error.toString());
            }
            remoteViews.setImageViewBitmap(R.id.widget_book_cover, bookCoverImage);
            Date date = new Date(cursor.getInt(INDEX_LAST_UPDATE) * 1000L);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int columns = prefs.getInt(appWidgetId, 4);
            if (columns > 2) {
//            remoteViews.setTextViewText(R.id.widget_library, cursor.getString(INDEX_LIBRARY_ID));
                remoteViews.setTextViewText(R.id.widget_library, "Central Public Library");
                remoteViews.setTextViewText(R.id.widget_last_update,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(date));
            } else {
                remoteViews.setTextViewText(R.id.widget_library, "CCL");
                remoteViews.setTextViewText(R.id.widget_last_update,
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date));
            }

            final Intent fillInIntent = new Intent();
            int dateColumnIndex = cursor.getColumnIndex(DataContract.BookEntry._ID);
            fillInIntent.setData(DataContract.BookEntry.buildBookUri(cursor.getLong(dateColumnIndex)));
            remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

            return remoteViews;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onCreate() {
            // Nothing to do
        }

        @Override
        public void onDataSetChanged() {
            if (cursor != null)
                cursor.close();
            cursor = getContentResolver().query(uri, LIBRARY_COLUMNS, null, null, null);
        }

        @Override
        public void onDestroy() {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

    }

}
