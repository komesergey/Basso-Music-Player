package com.basso.basso.adapters;

import android.content.Context;
import android.provider.MediaStore;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

import com.basso.basso.R;
import com.basso.basso.WearActivity;

public class PlaylistListAdapter extends WearableListView.Adapter {
    private ArrayList<String> playlistNames;
    private ArrayList<String> playlistId;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public PlaylistListAdapter(Context context, ArrayList<String> playlistNames, ArrayList<String> playlistId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.playlistNames = playlistNames;
        this.playlistId = playlistId;
    }

    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView playlistTitle;
        private ImageView playlistCircleImage;
        public ItemViewHolder(View itemView) {
            super(itemView);
            playlistCircleImage = (ImageView) itemView.findViewById(R.id.playlist_list_circle);
            playlistTitle = (TextView) itemView.findViewById(R.id.playlist_title);
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.playlist_list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, final int position) {

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        String playlist  = playlistNames.get(position);

        itemHolder.playlistCircleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WearActivity)mContext).sendMessage("playplaylist", playlistId.get(position) + "position" + 0);
            }
        });
        if(playlist.equals(MediaStore.UNKNOWN_STRING))
            itemHolder.playlistTitle.setText(mContext.getString(R.string.unknown_artist));
        else
            itemHolder.playlistTitle.setText(playlist);
        holder.itemView.setTag(position);

    }
    @Override
    public int getItemCount() {
        return playlistNames.size();
    }
}
