package com.udacity.nanodegree.nghianja.capstone.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.nanodegree.nghianja.capstone.DetailActivity;
import com.udacity.nanodegree.nghianja.capstone.MasterActivity;
import com.udacity.nanodegree.nghianja.capstone.R;
import com.udacity.nanodegree.nghianja.capstone.SplashActivity;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract;

/**
 * References:
 * [1] http://www.vogella.com/tutorials/AndroidWidgets/article.html
 * [2] http://www.worldbestlearningcenter.com/answers/1524/using-listview-in-home-screen-widget-in-android
 * [3] http://stackoverflow.com/questions/14270138/dynamically-adjusting-widgets-content-and-layout-to-the-size-the-user-defined-t
 * [4] https://github.com/nghianja/Advanced_Android_Development/blob/master/app/src/main/java/com/example/android/sunshine/app/widget/DetailWidgetProvider.java
 */
public class HomeWidgetProvider extends AppWidgetProvider {

    public static final String TAG = HomeWidgetProvider.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED = "com.udacity.nanodegree.nghianja.capstone.ACTION_DATA_UPDATED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, getRemoteViews(context, appWidgetId));
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_DATA_UPDATED)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // Get min width and height.
        int columns = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("appWidgetId" + appWidgetId, columns);
        editor.commit();

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
    }

    private RemoteViews getRemoteViews(Context context, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Create an Intent to launch SplashActivity
        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        // Set up the collection
        Intent serviceIntent = new Intent(context, HomeWidgetService.class);
        serviceIntent.setData(DataContract.BookEntry.CONTENT_URI);
        serviceIntent.putExtra("appWidgetId", "appWidgetId" + appWidgetId);
        remoteViews.setRemoteAdapter(R.id.widget_list, serviceIntent);

        boolean useDetailActivity = context.getResources().getBoolean(R.bool.use_detail_activity);
        Intent clickIntentTemplate = useDetailActivity
                ? new Intent(context, DetailActivity.class)
                : new Intent(context, MasterActivity.class);
        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
        remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);

        return remoteViews;
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private int getCellsForSize(int size) {
        return (int)(Math.ceil(size + 30d)/70d);
    }

}
