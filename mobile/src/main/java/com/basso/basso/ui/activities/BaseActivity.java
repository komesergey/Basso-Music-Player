package com.basso.basso.ui.activities;

import static com.basso.basso.utils.MusicUtils.mService;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import basso.IBassoService;
import com.basso.basso.MusicPlaybackService;
import com.basso.basso.MusicStateListener;
import com.basso.basso.R;
import com.basso.basso.cache.ImageWorker;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.MusicUtils.ServiceToken;
import com.basso.basso.utils.NavUtils;
import com.basso.basso.utils.ThemeUtils;
import com.basso.basso.widgets.PlayPauseButton;
import com.basso.basso.widgets.RepeatButton;
import com.basso.basso.widgets.ShuffleButton;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class BaseActivity extends FragmentActivity implements ServiceConnection {

    private final ArrayList<MusicStateListener> mMusicStateListener = Lists.newArrayList();

    private ServiceToken mToken;

    private PlayPauseButton mPlayPauseButton;

    private RepeatButton mRepeatButton;

    private ShuffleButton mShuffleButton;

    private TextView mTrackName;

    private TextView mArtistName;

    private ImageView mAlbumArt;

    private PlaybackStatus mPlaybackStatus;

    private boolean mIsBackPressed = false;

    protected ThemeUtils mResources;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = new ThemeUtils(this);
        mResources.setOverflowStyle(this);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mToken = MusicUtils.bindToService(this, this);
        mPlaybackStatus = new PlaybackStatus(this);
        mResources.themeActionBar(getActionBar(), getString(R.string.app_name));
        setContentView(setContentView());
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        mService = IBassoService.Stub.asInterface(service);
        invalidateOptionsMenu();
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        getMenuInflater().inflate(R.menu.activity_base, menu);
        mResources.setSearchIcon(menu);

        final SearchView searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        final SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        final SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);

        int searchIconId = searchView.getContext().getResources().getIdentifier("android:id/search_button", null, null);
        ImageView searchIcon = (ImageView) searchView.findViewById(searchIconId);
        searchIcon.setImageResource(R.drawable.ic_action_search);
        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.WHITE);

        int mVoiceButtonId = getResources().getIdentifier("android:id/search_voice_btn", null, null);
        ImageView voiceSearchButton = (ImageView)searchView.findViewById(mVoiceButtonId);
        voiceSearchButton.setImageResource(R.drawable.ic_btn_speak);

        int mCloseButtonId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeSearchButton = (ImageView)searchView.findViewById(mCloseButtonId);
        closeSearchButton.setImageResource(R.drawable.btn_notification_collapse);

        try {
            int queryTextViewId = getResources().getIdentifier("android:id/search_src_text", null, null);
            View autoComplete = searchView.findViewById(queryTextViewId);

            Class<?> clazz = Class.forName("android.widget.SearchView$SearchAutoComplete");

            SpannableStringBuilder stopHint = new SpannableStringBuilder("   ");
            stopHint.append(" ");

            Drawable searchIconDrawable = getResources().getDrawable(R.drawable.ic_action_search);
            Method textSizeMethod = clazz.getMethod("getTextSize");
            Float rawTextSize = (Float) textSizeMethod.invoke(autoComplete);
            int textSize = (int) (rawTextSize * 1.25);
            searchIconDrawable.setBounds(0, 0, textSize, textSize);
            stopHint.setSpan(new ImageSpan(searchIconDrawable), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            Method setHintMethod = clazz.getMethod("setHint", CharSequence.class);
            setHintMethod.invoke(autoComplete, stopHint);
        }catch (ClassNotFoundException ex){}
        catch (IllegalAccessException ex){}
        catch (NoSuchMethodException ex){}
        catch (InvocationTargetException ex){}

        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(final String query) {
                NavUtils.openSearch(BaseActivity.this, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                NavUtils.openSettings(this);
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
        filter.addAction(MusicPlaybackService.SHUFFLEMODE_CHANGED);
        filter.addAction(MusicPlaybackService.REPEATMODE_CHANGED);
        filter.addAction(MusicPlaybackService.META_CHANGED);
        filter.addAction(MusicPlaybackService.REFRESH);
        MusicUtils.notifyForegroundStateChanged(this, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MusicUtils.notifyForegroundStateChanged(this, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mToken != null) {
            MusicUtils.unbindFromService(mToken);
            mToken = null;
        }
        mMusicStateListener.clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIsBackPressed = true;
    }

    private void initBottomActionBar() {
        mPlayPauseButton = (PlayPauseButton)findViewById(R.id.action_button_play);
        mShuffleButton = (ShuffleButton)findViewById(R.id.action_button_shuffle);
        mRepeatButton = (RepeatButton)findViewById(R.id.action_button_repeat);
        mTrackName = (TextView)findViewById(R.id.bottom_action_bar_line_one);
        mArtistName = (TextView)findViewById(R.id.bottom_action_bar_line_two);
        mAlbumArt = (ImageView)findViewById(R.id.bottom_action_bar_album_art);
        mAlbumArt.setOnClickListener(mOpenCurrentAlbumProfile);
        final LinearLayout bottomActionBar = (LinearLayout)findViewById(R.id.bottom_action_bar);
        bottomActionBar.setOnClickListener(mOpenNowPlaying);
    }

    private void updateBottomActionBarInfo() {
        mTrackName.setText(MusicUtils.getTrackName());
        mArtistName.setText(MusicUtils.getArtistName());
        BassoUtils.getImageFetcher(this).loadCurrentArtwork(mAlbumArt, ImageWorker.ImageSource.OTHER);
    }

    private void updatePlaybackControls() {
        mPlayPauseButton.updateState();
        mShuffleButton.updateShuffleState();
        mRepeatButton.updateRepeatState();
    }

    private final View.OnClickListener mOpenCurrentAlbumProfile = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            if (MusicUtils.getCurrentAudioId() != -1) {
                NavUtils.openAlbumProfile(BaseActivity.this, MusicUtils.getAlbumName(),
                        MusicUtils.getArtistName(), MusicUtils.getCurrentAlbumId());
            } else {
                MusicUtils.shuffleAll(BaseActivity.this);
            }
            if (BaseActivity.this instanceof ProfileActivity) {
                finish();
            }
        }
    };

    private final View.OnClickListener mOpenNowPlaying = new View.OnClickListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onClick(final View v) {
            if (MusicUtils.getCurrentAudioId() != -1) {
                NavUtils.openAudioPlayer(BaseActivity.this);
            } else {
                MusicUtils.shuffleAll(BaseActivity.this);
            }
        }
    };

    private final static class PlaybackStatus extends BroadcastReceiver {

        private final WeakReference<BaseActivity> mReference;
        public PlaybackStatus(final BaseActivity activity) {
            mReference = new WeakReference<BaseActivity>(activity);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action.equals(MusicPlaybackService.META_CHANGED)) {
                mReference.get().updateBottomActionBarInfo();
                mReference.get().invalidateOptionsMenu();
                for (final MusicStateListener listener : mReference.get().mMusicStateListener) {
                    if (listener != null) {
                        listener.onMetaChanged();
                    }
                }
            } else if (action.equals(MusicPlaybackService.PLAYSTATE_CHANGED)) {
                mReference.get().mPlayPauseButton.updateState();
            } else if (action.equals(MusicPlaybackService.REPEATMODE_CHANGED)
                    || action.equals(MusicPlaybackService.SHUFFLEMODE_CHANGED)) {
                mReference.get().mRepeatButton.updateRepeatState();
                mReference.get().mShuffleButton.updateShuffleState();
            } else if (action.equals(MusicPlaybackService.REFRESH)) {
                for (final MusicStateListener listener : mReference.get().mMusicStateListener) {
                    if (listener != null) {
                        listener.restartLoader();
                    }
                }
            }
        }
    }

    public void setMusicStateListenerListener(final MusicStateListener status) {
        if (status != null) {
            mMusicStateListener.add(status);
        }
    }
    public abstract int setContentView();
}
