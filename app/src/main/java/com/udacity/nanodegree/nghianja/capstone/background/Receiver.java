package com.udacity.nanodegree.nghianja.capstone.background;

import android.os.Bundle;

/**
 * References:
 * [1] http://stacktips.com/tutorials/android/creating-a-background-service-in-android
 * [2] http://stackoverflow.com/questions/33889229/android-service-extends-resultreceiver-for-intentservice-how-to-implement-creat
 */
public interface Receiver {

    int STATUS_RUNNING = 0;
    int STATUS_FINISHED = 1;
    int STATUS_ERROR = 2;

    void onReceiveResult(int resultCode, Bundle resultData);
}
