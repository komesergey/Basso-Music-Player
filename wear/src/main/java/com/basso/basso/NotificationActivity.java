package com.basso.basso;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class NotificationActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener,  MessageApi.MessageListener {

    private ImageButton mPlayPauseButton;
    private ImageButton mFastForwardButton;
    private ImageButton mRewindButton;
    private GoogleApiClient googleClient;
    private String playstate;

    private void initGoogleApiClient() {
        googleClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleClient.connect();
    }

    @Override
    public void onDestroy(){
        System.out.println("Notification activity destroyed");
        if (null != googleClient && googleClient.isConnected()) {
            Wearable.MessageApi.removeListener( googleClient, this );
            googleClient.disconnect();
        }
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_layout);
        initGoogleApiClient();
        mPlayPauseButton = (ImageButton)findViewById(R.id.notification_play);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("playOrPause","");
            }
        });
        mFastForwardButton = (ImageButton)findViewById(R.id.notification_fast_forward);
        mFastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("nextTrack","");
            }
        });
        mRewindButton = (ImageButton)findViewById(R.id.notification_rewind);
        mRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("prevTrack", "");
            }
        });
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        playstate = pref.getString("playstate", "pause");
        updatePlayingState();
    }

    public void updatePlayingState(){
        if(playstate.equals("pause")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPlayPauseButton.setImageResource(R.drawable.ic_action_pause);
                }
            });
        }else if(playstate.equals("play")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPlayPauseButton.setImageResource(R.drawable.ic_action_play);

                }
            });
        }
    }
    private void sendMessage( final String path, final String text ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            googleClient, node.getId(), path, text.getBytes()).await();
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        if(messageEvent.getPath().equals("/playstateNotif") ) {
            System.out.println("Playstate updated in notification activity");
            playstate = new String(messageEvent.getData());
            updatePlayingState();

        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener( googleClient, this );
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
