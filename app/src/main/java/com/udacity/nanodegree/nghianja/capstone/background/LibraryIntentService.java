package com.udacity.nanodegree.nghianja.capstone.background;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.udacity.nanodegree.nghianja.capstone.MasterActivity;
import com.udacity.nanodegree.nghianja.capstone.R;
import com.udacity.nanodegree.nghianja.capstone.util.Network;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

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
 */
public class LibraryIntentService extends IntentService {

    private static final String TAG = LibraryIntentService.class.getSimpleName();

    public static final String RECEIVER = "receiver";
    public static final String AVAILABILITY = "availability";
    public static final String EAN = "ean";

    public LibraryIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
            final String action = intent.getAction();
            if (AVAILABILITY.equals(action)) {
                /* Sending running status back to fragment */
                receiver.send(Receiver.STATUS_RUNNING, Bundle.EMPTY);

                final String ean = intent.getStringExtra(EAN);
                getAvailability(ean, receiver);

                /* Sending finished status back to fragment */
                receiver.send(Receiver.STATUS_FINISHED, Bundle.EMPTY);
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

        // Commented out WebServiceTest.jar and the following as javax.* packages are not supported in Android
        /*
        GetAvailabilityInfoRequest request = new GetAvailabilityInfoRequest();
        request.setAPIKey(getString(R.string.api_key));
        request.setISBN(ean);
        CatalogueService service = new CatalogueService();
        ICatalogueService port = service.getBasicHttpBindingICatalogueService();
        GetAvailabilityInfoResponse response = port.getAvailabilityInfo(request);
        Log.d(TAG, response.getMessage());
        */

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

        HttpTransportSE httpTransport = new HttpTransportSE(SERVICE_URL);
        httpTransport.debug = true;
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            Log.d(TAG, httpTransport.requestDump);
            Log.d(TAG, httpTransport.responseDump);
        } catch (Exception e) {
            Log.e(TAG, "Error ", e);
        }

    }
}