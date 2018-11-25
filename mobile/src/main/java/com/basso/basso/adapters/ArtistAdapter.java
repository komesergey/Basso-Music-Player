package com.basso.basso.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.basso.basso.R;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.cache.ImageWorker;
import com.basso.basso.model.Artist;
import com.basso.basso.ui.MusicHolder;
import com.basso.basso.ui.MusicHolder.DataHolder;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;

public class ArtistAdapter extends ArrayAdapter<Artist> {

    private static final int VIEW_TYPE_COUNT = 2;

    private final int mLayoutId;

    private final ImageFetcher mImageFetcher;

    private final int mOverlay;

    private DataHolder[] mData;

    private boolean mLoadExtraData = false;

    public ArtistAdapter(final Activity context, final int layoutId) {
        super(context, 0);
        mLayoutId = layoutId;
        mImageFetcher = BassoUtils.getImageFetcher(context);
        mOverlay = context.getResources().getColor(R.color.list_item_background);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        MusicHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mLayoutId, parent, false);
            holder = new MusicHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder)convertView.getTag();
        }

        final DataHolder dataHolder = mData[position];

        holder.mLineOne.get().setText(dataHolder.mLineOne);
        holder.mLineTwo.get().setText(dataHolder.mLineTwo);
        mImageFetcher.loadArtistImage(dataHolder.mLineOne, holder.mImage.get(), ImageWorker.ImageSource.OTHER);
        if (mLoadExtraData) {
            holder.mOverlay.get().setBackgroundColor(mOverlay);
            holder.mLineThree.get().setText(dataHolder.mLineThree);
            mImageFetcher.loadArtistImage(dataHolder.mLineOne, holder.mBackground.get(), ImageWorker.ImageSource.OTHER);
            playArtist(holder.mImage.get(), position);
        }
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
            final Artist artist = getItem(i);

            mData[i] = new DataHolder();
            mData[i].mItemId = artist.mArtistId;
            mData[i].mLineOne = artist.mArtistName;
            mData[i].mLineTwo = MusicUtils.makeLabel(getContext(),
                    R.plurals.Nalbums, artist.mAlbumNumber);
            mData[i].mLineThree = MusicUtils.makeLabel(getContext(),
                    R.plurals.Nsongs, artist.mSongNumber);
        }
    }

    private void playArtist(final ImageView artist, final int position) {
        artist.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                final long id = getItem(position).mArtistId;
                final long[] list = MusicUtils.getSongListForArtist(getContext(), id);
                MusicUtils.playAll(getContext(), list, 0, false);
            }
        });
    }

    public void unload() {
        clear();
        mData = null;
    }

    public void setPauseDiskCache(final boolean pause) {
        if (mImageFetcher != null) {
            mImageFetcher.setPauseDiskCache(pause);
        }
    }

    public void removeFromCache(final Artist artist) {
        if (mImageFetcher != null) {
            mImageFetcher.removeFromCache(artist.mArtistName);
        }
    }

    public void flush() {
        mImageFetcher.flush();
    }

    public void setLoadExtraData(final boolean extra) {
        mLoadExtraData = extra;
    }
}
