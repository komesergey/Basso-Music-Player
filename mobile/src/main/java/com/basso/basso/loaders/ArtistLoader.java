package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.ArtistColumns;

import com.basso.basso.model.Artist;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class ArtistLoader extends WrappedAsyncTaskLoader<List<Artist>> {

    private final ArrayList<Artist> mArtistsList = Lists.newArrayList();

    private Cursor mCursor;

    public ArtistLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Artist> loadInBackground() {
        mCursor = makeArtistCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String artistName = mCursor.getString(1);
                final int albumCount = mCursor.getInt(2);
                final int songCount = mCursor.getInt(3);
                final Artist artist = new Artist(id, artistName, songCount, albumCount);
                mArtistsList.add(artist);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mArtistsList;
    }

    public static final Cursor makeArtistCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[] {
                        BaseColumns._ID,
                        ArtistColumns.ARTIST,
                        ArtistColumns.NUMBER_OF_ALBUMS,
                        ArtistColumns.NUMBER_OF_TRACKS
                }, null, null, PreferenceUtils.getInstance(context).getArtistSortOrder());
    }
}
