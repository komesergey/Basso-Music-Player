package com.basso.basso.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.basso.basso.R;
import com.basso.basso.ui.fragments.AlbumFragment;
import com.basso.basso.ui.fragments.ArtistFragment;
import com.basso.basso.ui.fragments.SongFragment;
import com.basso.basso.ui.fragments.phone.MusicBrowserPhoneFragment;
import com.basso.basso.ui.fragments.profile.AlbumSongFragment;
import com.basso.basso.ui.fragments.profile.ArtistAlbumFragment;
import com.basso.basso.ui.fragments.profile.ArtistSongFragment;

public final class PreferenceUtils {

    public static final int DEFFAULT_PAGE = 2;

    public static final String START_PAGE = "start_page";

    public static final String ARTIST_SORT_ORDER = "artist_sort_order";

    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";

    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";

    public static final String ALBUM_SORT_ORDER = "album_sort_order";

    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";

    public static final String SONG_SORT_ORDER = "song_sort_order";

    public static final String ARTIST_LAYOUT = "artist_layout";

    public static final String ALBUM_LAYOUT = "album_layout";

    public static final String RECENT_LAYOUT = "recent_layout";

    public static final String ONLY_ON_WIFI = "only_on_wifi";

    public static final String DOWNLOAD_MISSING_ARTWORK = "download_missing_artwork";

    public static final String DOWNLOAD_MISSING_ARTIST_IMAGES = "download_missing_artist_images";

    public static final String DEFAULT_THEME_COLOR = "default_theme_color";

    private static PreferenceUtils sInstance;

    private final SharedPreferences mPreferences;

    public PreferenceUtils(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final PreferenceUtils getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtils(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setStartPage(final int value) {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putInt(START_PAGE, value);
                editor.apply();

                return null;
            }
        }, (Void[]) null);
    }

    public final int getStartPage() {
        return mPreferences.getInt(START_PAGE, DEFFAULT_PAGE);
    }

    public void setDefaultThemeColor(final int value) {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putInt(DEFAULT_THEME_COLOR, value);
                editor.apply();

                return null;
            }
        }, (Void[]) null);
    }

    public final int getDefaultThemeColor(final Context context) {
        return mPreferences.getInt(DEFAULT_THEME_COLOR,
                context.getResources().getColor(R.color.holo_blue_light));
    }

    public final boolean onlyOnWifi() {
        return mPreferences.getBoolean(ONLY_ON_WIFI, true);
    }

    public final boolean downloadMissingArtwork() {
        return mPreferences.getBoolean(DOWNLOAD_MISSING_ARTWORK, true);
    }

    public final boolean downloadMissingArtistImages() {
        return mPreferences.getBoolean(DOWNLOAD_MISSING_ARTIST_IMAGES, true);
    }

    private void setSortOrder(final String key, final String value) {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(key, value);
                editor.apply();

                return null;
            }
        }, (Void[]) null);
    }

    public void setArtistSortOrder(final String value) {
        setSortOrder(ARTIST_SORT_ORDER, value);
    }

    public final String getArtistSortOrder() {
        final String defaultSortKey = SortOrder.ArtistSortOrder.ARTIST_A_Z;
        String key = mPreferences.getString(ARTIST_SORT_ORDER, defaultSortKey);
        if (key.equals(SortOrder.ArtistSongSortOrder.SONG_FILENAME)) {
            key = defaultSortKey;
        }
        return key;
    }

    public void setArtistSongSortOrder(final String value) {
        setSortOrder(ARTIST_SONG_SORT_ORDER, value);
    }

    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

    public void setArtistAlbumSortOrder(final String value) {
        setSortOrder(ARTIST_ALBUM_SORT_ORDER, value);
    }

    public final String getArtistAlbumSortOrder() {
        return mPreferences.getString(ARTIST_ALBUM_SORT_ORDER,
                SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSortOrder(final String value) {
        setSortOrder(ALBUM_SORT_ORDER, value);
    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSongSortOrder(final String value) {
        setSortOrder(ALBUM_SONG_SORT_ORDER, value);
    }

    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

    public void setSongSortOrder(final String value) {
        setSortOrder(SONG_SORT_ORDER, value);
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    private void setLayoutType(final String key, final String value) {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(key, value);
                editor.apply();

                return null;
            }
        }, (Void[]) null);
    }

    public void setArtistLayout(final String value) {
        setLayoutType(ARTIST_LAYOUT, value);
    }

    public void setAlbumLayout(final String value) {
        setLayoutType(ALBUM_LAYOUT, value);
    }

    public void setRecentLayout(final String value) {
        setLayoutType(RECENT_LAYOUT, value);
    }

    public boolean isSimpleLayout(final String which, final Context context) {
        final String simple = "simple";
        final String defaultValue = "grid";
        return mPreferences.getString(which, defaultValue).equals(simple);
    }

    public boolean isDetailedLayout(final String which, final Context context) {
        final String detailed = "detailed";
        final String defaultValue = "grid";
        return mPreferences.getString(which, defaultValue).equals(detailed);
    }

    public boolean isGridLayout(final String which, final Context context) {
        final String grid = "grid";
        final String defaultValue = "simple";
        return mPreferences.getString(which, defaultValue).equals(grid);
    }
}
