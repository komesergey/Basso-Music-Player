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
import com.basso.basso.R;
import com.basso.basso.adapters.ArtistListAdapter;

public class ArtistPage extends Fragment implements WearableListView.ClickListener {
    private ArrayList<ArrayList<String>> allArtists;
    private WearableListView listView;
    private ArtistListAdapter artistListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artist_page, container, false);
        listView = (WearableListView) view.findViewById(R.id.artist_list);
        new GetArtistsFromJSon().execute(getActivity());
        listView.setGreedyTouchMode(true);
        listView.setInitialOffset(-100);
        artistListAdapter =  new ArtistListAdapter(getActivity(), new ArrayList<String>(), new ArrayList<String>());
        listView.setAdapter(artistListAdapter);
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
        Intent intent = new Intent(getActivity().getApplicationContext(), ArtistSongsActivity.class);
        intent.putExtra("artist_key", allArtists.get(0).get(position));
        intent.putExtra("artist_name", allArtists.get(1).get(position));
        startActivity(intent);
    }
    @Override
    public void onTopEmptyRegionClick() {

    }

    private class GetArtistsFromJSon extends AsyncTask<Context, Void, ArrayList<ArrayList<String>>> {
        @Override
        protected ArrayList<ArrayList<String>> doInBackground(Context... params) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(params[0]);
            ArrayList<String> artist_key = new ArrayList<>();
            ArrayList<String> artist_title = new ArrayList<>();

            ArrayList<ArrayList<String>> result = new ArrayList<>();
            result.add(artist_key);
            result.add(artist_title);

            String jsonObject = pref.getString("artists", "empty");
            if(!jsonObject.equals("empty")) {
                try {
                    JSONObject jObject = new JSONObject(jsonObject);
                    JSONObject data = jObject.getJSONObject("data");
                    JSONArray array = data.getJSONArray("array");

                    for (int i = 0; i < array.length(); i++) {
                        artist_key.add(array.getJSONObject(i).getString("artist_key"));
                        artist_title.add(array.getJSONObject(i).getString("artist_title"));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            return  result;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<String>> artists) {
            allArtists = artists;
            artistListAdapter = new ArtistListAdapter(getActivity(), artists.get(1), artists.get(0));
            listView.setAdapter(artistListAdapter);
        }
    }
}
