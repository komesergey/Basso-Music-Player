package com.basso.basso;

import android.content.Intent;

import com.basso.basso.ui.activities.AudioPlayerActivity;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

public class StartFromWearService extends WearableListenerService {
    @Override
    public void onPeerConnected(Node peer) {
        if(AudioPlayerActivity.isAlive){
            Globals.audioPlayerActivity.updateWear();
            Globals.audioPlayerActivity.updateCurrentPlayingWear();
        }
        super.onPeerConnected(peer);
    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/start_main_application")) {
            if(!AudioPlayerActivity.isAlive){
                Intent intent = new Intent(getApplicationContext(), AudioPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Globals.audioPlayerActivity.updateWear();
                Globals.audioPlayerActivity.updateCurrentPlayingWear();
            }
        }  else {
            super.onMessageReceived(messageEvent);
        }
    }
}
