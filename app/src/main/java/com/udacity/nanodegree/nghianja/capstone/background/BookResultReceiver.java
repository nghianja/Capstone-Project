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
public class BookResultReceiver extends ResultReceiver {

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    private Receiver receiver;

    public BookResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }
}
