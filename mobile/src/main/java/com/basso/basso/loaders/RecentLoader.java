package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;

import com.basso.basso.model.Album;
import com.basso.basso.provider.RecentStore;
import com.basso.basso.provider.RecentStore.RecentStoreColumns;
import com.basso.basso.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class RecentLoader extends WrappedAsyncTaskLoader<List<Album>> {

    private final ArrayList<Album> mAlbumsList = Lists.newArrayList();

    private Cursor mCursor;

    public RecentLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Album> loadInBackground() {
        mCursor = makeRecentCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ID));
                final String albumName = mCursor.getString(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ALBUMNAME));
                final String artist = mCursor.getString(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ARTISTNAME));
                final int songCount = mCursor.getInt(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ALBUMSONGCOUNT));
                final String year = mCursor.getString(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ALBUMYEAR));
                final Album album = new Album(id, albumName, artist, songCount, year);
                mAlbumsList.add(album);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mAlbumsList;
    }

    public static final Cursor makeRecentCursor(final Context context) {
        return RecentStore
                .getInstance(context)
                .getReadableDatabase()
                .query(RecentStoreColumns.NAME,
                        new String[] {
                                RecentStoreColumns.ID + " as id", RecentStoreColumns.ID,
                                RecentStoreColumns.ALBUMNAME, RecentStoreColumns.ARTISTNAME,
                                RecentStoreColumns.ALBUMSONGCOUNT, RecentStoreColumns.ALBUMYEAR,
                                RecentStoreColumns.TIMEPLAYED
                        }, null, null, null, null, RecentStoreColumns.TIMEPLAYED + " DESC");
    }
}