package com.basso.basso.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.basso.basso.R;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.BitmapUtils;
import com.basso.basso.utils.MusicUtils;

@SuppressLint("NewApi")
public class CarouselTab extends FrameLayoutWithOverlay {


    private TextView mLabelView;

    private View mAlphaLayer;

    private View mColorstrip;

    private final ImageFetcher mFetcher;

    public CarouselTab(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mFetcher = BassoUtils.getImageFetcher((Activity) context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLabelView = (TextView)findViewById(R.id.profile_tab_label);
        mAlphaLayer = findViewById(R.id.profile_tab_alpha_overlay);
        mColorstrip = findViewById(R.id.profile_tab_colorstrip);
        mColorstrip.setBackgroundColor(Color.WHITE);
        setAlphaLayer(mAlphaLayer);
    }

    @Override
    public void setSelected(final boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mColorstrip.setVisibility(View.VISIBLE);
        } else {
            mColorstrip.setVisibility(View.GONE);
        }
    }

    public void setArtistPhoto(final Activity context, final String artist) {
        if (!TextUtils.isEmpty(artist)) {
        } else {
            setDefault(context);
        }
    }

    public void blurPhoto(final Activity context, final String artist, final String album) {
        Bitmap artistImage = mFetcher.getCachedBitmap(artist);
        if (artistImage == null) {
            artistImage = mFetcher.getCachedArtwork(album, artist);
        }
        if (artistImage == null) {
            artistImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty_cover_big);
        }
        final Bitmap blur = BitmapUtils.createBlurredBitmap(artistImage);
    }

    public void setAlbumPhoto(final Activity context, final String album, final String artist) {
        if (!TextUtils.isEmpty(album)) {
        } else {
            setDefault(context);
        }
    }

    public void fetchAlbumPhoto(final Activity context, final String album, final String artist) {
        if (!TextUtils.isEmpty(album)) {
            mFetcher.removeFromCache(ImageFetcher.generateAlbumCacheKey(album, artist));
        } else {
            setDefault(context);
        }
    }

    public void setArtistAlbumPhoto(final Activity context, final String artist) {
        final String lastAlbum = MusicUtils.getLastAlbumForArtist(context, artist);
        if (!TextUtils.isEmpty(lastAlbum)) {
        } else {
            setDefault(context);
        }
    }

    public void setPlaylistOrGenrePhoto(final Activity context,
            final String profileName) {
        if (!TextUtils.isEmpty(profileName)) {
            final Bitmap image = mFetcher.getCachedBitmap(profileName);
            if (image != null) {
            } else {
                setDefault(context);
            }
        } else {
            setDefault(context);
        }
    }

    public void setDefault(final Context context) {
    }

    public void setLabel(final String label) {
        mLabelView.setText(label);
    }

    public void showSelectedState() {
        mLabelView.setSelected(true);
    }

    public void showDeselectedState() {
        mLabelView.setSelected(false);
    }

    public ImageView getPhoto() {return null;}

    public ImageView getAlbumArt() {
        return null;
    }
}
