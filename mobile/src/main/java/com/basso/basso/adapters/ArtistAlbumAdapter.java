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
import com.basso.basso.model.Album;
import com.basso.basso.ui.MusicHolder;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.MusicUtils;

import java.util.List;


public class ArtistAlbumAdapter extends ArrayAdapter<Album> {

    private static final int ITEM_VIEW_TYPE_HEADER = 0;

    private static final int ITEM_VIEW_TYPE_MUSIC = 1;

    private static final int VIEW_TYPE_COUNT = 3;

    public final static int[] string1 = {105, 99, 95, 101, 109, 112, 116, 121, 95, 99, 111, 118, 101, 114, 95, 98, 105, 103, 95, 117, 110, 99, 111, 109, 112, 114, 101, 115, 115, 101, 100, 95, 109, 97, 105, 110};

    private final LayoutInflater mInflater;

    private final View mHeader;

    private final int mLayoutId;

    private final ImageFetcher mImageFetcher;

    private List<Album> mCount = Lists.newArrayList();

    public ArtistAlbumAdapter(final Activity context, final int layoutId) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mHeader = mInflater.inflate(R.layout.faux_carousel, null);
        mLayoutId = layoutId;
        mImageFetcher = BassoUtils.getImageFetcher(context);
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
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder)convertView.getTag();
        }

        final Album album = getItem(position - 1);
        final String albumName = album.mAlbumName;

        holder.mLineOne.get().setText(albumName);
        holder.mLineTwo.get().setText(MusicUtils.makeLabel(getContext(),
                R.plurals.Nsongs, album.mSongNumber));
        mImageFetcher.loadAlbumImage(album.mArtistName, albumName, album.mAlbumId,
                holder.mImage.get(), ImageWorker.ImageSource.OTHER);
        playAlbum(holder.mImage.get(), position);
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

    private void playAlbum(final ImageView album, final int position) {
        album.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                final long id = getItem(position - 1).mAlbumId;
                final long[] list = MusicUtils.getSongListForAlbum(getContext(), id);
                MusicUtils.playAll(getContext(), list, 0, false);
            }
        });
    }

    public void unload() {
        clear();
    }

    public void setPauseDiskCache(final boolean pause) {
        if (mImageFetcher != null) {
            mImageFetcher.setPauseDiskCache(pause);
        }
    }

    public void removeFromCache(final Album album) {
        if (mImageFetcher != null) {
            mImageFetcher.removeFromCache(ImageFetcher.generateAlbumCacheKey(album.mAlbumName, album.mArtistName));
        }
    }

    public void setCount(final List<Album> data) {
        mCount = data;
    }

    public void flush() {
        mImageFetcher.flush();
    }
}
