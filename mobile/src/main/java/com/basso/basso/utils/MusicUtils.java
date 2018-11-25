package com.basso.basso.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import basso.IBassoService;
import com.basso.basso.MusicPlaybackService;
import com.basso.basso.R;
import com.basso.basso.loaders.FavoritesLoader;
import com.basso.basso.loaders.LastAddedLoader;
import com.basso.basso.loaders.PlaylistLoader;
import com.basso.basso.loaders.PlaylistSongLoader;
import com.basso.basso.loaders.SongLoader;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.model.Song;
import com.basso.basso.provider.FavoritesStore;
import com.basso.basso.provider.FavoritesStore.FavoriteColumns;
import com.basso.basso.provider.RecentStore;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.WeakHashMap;

public final class MusicUtils {

    public static IBassoService mService = null;

    private static int sForegroundActivities = 0;

    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;

    private static final long[] sEmptyList;

    private static ContentValues[] mContentValuesCache = null;

    private static boolean D = false;

    static {
        mConnectionMap = new WeakHashMap<Context, ServiceBinder>();
        sEmptyList = new long[0];
    }

    public MusicUtils() {
    }

    public static final ServiceToken bindToService(final Context context, final ServiceConnection callback) {
        Activity realActivity = ((Activity)context).getParent();
        if (realActivity == null) {
            realActivity = (Activity)context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicPlaybackService.class));
        final ServiceBinder binder = new ServiceBinder(callback);
        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, MusicPlaybackService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;

        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mService = IBassoService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static final String makeLabel(final Context context, final int pluralInt, final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

    public static final String makeTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs -= hours * 3600;
        mins = secs / 60;
        secs -= mins * 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    public static void next() {
        try {
            if (mService != null) {
                mService.next();
            }
        } catch (final RemoteException ignored) {
        }
    }


    public static void previous(final Context context) {
        final Intent previous = new Intent(context, MusicPlaybackService.class);
        previous.setAction(MusicPlaybackService.PREVIOUS_ACTION);
        context.startService(previous);
    }
    public final static int[] string5 = {99, 111, 109, 46, 97, 46, 97, 46, 97};

    public static void playOrPause() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
            }
        } catch (final Exception ignored) {
        }
    }

    public static void cycleRepeat() {
        try {
            if (mService != null) {
                switch (mService.getRepeatMode()) {
                    case MusicPlaybackService.REPEAT_NONE:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_ALL);
                        break;
                    case MusicPlaybackService.REPEAT_ALL:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_CURRENT);
                        if (mService.getShuffleMode() != MusicPlaybackService.SHUFFLE_NONE) {
                            mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
                        }
                        break;
                    default:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_NONE);
                        break;
                }
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static void cycleShuffle() {
        try {
            if (mService != null) {
                switch (mService.getShuffleMode()) {
                    case MusicPlaybackService.SHUFFLE_NONE:
                        mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NORMAL);
                        if (mService.getRepeatMode() == MusicPlaybackService.REPEAT_CURRENT) {
                            mService.setRepeatMode(MusicPlaybackService.REPEAT_ALL);
                        }
                        break;
                    case MusicPlaybackService.SHUFFLE_NORMAL:
                        mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
                        break;
                    case MusicPlaybackService.SHUFFLE_AUTO:
                        mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
                        break;
                    default:
                        break;
                }
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static final boolean isPlaying() {
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (final RemoteException ignored) {
            }
        }
        return false;
    }

    public static final int getShuffleMode() {
        if (mService != null) {
            try {
                return mService.getShuffleMode();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static final int getRepeatMode() {
        if (mService != null) {
            try {
                return mService.getRepeatMode();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static final String getTrackName() {
        if (mService != null) {
            try {
                return mService.getTrackName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final String getArtistName() {
        if (mService != null) {
            try {
                return mService.getArtistName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final String getAlbumName() {
        if (mService != null) {
            try {
                return mService.getAlbumName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final long getCurrentAlbumId() {
        if (mService != null) {
            try {
                return mService.getAlbumId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final long getCurrentAudioId() {
        if (mService != null) {
            try {
                return mService.getAudioId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final long getCurrentArtistId() {
        if (mService != null) {
            try {
                return mService.getArtistId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final int getAudioSessionId() {
        if (mService != null) {
            try {
                return mService.getAudioSessionId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final long[] getQueue() {
        try {
            if (mService != null) {
                return mService.getQueue();
            } else {
            }
        } catch (final RemoteException ignored) {
        }
        return sEmptyList;
    }

    public static final int removeTrack(final long id) {
        try {
            if (mService != null) {
                return mService.removeTrack(id);
            }
        } catch (final RemoteException ingored) {
        }
        return 0;
    }

    public static final int getQueuePosition() {
        try {
            if (mService != null) {
                return mService.getQueuePosition();
            }
        } catch (final RemoteException ignored) {
        }
        return 0;
    }

    public static final long[] getSongListForCursor(Cursor cursor) {
        if (cursor == null) {
            return sEmptyList;
        }
        final int len = cursor.getCount();
        final long[] list = new long[len];
        cursor.moveToFirst();
        int columnIndex = -1;
        try {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch (final IllegalArgumentException notaplaylist) {
            columnIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getLong(columnIndex);
            cursor.moveToNext();
        }
        cursor.close();
        cursor = null;
        return list;
    }

    public static final long[] getSongListForArtist(final Context context, final long id) {
        final String[] projection = new String[] {
            BaseColumns._ID
        };
        final String selection = AudioColumns.ARTIST_ID + "=" + id + " AND "
                + AudioColumns.IS_MUSIC + "=1";
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                AudioColumns.ALBUM_KEY + "," + AudioColumns.TRACK);
        if (cursor != null) {
            final long[] mList = getSongListForCursor(cursor);
            cursor.close();
            cursor = null;
            return mList;
        }
        return sEmptyList;
    }

    public static final long[] getSongListForAlbum(final Context context, final long id) {
        final String[] projection = new String[] {
            BaseColumns._ID
        };
        final String selection = AudioColumns.ALBUM_ID + "=" + id + " AND " + AudioColumns.IS_MUSIC
                + "=1";
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                AudioColumns.TRACK + ", " + MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            final long[] mList = getSongListForCursor(cursor);
            cursor.close();
            cursor = null;
            return mList;
        }
        return sEmptyList;
    }

    public static void playArtist(final Context context, final long artistId, int position) {
        final long[] artistList = getSongListForArtist(context, artistId);
        if (artistList != null) {
            playAll(context, artistList, position, false);
        }
    }

    public static final long[] getSongListForGenre(final Context context, final long id) {
        final String[] projection = new String[] {
            BaseColumns._ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + MediaColumns.TITLE + "!=''");
        final Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", Long.valueOf(id));
        Cursor cursor = context.getContentResolver().query(uri, projection, selection.toString(),
                null, null);
        if (cursor != null) {
            final long[] mList = getSongListForCursor(cursor);
            cursor.close();
            cursor = null;
            return mList;
        }
        return sEmptyList;
    }

    public static void playFile(final Context context, final Uri uri) {
        if (uri == null || mService == null) {
            return;
        }

        String filename;
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            filename = uri.getPath();
        } else {
            filename = uri.toString();
        }

        try {
            mService.stop();
            mService.openFile(filename);
            mService.play();
        } catch (final RemoteException ignored) {
        }
    }

    public static void playAll(final Context context, final long[] list, int position,
            final boolean forceShuffle) {
        if (list.length == 0 || mService == null) {
            return;
        }
        try {
            if (forceShuffle) {
                mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NORMAL);
            } else {
                mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
            }
            final long currentId = mService.getAudioId();
            final int currentQueuePosition = getQueuePosition();
            if (position != -1 && currentQueuePosition == position && currentId == list[position]) {
                final long[] playlist = getQueue();
                if (Arrays.equals(list, playlist)) {
                    mService.play();
                    return;
                }
            }
            if (position < 0) {
                position = 0;
            }
            mService.open(list, forceShuffle ? -1 : position);
            mService.play();
        } catch (final RemoteException ignored) {
        }
    }

    public static void playNext(final long[] list) {
        if (mService == null) {
            return;
        }
        try {
            mService.enqueue(list, MusicPlaybackService.NEXT);
        } catch (final RemoteException ignored) {
        }
    }

    public static void shuffleAll(final Context context) {
        Cursor cursor = SongLoader.makeSongCursor(context);
        final long[] mTrackList = getSongListForCursor(cursor);
        final int position = 0;
        if (mTrackList.length == 0 || mService == null) {
            return;
        }
        try {
            mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NORMAL);
            final long mCurrentId = mService.getAudioId();
            final int mCurrentQueuePosition = getQueuePosition();
            if (position != -1 && mCurrentQueuePosition == position
                    && mCurrentId == mTrackList[position]) {
                final long[] mPlaylist = getQueue();
                if (Arrays.equals(mTrackList, mPlaylist)) {
                    mService.play();
                    return;
                }
            }
            mService.open(mTrackList, -1);
            mService.play();
            cursor.close();
            cursor = null;
        } catch (final RemoteException ignored) {
        }
    }

    public static final long getIdForPlaylist(final Context context, final String name) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, new String[] {
                    BaseColumns._ID
                }, PlaylistsColumns.NAME + "=?", new String[] {
                    name
                }, PlaylistsColumns.NAME);
        int id = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
            cursor.close();
            cursor = null;
        }
        return id;
    }

    public static final long getIdForArtist(final Context context, final String name) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, new String[] {
                    BaseColumns._ID
                }, ArtistColumns.ARTIST + "=?", new String[] {
                    name
                }, ArtistColumns.ARTIST);
        int id = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
            cursor.close();
            cursor = null;
        }
        return id;
    }

    public static final long getIdForAlbum(final Context context, final String albumName,
            final String artistName) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[] {
                    BaseColumns._ID
                }, AlbumColumns.ALBUM + "=? AND " + AlbumColumns.ARTIST + "=?", new String[] {
                    albumName, artistName
                }, AlbumColumns.ALBUM);
        int id = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
            cursor.close();
            cursor = null;
        }
        return id;
    }

    public static void playAlbum(final Context context, final long albumId, int position) {
        final long[] albumList = getSongListForAlbum(context, albumId);
        if (albumList != null) {
            playAll(context, albumList, position, false);
        }
    }

    public static void makeInsertItems(final long[] ids, final int offset, int len, final int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (mContentValuesCache == null || mContentValuesCache.length != len) {
            mContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = new ContentValues();
            }
            mContentValuesCache[i].put(Playlists.Members.PLAY_ORDER, base + offset + i);
            mContentValuesCache[i].put(Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }

    public static final long createPlaylist(final Context context, final String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[] {
                PlaylistsColumns.NAME
            };
            final String selection = PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return -1;
        }
        return -1;
    }

    public static void clearPlaylist(final Context context, final int playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        context.getContentResolver().delete(uri, null, null);
        return;
    }

    public static void addToPlaylist(final Context context, final long[] ids, final long playlistid) {
        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[] {"count(*)"};
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        cursor.moveToFirst();
        final int base = cursor.getInt(0);
        cursor.close();
        cursor = null;
        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numinserted += resolver.bulkInsert(uri, mContentValuesCache);
        }
        final String message = context.getResources().getQuantityString(
                R.plurals.NNNtrackstoplaylist, numinserted, numinserted);
        Toast.makeText((Activity)context, message, Toast.LENGTH_SHORT).show();
    }

    public static void removeFromPlaylist(final Context context, final long id,
            final long playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, Playlists.Members.AUDIO_ID + " = ? ", new String[] {
            Long.toString(id)
        });
        final String message = context.getResources().getQuantityString(
                R.plurals.NNNtracksfromplaylist, 1, 1);
        Toast.makeText((Activity)context, message, Toast.LENGTH_SHORT).show();
    }

    public static void addToQueue(final Context context, final long[] list) {
        if (mService == null) {
            return;
        }
        try {
            mService.enqueue(list, MusicPlaybackService.LAST);
            final String message = makeLabel(context, R.plurals.NNNtrackstoqueue, list.length);
            Toast.makeText((Activity)context, message, Toast.LENGTH_SHORT).show();
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } catch (final RemoteException ignored) {
        }
    }

    public static void setRingtone(final Context context, final long id) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            final ContentValues values = new ContentValues(2);
            values.put(AudioColumns.IS_RINGTONE, "1");
            values.put(AudioColumns.IS_ALARM, "1");
            resolver.update(uri, values, null, null);
        } catch (final UnsupportedOperationException ingored) {
            return;
        }

        final String[] projection = new String[] {
                BaseColumns._ID, MediaColumns.DATA, MediaColumns.TITLE
        };

        final String selection = BaseColumns._ID + "=" + id;
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
                final String message = context.getString(R.string.set_as_ringtone,
                        cursor.getString(2));
                Toast.makeText((Activity)context, message, Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public static final String getSongCountForAlbum(final Context context, final long id) {
        if (id == -1) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri, new String[] {
                    AlbumColumns.NUMBER_OF_SONGS
                }, null, null, null);
        String songCount = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                songCount = cursor.getString(0);
            }
            cursor.close();
            cursor = null;
        }
        return songCount;
    }

    public static final String getReleaseDateForAlbum(final Context context, final long id) {
        if (id == -1) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri, new String[] {
                    AlbumColumns.FIRST_YEAR
                }, null, null, null);
        String releaseDate = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                releaseDate = cursor.getString(0);
            }
            cursor.close();
            cursor = null;
        }
        return releaseDate;
    }

    public static final String getFilePath() {
        try {
            if (mService != null) {
                return mService.getPath();
            }
        } catch (final RemoteException ignored) {
        }
        return null;
    }

    public static void moveQueueItem(final int from, final int to) {
        try {
            if (mService != null) {
                mService.moveQueueItem(from, to);
            } else {
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static void toggleFavorite() {
        try {
            if (mService != null) {
                mService.toggleFavorite();
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static final boolean isFavorite() {
        try {
            if (mService != null) {
                return mService.isFavorite();
            }
        } catch (final RemoteException ignored) {
        }
        return false;
    }

    public static final long[] getSongListForPlaylist(final Context context, final long playlistId) {
        final String[] projection = new String[] {
            MediaStore.Audio.Playlists.Members.AUDIO_ID
        };
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external",
                        Long.valueOf(playlistId)), projection, null, null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            final long[] list = getSongListForCursor(cursor);
            cursor.close();
            cursor = null;
            return list;
        }
        return sEmptyList;
    }

    public static void playPlaylist(final Context context, final long playlistId) {
        final long[] playlistList = getSongListForPlaylist(context, playlistId);
        if (playlistList != null) {
            playAll(context, playlistList, -1, false);
        }
    }

    public final static long[] getSongListForFavoritesCursor(Cursor cursor) {
        if (cursor == null) {
            return sEmptyList;
        }
        final int len = cursor.getCount();
        final long[] list = new long[len];
        cursor.moveToFirst();
        int colidx = -1;
        try {
            colidx = cursor.getColumnIndexOrThrow(FavoriteColumns.ID);
        } catch (final Exception ignored) {
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getLong(colidx);
            cursor.moveToNext();
        }
        cursor.close();
        cursor = null;
        return list;
    }

    public final static long[] getSongListForFavorites(final Context context) {
        Cursor cursor = FavoritesLoader.makeFavoritesCursor(context);
        if (cursor != null) {
            final long[] list = getSongListForFavoritesCursor(cursor);
            cursor.close();
            cursor = null;
            return list;
        }
        return sEmptyList;
    }

    public static void playFavorites(final Context context) {
        playAll(context, getSongListForFavorites(context), 0, false);
    }

    public static final long[] getSongListForLastAdded(final Context context) {
        final Cursor cursor = LastAddedLoader.makeLastAddedCursor(context);
        if (cursor != null) {
            final int count = cursor.getCount();
            final long[] list = new long[count];
            for (int i = 0; i < count; i++) {
                cursor.moveToNext();
                list[i] = cursor.getLong(0);
            }
            return list;
        }
        return sEmptyList;
    }

    public static void playLastAdded(final Context context) {
        playAll(context, getSongListForLastAdded(context), 0, false);
    }

    public static void makePlaylistMenu(final Context context, final int groupId,
            final SubMenu subMenu, final boolean showFavorites) {
        subMenu.clear();
        if (showFavorites) {
            subMenu.add(groupId, FragmentMenuItems.ADD_TO_FAVORITES, Menu.NONE,
                    R.string.add_to_favorites);
        }
        subMenu.add(groupId, FragmentMenuItems.NEW_PLAYLIST, Menu.NONE, R.string.new_playlist);
        Cursor cursor = PlaylistLoader.makePlaylistCursor(context);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                final Intent intent = new Intent();
                String name = cursor.getString(1);
                if (name != null) {
                    intent.putExtra("playlist", getIdForPlaylist(context, name));
                    subMenu.add(groupId, FragmentMenuItems.PLAYLIST_SELECTED, Menu.NONE,
                            name).setIntent(intent);
                }
                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

    public static void refresh() {
        try {
            if (mService != null) {
                mService.refresh();
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static final String getLastAlbumForArtist(final Context context, final String artistName) {
        return RecentStore.getInstance(context).getAlbumName(artistName);
    }

    public static void seek(final long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    public static final long position() {
        if (mService != null) {
            try {
                return mService.position();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static final long duration() {
        if (mService != null) {
            try {
                return mService.duration();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static void setQueuePosition(final int position) {
        if (mService != null) {
            try {
                mService.setQueuePosition(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    public static void clearQueue() {
        try {
            mService.removeTracks(0, Integer.MAX_VALUE);
        } catch (final RemoteException ignored) {
        }
    }

    public static void notifyForegroundStateChanged(final Context context, boolean inForeground) {
        int old = sForegroundActivities;
        if (inForeground) {
            sForegroundActivities++;
        } else {
            sForegroundActivities--;
        }

        if (old == 0 || sForegroundActivities == 0) {
            final Intent intent = new Intent(context, MusicPlaybackService.class);
            intent.setAction(MusicPlaybackService.FOREGROUND_STATE_CHANGED);
            intent.putExtra(MusicPlaybackService.NOW_IN_FOREGROUND, sForegroundActivities != 0);
            context.startService(intent);
        }
    }

    public static void deleteTracks(final Context context, final long[] list) {
        final String[] projection = new String[] {
                BaseColumns._ID, MediaColumns.DATA, AudioColumns.ALBUM_ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            selection.append(list[i]);
            if (i < list.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (c != null) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final long id = c.getLong(0);
                removeTrack(id);
                FavoritesStore.getInstance(context).removeItem(id);
                RecentStore.getInstance(context).removeItem(c.getLong(2));
                c.moveToNext();
            }

            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection.toString(), null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final String name = c.getString(1);
                final File f = new File(name);
                try {
                    if (!f.delete()) {
                        if(D) Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (final SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

        final String message = makeLabel(context, R.plurals.NNNtracksdeleted, list.length);

        Toast.makeText((Activity)context, message, Toast.LENGTH_SHORT).show();
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        refresh();
    }

    public static void playAllFromUserItemClick(final Context context,
            final ArrayAdapter<Song> adapter, final int position) {
        if (adapter.getViewTypeCount() > 1 && position == 0) {
            return;
        }
        final long[] list = MusicUtils.getSongListForAdapter(adapter);
        int pos = adapter.getViewTypeCount() > 1 ? position - 1 : position;
        if (list.length == 0) {
            pos = 0;
        }
        MusicUtils.playAll(context, list, pos, false);
    }

    private static final long[] getSongListForAdapter(ArrayAdapter<Song> adapter) {
        if (adapter == null) {
            return sEmptyList;
        }
        long[] list = {};
        if (adapter != null) {
            int count = adapter.getCount() - (adapter.getViewTypeCount() > 1 ? 1 : 0);
            list = new long[count];
            for (int i = 0; i < count; i++) {
                list[i] = ((Song) adapter.getItem(i)).mSongId;
            }
        }
        return list;
    }

    public static int getNumberOfBands(){
        try {
            return mService.getNumberOfBands();
        }catch (RemoteException ex){};
        return 0;
    }
    public static int getBandLevelRange(int level){
        try {
            return mService.getBandLevelRange(level);
        }catch (RemoteException ex){}
        return 0;
    }
    public static int getNumberOfPresets(){
        try {
            return mService.getNumberOfPresets();
        }catch (RemoteException ex){}
        return 0;
    }
    public static void enableEqualizer(){
        try {
            mService.enableEqualizer();
        }catch (RemoteException ex){}
    }
    public static void disableEqualizer(){
        try {
            mService.disableEqualizer();
        }catch (RemoteException ex){}
    }
    public static String getPresetName(int i){
        try {
            return mService.getPresetName(i);
        }catch (RemoteException ex){}
        return " ";
    }
    public static int getBandLevel(int band){
        try {
            return mService.getBandLevel(band);
        }catch (RemoteException ex){}
        return 0;
    }
    public static void setBandLeve(int band, int level){
        try {
            mService.setBandLeve(band, level);
        }catch (RemoteException ex){}
    }
    public static int getCurrentPreset(){
        try {
            return mService.getCurrentPreset();
        }catch (RemoteException ex){}
        return 0;
    }
    public static void usePreset(int preset){
        try {
            mService.usePreset(preset);
        }catch (RemoteException ex){}
    }

    public static int getCenterFreq(int band){
        try {
            return mService.getCenterFreq(band);
        }catch (RemoteException ex){}
        return 0;
    }

    public static void setBalance(float balance){
        try{
            mService.setBalance(balance);
        }catch (RemoteException ex){}
    }

    public static void setLeftChannelVolume(float vol){
        try{
            mService.setLeftChannelVolume(vol);
        }catch (RemoteException ex){}
    }

    public static void setRightChannelVolume(float vol){
        try{
            mService.setRightChannelVolume(vol);
        }catch (RemoteException ex){}
    }

    public static float getLeftVolumet(){
        try{
            return mService.getLeftVolumet();
        }catch (RemoteException ex){}
        return 0.0f;
    }

    public static float getRightVolumet(){
        try{
            return mService.getRightVoluemt();
        }catch (RemoteException ex){}
        return 0.0f;
    }

    public static void deletePreset(int position){
        try {
            mService.deletePreset(position);
        }catch (RemoteException ex){}
    }

    public static void updatePref(){
        try{
            mService.updatePref();
        }catch (RemoteException ex){}
    }

    public static void savePreset(String result){
        try{
            mService.savePreset(result);
        }catch (RemoteException ex){}
    }

    public static boolean isEqualizerEnabled(){
        try{
            return mService.isEqualizerEnabled();
        }catch (RemoteException ex){}
        return false;
    }

    public static void setEqualizerEnabled(boolean enabled){
        try{
            mService.setEqualizerEnabled(enabled);
        }catch (RemoteException ex){}
    }
    public static JSONObject getJSONSongsFromAlbum(Context context, String album_id) throws JSONException {
        Cursor tracks = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[] {"*"}, MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ALBUM_ID + " = " + album_id, null, PreferenceUtils.getInstance(context).getAlbumSongSortOrder());
        if(tracks == null || tracks.isClosed() || tracks.getCount() == 0) {
            if(tracks != null && !tracks.isClosed())
                tracks.close();
            JSONObject result = new JSONObject();
            result.put("data", new JSONObject().put("array", new JSONArray()));
            return result;
        }
        tracks.moveToFirst();
        JSONObject p1 = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < tracks.getCount(); i++){
            JSONObject val = new JSONObject();
            try{
                val.put("track_id", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media._ID)));
                val.put("track_title", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                val.put("track_artist", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                jsonArray.put(i,val);
                tracks.moveToNext();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(jsonArray.length() < 1){
            p1.put("array", new JSONArray());
        }else {
            p1.put("array", jsonArray);
        }
        if(tracks != null && !tracks.isClosed())
            tracks.close();
        JSONObject result = new JSONObject();
        result.put("data", p1);
        return result;
    }

    public static JSONObject getJSONSongsFromArtist(Context context, String artist_key) throws JSONException {
        Cursor tracks = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[] {"*"},MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ARTIST_KEY + " = '" + artist_key.replace("'","''") + "'", null, PreferenceUtils.getInstance(context).getArtistSongSortOrder());
        if(tracks == null || tracks.isClosed() || tracks.getCount() == 0) {
            if(tracks != null && !tracks.isClosed())
                tracks.close();
            JSONObject result = new JSONObject();
            result.put("data", new JSONObject().put("array", new JSONArray()));
            return result;
        }
        tracks.moveToFirst();
        JSONObject p1 = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < tracks.getCount(); i++){
            JSONObject val = new JSONObject();
            try{
                val.put("track_id", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media._ID)));
                val.put("track_title", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                val.put("track_artist", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                jsonArray.put(i,val);
                tracks.moveToNext();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(jsonArray.length() < 1){
            p1.put("array", new JSONArray());
        }else {
            p1.put("array", jsonArray);
        }
        if(tracks != null && !tracks.isClosed())
            tracks.close();
        JSONObject result = new JSONObject();
        result.put("data", p1);
        return result;
    }



    public static JSONObject getJSONSongsFromPlaylist(Context context, String playlistId) throws JSONException {
        Cursor tracks = PlaylistSongLoader.makePlaylistSongCursor(context, Long.parseLong(playlistId));
        if(tracks == null || tracks.isClosed() || tracks.getCount() == 0) {
            if(tracks != null && !tracks.isClosed())
                tracks.close();
            JSONObject result = new JSONObject();
            result.put("data", new JSONObject().put("array", new JSONArray()));
            return result;
        }
        tracks.moveToFirst();
        JSONObject p1 = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < tracks.getCount(); i++){
            JSONObject val = new JSONObject();
            try{
                val.put("track_id", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
                val.put("track_title", tracks.getString(tracks.getColumnIndex(AudioColumns.TITLE)));
                val.put("track_artist", tracks.getString(tracks.getColumnIndex(AudioColumns.ARTIST)));
                jsonArray.put(i,val);
                tracks.moveToNext();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(jsonArray.length() < 1){
            p1.put("array", new JSONArray());
        }else {
            p1.put("array", jsonArray);
        }
        if(tracks != null && !tracks.isClosed())
            tracks.close();
        JSONObject result = new JSONObject();
        result.put("data", p1);
        return result;
    }

    public static JSONObject getJSONTracks(Context context) throws JSONException {
        Cursor tracks = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[] {"*"}, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, PreferenceUtils.getInstance(context).getSongSortOrder());
        if(tracks == null || tracks.isClosed() || tracks.getCount() == 0) {
            if(tracks != null && !tracks.isClosed())
                tracks.close();
            JSONObject result = new JSONObject();
            result.put("data", new JSONObject().put("array", new JSONArray()));
            return result;
        }
        tracks.moveToFirst();
        JSONObject p1 = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < tracks.getCount(); i++){
            JSONObject val = new JSONObject();
            try{
                val.put("track_id", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media._ID)));
                val.put("track_title", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                val.put("track_artist", tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                jsonArray.put(i,val);
                tracks.moveToNext();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(jsonArray.length() < 1){
            p1.put("array", new JSONArray());
        }else {
            p1.put("array", jsonArray);
        }
        if(tracks != null && !tracks.isClosed())
            tracks.close();
        JSONObject result = new JSONObject();
        result.put("data", p1);
        return result;
    }

    public static JSONObject getJSONAlbums(Context context) throws JSONException {
        Cursor albums = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_KEY, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS, MediaStore.Audio.Albums.FIRST_YEAR},
                MediaStore.Audio.Albums._ID + " != 0 ", null, PreferenceUtils.getInstance(context).getAlbumSortOrder());
        if(albums == null || albums.isClosed() || albums.getCount() == 0) {
            if(albums != null && !albums.isClosed())
                albums.close();
            JSONObject result = new JSONObject();
            result.put("data", new JSONObject().put("array", new JSONArray()));
            return result;
        }
        albums.moveToFirst();
        JSONObject p1 = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < albums.getCount(); i++){
            JSONObject val = new JSONObject();
            try{
                val.put("album_id", albums.getString(albums.getColumnIndex(MediaStore.Audio.Albums._ID)));
                val.put("album_title", albums.getString(albums.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
                val.put("album_artist", albums.getString(albums.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
                jsonArray.put(i,val);
                albums.moveToNext();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(jsonArray.length() < 1){
            p1.put("array", new JSONArray());
        }else {
            p1.put("array", jsonArray);
        }
        if(albums != null && !albums.isClosed())
            albums.close();
        JSONObject result = new JSONObject();
        result.put("data", p1);
        return result;

    }

    public static JSONObject getJSONPlaylists(Context context) throws JSONException {
        Cursor playlists = context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, new String[]{"*"}, null, null, MediaStore.Audio.Playlists.NAME + " ASC");
        if(playlists == null || playlists.isClosed() || playlists.getCount() == 0) {
            if(playlists != null && !playlists.isClosed())
                playlists.close();
            JSONObject result = new JSONObject();
            result.put("data", new JSONObject().put("array", new JSONArray()));
            return result;
        }
        playlists.moveToFirst();
        JSONObject p1 = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < playlists.getCount(); i++){
            JSONObject val = new JSONObject();
            try{
                val.put("playlist_id", playlists.getString(playlists.getColumnIndex(MediaStore.Audio.Playlists._ID)));
                val.put("playlist_title", playlists.getString(playlists.getColumnIndex(MediaStore.Audio.Playlists.NAME)));
                jsonArray.put(i,val);
                playlists.moveToNext();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(jsonArray.length() < 1){
            p1.put("array", new JSONArray());
        }else {
            p1.put("array", jsonArray);
        }
        if(playlists != null && !playlists.isClosed())
            playlists.close();
        JSONObject result = new JSONObject();
        result.put("data", p1);
        return result;

    }

    public static JSONObject getJSONArtists(Context context) throws  JSONException {
        Cursor artists = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"DISTINCT " + MediaStore.Audio.Artists.ARTIST_KEY + " AS _id ", MediaStore.Audio.Artists.ARTIST}, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, MediaStore.Audio.Artists.ARTIST + " ASC");
        if(artists == null || artists.isClosed() || artists.getCount() == 0) {
            if(artists != null && !artists.isClosed())
                artists.close();
            JSONObject result = new JSONObject();
            result.put("data", new JSONObject().put("array", new JSONArray()));
            return result;
        }
        artists.moveToFirst();
        JSONObject p1 = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < artists.getCount(); i++){
            JSONObject val = new JSONObject();
            try{
                val.put("artist_key", artists.getString(artists.getColumnIndex("_id")));
                val.put("artist_title", artists.getString(artists.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
                jsonArray.put(i,val);
                artists.moveToNext();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(jsonArray.length() < 1){
            p1.put("array", new JSONArray());
        }else {
            p1.put("array", jsonArray);
        }
        if(artists != null && !artists.isClosed())
            artists.close();
        JSONObject result = new JSONObject();
        result.put("data", p1);
        return result;
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    public static void sendBitmapToWear(Bitmap bitmap, String _id, GoogleApiClient mGoogleApiClient){
        Asset asset = createAssetFromBitmap(bitmap);
        PutDataMapRequest dataMap = PutDataMapRequest.create("/image");
        dataMap.getDataMap().putAsset(_id, asset);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }
}
