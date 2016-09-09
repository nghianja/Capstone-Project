package com.udacity.nanodegree.nghianja.capstone.background;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * References:
 * [1] http://stacktips.com/tutorials/android/creating-a-background-service-in-android
 * [2] http://stackoverflow.com/questions/33889229/android-service-extends-resultreceiver-for-intentservice-how-to-implement-creat
 */
@SuppressLint("ParcelCreator")
public class LibraryResultReceiver extends ResultReceiver {

    private Receiver receiver;

    public LibraryResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }
}
