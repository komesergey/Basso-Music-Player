package com.basso.basso;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class StartActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener,  MessageApi.MessageListener {
    private  GoogleApiClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGoogleApiClient();
        System.out.println("Start activity created");
    }

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
        System.out.println("Start activity destroyed");
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
    }
    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("Google api connected");
        new SendToDataLayerThread("/start_main_application", "").start();
    }
    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection suspended");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connection failed");
        new SendLocalMessage("/error", "").start();
    }
    class SendToDataLayerThread extends Thread {
        String path;
        String message;

        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            if(nodes.getNodes().size() == 0){
                System.out.println("Connection failed");
                new SendLocalMessage("/error", "").start();
                StartActivity.this.finish();
            }
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("myTag", "Message: sent to: " + node.getDisplayName());
                    StartActivity.this.finish();
                }
                else {
                    Log.v("myTag", "ERROR: failed to send Message");
                }
            }
        }
    }
    class SendLocalMessage extends Thread {
        String path;
        String message;

        SendLocalMessage(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            NodeApi.GetLocalNodeResult nodes = Wearable.NodeApi.getLocalNode(googleClient).await();
            Node node = nodes.getNode();
            if(nodes.getNode() == null){
                System.out.println("No local service");
            }
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                System.out.println("Local message sent");
                StartActivity.this.finish();
            }
            else {
                System.out.println("Error occurred while sending local message");
                StartActivity.this.finish();
            }

        }
    }
}
