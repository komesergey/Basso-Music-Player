package com.basso.basso.adapters;

import android.content.Context;
import android.provider.MediaStore;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.basso.basso.R;
import com.basso.basso.WearActivity;

import java.util.ArrayList;

public class AlbumListAdapter extends WearableListView.Adapter {
    private ArrayList<String> albumNames;
    private ArrayList<String> artistNames;
    private ArrayList<String> albumsId;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public AlbumListAdapter(Context context, ArrayList<String> albumNames, ArrayList<String> artistNames, ArrayList<String> albumId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.albumNames = albumNames;
        this.artistNames = artistNames;
        this.albumsId = albumId;
    }

    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView albumName;
        private TextView artistName;
        private ImageView albumListCircle;

        public ItemViewHolder(View itemView) {
            super(itemView);

            albumName = (TextView) itemView.findViewById(R.id.album_title);
            artistName = (TextView) itemView.findViewById(R.id.album_artist);
            albumListCircle = (ImageView)itemView.findViewById(R.id.album_list_circle);
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.album_list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, final int position) {

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        itemHolder.albumName.setText(albumNames.get(position));
        String artist = artistNames.get(position);

        itemHolder.albumListCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WearActivity)mContext).sendMessage("playalbum", albumsId.get(position) + "position" + 0);
            }
        });
        if(artist.equals(MediaStore.UNKNOWN_STRING))
            itemHolder.artistName.setText(mContext.getString(R.string.unknown_artist));
        else
            itemHolder.artistName.setText(artist);

        holder.itemView.setTag(position);

    }
    @Override
    public int getItemCount() {
        return albumNames.size();
    }
}
