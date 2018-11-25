package com.basso.basso.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.basso.basso.R;
import com.basso.basso.model.Playlist;
import com.basso.basso.ui.MusicHolder;
import com.basso.basso.ui.MusicHolder.DataHolder;

public class PlaylistAdapter extends ArrayAdapter<Playlist> {

    private static final int VIEW_TYPE_COUNT = 1;

    private final int mLayoutId;

    private DataHolder[] mData;

    public PlaylistAdapter(final Context context, final int layoutId) {
        super(context, 0);
        mLayoutId = layoutId;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        MusicHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mLayoutId, parent, false);
            holder = new MusicHolder(convertView);
            holder.mLineTwo.get().setVisibility(View.GONE);
            holder.mLineThree.get().setVisibility(View.GONE);
            holder.mLineOne.get().setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.text_size_large));
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder)convertView.getTag();
        }

        final DataHolder dataHolder = mData[position];

        holder.mLineOne.get().setText(dataHolder.mLineOne);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public void buildCache() {
        mData = new DataHolder[getCount()];
        for (int i = 0; i < getCount(); i++) {
            final Playlist playlist = getItem(i);
            mData[i] = new DataHolder();
            mData[i].mItemId = playlist.mPlaylistId;
            mData[i].mLineOne = playlist.mPlaylistName;
        }
    }

    public void unload() {
        clear();
        mData = null;
    }

}
