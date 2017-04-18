package com.udacity.nanodegree.nghianja.capstone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.udacity.nanodegree.nghianja.capstone.data.BookAdapter;

/**
 * References:
 * [1] http://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
 * [2] http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
 * [3] http://stackoverflow.com/questions/32491960/android-check-permission-for-locationmanager
 */
public class MasterActivity extends AppCompatActivity 
        implements MasterFragment.Callback, ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = MasterActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 8000;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private MyApplication myApp;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Toast.makeText(MasterActivity.this,
                        intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        /*
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        */
        
        // Create an instance of GoogleAPIClient.
        if (checkPlayServices() && mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        }
    }
    
    @Override
    protected void onStart() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register messageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter(MESSAGE_EVENT));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onPause();
    }

    @Override
    public void onItemSelected(Uri bookUri, BookAdapter.BookAdapterViewHolder vh) {
        Intent intent = new Intent(this, DetailActivity.class).setData(bookUri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_COURSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastKnownLocation();
                } else {
                    Log.i(TAG, "Location permission is not given.");
                    finish();
                }
        }
    }
    
    @Override
    public void onConnected(Bundle connectionHint) {
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.w(TAG, "The client is disconnected.");
        Toast.makeText(MasterActivity.this,
                "Reconnecting to location service...", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.w(TAG, "The client fails to connect.");
        Toast.makeText(MasterActivity.this,
                "Unable to connect to location service.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    MY_PERMISSION_ACCESS_COURSE_LOCATION);
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                myApp.setLatitude(mLastLocation.getLatitude());
                myApp.setLongitude(mLastLocation.getLongitude());
            }
        }
    }
}
