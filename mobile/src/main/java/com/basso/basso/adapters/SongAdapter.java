package com.basso.basso.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.basso.basso.model.Song;
import com.basso.basso.ui.MusicHolder;
import com.basso.basso.ui.MusicHolder.DataHolder;
import com.basso.basso.utils.MusicUtils;

public class SongAdapter extends ArrayAdapter<Song> {

    private static final int VIEW_TYPE_COUNT = 1;

    private final int mLayoutId;

    private DataHolder[] mData;

    public SongAdapter(final Context context, final int layoutId) {
        super(context, 0);
        mLayoutId = layoutId;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        MusicHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mLayoutId, parent, false);
            holder = new MusicHolder(convertView);
            holder.mLineThree.get().setVisibility(View.GONE);
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder)convertView.getTag();
        }

        final DataHolder dataHolder = mData[position];
        holder.mLineOne.get().setText(dataHolder.mLineOne);
        holder.mLineOneRight.get().setText(dataHolder.mLineOneRight);
        holder.mLineTwo.get().setText(dataHolder.mLineTwo);
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
            final Song song = getItem(i);
            mData[i] = new DataHolder();
            mData[i].mItemId = song.mSongId;
            mData[i].mLineOne = song.mSongName;
            mData[i].mLineOneRight = MusicUtils.makeTimeString(getContext(), song.mDuration);
            mData[i].mLineTwo = song.mAlbumName;
        }
    }

    public void unload() {
        clear();
        mData = null;
    }
}
