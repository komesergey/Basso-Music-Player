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

public class ArtistAlbumLoader extends WrappedAsyncTaskLoader<List<Album>> {

    private final ArrayList<Album> mAlbumsList = Lists.newArrayList();

    private Cursor mCursor;

    private final Long mArtistID;

    public ArtistAlbumLoader(final Context context, final Long artistId) {
        super(context);
        mArtistID = artistId;
    }

    @Override
    public List<Album> loadInBackground() {
        mCursor = makeArtistAlbumCursor(getContext(), mArtistID);
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

    public static final Cursor makeArtistAlbumCursor(final Context context, final Long artistId) {
        return context.getContentResolver().query(
                MediaStore.Audio.Artists.Albums.getContentUri("external", artistId), new String[] {
                        BaseColumns._ID,
                        AlbumColumns.ALBUM,
                        AlbumColumns.ARTIST,
                        AlbumColumns.NUMBER_OF_SONGS,
                        AlbumColumns.FIRST_YEAR
                }, null, null, PreferenceUtils.getInstance(context).getArtistAlbumSortOrder());
    }
}
