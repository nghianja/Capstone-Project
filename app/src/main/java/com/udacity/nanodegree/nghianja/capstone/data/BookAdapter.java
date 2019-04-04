package com.udacity.nanodegree.nghianja.capstone.data;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.udacity.nanodegree.nghianja.capstone.MasterFragment;
import com.udacity.nanodegree.nghianja.capstone.R;

/**
 * {@link BookAdapter} exposes a list of books from a
 * {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 *
 * References:
 * [1] https://github.com/nghianja/Advanced_Android_Development/blob/master/app/src/main/java/com/example/android/sunshine/app/ForecastAdapter.java
 * [2] https://github.com/pollux-/RecyclerView/blob/master/src/com/demo/recylerview/RecycleAdapter.java
 * [3] http://www.programering.com/a/MDM0QTNwATQ.html
 * [4] http://www.vogella.com/tutorials/AndroidRecyclerView/article.html
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookAdapterViewHolder> {

    final private Context context;
    final private BookAdapterOnClickHandler clickHandler;
    final private View emptyView;

    private Cursor cursor;

    public BookAdapter(Context context, BookAdapterOnClickHandler clickHandler, View emptyView) {
        this.context = context;
        this.clickHandler = clickHandler;
        this.emptyView = emptyView;
    }

    @Override
    public int getItemCount() {
        if (cursor == null) return 0;
        return cursor.getCount();
    }

    public int getSelectedItemPosition() {
        // TODO: to get selected item on list for dual-pane layout
        return 0;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO: restore adapter state for selected item
    }

    public void onSaveInstanceState(Bundle outState) {
        // TODO: save adapter state for selected item
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
        emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof BookAdapterViewHolder) {
            BookAdapterViewHolder vfh = (BookAdapterViewHolder) viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

    @Override
    public BookAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = R.layout.recycler_item;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new BookAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(BookAdapterViewHolder viewHolder, int position) {
        cursor.moveToPosition(position);
        String imageUrl = cursor.getString(MasterFragment.COL_IMAGE_URL);
        String title = cursor.getString(MasterFragment.COL_TITLE);
        String subtitle = cursor.getString(MasterFragment.COL_SUBTITLE);
        String author = cursor.getString(MasterFragment.COL_AUTHOR);

        RequestOptions options = new RequestOptions().error(R.mipmap.ic_launcher);
        Glide.with(context).load(imageUrl).apply(options).into(viewHolder.bookCover);

        // this enables better animations. even if we lose state due to a device rotation,
        // the animator can use this to re-find the original view
        ViewCompat.setTransitionName(viewHolder.bookCover, "bookCover" + position);

        viewHolder.bookTitle.setText(title);
        viewHolder.bookSubTitle.setText(subtitle);
        viewHolder.bookAuthor.setText(author);
    }

    public interface BookAdapterOnClickHandler {
        void onClick(long id, BookAdapterViewHolder viewHolder);
    }

    /**
     * Cache of the children views for a book list item.
     */
    public class BookAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;
        public final TextView bookAuthor;

        public BookAdapterViewHolder(View view) {
            super(view);
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
            bookAuthor = (TextView) view.findViewById(R.id.listBookAuthor);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            int dateColumnIndex = cursor.getColumnIndex(DataContract.BookEntry._ID);
            clickHandler.onClick(cursor.getLong(dateColumnIndex), this);
        }
    }
}
