package com.udacity.nanodegree.nghianja.capstone;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.udacity.nanodegree.nghianja.capstone.data.DataContract.BookEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DetailFragment.class.getSimpleName();
    private static final int LOADER_ID = 0;
    private static final String[] LOADER_COLUMNS = {
            BookEntry.TABLE_NAME + "." + BookEntry._ID,
            BookEntry.COLUMN_IMAGE_URL,
            BookEntry.COLUMN_AUTHOR,
            BookEntry.COLUMN_TITLE,
            BookEntry.COLUMN_SUBTITLE,
            BookEntry.COLUMN_DESC
    };

    private Uri uri;
    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private ImageView bookCover;
    private TextView bookAuthor;
    private TextView bookSubtitle;
    private TextView bookDesc;
    private ImageView libraryImage;

    // These indices are tied to LOADER_COLUMNS.  If LOADER_COLUMNS changes, these must change.
    public static final int COL_BOOK_ID = 0;
    public static final int COL_IMAGE_URL = 1;
    public static final int COL_AUTHOR = 2;
    public static final int COL_TITLE = 3;
    public static final int COL_SUBTITLE = 4;
    public static final int COL_DESC = 5;

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
//        bookCover.setDefaultImageResId(R.drawable.ic_grayscale);
//        bookCover.setErrorImageResId(R.drawable.ic_grayscale);

        bookAuthor = (TextView) rootView.findViewById(R.id.book_detail_author);
        bookSubtitle = (TextView) rootView.findViewById(R.id.book_detail_subtitle);
        bookDesc = (TextView) rootView.findViewById(R.id.book_detail_desc);

        libraryImage = (ImageView) rootView.findViewById(R.id.library_image);
//        libraryImage.setDefaultImageResId(R.drawable.ic_grayscale);
//        libraryImage.setErrorImageResId(R.drawable.ic_grayscale);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Get the ImageLoader through your singleton class.
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
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

            // TODO: move to after getting library query result
            Glide.with(DetailFragment.this)
                    .load("http://www.nlb.gov.sg/Portals/0/library/gallery/Central/MAIN-CTPL-cropped.jpg")
                    .error(R.drawable.ic_grayscale).into(libraryImage);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
