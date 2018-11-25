package com.basso.basso.utils;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.ui.activities.AudioPlayerActivity;
import com.basso.basso.ui.activities.HomeActivity;
import com.basso.basso.ui.activities.ProfileActivity;
import com.basso.basso.ui.activities.SearchActivity;
import com.basso.basso.ui.activities.SettingsActivity;

public final class NavUtils {

    public static void openArtistProfile(final Activity context, final String artistName) {

        final Bundle bundle = new Bundle();
        bundle.putLong(Config.ID, MusicUtils.getIdForArtist(context, artistName));
        bundle.putString(Config.MIME_TYPE, MediaStore.Audio.Artists.CONTENT_TYPE);
        bundle.putString(Config.ARTIST_NAME, artistName);
        final Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void openAlbumProfile(final Activity context, final String albumName, final String artistName, final long albumId) {
        final Bundle bundle = new Bundle();
        bundle.putString(Config.ALBUM_YEAR, MusicUtils.getReleaseDateForAlbum(context, albumId));
        bundle.putString(Config.ARTIST_NAME, artistName);
        bundle.putString(Config.MIME_TYPE, MediaStore.Audio.Albums.CONTENT_TYPE);
        bundle.putLong(Config.ID, albumId);
        bundle.putString(Config.NAME, albumName);
        final Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void openEffectsPanel(final Activity context) {
        try {
            final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicUtils.getAudioSessionId());
            context.startActivity(effects);
        } catch (final ActivityNotFoundException notFound) {
            Toast.makeText(context, context.getString(R.string.no_effects_for_you),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void openSettings(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }

    public static void openAudioPlayer(final Activity activity) {
        final Intent intent = new Intent(activity, AudioPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void openSearch(final Activity activity, final String query) {
        final Bundle bundle = new Bundle();
        final Intent intent = new Intent(activity, SearchActivity.class);
        intent.putExtra(SearchManager.QUERY, query);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public static void goHome(final Activity activity) {
        final Intent intent = new Intent(activity, AudioPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}