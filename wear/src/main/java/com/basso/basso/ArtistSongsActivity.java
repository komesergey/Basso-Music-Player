package com.basso.basso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.basso.basso.adapters.ArtistListAdapter;
import com.basso.basso.adapters.TrackListAdapter;

public class ArtistSongsActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener,  MessageApi.MessageListener, WearableListView.ClickListener  {

    private  GoogleApiClient googleClient;
    private  String artist_key;
    private WearableListView artistSongs;
    private ArrayList<ArrayList<String>> tracks;
    private TrackListAdapter artistSongsAdapter;
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
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_songs_activity);
        artistSongs = (WearableListView) findViewById(R.id.artist_songs);
        artistSongs.setGreedyTouchMode(true);
        artistSongs.setInitialOffset(-100);
        artistSongsAdapter = new TrackListAdapter(this, new ArrayList<String>(), new ArrayList<String>());
        artistSongs.setAdapter(artistSongsAdapter);
        artistSongs.setClickListener(this);
        initGoogleApiClient();
        Intent intent = this.getIntent();
        if(intent != null){
            TextView albumName = (TextView)findViewById(R.id.artist_name);
            albumName.setText(intent.getStringExtra("artist_name"));
            artist_key = intent.getStringExtra("artist_key");
        }
        sendMessage("/getArtist", artist_key);
    }
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        int position = v.getPosition();
        sendMessage("playartist", artist_key + "position" + position);
    }
    @Override
    public void onTopEmptyRegionClick() {
    }
    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        if(messageEvent.getPath().equals(artist_key)) {
            new GetTracksFromJSonString().execute(new String(messageEvent.getData()));
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

    private class GetTracksFromJSonString extends AsyncTask<String, Void, ArrayList<ArrayList<String>>> {
        @Override
        protected ArrayList<ArrayList<String>> doInBackground(String... params) {

            ArrayList<String> track_id = new ArrayList<String>();
            ArrayList<String> track_title = new ArrayList<String>();
            ArrayList<String> track_artist = new ArrayList<String>();

            ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
            result.add(track_id);
            result.add(track_title);
            result.add(track_artist);

            if(!params[0].equals("empty")) {
                try {
                    JSONObject jObject = new JSONObject(params[0]);
                    JSONObject data = jObject.getJSONObject("data");
                    JSONArray array = data.getJSONArray("array");

                    for (int i = 0; i < array.length(); i++) {
                        track_id.add(array.getJSONObject(i).getString("track_id"));
                        track_title.add(array.getJSONObject(i).getString("track_title"));
                        track_artist.add(array.getJSONObject(i).getString("track_artist"));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            return  result;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<String>> artistTracks) {
            tracks = artistTracks;
            artistSongsAdapter = new TrackListAdapter(ArtistSongsActivity.this, artistTracks.get(1), artistTracks.get(2));
            artistSongs.setAdapter(artistSongsAdapter);
        }
    }
}
