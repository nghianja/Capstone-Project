package com.udacity.nanodegree.nghianja.capstone;

import android.app.Application;

/**
 * Application class for loading required setup data from server
 * or work as a single instance for global attributes.
 */
public class MyApplication extends Application {

    private double latitude = 1.3;
    private double longitude = 103.85;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
