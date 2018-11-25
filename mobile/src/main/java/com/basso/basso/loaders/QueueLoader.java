package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;

import com.basso.basso.model.Song;
import com.basso.basso.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class QueueLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private final ArrayList<Song> mSongList = Lists.newArrayList();

    private NowPlayingCursor mCursor;

    public QueueLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Song> loadInBackground() {
        mCursor = new NowPlayingCursor(getContext());
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
    public static final Cursor makeQueueCursor(final Context context) {
        final Cursor cursor = new NowPlayingCursor(context);
        return cursor;
    }
}
