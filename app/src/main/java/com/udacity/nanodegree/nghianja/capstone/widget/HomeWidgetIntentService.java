package com.udacity.nanodegree.nghianja.capstone.widget;

import android.app.IntentService;
import android.content.Intent;

/**
 * IntentService which handles updating home widget with the latest data.
 */
public class HomeWidgetIntentService extends IntentService {

    public HomeWidgetIntentService() {
        super("HomeWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {}

}
