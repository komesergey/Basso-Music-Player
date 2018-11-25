package com.basso.basso.ui.activities;

import static com.basso.basso.Config.MIME_TYPE;
import static com.basso.basso.utils.MusicUtils.mService;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.basso.basso.Config;
import basso.IBassoService;
import com.basso.basso.R;
import com.basso.basso.format.Capitalize;
import com.basso.basso.loaders.AsyncHandler;
import com.basso.basso.loaders.LastAddedLoader;
import com.basso.basso.loaders.SearchLoader;
import com.basso.basso.model.Song;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.MusicUtils.ServiceToken;

import java.util.ArrayList;
import java.util.List;

public class ShortcutActivity extends FragmentActivity implements ServiceConnection {

    public static final String OPEN_AUDIO_PLAYER = null;

    private ServiceToken mToken;

    private Intent mIntent;

    private long[] mList;

    private boolean mShouldShuffle;

    private String mVoiceQuery;

    private final ArrayList<Song> mSong = Lists.newArrayList();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        mToken = MusicUtils.bindToService(this, this);
        mIntent = getIntent();
        mVoiceQuery = Capitalize.capitalize(mIntent.getStringExtra(SearchManager.QUERY));
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        mService = IBassoService.Stub.asInterface(service);

        if (mIntent.getAction().equals(Config.PLAY_FROM_SEARCH)) {
            getSupportLoaderManager().initLoader(0, null, mSongAlbumArtistQuery);
        } else if (mService != null) {
            AsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    final String requestedMimeType = mIntent.getExtras().getString(MIME_TYPE);
                    if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(requestedMimeType)) {
                        mShouldShuffle = true;
                        mList = MusicUtils.getSongListForArtist(ShortcutActivity.this, getId());
                    } else
                    if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(requestedMimeType)) {
                        mShouldShuffle = true;
                        mList = MusicUtils.getSongListForAlbum(ShortcutActivity.this, getId());
                    } else
                    if (MediaStore.Audio.Genres.CONTENT_TYPE.equals(requestedMimeType)) {
                        mShouldShuffle = true;
                        mList = MusicUtils.getSongListForGenre(ShortcutActivity.this, getId());
                    } else
                    if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(requestedMimeType)) {
                        mShouldShuffle = false;
                        mList = MusicUtils.getSongListForPlaylist(ShortcutActivity.this, getId());
                    } else
                    if (getString(R.string.playlist_favorites).equals(requestedMimeType)) {
                        mShouldShuffle = false;
                        mList = MusicUtils.getSongListForFavorites(ShortcutActivity.this);
                    } else
                    if (getString(R.string.playlist_last_added).equals(requestedMimeType)) {
                        mShouldShuffle = false;
                        Cursor cursor = LastAddedLoader.makeLastAddedCursor(ShortcutActivity.this);
                        if (cursor != null) {
                            mList = MusicUtils.getSongListForCursor(cursor);
                            cursor.close();
                        }
                    }
                    allDone();
                }
            });
        }
    }


    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mService = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            MusicUtils.unbindFromService(mToken);
            mToken = null;
        }
    }

    private final LoaderCallbacks<List<Song>> mSongAlbumArtistQuery = new LoaderCallbacks<List<Song>>() {

        @Override
        public Loader<List<Song>> onCreateLoader(final int id, final Bundle args) {
            return new SearchLoader(ShortcutActivity.this, mVoiceQuery);
        }

        @Override
        public void onLoadFinished(final Loader<List<Song>> loader, final List<Song> data) {
            if (data.isEmpty()) {
                if (isFavorite()) {
                    MusicUtils.playFavorites(ShortcutActivity.this);
                }
                allDone();
                return;
            }

            mSong.clear();
            for (final Song song : data) {
                mSong.add(song);
            }
            final String song = mSong.get(0).mSongName;
            final String album = mSong.get(0).mAlbumName;
            final String artist = mSong.get(0).mArtistName;
            final long id = mSong.get(0).mSongId;
            if (mList == null && song != null) {
                mList = new long[] {
                    id
                };
            } else
            if (mList == null && album != null) {
                mList = MusicUtils.getSongListForAlbum(ShortcutActivity.this, id);
            } else
            if (mList == null && artist != null) {
                mList = MusicUtils.getSongListForArtist(ShortcutActivity.this, id);
            }
            allDone();
        }
        @Override
        public void onLoaderReset(final Loader<List<Song>> loader) {
            mSong.clear();
        }
    };
    private long getId() {
        return mIntent.getExtras().getLong(Config.ID);
    }

    private boolean isFavorite() {
        final String favoritePlaylist = getString(R.string.playlist_favorites);
        if (mVoiceQuery.equals(favoritePlaylist)) {
            return true;
        }
        final String favorite = getString(R.string.playlist_favorite);
        if (mVoiceQuery.equals(favorite)) {
            return true;
        }
        return false;
    }
    private void allDone() {
        final boolean shouldOpenAudioPlayer = mIntent.getBooleanExtra(OPEN_AUDIO_PLAYER, true);
        if (mList != null && mList.length > 0) {
            MusicUtils.playAll(this, mList, 0, mShouldShuffle);
        }
        if (shouldOpenAudioPlayer) {
            final Intent intent = new Intent(this, AudioPlayerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }
}
