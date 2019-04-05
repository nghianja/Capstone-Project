package com.udacity.nanodegree.nghianja.capstone.background;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.udacity.nanodegree.nghianja.capstone.MasterActivity;
import com.udacity.nanodegree.nghianja.capstone.MyApplication;
import com.udacity.nanodegree.nghianja.capstone.R;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract;
import com.udacity.nanodegree.nghianja.capstone.serialization.GetAvailabilityInfoResponse;
import com.udacity.nanodegree.nghianja.capstone.serialization.Item;
import com.udacity.nanodegree.nghianja.capstone.util.Distance;
import com.udacity.nanodegree.nghianja.capstone.util.Network;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Vector;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * References:
 * [1] http://www.ibm.com/developerworks/opensource/library/x-android/
 * [2] https://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
 * [3] http://stackoverflow.com/questions/297586/how-to-call-a-soap-web-service-on-android
 * [4] http://stackoverflow.com/questions/11814962/sending-xml-to-a-web-service-in-android
 * [5] http://simpligility.github.io/ksoap2-android/tips.html
 * [6] http://stackoverflow.com/questions/9805946/nesting-properties-inside-a-tag-in-ksoap2
 * [7] http://javatutorialspoint.blogspot.sg/2012/02/android-web-service-access-using-ksoap2.html
 * [8] http://stackoverflow.com/questions/12634082/setting-ksoap2-prefix-on-the-xmlns-attribute-only
 * [9] http://stackoverflow.com/questions/34802771/java-lang-classcastexception-java-util-vector-cannot-be-cast-to-org-ksoap2-seri
 */
public class LibraryIntentService extends IntentService {

    private static final String TAG = LibraryIntentService.class.getSimpleName();
    private static final int LOADER_ID = 0;
    private static final String[] LOADER_COLUMNS = {
            DataContract.LibraryEntry.TABLE_NAME + "." + DataContract.LibraryEntry._ID,
            DataContract.LibraryEntry.COLUMN_TITLE,
            DataContract.LibraryEntry.COLUMN_GEO_POINT
    };

    // These indices are tied to LOADER_COLUMNS.  If LOADER_COLUMNS changes, these must change.
    public static final int COL_LIBRARY_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_GEO_POINT = 2;

    public static final String RECEIVER = "receiver";
    public static final String AVAILABILITY = "availability";
    public static final String EAN = "ean";
    public static final String LIBRARY = "library";

    private MyApplication myApp;
    private String library;

    public LibraryIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        myApp = (MyApplication) getApplication();
        library = null;

        if (intent != null) {
            final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
            final String action = intent.getAction();
            if (AVAILABILITY.equals(action)) {
                /* Sending running status back to fragment */
                receiver.send(Receiver.STATUS_RUNNING, Bundle.EMPTY);

                final String ean = intent.getStringExtra(EAN);
                getAvailability(ean, receiver);

                /* Sending finished status back to fragment */
                if (library == null) {
                    receiver.send(Receiver.STATUS_FINISHED, Bundle.EMPTY);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence(LIBRARY, library);
                    receiver.send(Receiver.STATUS_FINISHED, bundle);
                }
            }
        }
    }

    /**
     * Handle action getAvailability in the provided background thread with the provided parameters.
     */
    private void getAvailability(String ean, ResultReceiver receiver) {
        if (ean.length() != 13) {
            return;
        }

        // Checks for an internet connection.
        if (!Network.isNetworkAvailable(this)) {
            Log.i(TAG, "No internet connection!");
            Intent messageIntent = new Intent(MasterActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MasterActivity.MESSAGE_KEY, "No internet connection!");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
            return;
        }

        final String NAMESPACE = getString(R.string.namespace);
        final String METHOD_NAME = getString(R.string.method_name);
        final String SOAP_ACTION = getString(R.string.soap_action);
        final String SERVICE_URL = getString(R.string.service_url);
        final String API_KEY = getString(R.string.api_key);

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("APIKey", API_KEY);
        request.addProperty("ISBN", ean);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(SERVICE_URL, 60000);
        httpTransport.debug = true;
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            Log.d(TAG, httpTransport.requestDump);
            Log.d(TAG, httpTransport.responseDump);

            GetAvailabilityInfoResponse response = new GetAvailabilityInfoResponse();
            if (envelope.getResponse() != null) {
                Vector result = (Vector) envelope.getResponse();
                String status = result.get(0).toString();
                Log.d(TAG, status);
                if (!status.equals("OK")) {
                    return;
                }

                int resultCount = result.size();
                Object object = result.get(resultCount - 1);
                if (object instanceof SoapObject) {
                    ArrayList<Item> items = new ArrayList<>();
                    SoapObject soapObject = (SoapObject) object;
                    int totalCount = soapObject.getPropertyCount();
                    for (int detailCount = 0; detailCount < totalCount; detailCount++) {
                        SoapObject pojoSoap = (SoapObject) soapObject.getProperty(detailCount);
                        Item item = new Item();
                        item.ItemNo = pojoSoap.getProperty("ItemNo").toString();
                        item.BranchID = pojoSoap.getProperty("BranchID").toString();
                        item.BranchName = pojoSoap.getProperty("BranchName").toString();
                        item.LocationCode = pojoSoap.getProperty("LocationCode").toString();
                        item.LocationDesc = pojoSoap.getProperty("LocationDesc").toString();
                        item.CallNumber = pojoSoap.getProperty("CallNumber").toString();
                        item.StatusCode = pojoSoap.getProperty("StatusCode").toString();
                        item.StatusDesc = pojoSoap.getProperty("StatusDesc").toString();
                        item.MediaCode = pojoSoap.getProperty("MediaCode").toString();
                        item.MediaDesc = pojoSoap.getProperty("MediaDesc").toString();
                        item.StatusDate = pojoSoap.getProperty("StatusDate").toString();
                        items.add(item);
                    }
                    response.setItems(items);
                }

                response.setStatus(result.get(0).toString());
                response.setMessage(result.get(1).toString());
                String s = result.get(2).toString();
                try {
                    if (!s.isEmpty()) {
                        Integer.parseInt(s);
                    }
                    response.setNextRecordPosition(s);
                    response.setSetId(result.get(3).toString());
                } catch (Exception d) {
                    response.setErrorMessage(s);
                    response.setSetId(result.get(4).toString());
                }

                writeBackLibrary(ean, response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ", e);
            try {
                SoapObject response = (SoapObject) envelope.getResponse();
                if (response != null) {
                    Log.d(TAG, response.toString());
                }
            } catch (SoapFault f) {
                Log.e(TAG, "Fault ", f);
            }
        }
    }

    private void writeBackLibrary(String ean, GetAvailabilityInfoResponse response) {
        String statusCode = null;
        String libraryCode = null;
        double distance = 0;
        double temp;

        for (Item item : response.getItems()) {
            temp = getDistance(item.BranchID);
            if (statusCode == null) {
                statusCode = item.StatusCode;
                libraryCode = item.BranchID;
                distance = temp;
            } else if (!statusCode.equals("S")) {
                if (item.StatusCode.equals("S")) {
                    statusCode = item.StatusCode;
                    libraryCode = item.BranchID;
                    distance = temp;
                } else if (distance > temp) {
                    statusCode = item.StatusCode;
                    libraryCode = item.BranchID;
                    distance = temp;
                }
            } else if (item.StatusCode.equals("S")) {
                if (distance > temp) {
                    statusCode = item.StatusCode;
                    libraryCode = item.BranchID;
                    distance = temp;
                }
            }
        }

        ContentValues values = new ContentValues();
        if (statusCode != null) {
            values.put(DataContract.BookEntry.COLUMN_STATUS_CODE, statusCode);
            values.put(DataContract.BookEntry.COLUMN_LIBRARY_ID, libraryCode);
            library = libraryCode;
        }
        values.put(DataContract.BookEntry.COLUMN_LAST_UPDATE, System.currentTimeMillis());
        getContentResolver().update(DataContract.BookEntry.buildBookUri(Long.parseLong(ean)), values, null, null);
    }

    private double getDistance(String branchId) {
        double distance = Distance.EARTH_RAD;
        // get GeoPoint of library
        Cursor libraryEntry = getContentResolver().query(
                DataContract.LibraryEntry.buildLibraryUri(branchId), LOADER_COLUMNS, null, null, null
        );
        if (libraryEntry != null && libraryEntry.moveToFirst()) {
            Log.d(TAG, libraryEntry.getString(COL_TITLE) + ": " + libraryEntry.getString(COL_GEO_POINT));
            String[] geopoint = libraryEntry.getString(COL_GEO_POINT).split(" ");
            libraryEntry.close();
            distance = Distance.getDistance(
                    myApp.getLatitude(), myApp.getLongitude(),
                    Double.parseDouble(geopoint[0]), Double.parseDouble(geopoint[1])
            );
        }
        Log.d(TAG, "Distance: " + distance);
        return distance;
    }

}
