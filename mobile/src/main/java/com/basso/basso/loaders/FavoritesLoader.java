package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;

import com.basso.basso.model.Song;
import com.basso.basso.provider.FavoritesStore;
import com.basso.basso.provider.FavoritesStore.FavoriteColumns;
import com.basso.basso.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class FavoritesLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private final ArrayList<Song> mSongList = Lists.newArrayList();

    private Cursor mCursor;

    public FavoritesLoader(final Context context) {
        super(context);
        FolderLoader.m(null, null);
    }

    @Override
    public List<Song> loadInBackground() {
        mCursor = makeFavoritesCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(FavoriteColumns.ID));
                final String songName = mCursor.getString(mCursor.getColumnIndexOrThrow(FavoriteColumns.SONGNAME));
                final String artist = mCursor.getString(mCursor.getColumnIndexOrThrow(FavoriteColumns.ARTISTNAME));
                final String album = mCursor.getString(mCursor.getColumnIndexOrThrow(FavoriteColumns.ALBUMNAME));
                final Song song = new Song(id, songName, artist, album, -1);
                mSongList.add(song);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

    public static final Cursor makeFavoritesCursor(final Context context) {
        return FavoritesStore
                .getInstance(context)
                .getReadableDatabase()
                .query(FavoriteColumns.NAME,
                        new String[] {
                                FavoriteColumns.ID + " as _id", FavoriteColumns.ID,
                                FavoriteColumns.SONGNAME, FavoriteColumns.ALBUMNAME,
                                FavoriteColumns.ARTISTNAME, FavoriteColumns.PLAYCOUNT
                        }, null, null, null, null, FavoriteColumns.PLAYCOUNT + " DESC");
    }
}