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
import com.basso.basso.AlbumSongsActivity;
import com.basso.basso.R;
import com.basso.basso.adapters.AlbumListAdapter;

public class AlbumPage extends Fragment implements WearableListView.ClickListener{
    private ArrayList<ArrayList<String>> allAlbums;
    private AlbumListAdapter albumListAdapter;
    private WearableListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.album_page, container, false);
        listView = (WearableListView) view.findViewById(R.id.albums_list);
        new GetAlbumsFromJSon().execute(getActivity());
        listView.setGreedyTouchMode(true);
        listView.setInitialOffset(-100);
        albumListAdapter = new AlbumListAdapter(getActivity(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
        listView.setAdapter(albumListAdapter);
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
        Intent intent = new Intent(getActivity().getApplicationContext(), AlbumSongsActivity.class);
        intent.putExtra("album_id", allAlbums.get(0).get(position));
        intent.putExtra("album_title", allAlbums.get(1).get(position));
        startActivity(intent);

    }
    @Override
    public void onTopEmptyRegionClick() {
    }

    private class GetAlbumsFromJSon extends AsyncTask<Context, Void, ArrayList<ArrayList<String>>> {
        @Override
        protected ArrayList<ArrayList<String>> doInBackground(Context... params) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(params[0]);
            ArrayList<String> album_id = new ArrayList<>();
            ArrayList<String> album_title = new ArrayList<>();
            ArrayList<String> album_artist = new ArrayList<>();

            ArrayList<ArrayList<String>> result = new ArrayList<>();
            result.add(album_id);
            result.add(album_title);
            result.add(album_artist);

            String jsonObject = pref.getString("albums", "empty");
            if(!jsonObject.equals("empty")) {
                try {
                    JSONObject jObject = new JSONObject(jsonObject);
                    JSONObject data = jObject.getJSONObject("data");
                    JSONArray array = data.getJSONArray("array");

                    for (int i = 0; i < array.length(); i++) {
                        album_id.add(array.getJSONObject(i).getString("album_id"));
                        album_title.add(array.getJSONObject(i).getString("album_title"));
                        album_artist.add(array.getJSONObject(i).getString("album_artist"));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            return  result;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<String>> albums) {
            allAlbums = albums;
            albumListAdapter = new AlbumListAdapter(getActivity(), albums.get(1), albums.get(2), albums.get(0));
            listView.setAdapter(albumListAdapter);
        }
    }
}
