package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.basso.basso.model.Song;
import com.basso.basso.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class GenreSongLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private final ArrayList<Song> mSongList = Lists.newArrayList();

    private Cursor mCursor;

    private final Long mGenreID;

    public GenreSongLoader(final Context context, final Long genreId) {
        super(context);
        mGenreID = genreId;
    }

    @Override
    public List<Song> loadInBackground() {
        mCursor = makeGenreSongCursor(getContext(), mGenreID);
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String songName = mCursor.getString(1);
                final String album = mCursor.getString(2);
                final String artist = mCursor.getString(3);
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

    public static final Cursor makeGenreSongCursor(final Context context, final Long genreId) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Genres.Members.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Genres.Members.TITLE + "!=''"); //$NON-NLS-2$
        return context.getContentResolver().query(
                MediaStore.Audio.Genres.Members.getContentUri("external", genreId), new String[] {
                        MediaStore.Audio.Genres.Members._ID,
                        MediaStore.Audio.Genres.Members.TITLE,
                        MediaStore.Audio.Genres.Members.ALBUM,
                        MediaStore.Audio.Genres.Members.ARTIST,
                        MediaStore.Audio.Genres.Members.DURATION
                }, selection.toString(), null, MediaStore.Audio.Genres.Members.DEFAULT_SORT_ORDER);
    }
}
