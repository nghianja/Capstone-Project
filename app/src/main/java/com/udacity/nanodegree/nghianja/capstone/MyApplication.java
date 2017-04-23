package com.udacity.nanodegree.nghianja.capstone;

import android.app.Application;
import android.util.Log;

/**
 * Application class for loading required setup data from server
 * or work as a single instance for global attributes.
 */
public class MyApplication extends Application {
    
    private static final String TAG = MyApplication.class.getSimpleName();

    private double latitude = 1.3;
    private double longitude = 103.85;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public double getLatitude() {
        Log.d(TAG, "Getting latitude: " + latitude);
        return latitude;
    }

    public void setLatitude(double latitude) {
        Log.d(TAG, "Setting latitude: " + latitude);
        this.latitude = latitude;
    }

    public double getLongitude() {
        Log.d(TAG, "Getting longitude: " + longitude);
        return longitude;
    }

    public void setLongitude(double longitude) {
        Log.d(TAG, "Setting longitude: " + longitude);
        this.longitude = longitude;
    }

}
