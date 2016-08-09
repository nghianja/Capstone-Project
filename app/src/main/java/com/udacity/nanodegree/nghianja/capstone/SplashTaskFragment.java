package com.udacity.nanodegree.nghianja.capstone;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.udacity.nanodegree.nghianja.capstone.data.DataContract;
import com.udacity.nanodegree.nghianja.capstone.util.RssFeed;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * This Fragment manages a single background task and retains
 * itself across configuration changes.
 *
 * References:
 * [1] http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class SplashTaskFragment extends Fragment {

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute();
    }

    private static final String FEED_URL = "feedUrl";

    private String feedUrl;

    private TaskCallbacks callbacks;

    public SplashTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param feedUrl RSS library info.
     * @return A new instance of fragment SplashTaskFragment.
     */
    public static SplashTaskFragment newInstance(String feedUrl) {
        SplashTaskFragment fragment = new SplashTaskFragment();
        Bundle args = new Bundle();
        args.putString(FEED_URL, feedUrl);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (TaskCallbacks) context;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if (getArguments() != null) {
            feedUrl = getArguments().getString(FEED_URL);
            new RssFeedReader().execute(feedUrl);
        }
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    private class RssFeedReader extends AsyncTask<String, Void, List<ContentValues>> {

        private final String LOG_TAG = RssFeedReader.class.getSimpleName();

        protected List<ContentValues> doInBackground(String... urls) {
            try {
                InputStream stream;
                URL url = new URL(urls[0]);
                stream = url.openConnection().getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(stream, "UTF_8");
                List<ContentValues> valuesList = RssFeed.handleLibraryXml(xpp);
                stream.close();

                return valuesList;
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
                return null;
            }
        }

        protected void onPostExecute(List<ContentValues> valuesList) {
            onXmlHandled(valuesList);
            if (callbacks != null) {
                callbacks.onPostExecute();
            }
        }

        private void onXmlHandled(List<ContentValues> valuesList) {
            if (valuesList != null) {
                ContentResolver contentResolver = getActivity().getContentResolver();
                int rows = contentResolver.delete(DataContract.LibraryEntry.CONTENT_URI, "1", null);
                Log.d(LOG_TAG, "# of rows deleted = " + rows);
                ContentValues[] valuesArray = new ContentValues[valuesList.size()];
                valuesList.toArray(valuesArray);
                rows = contentResolver.bulkInsert(DataContract.LibraryEntry.CONTENT_URI, valuesArray);
                Log.d(LOG_TAG, "# of rows inserted = " + rows);
            }
        }
    }
}
