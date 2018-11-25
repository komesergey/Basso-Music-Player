package com.basso.basso;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.basso.basso.utils.BassoUtils;

@SuppressLint("NewApi")
public class NotificationHelper {

    private static final int Basso_MUSIC_SERVICE = 1;

    private final NotificationManager mNotificationManager;

    private final MusicPlaybackService mService;

    private RemoteViews mNotificationTemplate;

    private Notification mNotification = null;

    private RemoteViews mExpandedView;

    public NotificationHelper(final MusicPlaybackService service) {
        mService = service;
        mNotificationManager = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification(final String albumName, final String artistName,
            final String trackName, final Long albumId, final Bitmap albumArt,
            final boolean isPlaying) {

        if(BassoUtils.hasLollipop()){
            mNotificationTemplate = new RemoteViews(mService.getPackageName(), R.layout.notification_template_base_lollipop);
        }else {
            mNotificationTemplate = new RemoteViews(mService.getPackageName(), R.layout.notification_template_base);
        }
        initCollapsedLayout(trackName, artistName, albumArt);
        mNotification = new NotificationCompat.Builder(mService)
                .setSmallIcon(R.drawable.stat_notify_music)
                .setContentIntent(getPendingIntent())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContent(mNotificationTemplate)
                .build();
        initPlaybackActions(isPlaying);
        if (BassoUtils.hasJellyBean()) {
            if(BassoUtils.hasLollipop()) {
                mExpandedView = new RemoteViews(mService.getPackageName(), R.layout.notification_template_expanded_base_lollipop);
            } else {
                mExpandedView = new RemoteViews(mService.getPackageName(), R.layout.notification_template_expanded_base);
            }
            mNotification.bigContentView = mExpandedView;
            initExpandedPlaybackActions(isPlaying);
            initExpandedLayout(trackName, albumName, artistName, albumArt);
        }
        mService.startForeground(Basso_MUSIC_SERVICE, mNotification);
    }

    public void killNotification() {
        mService.stopForeground(true);
        mNotification = null;
    }

    public void updatePlayState(final boolean isPlaying) {
        if (mNotification == null || mNotificationManager == null) {
            return;
        }
        if (mNotificationTemplate != null) {
            if(BassoUtils.hasLollipop()){
                mNotificationTemplate.setImageViewResource(R.id.notification_base_play, isPlaying ? R.drawable.btn_playback_pause_notif_lollipop : R.drawable.btn_playback_play_notif_lollipop);
            } else {
                mNotificationTemplate.setImageViewResource(R.id.notification_base_play, isPlaying ? R.drawable.btn_playback_pause_notif : R.drawable.btn_playback_play_notif);
            }
        }
        if (BassoUtils.hasJellyBean() && mExpandedView != null) {
            if(BassoUtils.hasLollipop()){
                mExpandedView.setImageViewResource(R.id.notification_expanded_base_play, isPlaying ? R.drawable.btn_playback_pause_notif_lollipop : R.drawable.btn_playback_play_notif_lollipop);
            }else {
                mExpandedView.setImageViewResource(R.id.notification_expanded_base_play, isPlaying ? R.drawable.btn_playback_pause_notif : R.drawable.btn_playback_play_notif);
            }
        }
        mNotificationManager.notify(Basso_MUSIC_SERVICE, mNotification);
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getActivity(mService, 0, new Intent("com.basso.basso.AUDIO_PLAYER")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
    }

    private void initExpandedPlaybackActions(boolean isPlaying) {
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_base_play, retreivePlaybackActions(1));
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_base_next, retreivePlaybackActions(2));
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_base_previous, retreivePlaybackActions(3));
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, retreivePlaybackActions(4));
        if(BassoUtils.hasLollipop()){
            mExpandedView.setImageViewResource(R.id.notification_expanded_base_play, isPlaying ? R.drawable.btn_playback_pause_notif_lollipop : R.drawable.btn_playback_play_notif_lollipop);
        } else {
            mExpandedView.setImageViewResource(R.id.notification_expanded_base_play, isPlaying ? R.drawable.btn_playback_pause_notif : R.drawable.btn_playback_play_notif);
        }
    }

    private void initPlaybackActions(boolean isPlaying) {
        mNotificationTemplate.setOnClickPendingIntent(R.id.notification_base_play, retreivePlaybackActions(1));
        mNotificationTemplate.setOnClickPendingIntent(R.id.notification_base_next, retreivePlaybackActions(2));
        mNotificationTemplate.setOnClickPendingIntent(R.id.notification_base_previous, retreivePlaybackActions(3));
        mNotificationTemplate.setOnClickPendingIntent(R.id.notification_base_collapse, retreivePlaybackActions(4));
        if(BassoUtils.hasLollipop()){
            mNotificationTemplate.setImageViewResource(R.id.notification_base_play, isPlaying ? R.drawable.btn_playback_pause_notif_lollipop : R.drawable.btn_playback_play_notif_lollipop);
        } else {
            mNotificationTemplate.setImageViewResource(R.id.notification_base_play, isPlaying ? R.drawable.btn_playback_pause_notif : R.drawable.btn_playback_play_notif);
        }
    }

    private final PendingIntent retreivePlaybackActions(final int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(mService, MusicPlaybackService.class);
        switch (which) {
            case 1:
                action = new Intent(MusicPlaybackService.TOGGLEPAUSE_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 1, action, 0);
                return pendingIntent;
            case 2:
                action = new Intent(MusicPlaybackService.NEXT_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 2, action, 0);
                return pendingIntent;
            case 3:
                action = new Intent(MusicPlaybackService.PREVIOUS_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 3, action, 0);
                return pendingIntent;
            case 4:
                action = new Intent(MusicPlaybackService.STOP_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 4, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }

    private void initCollapsedLayout(final String trackName, final String artistName, final Bitmap albumArt) {
        mNotificationTemplate.setTextViewText(R.id.notification_base_line_one, trackName);
        mNotificationTemplate.setTextViewText(R.id.notification_base_line_two, artistName);
        mNotificationTemplate.setImageViewBitmap(R.id.notification_base_image, albumArt);
    }

    private void initExpandedLayout(final String trackName, final String artistName, final String albumName, final Bitmap albumArt) {
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_one, trackName);
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_two, albumName);
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_three, artistName);
        mExpandedView.setImageViewBitmap(R.id.notification_expanded_base_image, albumArt);
    }
}
