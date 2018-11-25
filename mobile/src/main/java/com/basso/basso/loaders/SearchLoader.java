package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.basso.basso.model.Song;
import com.basso.basso.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class SearchLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private final ArrayList<Song> mSongList = Lists.newArrayList();

    private Cursor mCursor;

    public SearchLoader(final Context context, final String query) {
        super(context);
        mCursor = makeSearchCursor(context, query);
    }

    @Override
    public List<Song> loadInBackground() {
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                long id = -1;
                final String songName = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                if (!TextUtils.isEmpty(songName)) {
                    id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                }

                final String album = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));

                if (id < 0 && !TextUtils.isEmpty(album)) {
                    id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
                }

                final String artist = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));

                if (id < 0 && !TextUtils.isEmpty(artist)) {
                    id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));
                }
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

    public static final Cursor makeSearchCursor(final Context context, final String query) {
        return context.getContentResolver().query(
                Uri.parse("content://media/external/audio/search/fancy/" + Uri.encode(query)),
                new String[] {
                        BaseColumns._ID, MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Albums.ALBUM,
                        MediaStore.Audio.Media.TITLE, "data1", "data2"
                }, null, null, null);
    }

}
