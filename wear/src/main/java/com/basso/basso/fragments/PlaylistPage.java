package com.basso.basso.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import com.basso.basso.ArtistSongsActivity;
import com.basso.basso.PlaylistSongsActivity;
import com.basso.basso.R;
import com.basso.basso.adapters.PlaylistListAdapter;

public class PlaylistPage extends Fragment implements WearableListView.ClickListener{

    private PlaylistListAdapter playlistListAdapter;
    private WearableListView listView;
    private ArrayList<ArrayList<String>> allPlaylists;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_page, container, false);
        listView =(WearableListView) view.findViewById(R.id.playlists_list);
        new GetPlaylistsFromJSon().execute(getActivity());
        listView.setGreedyTouchMode(true);
        listView.setInitialOffset(-100);
        playlistListAdapter = new PlaylistListAdapter(getActivity(), new ArrayList<String>(),new ArrayList<String>());
        listView.setAdapter(playlistListAdapter);
        listView.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {
            }

            @Override
            public void onAbsoluteScrollChange(int i) {

            }

            @Override
            public void onScrollStateChanged(int i) {

            }

            @Override
            public void onCentralPositionChanged(int i) {

            }
        });

        listView.setClickListener(this);
        return view;
    }
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        int position = v.getPosition();
        Intent intent = new Intent(getActivity().getApplicationContext(), PlaylistSongsActivity.class);
        intent.putExtra("playlist_id", allPlaylists.get(0).get(position));
        intent.putExtra("playlist_title", allPlaylists.get(1).get(position));
        startActivity(intent);

    }
    @Override
    public void onTopEmptyRegionClick() {
    }

    private class GetPlaylistsFromJSon extends AsyncTask<Context, Void, ArrayList<ArrayList<String>>> {
        @Override
        protected ArrayList<ArrayList<String>> doInBackground(Context... params) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(params[0]);
            ArrayList<String> playlist_id = new ArrayList<>();
            ArrayList<String> playlist_title = new ArrayList<>();

            ArrayList<ArrayList<String>> result = new ArrayList<>();
            result.add(playlist_id);
            result.add(playlist_title);

            String jsonObject = pref.getString("playlists", "empty");
            if(!jsonObject.equals("empty")) {
                try {
                    JSONObject jObject = new JSONObject(jsonObject);
                    JSONObject data = jObject.getJSONObject("data");
                    JSONArray array = data.getJSONArray("array");

                    for (int i = 0; i < array.length(); i++) {
                        playlist_id.add(array.getJSONObject(i).getString("playlist_id"));
                        playlist_title.add(array.getJSONObject(i).getString("playlist_title"));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            return  result;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<String>> playlists) {
            allPlaylists = playlists;
            playlistListAdapter = new PlaylistListAdapter(getActivity(), playlists.get(1), playlists.get(0));
            listView.setAdapter(playlistListAdapter);
        }
    }
}
