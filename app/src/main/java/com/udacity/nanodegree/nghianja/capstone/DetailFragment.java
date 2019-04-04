package com.udacity.nanodegree.nghianja.capstone;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.udacity.nanodegree.nghianja.capstone.background.LibraryIntentService;
import com.udacity.nanodegree.nghianja.capstone.background.LibraryResultReceiver;
import com.udacity.nanodegree.nghianja.capstone.background.Receiver;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract.BookEntry;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract.LibraryEntry;
import com.udacity.nanodegree.nghianja.capstone.widget.HomeWidgetProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Receiver {

    private static final String TAG = DetailFragment.class.getSimpleName();
    private static final int LOADER_ID = 0;
    private static final String[] LOADER_COLUMNS = {
            BookEntry.TABLE_NAME + "." + BookEntry._ID,
            BookEntry.COLUMN_IMAGE_URL,
            BookEntry.COLUMN_AUTHOR,
            BookEntry.COLUMN_TITLE,
            BookEntry.COLUMN_SUBTITLE,
            BookEntry.COLUMN_DESC,
            BookEntry.COLUMN_LIBRARY_ID,
            BookEntry.COLUMN_LAST_UPDATE
    };
    private static final String[] LIBRARY_COLUMNS = {
            LibraryEntry.TABLE_NAME + "." + LibraryEntry._ID,
            LibraryEntry.COLUMN_LIBRARY_IMAGE,
            LibraryEntry.COLUMN_TITLE,
            LibraryEntry.COLUMN_ADDRESS,
            LibraryEntry.COLUMN_GEO_POINT,
            LibraryEntry.COLUMN_OPERATING,
            LibraryEntry.COLUMN_GUIDE
    };

    private Uri uri;
    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private ImageView bookCover;
    private TextView bookAuthor;
    private TextView bookSubtitle;
    private TextView bookDesc;
    private TextView updated;
    private ProgressBar loading;
    private TextView libraryName;
    private TextView libraryAddress;
    private TextView libraryHours;
    private TextView libraryGuide;
    private ImageView libraryImage;
    private LibraryResultReceiver resultReceiver;

    private String latitude = "1.297414";
    private String longitude = "103.854235";
    private String label = "Central Public Library";

    // These indices are tied to LOADER_COLUMNS.  If LOADER_COLUMNS changes, these must change.
    public static final int COL_BOOK_ID = 0;
    public static final int COL_IMAGE_URL = 1;
    public static final int COL_AUTHOR = 2;
    public static final int COL_TITLE = 3;
    public static final int COL_SUBTITLE = 4;
    public static final int COL_DESC = 5;
    public static final int COL_LIBRARY_ID = 6;
    public static final int COL_LAST_UPDATE = 7;

    // These indices are tied to LIBRARY_COLUMNS.  If LIBRARY_COLUMNS changes, these must change.
    public static final int COL_BRANCH_ID = 0;
    public static final int COL_IMAGE = 1;
    public static final int COL_NAME = 2;
    public static final int COL_ADDRESS = 3;
    public static final int COL_GEO_POINT = 4;
    public static final int COL_OPERATING = 5;
    public static final int COL_GUIDE = 6;

    public static final String DETAIL_URI = "uri";

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            uri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        final FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestMapIntent();
            }
        });

        toolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.toolbar_layout);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        bookCover = (ImageView) rootView.findViewById(R.id.book_cover);

        bookAuthor = (TextView) rootView.findViewById(R.id.book_detail_author);
        bookSubtitle = (TextView) rootView.findViewById(R.id.book_detail_subtitle);
        bookDesc = (TextView) rootView.findViewById(R.id.book_detail_desc);

        updated = (TextView) rootView.findViewById(R.id.updated);
        loading = (ProgressBar) rootView.findViewById(R.id.loading);
        loading.setVisibility(View.INVISIBLE);

        libraryName = (TextView) rootView.findViewById(R.id.library_name);
        libraryAddress = (TextView) rootView.findViewById(R.id.library_address);
        libraryHours = (TextView) rootView.findViewById(R.id.library_hours);
        libraryGuide = (TextView) rootView.findViewById(R.id.library_guide);
        libraryImage = (ImageView) rootView.findViewById(R.id.library_image);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getLoaderManager().initLoader(LOADER_ID, null, this);
        resultReceiver = new LibraryResultReceiver(new Handler());
        resultReceiver.setReceiver(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader=" + uri.toString());
        if (uri != null) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(), uri, LOADER_COLUMNS, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        if (data != null && data.moveToFirst()) {
            Log.d(TAG, data.getString(COL_IMAGE_URL));
            RequestOptions options = new RequestOptions().error(R.drawable.ic_grayscale);
            Glide.with(DetailFragment.this).load(data.getString(COL_IMAGE_URL)).apply(options).into(bookCover);
            toolbarLayout.setTitle(data.getString(COL_TITLE));
            bookAuthor.setText(getString(R.string.book_detail_author, data.getString(COL_AUTHOR)));
            if (data.getString(COL_SUBTITLE).isEmpty()) {
                bookSubtitle.setVisibility(View.GONE);
            } else {
                bookSubtitle.setText(data.getString(COL_SUBTITLE));
            }
            bookDesc.setText(data.getString(COL_DESC));

            SimpleDateFormat sdf = new SimpleDateFormat("d MMM h:mm a");
            Date lastUpdated = new Date(data.getLong(COL_LAST_UPDATE));
            Log.d(TAG, lastUpdated.toString());
            updated.setText(getString(R.string.updated, sdf.format(lastUpdated)));

            libraryName.setText(getString(R.string.library_name, ""));
            showLibraryDetails(data.getString(COL_LIBRARY_ID));

            Intent libraryIntent = new Intent(getActivity(), LibraryIntentService.class);
            int dateColumnIndex = data.getColumnIndex(DataContract.BookEntry._ID);
            libraryIntent.putExtra(LibraryIntentService.EAN, Long.toString(data.getLong(dateColumnIndex)));
            libraryIntent.setAction(LibraryIntentService.AVAILABILITY);
            libraryIntent.putExtra(LibraryIntentService.RECEIVER, resultReceiver);
            getActivity().startService(libraryIntent);
        }
    }

    private void showLibraryDetails(String libraryID) {
        if (libraryID != null && !libraryID.isEmpty()) {
            Uri uri = LibraryEntry.buildLibraryUri(libraryID);
            Cursor cursor = getActivity().getContentResolver().query(uri, LIBRARY_COLUMNS, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                libraryName.setText(getString(R.string.library_name, cursor.getString(COL_NAME)));
                libraryAddress.setText(cursor.getString(COL_ADDRESS));
                libraryHours.setText(cursor.getString(COL_OPERATING));
                libraryGuide.setText(cursor.getString(COL_GUIDE));
                RequestOptions options = new RequestOptions().error(R.drawable.ic_grayscale);
                Glide.with(DetailFragment.this)
                        .load(cursor.getString(COL_IMAGE))
                        .apply(options)
                        .into(libraryImage);

                String[] geopoint = cursor.getString(COL_GEO_POINT).split(" ");
                latitude = geopoint[0];
                longitude = geopoint[1];
                label = cursor.getString(COL_NAME);
                
                cursor.close();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case Receiver.STATUS_RUNNING:
                loading.setVisibility(View.VISIBLE);
                break;
            case Receiver.STATUS_FINISHED:
                loading.setVisibility(View.INVISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("d MMM h:mm a");
                Date lastUpdated = new Date(System.currentTimeMillis());
                updated.setText(getString(R.string.updated, sdf.format(lastUpdated)));
                showLibraryDetails(resultData.getString(LibraryIntentService.LIBRARY));

                // Setting the package ensures that only components in our app will receive the broadcast
                Intent dataUpdatedIntent = new Intent(HomeWidgetProvider.ACTION_DATA_UPDATED)
                        .setPackage(getActivity().getPackageName());
                getActivity().sendBroadcast(dataUpdatedIntent);
                break;
            case Receiver.STATUS_ERROR:
                loading.setVisibility(View.INVISIBLE);
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Log.e(TAG, error);
        }
    }

    private void requestMapIntent() {
        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri gmmIntentUri = Uri.parse("geo:1.3,103.85?q=" +
                Uri.encode(latitude + "," + longitude + "(" + label + ")"));

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}
