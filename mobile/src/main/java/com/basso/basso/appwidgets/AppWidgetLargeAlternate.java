package com.basso.basso.appwidgets;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.basso.basso.MusicPlaybackService;
import com.basso.basso.R;
import com.basso.basso.ui.activities.AudioPlayerActivity;
import com.basso.basso.ui.activities.HomeActivity;
import com.basso.basso.utils.BassoUtils;

@SuppressLint("NewApi")
public class AppWidgetLargeAlternate extends AppWidgetBase {

    public static final String CMDAPPWIDGETUPDATE = "app_widget_large_alternate_update";

    private static AppWidgetLargeAlternate mInstance;

    public static synchronized AppWidgetLargeAlternate getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetLargeAlternate();
        }
        return mInstance;
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
            final int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        final Intent updateIntent = new Intent(MusicPlaybackService.SERVICECMD);
        updateIntent.putExtra(MusicPlaybackService.CMDNAME, AppWidgetLargeAlternate.CMDAPPWIDGETUPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    private void defaultAppWidget(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_large_alternate);
        linkButtons(context, appWidgetViews, false);
        pushUpdate(context, appWidgetIds, appWidgetViews);
    }

    private void pushUpdate(final Context context, final int[] appWidgetIds, final RemoteViews views) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, getClass()), views);
        }
    }

    private boolean hasInstances(final Context context) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] mAppWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                getClass()));
        return mAppWidgetIds.length > 0;
    }

    public void notifyChange(final MusicPlaybackService service, final String what) {
        if (hasInstances(service)) {
            if (MusicPlaybackService.META_CHANGED.equals(what)
                    || MusicPlaybackService.PLAYSTATE_CHANGED.equals(what)
                    || MusicPlaybackService.REPEATMODE_CHANGED.equals(what)
                    || MusicPlaybackService.SHUFFLEMODE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }

    public void performUpdate(final MusicPlaybackService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_large_alternate);

        final CharSequence trackName = service.getTrackName();
        final CharSequence artistName = service.getArtistName();
        final CharSequence albumName = service.getAlbumName();
        final Bitmap bitmap = service.getAlbumArt();

        appWidgetView.setTextViewText(R.id.app_widget_large_alternate_line_one, trackName);
        appWidgetView.setTextViewText(R.id.app_widget_large_alternate_line_two, artistName);
        appWidgetView.setTextViewText(R.id.app_widget_large_alternate_line_three, albumName);
        appWidgetView.setImageViewBitmap(R.id.app_widget_large_alternate_image, bitmap);

        final boolean isPlaying = service.isPlaying();
        if (isPlaying) {
            appWidgetView.setImageViewResource(R.id.app_widget_large_alternate_play, R.drawable.btn_playback_pause);
            if (BassoUtils.hasJellyBean()) {
                appWidgetView.setContentDescription(R.id.app_widget_large_alternate_play, service.getString(R.string.accessibility_pause));
            }
        } else {
            appWidgetView.setImageViewResource(R.id.app_widget_large_alternate_play, R.drawable.btn_playback_play);
            if (BassoUtils.hasJellyBean()) {
                appWidgetView.setContentDescription(R.id.app_widget_large_alternate_play, service.getString(R.string.accessibility_play));
            }
        }

        switch (service.getRepeatMode()) {
            case MusicPlaybackService.REPEAT_ALL:
                appWidgetView.setImageViewResource(R.id.app_widget_large_alternate_repeat, R.drawable.btn_playback_repeat_all);
                break;
            case MusicPlaybackService.REPEAT_CURRENT:
                appWidgetView.setImageViewResource(R.id.app_widget_large_alternate_repeat, R.drawable.btn_playback_repeat_one);
                break;
            default:
                appWidgetView.setImageViewResource(R.id.app_widget_large_alternate_repeat, R.drawable.btn_playback_repeat);
                break;
        }

        switch (service.getShuffleMode()) {
            case MusicPlaybackService.SHUFFLE_NONE:
                appWidgetView.setImageViewResource(R.id.app_widget_large_alternate_shuffle, R.drawable.btn_playback_shuffle);
                break;
            case MusicPlaybackService.SHUFFLE_AUTO:
                appWidgetView.setImageViewResource(R.id.app_widget_large_alternate_shuffle, R.drawable.btn_playback_shuffle_all);
                break;
            default:
                appWidgetView.setImageViewResource(R.id.app_widget_large_alternate_shuffle, R.drawable.btn_playback_shuffle_all);
                break;
        }

        linkButtons(service, appWidgetView, isPlaying);

        pushUpdate(service, appWidgetIds, appWidgetView);
    }

    private void linkButtons(final Context context, final RemoteViews views, final boolean playerActive) {
        Intent action;
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(context, MusicPlaybackService.class);

        if (playerActive) {
            action = new Intent(context, AudioPlayerActivity.class);
            pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
            views.setOnClickPendingIntent(R.id.app_widget_large_alternate_info_container, pendingIntent);
            views.setOnClickPendingIntent(R.id.app_widget_large_alternate_image, pendingIntent);
        } else {
            action = new Intent(context, HomeActivity.class);
            pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
            views.setOnClickPendingIntent(R.id.app_widget_large_alternate_info_container, pendingIntent);
            views.setOnClickPendingIntent(R.id.app_widget_large_alternate_image, pendingIntent);
        }
        pendingIntent = buildPendingIntent(context, MusicPlaybackService.SHUFFLE_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_large_alternate_shuffle, pendingIntent);

        pendingIntent = buildPendingIntent(context, MusicPlaybackService.PREVIOUS_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_large_alternate_previous, pendingIntent);

        pendingIntent = buildPendingIntent(context, MusicPlaybackService.TOGGLEPAUSE_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_large_alternate_play, pendingIntent);

        pendingIntent = buildPendingIntent(context, MusicPlaybackService.NEXT_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_large_alternate_next, pendingIntent);

        pendingIntent = buildPendingIntent(context, MusicPlaybackService.REPEAT_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_large_alternate_repeat, pendingIntent);
    }

}
