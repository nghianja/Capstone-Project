package com.udacity.nanodegree.nghianja.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.udacity.nanodegree.nghianja.capstone.util.Network;

/**
 * References:
 * [1] https://www.bignerdranch.com/blog/splash-screens-the-right-way/
 * [2] http://semycolon.blogspot.sg/2014/11/async-task-can-you-wait-for-asynctask.html
 * [3] http://www.tutorialspoint.com/android/android_rss_reader.htm
 */
public class SplashActivity extends AppCompatActivity implements SplashTaskFragment.TaskCallbacks {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final String TAG_TASK_FRAGMENT = "splashTaskFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Network.isNetworkAvailable(this)) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            SplashTaskFragment taskFragment =
                    (SplashTaskFragment) fragmentManager.findFragmentByTag(TAG_TASK_FRAGMENT);
            if (taskFragment == null) {
                taskFragment =
                        SplashTaskFragment.newInstance(getString(R.string.rss_library_info));
                fragmentManager.beginTransaction().add(taskFragment, TAG_TASK_FRAGMENT).commit();
            }
        } else {
            Log.d(TAG, "network unavailable to load library data");
            Toast.makeText(SplashActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            onPostExecute();
        }
    }

    @Override
    public void onPreExecute() {
        // Required empty callback
    }

    @Override
    public void onProgressUpdate(int percent) {
        // Required empty callback
    }

    @Override
    public void onCancelled() {
        // Required empty callback
    }

    @Override
    public void onPostExecute() {
        Intent intent = new Intent(this, MasterActivity.class);
        startActivity(intent);
        finish();
    }
}
