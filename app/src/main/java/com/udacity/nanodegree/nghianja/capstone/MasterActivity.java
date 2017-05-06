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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.udacity.nanodegree.nghianja.capstone.data.BookAdapter;

/**
 * References:
 * [1] http://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
 * [2] http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
 * [3] http://stackoverflow.com/questions/32491960/android-check-permission-for-locationmanager
 */
public class MasterActivity extends AppCompatActivity 
        implements MasterFragment.Callback, LocationListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = MasterActivity.class.getSimpleName();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10 * 60 * 1000; // 10 minutes

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 8000;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Represents a geographical location.
     */
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

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        myApp = (MyApplication) getApplication();
        
        // Kick off the process of building the GoogleApiClient, LocationRequest, and LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }
    
    @Override
    protected void onStart() {
        super.onStart(); // call the superclass method first

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume(); // call the superclass method first

        // Register messageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter(MESSAGE_EVENT));

        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        updateLocation();
    }

    @Override
    protected void onPause() {
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);

        super.onPause(); // call the superclass method last
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();

        super.onStop(); // call the superclass method last
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (checkPlayServices() && mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_COARSE_LOCATION, as defined
     * in the AndroidManifest.xml.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateLocation();
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
            if (mLastLocation == null) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                updateLocation();
            }
            Log.i(TAG, "in onConnected(), starting location updates");
            startLocationUpdates();
        }
    }

    private void updateLocation() {
        if (mLastLocation != null) {
            myApp.setLatitude(mLastLocation.getLatitude());
            myApp.setLongitude(mLastLocation.getLongitude());
        }
    }
}
