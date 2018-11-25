package com.basso.basso;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;


public class DataLayerListenerService extends WearableListenerService {
    public static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";
    public static WeakReference<WearActivity> wearActivityWeakReference = null;
    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        NotificationManagerCompat.from(this).cancel(0);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("tracksUpdated", false);
        edit.putBoolean("albumsUpdated",false);
        edit.putBoolean("artistsUpdated", false);
        edit.putBoolean("playlistUpdated", false);
        edit.putBoolean("commonUpdated",false);
        edit.putBoolean("currentUpdated",false);
        edit.commit();
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if ("/update_current".equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                    String trackName = dataMapItem.getDataMap().getString("/track_title");
                    String artistName = dataMapItem.getDataMap().getString("/track_artist");
                    String albumName = dataMapItem.getDataMap().getString("/track_album");
                    Asset trackCover = dataMapItem.getDataMap().getAsset("/track_cover");

                    GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(Wearable.API).build();

                    ConnectionResult result = googleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);

                    if (result.isSuccess()) {
                        if(trackCover != null) {
                            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                                    googleApiClient, trackCover).await().getInputStream();
                            googleApiClient.disconnect();
                            if (assetInputStream != null) {
                                Bitmap bitmap = BitmapFactory.decodeStream(assetInputStream);
                                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                                File directory = cw.getDir("cover", Context.MODE_PRIVATE);
                                File mypath = new File(directory, "cover.png");

                                FileOutputStream out = null;
                                try {
                                    out = new FileOutputStream(mypath);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    out.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (out != null) {
                                            out.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putBoolean("currentUpdated", true);
                    edit.putString("trackName", trackName);
                    edit.putString("artistName",artistName);
                    edit.putString("albumName", albumName);
                    edit.commit();
                    if(wearActivityWeakReference != null) wearActivityWeakReference.get().updateCurrentInfo();
                    showMainNotification();
                }
            }
        }
    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/tracks")) {
            final String message = new String(messageEvent.getData());

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("tracks", message);
            edit.putBoolean("tracksUpdated", true);
            edit.commit();
            showMainNotification();

        } else if(messageEvent.getPath().equals("/albums")) {
            final String message = new String(messageEvent.getData());

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("albums", message);
            edit.putBoolean("albumsUpdated", true);
            edit.commit();
            showMainNotification();


        } else if(messageEvent.getPath().equals("/artists")) {
            final String message = new String(messageEvent.getData());

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("artists", message);
            edit.putBoolean("artistsUpdated", true);
            edit.commit();
            showMainNotification();


        } else if(messageEvent.getPath().equals("/playlists")) {
            final String message = new String(messageEvent.getData());

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("playlists", message);
            edit.putBoolean("playlistUpdated", true);
            edit.commit();
            showMainNotification();

        } else if(messageEvent.getPath().equals("/common")) {
            final String message = new String(messageEvent.getData());

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("common", message).apply();
            edit.putBoolean("commonUpdated", true);
            edit.commit();

        } else if(messageEvent.getPath().equals("/error")) {
            showErrorNotification();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = pref.edit();
            edit.putBoolean("tracksUpdated", false);
            edit.putBoolean("albumsUpdated",false);
            edit.putBoolean("artistsUpdated", false);
            edit.putBoolean("playlistUpdated", false);
            edit.putBoolean("commonUpdated",false);
            edit.putBoolean("currentUpdated",false);
            edit.commit();
        } else if(messageEvent.getPath().equals("/dismissNotification")){
            NotificationManagerCompat.from(this).cancel(0);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = pref.edit();
            edit.putBoolean("tracksUpdated", false);
            edit.putBoolean("albumsUpdated",false);
            edit.putBoolean("artistsUpdated", false);
            edit.putBoolean("playlistUpdated", false);
            edit.putBoolean("commonUpdated",false);
            edit.putBoolean("currentUpdated",false);
            edit.commit();
        } else if(messageEvent.getPath().equals("/playstateData")) {
            final String message = new String(messageEvent.getData());
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("playstate", message);
            edit.commit();
        } else
            super.onMessageReceived(messageEvent);
    }

   public void showErrorNotification(){
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Connection error")
                .setContentText("Main application not started")
                .build();
        NotificationManagerCompat.from(this).notify(0, notification);
       SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       SharedPreferences.Editor edit = pref.edit();
       edit.putBoolean("tracksUpdated", false);
       edit.putBoolean("albumsUpdated",false);
       edit.putBoolean("artistsUpdated", false);
       edit.putBoolean("playlistUpdated", false);
       edit.putBoolean("commonUpdated",false);
       edit.putBoolean("currentUpdated",false);
       edit.commit();
    }

    private void showMainNotification() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean tracksUpdated = pref.getBoolean("tracksUpdated", false);
        boolean artistsUpdated = pref.getBoolean("artistsUpdated", false);
        boolean albumsUpdated = pref.getBoolean("albumsUpdated", false);
        boolean playlistUpdated = pref.getBoolean("playlistUpdated", false);
        boolean currentUpdated = pref.getBoolean("currentUpdated", false);
        boolean commonUpdated = pref.getBoolean("commonUpdated", false);
        if (tracksUpdated && artistsUpdated && albumsUpdated && playlistUpdated && currentUpdated) {

            String trackName = pref.getString("trackName", getString(R.string.choose_song_to_play));
            String artistName = pref.getString("artistName", "");
            String albumName = pref.getString("albumName", "");
            Bitmap cover = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("cover", Context.MODE_PRIVATE);
                cover = BitmapFactory.decodeStream(new FileInputStream(new File(directory, "cover.png")), null, options);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(cover == null) {
                cover = BitmapFactory.decodeResource(getResources(), R.drawable.no_art_ghost_album);
                cover = Bitmap.createScaledBitmap(cover, 400, 400, true);
            }
            cover = drawTextToBitmap(getApplicationContext(), cover, artistName,trackName, albumName);

            Intent mainIntent = new Intent(this, WearActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, mainIntent, 0);

            Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
            PendingIntent pendintDeleteIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, deleteIntent, 0);


            Intent notificationIntent = new Intent(this, NotificationActivity.class);
            notificationIntent.putExtra("track_title", trackName);
            notificationIntent.putExtra("track_artist", artistName);
            notificationIntent.putExtra("track_album", albumName);
            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (trackName.length() > 14) {
                trackName = trackName.substring(0, 14);
                trackName = trackName + "...";
            }
            if (artistName.length() > 14) {
                artistName = artistName.substring(0, 14);
                artistName = artistName + "...";
            }
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(false)
                    .setLocalOnly(true)
                    .setContentTitle(trackName)
                    .setSmallIcon(R.drawable.ic_action_play)
                    .setDeleteIntent(pendintDeleteIntent)
                    .setContentText(artistName)
                    .setContentIntent(pendingIntent)
                    .extend(new Notification.WearableExtender()
                            .setBackground(cover)
                            .setHintHideIcon(true)
                            .setDisplayIntent(pendingNotificationIntent)
                            .setCustomSizePreset(Notification.WearableExtender.SIZE_SMALL));

            NotificationManagerCompat.from(this).notify(0, builder.build());
            SharedPreferences.Editor edit = pref.edit();
            edit.putBoolean("tracksUpdated", false);
            edit.putBoolean("albumsUpdated",false);
            edit.putBoolean("artistsUpdated", false);
            edit.putBoolean("playlistUpdated", false);
            edit.putBoolean("commonUpdated",false);
            edit.putBoolean("currentUpdated",false);
            edit.commit();
        }
    }
    public static Bitmap drawTextToBitmap(Context gContext, Bitmap bitmap, String artistName, String trackName, String albumName) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;


        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paintRect = new Paint();
        paintRect.setColor(Color.WHITE);
        paintRect.setAlpha(170);
        canvas.drawRect(0.0f, canvas.getHeight()/2 - (int)(50*scale), canvas.getWidth(), canvas.getHeight()/2 + (int)(40*scale), paintRect );
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#35465c"));
        paint.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD));
        paint.setTextSize((int) (18 * scale));
        String oneLetter = "A";
        Rect oneLetterBounds = new Rect();
        paint.getTextBounds(oneLetter, 0, oneLetter.length(), oneLetterBounds);
        int numberOfLettersOnScreen = canvas.getWidth() / oneLetterBounds.width();

        if(albumName.length() > numberOfLettersOnScreen){
            albumName = albumName.substring(0, numberOfLettersOnScreen);
        }
        Rect bounds = new Rect();
        paint.getTextBounds(albumName, 0, albumName.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;
        canvas.drawText(albumName, x, y, paint);

        if(artistName.length() > numberOfLettersOnScreen){
            artistName = artistName.substring(0, numberOfLettersOnScreen);
        }
        Rect bounds1 = new Rect();
        paint.getTextBounds(artistName, 0, artistName.length(), bounds1);
        int x1 = (bitmap.getWidth() - bounds1.width())/2;
        int y1 = (bitmap.getHeight() + bounds1.height())/2- bounds1.height();
        canvas.drawText(artistName, x1, y1, paint);

        paint.setTextSize((int) (20 * scale));

        String oneBigLetter = "A";
        Rect oneBigLetterBounds = new Rect();
        paint.getTextBounds(oneBigLetter, 0, oneBigLetter.length(), oneBigLetterBounds);
        int numberOfBigLettersOnScreen = canvas.getWidth() / oneBigLetterBounds.width();

        if(trackName.length() > numberOfBigLettersOnScreen){
            trackName = trackName.substring(0, numberOfBigLettersOnScreen);
        }
        Rect bounds2 = new Rect();
        paint.getTextBounds(trackName, 0, trackName.length(), bounds2);
        int x2 = (bitmap.getWidth() - bounds2.width())/2;
        int y2 = (bitmap.getHeight() + bounds2.height())/2- bounds2.height() - bounds1.height();
        canvas.drawText(trackName, x2, y2, paint);
        return bitmap;
    }

}