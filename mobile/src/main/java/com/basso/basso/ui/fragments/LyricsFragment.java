package com.basso.basso.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.basso.basso.R;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.songs.LyricsProvider;
import com.basso.basso.songs.LyricsProviderFactory;
import com.basso.basso.songs.OfflineLyricsProvider;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.BitmapUtils;
import com.basso.basso.utils.MusicUtils;

@SuppressLint("NewApi")
public class LyricsFragment extends Fragment {

    private TextView mLyrics;

    private ImageView background;

    private Bitmap backgroundDrawable;

    private ProgressBar mProgressBar;

    private boolean mTryOnline = false;

    public LyricsFragment() {}

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.lyrics_base, null);
        mLyrics = (TextView)rootView.findViewById(R.id.audio_player_lyrics);
        mLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent("com.basso.basso.TOGGLE_LYRICS");
                getActivity().sendBroadcast(i);
            }
        });
        background = (ImageView)rootView.findViewById(R.id.lyrics_background);
        if (BassoUtils.hasHoneycomb()) {
            mLyrics.setTextIsSelectable(true);
        }
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.audio_player_lyrics_progess);
        Intent i = new Intent("com.basso.basso.UPDATE_LYRICS");
        getActivity().sendBroadcast(i);
        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void fetchLyrics(final boolean force) {
        if (isAdded()) {
            BassoUtils.execute(false, new FetchLyrics(), force);
        } else {
        }
    }

    private void saveLyrics(final String lyrics) {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                final String path = MusicUtils.getFilePath();
                if (path != null) {
                    OfflineLyricsProvider.saveLyrics(lyrics, path);
                }
                return null;
            }
        }, (Void[])null);
    }

    private final class FetchLyrics extends AsyncTask<Boolean, Void, String> {

        private final String mArtist;

        private final String mSong;

        public FetchLyrics() {
            mArtist = MusicUtils.getArtistName();
            mSong = MusicUtils.getTrackName();
        }

        @Override
        protected void onPreExecute() {
            mTryOnline = false;
            mLyrics.setText(null);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(final Boolean... force) {
            backgroundDrawable = null;
            backgroundDrawable = BitmapUtils.createBlurredBitmap(ImageFetcher.getInstance(getActivity()).getOriginalArtwork());
            LyricsProvider provider = null;
            String lyrics = null;
            provider = LyricsProviderFactory.getOfflineProvider(MusicUtils.getFilePath());
            lyrics = provider.getLyrics(null, null);
            if ((lyrics == null || TextUtils.isEmpty(lyrics)) && BassoUtils.isOnline(getActivity())) {
                mTryOnline = true;
                provider = LyricsProviderFactory.getMainOnlineProvider();
                lyrics = provider.getLyrics(mArtist, mSong);
            }
            return lyrics;
        }

        @Override
        protected void onPostExecute(final String result) {
            background.setImageBitmap(backgroundDrawable);
            if (!TextUtils.isEmpty(result) && isAdded()) {
                mLyrics.setText(result);
                saveLyrics(result);
                mProgressBar.setVisibility(View.GONE);
            } else if(isAdded()) {
                mLyrics.setText(getString(R.string.no_lyrics, mSong));
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }
}