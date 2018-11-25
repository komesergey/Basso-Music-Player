package com.basso.basso.appwidgets;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.basso.basso.MusicPlaybackService;
import com.basso.basso.R;
import com.basso.basso.ui.activities.AudioPlayerActivity;
import com.basso.basso.ui.activities.HomeActivity;
import com.basso.basso.utils.BassoUtils;

@SuppressLint("NewApi")
public class AppWidgetSmall extends AppWidgetBase {

    public static final String CMDAPPWIDGETUPDATE = "app_widget_small_update";

    private static AppWidgetSmall mInstance;

    public static synchronized AppWidgetSmall getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetSmall();
        }
        return mInstance;
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
            final int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        final Intent updateIntent = new Intent(MusicPlaybackService.SERVICECMD);
        updateIntent.putExtra(MusicPlaybackService.CMDNAME, AppWidgetSmall.CMDAPPWIDGETUPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    private void defaultAppWidget(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetViews = new RemoteViews(context.getPackageName(),
                R.layout.app_widget_small);
        appWidgetViews.setViewVisibility(R.id.app_widget_small_info_container, View.INVISIBLE);
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
                    || MusicPlaybackService.PLAYSTATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }

    public void performUpdate(final MusicPlaybackService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_small);
        final CharSequence trackName = service.getTrackName();
        final CharSequence artistName = service.getArtistName();
        final Bitmap bitmap = service.getAlbumArt();

        if (TextUtils.isEmpty(trackName) && TextUtils.isEmpty(artistName)) {
            appWidgetView.setViewVisibility(R.id.app_widget_small_info_container, View.INVISIBLE);
        } else {
            appWidgetView.setViewVisibility(R.id.app_widget_small_info_container, View.VISIBLE);
            appWidgetView.setTextViewText(R.id.app_widget_small_line_one, trackName);
            appWidgetView.setTextViewText(R.id.app_widget_small_line_two, artistName);
        }
        appWidgetView.setImageViewBitmap(R.id.app_widget_small_image, bitmap);
        final boolean isPlaying = service.isPlaying();
        if (isPlaying) {
            appWidgetView.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_pause);
            if (BassoUtils.hasJellyBean()) {
                appWidgetView.setContentDescription(R.id.app_widget_small_play, service.getString(R.string.accessibility_pause));
            }
        } else {
            appWidgetView.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_play);
            if (BassoUtils.hasJellyBean()) {
                appWidgetView.setContentDescription(R.id.app_widget_small_play, service.getString(R.string.accessibility_play));
            }
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
            views.setOnClickPendingIntent(R.id.app_widget_small_info_container, pendingIntent);
            views.setOnClickPendingIntent(R.id.app_widget_small_image, pendingIntent);
        } else {
            action = new Intent(context, HomeActivity.class);
            pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
            views.setOnClickPendingIntent(R.id.app_widget_small_info_container, pendingIntent);
            views.setOnClickPendingIntent(R.id.app_widget_small_image, pendingIntent);
        }

        pendingIntent = buildPendingIntent(context, MusicPlaybackService.PREVIOUS_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_small_previous, pendingIntent);
        pendingIntent = buildPendingIntent(context, MusicPlaybackService.TOGGLEPAUSE_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_small_play, pendingIntent);
        pendingIntent = buildPendingIntent(context, MusicPlaybackService.NEXT_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_small_next, pendingIntent);
    }
}
