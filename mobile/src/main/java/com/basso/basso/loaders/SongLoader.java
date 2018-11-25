package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import com.basso.basso.model.Song;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;


public class SongLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private final ArrayList<Song> mSongList = Lists.newArrayList();

    private Cursor mCursor;

    public SongLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Song> loadInBackground() {
        mCursor = makeSongCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String songName = mCursor.getString(1);
                final String artist = mCursor.getString(2);
                final String album = mCursor.getString(3);
                final long duration = mCursor.getLong(4);
                final int durationInSecs = (int) duration / 1000;
                final Song song = new Song(id, songName, artist, album, durationInSecs);
                mSongList.add(song);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

    public static final Cursor makeSongCursor(final Context context) {
        final StringBuilder mSelection = new StringBuilder();
        mSelection.append(AudioColumns.IS_MUSIC + "=1");
        mSelection.append(" AND " + AudioColumns.TITLE + " != ''"); //$NON-NLS-2$
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        BaseColumns._ID,
                        AudioColumns.TITLE,
                        AudioColumns.ARTIST,
                        AudioColumns.ALBUM,
                        AudioColumns.DURATION
                }, mSelection.toString(), null,
                PreferenceUtils.getInstance(context).getSongSortOrder());
    }
}