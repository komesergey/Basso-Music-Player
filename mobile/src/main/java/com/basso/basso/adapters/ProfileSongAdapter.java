package com.basso.basso.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.basso.basso.R;
import com.basso.basso.model.Song;
import com.basso.basso.ui.MusicHolder;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.MusicUtils;

import java.util.List;


public class ProfileSongAdapter extends ArrayAdapter<Song> {

    public static final int DISPLAY_DEFAULT_SETTING = 0;

    public static final int DISPLAY_PLAYLIST_SETTING = 1;

    public static final int DISPLAY_ALBUM_SETTING = 2;

    private static final int ITEM_VIEW_TYPE_HEADER = 0;

    private static final int ITEM_VIEW_TYPE_MUSIC = 1;

    private static final int VIEW_TYPE_COUNT = 3;

    private final LayoutInflater mInflater;

    private final View mHeader;

    private final int mLayoutId;

    private final int mDisplaySetting;

    private final String SEPARATOR_STRING = " - ";

    private List<Song> mCount = Lists.newArrayList();

    public ProfileSongAdapter(final Context context, final int layoutId, final int setting) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mHeader = mInflater.inflate(R.layout.faux_carousel, null);
        mLayoutId = layoutId;
        mDisplaySetting = setting;
    }

    public ProfileSongAdapter(final Context context, final int layoutId) {
        this(context, layoutId, DISPLAY_DEFAULT_SETTING);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (position == 0) {
            return mHeader;
        }

        MusicHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mLayoutId, parent, false);
            holder = new MusicHolder(convertView);
            holder.mLineThree.get().setVisibility(View.GONE);
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder)convertView.getTag();
        }

        final Song song = getItem(position - 1);
        holder.mLineOne.get().setText(song.mSongName);
        switch (mDisplaySetting) {
            case DISPLAY_ALBUM_SETTING:
                holder.mLineOneRight.get().setVisibility(View.GONE);
                holder.mLineTwo.get().setText(MusicUtils.makeTimeString(getContext(), song.mDuration));
                break;
            case DISPLAY_PLAYLIST_SETTING:
                if (song.mDuration == -1) {
                    holder.mLineOneRight.get().setVisibility(View.GONE);
                } else {
                    holder.mLineOneRight.get().setVisibility(View.VISIBLE);
                    holder.mLineOneRight.get().setText(MusicUtils.makeTimeString(getContext(), song.mDuration));
                }
                final StringBuilder sb = new StringBuilder(song.mArtistName);
                sb.append(SEPARATOR_STRING);
                sb.append(song.mAlbumName);
                holder.mLineTwo.get().setText(sb.toString());
                break;
            case DISPLAY_DEFAULT_SETTING:
            default:
                holder.mLineOneRight.get().setVisibility(View.VISIBLE);
                holder.mLineOneRight.get().setText(MusicUtils.makeTimeString(getContext(), song.mDuration));
                holder.mLineTwo.get().setText(song.mAlbumName);
                break;
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        final int size = mCount.size();
        return size == 0 ? 0 : size + 1;
    }

    @Override
    public long getItemId(final int position) {
        if (position == 0) {
            return -1;
        }
        return position - 1;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == 0) {
            return ITEM_VIEW_TYPE_HEADER;
        }
        return ITEM_VIEW_TYPE_MUSIC;
    }

    public void unload() {
        clear();
    }

    public void setCount(final List<Song> data) {
        mCount = data;
    }
}
