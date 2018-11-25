package com.basso.basso.adapters;

import android.content.Context;
import android.provider.MediaStore;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import com.basso.basso.R;


public class TrackListAdapter extends WearableListView.Adapter {
    private ArrayList<String> trackNames;
    private ArrayList<String> artistNames;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public TrackListAdapter(Context context, ArrayList<String> trackNames, ArrayList<String> artistNames) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.trackNames = trackNames;
        this.artistNames = artistNames;
    }

    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView trackName;
        private TextView artistName;
        public ItemViewHolder(View itemView) {
            super(itemView);
            trackName = (TextView) itemView.findViewById(R.id.track_title);
            artistName = (TextView) itemView.findViewById(R.id.track_artist);
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.track_list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        itemHolder.trackName.setText(trackNames.get(position));
        String artist = artistNames.get(position);
        if(artist.equals(MediaStore.UNKNOWN_STRING))
            itemHolder.artistName.setText(mContext.getString(R.string.unknown_artist));
        else
            itemHolder.artistName.setText(artist);

        holder.itemView.setTag(position);

    }
    @Override
    public int getItemCount() {
        return trackNames.size();
    }
}
