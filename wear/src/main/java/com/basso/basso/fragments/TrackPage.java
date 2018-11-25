package com.basso.basso.fragments;

import android.app.Fragment;
import android.content.Context;
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
import com.basso.basso.R;
import com.basso.basso.WearActivity;
import com.basso.basso.adapters.TrackListAdapter;

public class TrackPage extends Fragment implements WearableListView.ClickListener{

    private TrackListAdapter trackListAdapter;
    private WearableListView listView;
    private ArrayList<ArrayList<String>> allTracks;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_page, container, false);
        listView =(WearableListView) view.findViewById(R.id.track_list);
        new GetTracksFromJSon().execute(getActivity());
        listView.setGreedyTouchMode(true);
        listView.setInitialOffset(-100);
        trackListAdapter = new TrackListAdapter(getActivity(), new ArrayList<String>(), new ArrayList<String>());
        listView.setAdapter(trackListAdapter);
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
        ((WearActivity)getActivity()).sendMessage("playsong", position + "");
    }
    @Override
    public void onTopEmptyRegionClick() {
    }

    private class GetTracksFromJSon extends AsyncTask<Context, Void, ArrayList<ArrayList<String>>> {
        @Override
        protected ArrayList<ArrayList<String>> doInBackground(Context... params) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(params[0]);
            ArrayList<String> track_id = new ArrayList<>();
            ArrayList<String> track_title = new ArrayList<>();
            ArrayList<String> track_artist = new ArrayList<>();

            ArrayList<ArrayList<String>> result = new ArrayList<>();
            result.add(track_id);
            result.add(track_title);
            result.add(track_artist);

            String jsonObject = pref.getString("tracks", "empty");
            if(!jsonObject.equals("empty")) {
                try {
                    JSONObject jObject = new JSONObject(jsonObject);
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
        protected void onPostExecute(ArrayList<ArrayList<String>> tracks) {
            allTracks = tracks;
            trackListAdapter = new TrackListAdapter(getActivity(), tracks.get(1), tracks.get(2));
            listView.setAdapter(trackListAdapter);
        }
    }
}
