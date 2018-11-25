package com.basso.basso.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.basso.basso.BitmapUtils;
import com.basso.basso.R;
import com.basso.basso.WearActivity;
import com.basso.basso.adapters.ControlListAdapter;
import com.basso.basso.adapters.TrackListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class ControlPage extends Fragment {

    private ControlListAdapter controlListAdapter;
    private WearableListView listView;
    private SharedPreferences mPref;
    private TextView trackName;
    private TextView artistName;
    private BoxInsetLayout boxInsetLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.control_page, container, false);
        listView =(WearableListView) view.findViewById(R.id.control_list);
        trackName = (TextView) view.findViewById(R.id.control_track_name);
        artistName = (TextView) view.findViewById(R.id.control_artist_name);
        boxInsetLayout = (BoxInsetLayout)view.findViewById(R.id.box_inset_layout);
        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String trackNameInfo = mPref.getString("trackName", getString(R.string.choose_song_to_play));
        String artistNameInfo = mPref.getString("artistName", "");
        trackName.setText(trackNameInfo);
        artistName.setText(artistNameInfo);
        listView.setGreedyTouchMode(true);
        controlListAdapter = new ControlListAdapter(getActivity());
        listView.setAdapter(controlListAdapter);
        updateControlPage();
        setBitmap();
        return view;
    }

    public void setBitmap(){
        Bitmap cover = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            ContextWrapper cw = new ContextWrapper(getActivity());
            File directory = cw.getDir("cover", Context.MODE_PRIVATE);
            cover = BitmapFactory.decodeStream(new FileInputStream(new File(directory, "cover.png")), null, options);
            cover = BitmapUtils.createBlurredBitmap(cover);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(cover == null) {
            cover = BitmapFactory.decodeResource(getResources(), R.drawable.no_art_ghost_album);
            cover = Bitmap.createScaledBitmap(cover, 400, 400, true);
            cover = BitmapUtils.createBlurredBitmap(cover);
        }
        final Bitmap result = cover;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boxInsetLayout.setBackground(new BitmapDrawable(getResources(),result));
            }
        });
    }

    public void updateControlPage(){
        sendMessage("getBalance", "");
        sendMessage("getPlaystate","");
        sendMessage("getRepeatState","");
        sendMessage("getShuffleState","");
        sendMessage("getMusicVolume","");
    }
    public void updateControls(){
        System.out.println("Controls updated");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (controlListAdapter != null)
                    controlListAdapter.notifyDataSetChanged();
            }
        });

    }

    public void sendMessage(String path, String text){
        ((WearActivity)getActivity()).sendMessage(path,text);
    }

    public void updateInfo(){
        final String trackNameInfo = mPref.getString("trackName", "Nothing playing");
        final String artistNameInfo = mPref.getString("artistName", "No artist");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trackName.setText(trackNameInfo);
                artistName.setText(artistNameInfo);
            }
        });
        setBitmap();
    }
}
