package com.basso.basso.appwidgets;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.widget.RemoteViews;

import com.basso.basso.Config;
import com.basso.basso.MusicPlaybackService;
import com.basso.basso.R;
import com.basso.basso.ui.activities.AudioPlayerActivity;
import com.basso.basso.ui.activities.HomeActivity;
import com.basso.basso.ui.activities.ProfileActivity;
import com.basso.basso.ui.activities.ShortcutActivity;
import com.basso.basso.utils.MusicUtils;

@TargetApi(11)
public class RecentWidgetProvider extends AppWidgetBase {

    public static final String SET_ACTION = "set_action";

    public static final String OPEN_PROFILE = "open_profile";

    public static final String PLAY_ALBUM = "play_album";

    public static final String CMDAPPWIDGETUPDATE = "app_widget_recents_update";

    public static final String CLICK_ACTION = "com.basso.basso.recents.appwidget.action.CLICK";

    public static final String REFRESH_ACTION = "com.basso.basso.recents.appwidget.action.REFRESH";

    private static Handler sWorkerQueue;

    private static RecentWidgetProvider mInstance;

    private RemoteViews mViews;

    public RecentWidgetProvider() {
        final HandlerThread workerThread = new HandlerThread("RecentWidgetProviderWorker", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        workerThread.start();
        sWorkerQueue = new Handler(workerThread.getLooper());
    }

    public static synchronized RecentWidgetProvider getInstance() {
        if (mInstance == null) {
            mInstance = new RecentWidgetProvider();
        }
        return mInstance;
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
            final int[] appWidgetIds) {
        for (final int appWidgetId : appWidgetIds) {
            mViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_recents);
            linkButtons(context, mViews, false);
            final Intent intent = new Intent(context, RecentWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            compatSetRemoteAdapter(mViews, appWidgetId, intent);
            final Intent updateIntent = new Intent(MusicPlaybackService.SERVICECMD);
            updateIntent.putExtra(MusicPlaybackService.CMDNAME, RecentWidgetProvider.CMDAPPWIDGETUPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            updateIntent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            context.sendBroadcast(updateIntent);
            final Intent onClickIntent = new Intent(context, RecentWidgetProvider.class);
            onClickIntent.setAction(RecentWidgetProvider.CLICK_ACTION);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mViews.setPendingIntentTemplate(R.id.app_widget_recents_list, onClickPendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, mViews);
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();

        if (CLICK_ACTION.equals(action)) {
            final long albumId = intent.getLongExtra(Config.ID, -1);

            if (intent.getStringExtra(SET_ACTION).equals(PLAY_ALBUM)) {
                if (albumId != -1) {
                    final Intent shortcutIntent = new Intent(context, ShortcutActivity.class);
                    shortcutIntent.setAction(Intent.ACTION_VIEW);
                    shortcutIntent.putExtra(Config.ID, albumId);
                    shortcutIntent.putExtra(Config.MIME_TYPE, MediaStore.Audio.Albums.CONTENT_TYPE);
                    shortcutIntent.putExtra(ShortcutActivity.OPEN_AUDIO_PLAYER, false);
                    shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(shortcutIntent);
                }
            } else if (intent.getStringExtra(SET_ACTION).equals(OPEN_PROFILE)) {
                final String albumName = intent.getStringExtra(Config.NAME);
                final Bundle bundle = new Bundle();
                bundle.putString(Config.MIME_TYPE, MediaStore.Audio.Albums.CONTENT_TYPE);
                bundle.putString(Config.NAME, albumName);
                bundle.putString(Config.ARTIST_NAME, intent.getStringExtra(Config.ARTIST_NAME));
                bundle.putString(Config.ALBUM_YEAR,
                        MusicUtils.getReleaseDateForAlbum(context, albumId));
                bundle.putLong(Config.ID, albumId);

                final Intent profileIntent = new Intent(context, ProfileActivity.class);
                profileIntent.putExtras(bundle);
                profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (albumId != -1) {
                    context.startActivity(profileIntent);
                }
            }

        }
        super.onReceive(context, intent);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void compatSetRemoteAdapter(final RemoteViews rv, final int appWidgetId,
            final Intent intent) {
        rv.setRemoteAdapter(R.id.app_widget_recents_list, intent);
    }

    private boolean hasInstances(final Context context) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this
                .getClass()));
        return appWidgetIds.length > 0;
    }

    private void pushUpdate(final Context context, final int[] appWidgetIds, final RemoteViews views) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }
    }

    public void notifyChange(final MusicPlaybackService service, final String what) {
        if (hasInstances(service)) {
            if (MusicPlaybackService.PLAYSTATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            } else if (MusicPlaybackService.META_CHANGED.equals(what)) {
                synchronized (service) {
                    sWorkerQueue.post(new Runnable() {
                        @Override
                        public void run() {
                            final AppWidgetManager appWidgetManager = AppWidgetManager
                                    .getInstance(service);
                            final ComponentName componentName = new ComponentName(service,
                                    RecentWidgetProvider.class);
                            appWidgetManager.notifyAppWidgetViewDataChanged(
                                    appWidgetManager.getAppWidgetIds(componentName),
                                    R.id.app_widget_recents_list);
                        }
                    });
                }
            }
        }
    }

    public void performUpdate(final MusicPlaybackService service, final int[] appWidgetIds) {
        mViews = new RemoteViews(service.getPackageName(), R.layout.app_widget_recents);

        final boolean isPlaying = service.isPlaying();
        if (isPlaying) {
            mViews.setImageViewResource(R.id.app_widget_recents_play, R.drawable.btn_playback_pause);
        } else {
            mViews.setImageViewResource(R.id.app_widget_recents_play, R.drawable.btn_playback_play);
        }

        linkButtons(service, mViews, isPlaying);
        pushUpdate(service, appWidgetIds, mViews);
    }


    private void linkButtons(final Context context, final RemoteViews views, final boolean playerActive) {
        Intent action;
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(context, MusicPlaybackService.class);

        if (playerActive) {
            action = new Intent(context, AudioPlayerActivity.class);
            pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
            views.setOnClickPendingIntent(R.id.app_widget_recents_action_bar, pendingIntent);
        } else {
            action = new Intent(context, HomeActivity.class);
            pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
            views.setOnClickPendingIntent(R.id.app_widget_recents_action_bar, pendingIntent);
        }

        pendingIntent = buildPendingIntent(context, MusicPlaybackService.PREVIOUS_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_recents_previous, pendingIntent);

        pendingIntent = buildPendingIntent(context, MusicPlaybackService.TOGGLEPAUSE_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_recents_play, pendingIntent);

        pendingIntent = buildPendingIntent(context, MusicPlaybackService.NEXT_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.app_widget_recents_next, pendingIntent);
    }
}
