package com.udacity.nanodegree.nghianja.capstone;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.udacity.nanodegree.nghianja.capstone.background.LibraryIntentService;
import com.udacity.nanodegree.nghianja.capstone.background.LibraryResultReceiver;
import com.udacity.nanodegree.nghianja.capstone.background.Receiver;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract.BookEntry;

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

    private Uri uri;
    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private ImageView bookCover;
    private TextView bookAuthor;
    private TextView bookSubtitle;
    private TextView bookDesc;
    private ImageView libraryImage;
    private LibraryResultReceiver resultReceiver;

    // These indices are tied to LOADER_COLUMNS.  If LOADER_COLUMNS changes, these must change.
    public static final int COL_BOOK_ID = 0;
    public static final int COL_IMAGE_URL = 1;
    public static final int COL_AUTHOR = 2;
    public static final int COL_TITLE = 3;
    public static final int COL_SUBTITLE = 4;
    public static final int COL_DESC = 5;
    public static final int COL_LIBRARY_ID = 6;
    public static final int COL_LAST_UPDATE = 7;

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

        toolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.toolbar_layout);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        bookCover = (ImageView) rootView.findViewById(R.id.book_cover);

        bookAuthor = (TextView) rootView.findViewById(R.id.book_detail_author);
        bookSubtitle = (TextView) rootView.findViewById(R.id.book_detail_subtitle);
        bookDesc = (TextView) rootView.findViewById(R.id.book_detail_desc);

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
            Glide.with(DetailFragment.this).load(data.getString(COL_IMAGE_URL)).error(R.drawable.ic_grayscale).into(bookCover);
            toolbarLayout.setTitle(data.getString(COL_TITLE));
            bookAuthor.setText(getString(R.string.book_detail_author, data.getString(COL_AUTHOR)));
            if (data.getString(COL_SUBTITLE).isEmpty()) {
                bookSubtitle.setVisibility(View.GONE);
            } else {
                bookSubtitle.setText(data.getString(COL_SUBTITLE));
            }
            bookDesc.setText(data.getString(COL_DESC));

            String libraryID = data.getString(COL_LIBRARY_ID);
            if (libraryID != null && !libraryID.isEmpty()) {
                Glide.with(DetailFragment.this)
                        .load("http://www.nlb.gov.sg/Portals/0/library/gallery/Central/MAIN-CTPL-cropped.jpg")
                        .error(R.drawable.ic_grayscale).into(libraryImage);
            }

            Intent libraryIntent = new Intent(getActivity(), LibraryIntentService.class);
            int dateColumnIndex = data.getColumnIndex(DataContract.BookEntry._ID);
            libraryIntent.putExtra(LibraryIntentService.EAN, Long.toString(data.getLong(dateColumnIndex)));
            libraryIntent.setAction(LibraryIntentService.AVAILABILITY);
            libraryIntent.putExtra(LibraryIntentService.RECEIVER, resultReceiver);
            getActivity().startService(libraryIntent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case Receiver.STATUS_RUNNING:
                break;
            case Receiver.STATUS_FINISHED:
                break;
            case Receiver.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Log.e(TAG, error);
        }
    }
}
