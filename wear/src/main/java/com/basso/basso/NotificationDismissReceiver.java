package com.basso.basso;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class NotificationDismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(DataLayerListenerService.NOTIFICATION_DELETED_ACTION)) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            System.out.println("Broadcast received");
            SharedPreferences.Editor edit = pref.edit();
            edit.putBoolean("tracksUpdated", false);
            edit.putBoolean("albumsUpdated", false);
            edit.putBoolean("artistsUpdated", false);
            edit.putBoolean("playlistUpdated", false);
            edit.putBoolean("commonUpdated", false);
            edit.putBoolean("currentUpdated", false);
            edit.commit();
        }
    }
}
