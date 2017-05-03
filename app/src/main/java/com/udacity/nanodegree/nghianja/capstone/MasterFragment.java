package com.udacity.nanodegree.nghianja.capstone;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.udacity.nanodegree.nghianja.capstone.background.BookIntentService;
import com.udacity.nanodegree.nghianja.capstone.background.BookResultReceiver;
import com.udacity.nanodegree.nghianja.capstone.background.Receiver;
import com.udacity.nanodegree.nghianja.capstone.data.BookAdapter;
import com.udacity.nanodegree.nghianja.capstone.data.DataContract;
import com.udacity.nanodegree.nghianja.capstone.util.Isbn;
import com.udacity.nanodegree.nghianja.capstone.widget.HomeWidgetProvider;

/**
 * Encapsulates fetching the books and displaying it as a {@link android.support.v7.widget.RecyclerView} layout.
 * <p/>
 * References:
 * [1] http://www.vogella.com/tutorials/AndroidRecyclerView/article.html
 * [2] http://stackoverflow.com/questions/28236390/recyclerview-store-restore-state-between-activities
 * [3] https://github.com/JorgeCastilloPrz/FABProgressCircle
 * [4] http://antonioleiva.com/collapsing-toolbar-layout/
 * [5] http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
 */
public class MasterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Receiver {

    private static final String TAG = MasterFragment.class.getSimpleName();
    private static final int LOADER_ID = 1;
    private static final String[] LOADER_COLUMNS = {
            DataContract.BookEntry._ID,
            DataContract.BookEntry.COLUMN_TITLE,
            DataContract.BookEntry.COLUMN_SUBTITLE,
            DataContract.BookEntry.COLUMN_IMAGE_URL,
            DataContract.BookEntry.COLUMN_AUTHOR
    };

    public static final int COL_BOOK_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_SUBTITLE = 2;
    public static final int COL_IMAGE_URL = 3;
    public static final int COL_AUTHOR = 4;

    private Toolbar toolbar;
    private IntentIntegrator integrator;
    private BookAdapter bookAdapter;
    private BookResultReceiver resultReceiver;
    private FABProgressCircle fabProgressCircle;
    private boolean fabClickable;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri bookUri, BookAdapter.BookAdapterViewHolder vh);
    }

    public MasterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_master, container, false);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);

        fabClickable = true;
        rootView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabClickable) {
                    integrator.initiateScan();
                }
            }
        });

        fabProgressCircle = (FABProgressCircle) rootView.findViewById(R.id.fabProgressCircle);

        // Get a reference to the RecyclerView, and attach this adapter to it.
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_books);

        // Set the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.empty_layout);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // The BookAdapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        bookAdapter = new BookAdapter(getActivity(), new BookAdapter.BookAdapterOnClickHandler() {
            @Override
            public void onClick(long id, BookAdapter.BookAdapterViewHolder viewHolder) {
                ((Callback) getActivity())
                        .onItemSelected(DataContract.BookEntry.buildBookUri(id), viewHolder);
            }
        }, emptyView);

        // specify an adapter (see also next example)
        recyclerView.setAdapter(bookAdapter);

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null) {
            bookAdapter.onRestoreInstanceState(savedInstanceState);
        }

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        Cursor cursor = bookAdapter.getCursor();
                        cursor.moveToPosition(viewHolder.getAdapterPosition());
                        int dateColumnIndex = cursor.getColumnIndex(DataContract.BookEntry._ID);
                        Long id = cursor.getLong(dateColumnIndex);
                        Uri uri = DataContract.BookEntry.buildBookUri(id);
                        int rowsDeleted = getActivity().getContentResolver().delete(uri, null, null);
                        if (rowsDeleted > 0) {
                            Log.d(TAG, "Deleted ID: " + id);
                            updateViewAndWidget();
                        }
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        getLoaderManager().initLoader(LOADER_ID, null, this);
        resultReceiver = new BookResultReceiver(new Handler());
        resultReceiver.setReceiver(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        bookAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                Log.d(TAG, result.getContents());
                String ean = result.getContents();
                // catch isbn10 numbers
                if (!ean.startsWith("978") && Isbn.isIsbn10(ean)) {
                    ean = Isbn.isbn10To13(ean);
                }
                if (ean.length() == 13) {
                    // once we have an ISBN, start a book intent
                    Intent bookIntent = new Intent(getActivity(), BookIntentService.class);
                    bookIntent.putExtra(BookIntentService.EAN, ean);
                    bookIntent.setAction(BookIntentService.FETCH_BOOK);
                    bookIntent.putExtra(BookIntentService.RECEIVER, resultReceiver);
                    getActivity().startService(bookIntent);
                } else {
                    Intent messageIntent = new Intent(MasterActivity.MESSAGE_EVENT);
                    messageIntent.putExtra(MasterActivity.MESSAGE_KEY, getString(R.string.not_ean));
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(messageIntent);
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.

        // Sort order:  Ascending, by title.
        String sortOrder = DataContract.BookEntry.COLUMN_TITLE + " ASC";
        Uri uri = DataContract.BookEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), uri, LOADER_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookAdapter.swapCursor(null);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case Receiver.STATUS_RUNNING:
                fabClickable = false;
                fabProgressCircle.show();
                break;
            case Receiver.STATUS_FINISHED:
                updateViewAndWidget();
                fabProgressCircle.hide();
                fabClickable = true;
                break;
            case Receiver.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Log.e(TAG, error);
        }
    }

    private void updateViewAndWidget() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);

        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(HomeWidgetProvider.ACTION_DATA_UPDATED)
                .setPackage(getActivity().getPackageName());
        getActivity().sendBroadcast(dataUpdatedIntent);
    }
}
