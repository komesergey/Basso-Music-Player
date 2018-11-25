package com.basso.basso.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import com.basso.basso.model.Song;
import com.basso.basso.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private final ArrayList<Song> mSongList = Lists.newArrayList();

    private Cursor mCursor;

    private final Long mPlaylistID;

    public PlaylistSongLoader(final Context context, final Long playlistId) {
        super(context);
        mPlaylistID = playlistId;
    }

    @Override
    public List<Song> loadInBackground() {
        mCursor = makePlaylistSongCursor(getContext(), mPlaylistID);
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                final String songName = mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.TITLE));
                final String artist = mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ARTIST));
                final String album = mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ALBUM));
                final long duration = mCursor.getLong(mCursor.getColumnIndexOrThrow(AudioColumns.DURATION));
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

    public static final Cursor makePlaylistSongCursor(final Context context, final Long playlistID) {
        final StringBuilder mSelection = new StringBuilder();
        mSelection.append(AudioColumns.IS_MUSIC + "=1");
        mSelection.append(" AND " + AudioColumns.TITLE + " != ''"); //$NON-NLS-2$
        return context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID),
                new String[] {
                        MediaStore.Audio.Playlists.Members._ID,
                        MediaStore.Audio.Playlists.Members.AUDIO_ID,
                        AudioColumns.TITLE,
                        AudioColumns.ARTIST,
                        AudioColumns.ALBUM,
                        AudioColumns.DURATION
                }, mSelection.toString(), null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
    }
}