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

public class ArtistListAdapter extends WearableListView.Adapter {
    private ArrayList<String> artistNames;
    private ArrayList<String> artistId;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public ArtistListAdapter(Context context, ArrayList<String> artistNames, ArrayList<String> artistId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.artistNames = artistNames;
        this.artistId = artistId;
    }

    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView artistName;
        private ImageView artistListCircle;
        public ItemViewHolder(View itemView) {
            super(itemView);
            artistListCircle = (ImageView) itemView.findViewById(R.id.artist_list_circle);
            artistName = (TextView) itemView.findViewById(R.id.artist_title);
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.artist_list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, final int position) {

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        String artist  = artistNames.get(position);
        itemHolder.artistListCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WearActivity)mContext).sendMessage("playartist", artistId.get(position) + "position" + 0);
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
        return artistNames.size();
    }
}
