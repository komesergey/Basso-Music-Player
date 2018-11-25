package com.basso.basso;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import com.basso.basso.adapters.MainGridViewPagerAdapter;
import com.basso.basso.fragments.AlbumPage;
import com.basso.basso.fragments.ArtistPage;
import com.basso.basso.fragments.ControlPage;
import com.basso.basso.fragments.PlaylistPage;
import com.basso.basso.fragments.TrackPage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import java.lang.ref.WeakReference;

public class WearActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener,  MessageApi.MessageListener {
    private GoogleApiClient googleClient;
    private SharedPreferences pref;
    private Fragment[][] fragments = new Fragment[][] { {
            new ControlPage(),
            new TrackPage(),
            new AlbumPage(),
            new ArtistPage(),
            new PlaylistPage()
    }};
    private void initGoogleApiClient() {
        googleClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataLayerListenerService.wearActivityWeakReference = new WeakReference<>(this);
        initGoogleApiClient();
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                    GridViewPager pager = (GridViewPager)stub.findViewById(R.id.pager);
                    pager.setAdapter(new MainGridViewPagerAdapter(getFragmentManager(), fragments));

            }
        });
    }

    @Override
    public void onDestroy(){
        if (null != googleClient && googleClient.isConnected()) {
            Wearable.MessageApi.removeListener( googleClient, this );
            googleClient.disconnect();
        }
        DataLayerListenerService.wearActivityWeakReference = null;
        super.onDestroy();
    }

    public void updateCurrentInfo(){
        ((ControlPage)fragments[0][0]).updateInfo();
    }

    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        if(messageEvent.getPath().equals("/playstateMain") ) {
            System.out.println("Playstate updated in wear activity");
            String playstate = new String(messageEvent.getData());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("playstate", playstate);
            edit.commit();
            ((ControlPage)fragments[0][0]).updateControls();
        }
        if(messageEvent.getPath().equals("setPlaystate")){

        }
        if(messageEvent.getPath().equals("setShuffleState")){
            String shuffleState = new String(messageEvent.getData());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("shufflestate", shuffleState);
            edit.commit();
            ((ControlPage)fragments[0][0]).updateControls();
        }
        if(messageEvent.getPath().equals("setRepeatState")){
            String repeatState = new String(messageEvent.getData());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("repeatstate", repeatState);
            edit.commit();
            ((ControlPage)fragments[0][0]).updateControls();
        }
        if(messageEvent.getPath().equals("setBalance")){
            String balance = new String(messageEvent.getData());
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("balance", balance);
            edit.commit();
            ((ControlPage)fragments[0][0]).updateControls();
        }
        if(messageEvent.getPath().equals("setMusicVolume")){
            String musicVolume = new String(messageEvent.getData());
            String[] tokens = musicVolume.split("/");
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("currentVolume", tokens[0]);
            edit.putString("maxVolume", tokens[1]);
            edit.commit();
            ((ControlPage)fragments[0][0]).updateControls();
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

    public void sendMessage( final String path, final String text ) {
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

}
