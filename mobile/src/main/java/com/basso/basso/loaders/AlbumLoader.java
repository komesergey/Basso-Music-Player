package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;

import com.basso.basso.model.Album;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class AlbumLoader extends WrappedAsyncTaskLoader<List<Album>> {

    private final ArrayList<Album> mAlbumsList = Lists.newArrayList();

    private Cursor mCursor;

    public AlbumLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Album> loadInBackground() {
        mCursor = makeAlbumCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String albumName = mCursor.getString(1);
                final String artist = mCursor.getString(2);
                final int songCount = mCursor.getInt(3);
                final String year = mCursor.getString(4);
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

    public static final Cursor makeAlbumCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        BaseColumns._ID,
                        AlbumColumns.ALBUM,
                        AlbumColumns.ARTIST,
                        AlbumColumns.NUMBER_OF_SONGS,
                        AlbumColumns.FIRST_YEAR
                }, null, null, PreferenceUtils.getInstance(context).getAlbumSortOrder());
    }
}
