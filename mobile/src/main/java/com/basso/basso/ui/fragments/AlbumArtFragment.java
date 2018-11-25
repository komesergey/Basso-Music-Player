package com.basso.basso.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.basso.basso.R;

public class AlbumArtFragment extends Fragment {
    public ImageView albumArt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.nowplaying_album_art, null);
        albumArt = (ImageView)root.findViewById(R.id.audio_player_album_art);
        albumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent("com.basso.basso.TOGGLE_LYRICS");
                getActivity().sendBroadcast(i);
            }
        });
        Intent i = new Intent("com.basso.basso.UPDATE_ART");
        getActivity().sendBroadcast(i);
        return root;
    }
}