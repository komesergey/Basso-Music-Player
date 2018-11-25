package com.basso.basso.loaders;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;

import com.basso.basso.R;
import com.basso.basso.model.Playlist;
import com.basso.basso.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class PlaylistLoader extends WrappedAsyncTaskLoader<List<Playlist>> {

    private final ArrayList<Playlist> mPlaylistList = Lists.newArrayList();

    private Cursor mCursor;

    public PlaylistLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Playlist> loadInBackground() {
        makeDefaultPlaylists();
        mCursor = makePlaylistCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String name = mCursor.getString(1);
                final Playlist playlist = new Playlist(id, name);
                mPlaylistList.add(playlist);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mPlaylistList;
    }

    private void makeDefaultPlaylists() {
        final Resources resources = getContext().getResources();
        final Playlist favorites = new Playlist(-1, resources.getString(R.string.playlist_favorites));
        mPlaylistList.add(favorites);
        final Playlist lastAdded = new Playlist(-2, resources.getString(R.string.playlist_last_added));
        mPlaylistList.add(lastAdded);
    }

    public static final Cursor makePlaylistCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] {
                        BaseColumns._ID,
                        PlaylistsColumns.NAME
                }, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }
}
