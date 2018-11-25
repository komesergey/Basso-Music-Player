package com.basso.basso.ui.activities;

import static com.basso.basso.utils.MusicUtils.mService;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Artists;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import basso.IBassoService;

import com.basso.basso.Globals;
import com.basso.basso.MusicPlaybackService;
import com.basso.basso.R;
import com.basso.basso.adapters.AlbumArtPagerAdapter;
import com.basso.basso.adapters.PagerAdapter;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.cache.ImageWorker;
import com.basso.basso.lastfm.Image;
import com.basso.basso.loaders.ArtistSongLoader;
import com.basso.basso.loaders.NowPlayingCursor;
import com.basso.basso.loaders.PlaylistSongLoader;
import com.basso.basso.loaders.QueueLoader;
import com.basso.basso.loaders.SongLoader;
import com.basso.basso.menu.CreateNewPlaylist;
import com.basso.basso.ui.fragments.AlbumArtFragment;
import com.basso.basso.ui.fragments.EqualizerFragment;
import com.basso.basso.ui.fragments.LyricsFragment;
import com.basso.basso.ui.fragments.QueueFragment;
import com.basso.basso.menu.DeleteDialog;
import com.basso.basso.ui.fragments.phone.MusicBrowserPhoneFragment;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.MusicUtils.ServiceToken;
import com.basso.basso.utils.NavUtils;
import com.basso.basso.utils.PreferenceUtils;
import com.basso.basso.utils.ThemeUtils;
import com.basso.basso.widgets.PlayPauseButton;
import com.basso.basso.widgets.RepeatButton;
import com.basso.basso.widgets.RepeatingImageButton;
import com.basso.basso.widgets.ShuffleButton;
import com.basso.basso.widgets.theme.BottomActionBar;
import com.basso.basso.widgets.theme.ThemeableFrameLayout;
import com.basso.basso.widgets.theme.ThemeableTextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

public class AudioPlayerActivity extends BaseActivity implements ServiceConnection,
        OnSeekBarChangeListener, DeleteDialog.DeleteDialogCallback,  GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private GoogleApiClient googleClient;

    private static final int REFRESH_TIME = 1;

    private ServiceToken mToken;

    private PlayPauseButton mPlayPauseButton;

    private RepeatButton mRepeatButton;

    private ShuffleButton mShuffleButton;

    private RepeatingImageButton mPreviousButton;

    private RepeatingImageButton mNextButton;

    private PlayPauseButton mPlayPauseButtonPanel;

    private ShuffleButton mShuffleButtonPanel;

    private RepeatButton mRepeatButtonPanel;

    private RepeatingImageButton mPreviousButtonPanel;

    private RepeatingImageButton mNextButtonPanel;

    private LinearLayout mAudioControls;

    private ImageButton equalizerButton;

    private ImageButton queueButton;

    private SearchView searchView;

    private TextView mTrackName;

    private TextView mArtistName;

    private ImageView mAlbumArtSmall;

    private TextView mCurrentTime;

    private TextView mTrackNameBottom;

    private TextView mArtistNameBottom;

    private ImageView mAlbumArtBottom;

    private TextView mTotalTime;

    private SeekBar mProgress;

    private ProgressBar mProgressTop;

    private PlaybackStatus mPlaybackStatus;

    private SlidingUpPanelLayout mSlidingPanel;

    private TimeHandler mTimeHandler;

    private ViewPager mViewPager;

    private ViewPager mEqualizerPager;

    private ViewPager mAlbumArtPager;

    private PagerAdapter mPagerAdapter;

    private PagerAdapter mLyricsPagerAdapter;

    private ViewPager mLyricsPager;

    private FrameLayout mLyricsPagerContainer;

    private AlbumArtPagerAdapter mAlbumArtPagerAdapter;

    private FrameLayout mEqualizerLayout;

    private PagerAdapter mEqualizerPagerAdapter;

    private FrameLayout mPageContainer;

    private LinearLayout mAudioPlayerHeader;

    private ImageFetcher mImageFetcher;

    private ThemeUtils mResources;

    private long mPosOverride = -1;

    private long mStartSeekPos = 0;

    private long mLastSeekEventTime;

    private long mLastShortSeekEventTime;

    private boolean mIsPaused = false;

    private boolean mFromTouch = false;

    private boolean equalizerUpdated = false;

    private boolean D = false;

    private String TAG = "Audio player activity";

    private MusicBrowserPhoneFragment musicBrowserPhoneFragment;

    private ActionBar actionBar;

    public static volatile boolean isAlive = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSlidingPanel = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            findViewById(R.id.activity_base_content).setFitsSystemWindows(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(true);
            tintManager.setTintColor(Color.RED);
            tintManager.setStatusBarTintDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_background)));
            int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
            mSlidingPanel.setPanelHeight(getStatusBarHeight() + getActionBarHeight() - px);
            BottomActionBar bottomActionBar = (BottomActionBar)findViewById(R.id.bottom_action_bar_parent);
            ViewGroup.LayoutParams params = bottomActionBar.getLayoutParams();
            params.height = getStatusBarHeight() + getActionBarHeight() - px;
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;
            findViewById(R.id.sliding_panel).getLayoutParams().height = (height - px);
            ((ImageView)findViewById(R.id.bottom_action_bar_album_art)).getLayoutParams().width = getStatusBarHeight() + getActionBarHeight() - px;
            ((ImageView)findViewById(R.id.bottom_action_bar_album_art)).getLayoutParams().height = getStatusBarHeight() + getActionBarHeight()- px;
            ((ProgressBar)findViewById(R.id.progress_top)).setPadding(getStatusBarHeight() + getActionBarHeight()- px, 0, 0, 0);
            ((ThemeableTextView)findViewById(R.id.bottom_action_bar_line_one)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            ((ThemeableTextView)findViewById(R.id.bottom_action_bar_line_one)).setPadding(getResources().getDimensionPixelSize(R.dimen.bottom_action_bar_info_padding_left),0,0,getResources().getDimensionPixelSize(R.dimen.bottom_action_bar_info_padding_left));
            ((ThemeableTextView)findViewById(R.id.bottom_action_bar_line_two)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }

        if(savedInstanceState == null){
            if(D) Log.i(TAG, "Saved state null");
            musicBrowserPhoneFragment = new MusicBrowserPhoneFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_base_content, musicBrowserPhoneFragment).commit();
        }else {
            musicBrowserPhoneFragment = (MusicBrowserPhoneFragment)getSupportFragmentManager().getFragments().get(0);
        }


        Globals.audioPlayerActivity = this;
        mResources = new ThemeUtils(this);
        mResources.setOverflowStyle(this);
        initGoogleApiClient();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mToken = MusicUtils.bindToService(this, this);
        mImageFetcher = BassoUtils.getImageFetcher(this);
        mTimeHandler = new TimeHandler(this);
        mPlaybackStatus = new PlaybackStatus(this);
        actionBar = getActionBar();
        mResources.themeActionBar(actionBar, getString(R.string.app_name));
        initPlaybackControls();
    }

    @Override
    public int setContentView(){
        return R.layout.activity_player_base;
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        startPlayback();
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        mService = IBassoService.Stub.asInterface(service);
        startPlayback();
        updatePlaybackControls();
        updateNowPlayingInfo();
        updateCurrentPlayingWear();
        invalidateOptionsMenu();
        ((QueueFragment)mPagerAdapter.getFragment(0)).refreshQueue();
        if(mEqualizerPagerAdapter.getCount() == 0){
            mEqualizerPagerAdapter.add(EqualizerFragment.class, null);
        }
    }

    private final View.OnClickListener mOpenCurrentAlbumProfile = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            if (MusicUtils.getCurrentAudioId() != -1) {
                NavUtils.openAlbumProfile(AudioPlayerActivity.this, MusicUtils.getAlbumName(),
                        MusicUtils.getArtistName(), MusicUtils.getCurrentAlbumId());
            } else {
                MusicUtils.shuffleAll(AudioPlayerActivity.this);
            }
        }
    };
    public int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mService = null;
    }

    @Override
    public void onProgressChanged(final SeekBar bar, final int progress, final boolean fromuser) {
        if (!fromuser || mService == null) {
            return;
        }
        final long now = SystemClock.elapsedRealtime();
        if (now - mLastSeekEventTime > 250) {
            mLastSeekEventTime = now;
            mLastShortSeekEventTime = now;
            mPosOverride = MusicUtils.duration() * progress / 1000;
            MusicUtils.seek(mPosOverride);
            if (!mFromTouch) {
                mPosOverride = -1;
            }
        } else if (now - mLastShortSeekEventTime > 5) {
            mLastShortSeekEventTime = now;
            mPosOverride = MusicUtils.duration() * progress / 1000;
            refreshCurrentTimeText(mPosOverride);
        }
    }

    @Override
    public void onStartTrackingTouch(final SeekBar bar) {
        mLastSeekEventTime = 0;
        mFromTouch = true;
        mCurrentTime.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStopTrackingTouch(final SeekBar bar) {
        if (mPosOverride != -1) {
            MusicUtils.seek(mPosOverride);
        }
        mPosOverride = -1;
        mFromTouch = false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        boolean expanded = mSlidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;

        mResources.setFavoriteIcon(menu);

        for(int i = 0; i < menu.size(); i++){
            menu.getItem(i).setVisible(!expanded);
        }

        menu.findItem(R.id.menu_audio_player_share).setVisible(expanded);
        menu.findItem(R.id.menu_audio_player_delete).setVisible(expanded);
        menu.findItem(R.id.menu_audio_player_ringtone).setVisible(expanded);
        menu.findItem(R.id.menu_audio_player_equalizer).setVisible(expanded);
        menu.findItem(R.id.menu_clear_queue).setVisible(expanded);
        menu.findItem(R.id.menu_save_queue).setVisible(expanded);
        menu.findItem(R.id.menu_go_to).setVisible(expanded);
        menu.findItem(R.id.menu_favorite_player).setVisible(expanded);
        menu.findItem(R.id.menu_shuffle_player).setVisible(false);
        menu.findItem(R.id.menu_audio_player_equalizer).setVisible(false);
        menu.findItem(R.id.menu_settings).setVisible(true);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
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

        final SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        final SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(final String query) {
                NavUtils.openSearch(AudioPlayerActivity.this, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                return false;
            }
        });
        getMenuInflater().inflate(R.menu.go, menu);
        getMenuInflater().inflate(R.menu.favorite_player, menu);
        getMenuInflater().inflate(R.menu.shuffle_player, menu);
        getMenuInflater().inflate(R.menu.audio_player, menu);
        getMenuInflater().inflate(R.menu.activity_base, menu);
        getMenuInflater().inflate(R.menu.queue, menu);
        boolean expanded = mSlidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
        if(expanded && searchView != null && !searchView.isIconified()){
            searchView.onActionViewCollapsed();
        }
        menu.findItem(R.id.menu_search).setVisible(!expanded);
        menu.findItem(R.id.menu_favorite_player).setVisible(expanded);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(D) Log.i(TAG, "Home clicked");
                NavUtils.goHome(this);
                return true;
            case R.id.menu_shuffle_player:
                MusicUtils.shuffleAll(this);
                ((QueueFragment)mPagerAdapter.getFragment(0)).refreshQueue();
                return true;
            case R.id.menu_favorite_player:
                if (D) Log.d(TAG,"Favorite player pressed");
                MusicUtils.toggleFavorite();
                invalidateOptionsMenu();
                return true;
            case R.id.menu_audio_player_ringtone:
                MusicUtils.setRingtone(this, MusicUtils.getCurrentAudioId());
                return true;
            case R.id.menu_audio_player_share:
                shareCurrentTrack();
                return true;
            case R.id.menu_audio_player_equalizer:
                NavUtils.openEffectsPanel(this);
                return true;
            case R.id.menu_settings:
                NavUtils.openSettings(this);
                return true;
            case R.id.menu_audio_player_delete:
                DeleteDialog.newInstance(MusicUtils.getTrackName(), new long[] {
                    MusicUtils.getCurrentAudioId()
                }, null).show(getSupportFragmentManager(), "DeleteDialog");
                return true;
            case R.id.menu_save_queue:
                NowPlayingCursor queue = (NowPlayingCursor) QueueLoader.makeQueueCursor(this);
                CreateNewPlaylist.getInstance(MusicUtils.getSongListForCursor(queue)).show(getSupportFragmentManager(), "CreatePlaylist");
                queue.close();
                queue = null;
                return true;
            case R.id.menu_clear_queue:
                MusicUtils.clearQueue();
                NavUtils.goHome(this);
                return true;
            case R.id.menu_go_to_album:
                NavUtils.openAlbumProfile(AudioPlayerActivity.this, MusicUtils.getAlbumName(), MusicUtils.getArtistName(), MusicUtils.getIdForAlbum(AudioPlayerActivity.this,MusicUtils.getAlbumName(), MusicUtils.getArtistName()));
                return true;
            case R.id.menu_go_to_artist:
                NavUtils.openArtistProfile(AudioPlayerActivity.this, MusicUtils.getArtistName());
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDelete(long[] ids) {
        ((QueueFragment)mPagerAdapter.getFragment(0)).refreshQueue();
        if (MusicUtils.getQueue().length == 0) {
           mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if(!searchView.isIconified()){
                if(D) Log.i(TAG,"Up clicked");
                searchView.onActionViewCollapsed();
                return;
            }
            if (musicBrowserPhoneFragment.currentItem() == 0) {
                if (D) Log.d(TAG, "Back pressed in folder fragment");
                if (!musicBrowserPhoneFragment.getFolderFragment().onBackPressed())
                    super.onBackPressed();
            } else
                super.onBackPressed();
        }catch (Exception e){}
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePlaybackControls();
        updateNowPlayingInfo();
        ((QueueFragment)mPagerAdapter.getFragment(0)).refreshQueue();
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
        filter.addAction(MusicPlaybackService.SERVICE_DEAD);
        filter.addAction("com.basso.basso.UPDATE_ART");
        filter.addAction("com.basso.basso.UPDATE_LYRICS");
        filter.addAction("com.basso.basso.TOGGLE_LYRICS");
        registerReceiver(mPlaybackStatus, filter);
        final long next = refreshCurrentTime();
        queueNextRefresh(next);
        if(mAlbumArtPagerAdapter != null && mAlbumArtPager != null && mImageFetcher != null) {
            AlbumArtFragment cur = (AlbumArtFragment) mAlbumArtPagerAdapter.getItem(mAlbumArtPager.getCurrentItem());
            if(cur != null && cur.albumArt != null) {
                mImageFetcher.loadCurrentArtwork(cur.albumArt, ImageWorker.ImageSource.MAIN_ARTWORK);
            }
        }
        MusicUtils.notifyForegroundStateChanged(this, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (D) Log.d(TAG,"App stopped");
        if (D) Log.d(TAG,"App is finishing " + this.isFinishing());
        cleanAll();
        MusicUtils.notifyForegroundStateChanged(this, false);
        mImageFetcher.flush();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (D) Log.d(TAG,"App is finishing in onPause" + this.isFinishing());
    }

    @Override
    protected void onDestroy() {
        if (D) Log.d(TAG,"On destroy audio player activity called");
        new SendToDataLayerThread("/dismissNotification", "").start();
        Wearable.MessageApi.removeListener(googleClient, this);
        mIsPaused = false;
        isAlive = false;
        mTimeHandler.removeMessages(REFRESH_TIME);
        cleanAll();
        if (mService != null) {
            MusicUtils.unbindFromService(mToken);
            mToken = null;
        }
        super.onDestroy();
        try {
            unregisterReceiver(mPlaybackStatus);
        } catch (final Throwable e) {
        }
        if (D) Log.d(TAG,"On destroy ended");
    }
    public void cleanAll(){
        File[] list = getCacheDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.endsWith(".dex") || filename.endsWith(".jar"))
                    return true;
                else
                    return false;
            }
        });
        for(File file: list){
            file.delete();
        }
    }

    @SuppressWarnings("deprecation")
    private void initPlaybackControls() {
        mPageContainer = (FrameLayout)findViewById(R.id.audio_player_pager_container);
        mPageContainer.setBackgroundDrawable(mResources.getDrawable("audio_player_pager_container"));
        mAudioPlayerHeader = (LinearLayout)findViewById(R.id.audio_player_header);
        mAudioPlayerHeader.setOnClickListener(mOpenAlbumProfile);
        mPagerAdapter = new PagerAdapter(this);
        mEqualizerPagerAdapter = new PagerAdapter(this);
        mPagerAdapter.add(QueueFragment.class, null);
        mLyricsPagerContainer = (FrameLayout)findViewById(R.id.lyrics_pager_container);
        mLyricsPager = (ViewPager)findViewById(R.id.lyrics_pager);
        mLyricsPagerAdapter = new PagerAdapter(this);
        mLyricsPagerAdapter.add(LyricsFragment.class, null);
        mLyricsPager.setAdapter(mLyricsPagerAdapter);
        mViewPager = (ViewPager)findViewById(R.id.audio_player_pager);
        mEqualizerPager = (ViewPager)findViewById(R.id.equalizer_pader);
        mAlbumArtPager = (ViewPager)findViewById(R.id.album_art_pager);
        mAlbumArtPagerAdapter = new AlbumArtPagerAdapter(getSupportFragmentManager());
        mAlbumArtPagerAdapter.addFragment(new AlbumArtFragment());
        mAlbumArtPagerAdapter.addFragment(new AlbumArtFragment());
        mAlbumArtPagerAdapter.addFragment(new AlbumArtFragment());
        mAlbumArtPager.setAdapter(mAlbumArtPagerAdapter);
        mAlbumArtPager.setOffscreenPageLimit(3);
        mAlbumArtPager.setCurrentItem(1);
        mAlbumArtPager.setOnPageChangeListener(new AlbumArtPageListener());
        mViewPager.setAdapter(mPagerAdapter);
        mEqualizerPager.setAdapter(mEqualizerPagerAdapter);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
        mEqualizerPager.setOffscreenPageLimit(mEqualizerPagerAdapter.getCount() - 1);
        mPlayPauseButton = (PlayPauseButton)findViewById(R.id.action_button_play);
        mEqualizerLayout = (FrameLayout)findViewById(R.id.equalizer_pager_container);
        mShuffleButton = (ShuffleButton)findViewById(R.id.action_button_shuffle);
        mRepeatButton = (RepeatButton)findViewById(R.id.action_button_repeat);
        mPreviousButton = (RepeatingImageButton)findViewById(R.id.action_button_previous);
        mNextButton = (RepeatingImageButton)findViewById(R.id.action_button_next);
        mPlayPauseButtonPanel = (PlayPauseButton)findViewById(R.id.action_button_play_panel);
        mShuffleButtonPanel = (ShuffleButton)findViewById(R.id.action_button_shuffle_panel);
        mRepeatButtonPanel = (RepeatButton)findViewById(R.id.action_button_repeat_panel);
        mPreviousButtonPanel = (RepeatingImageButton)findViewById(R.id.action_button_previous_panel);
        mNextButtonPanel = (RepeatingImageButton)findViewById(R.id.action_button_next_panel);
        equalizerButton = (ImageButton)findViewById(R.id.equalizer_button);
        equalizerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && BassoUtils.isSW600P(AudioPlayerActivity.this)){
                    if(mPageContainer.getVisibility() == View.VISIBLE){
                        hideQueue();
                    }else{
                        showQueue();
                    }
                }else {
                    if (mEqualizerLayout.getVisibility() == View.VISIBLE) {
                        hideEqualizer();
                    } else {
                        hideQueue();
                        showEqualizer();
                        mEqualizerLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        queueButton = (ImageButton)findViewById(R.id.queue_button);
        queueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && BassoUtils.isSW600P(AudioPlayerActivity.this)){
                    if(mPageContainer.getVisibility() == View.VISIBLE){
                        hideQueue();
                    }else{
                        showQueue();
                    }
                }else {
                    if (mPageContainer.getVisibility() == View.VISIBLE) {
                        hideQueue();
                    } else {
                        hideEqualizer();
                        showQueue();
                    }
                }
            }
        });
        mTrackName = (TextView)findViewById(R.id.audio_player_track_name);
        mArtistName = (TextView)findViewById(R.id.audio_player_artist_name);
        mCurrentTime = (TextView)findViewById(R.id.audio_player_current_time);
        mCurrentTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mTotalTime = (TextView)findViewById(R.id.audio_player_total_time);
        mTotalTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mProgress = (SeekBar)findViewById(android.R.id.progress);
        mProgress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mProgressTop = (ProgressBar)findViewById(R.id.progress_top);
        mAudioControls = (LinearLayout)findViewById(R.id.audio_player_controlss);
        mAudioControls.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        (findViewById(R.id.relative_make_not_clickable)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        (findViewById(R.id.audio_player_footer)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mTrackNameBottom = (TextView)findViewById(R.id.bottom_action_bar_line_one);
        mArtistNameBottom = (TextView)findViewById(R.id.bottom_action_bar_line_two);
        mAlbumArtBottom = (ImageView)findViewById(R.id.bottom_action_bar_album_art);
        mAlbumArtBottom.setOnClickListener(mOpenCurrentAlbumProfile);
        mAlbumArtBottom.setBackgroundColor(Color.WHITE);
        mSlidingPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    if (D) Log.d(TAG,"Panel collapsed");
                    mResources.themeActionBar(getActionBar(), getString(R.string.app_name));
                    invalidateOptionsMenu();
                }

                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                    if (D) Log.d(TAG,"Panel expanded");
                    mResources.themeActionBar(getActionBar(), getString(R.string.now_playing));
                    invalidateOptionsMenu();
                }
            }
        });

        mPreviousButton.setRepeatListener(mRewindListener);
        mNextButton.setRepeatListener(mFastForwardListener);
        mPreviousButtonPanel.setRepeatListener(mRewindListener);
        mNextButtonPanel.setRepeatListener(mFastForwardListener);
        mProgress.setOnSeekBarChangeListener(this);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE  && BassoUtils.isSW600P(AudioPlayerActivity.this)){
            hideEqualizer();
            showQueue();
        }
        if (D) Log.d(TAG,"Hiding panel");
        mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    public void toggleLyrics(){
        if(mLyricsPagerContainer != null){
            if(mLyricsPagerContainer.getVisibility() == View.GONE){
                if(D) Log.i(TAG, "Lyrics visible");
                mLyricsPagerContainer.setVisibility(View.VISIBLE);
            } else {
                if(D) Log.i(TAG, "Lyrics hidden");
                mLyricsPagerContainer.setVisibility(View.GONE);
            }
        }
    }

    public int getStatusBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void updateNowPlayingInfo() {
        if(MusicUtils.getCurrentAudioId() == -1) {
            if (D) Log.d(TAG,"Hiding panel in update playing");
            mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            return;
        }
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE  && BassoUtils.isSW600P(AudioPlayerActivity.this)){
            ((QueueFragment)mPagerAdapter.getFragment(0)).scrollToCurrentSong();
        }
        updateLyrics();
        updateArt();
        updateWear();
        mTrackName.setText(MusicUtils.getTrackName());
        mArtistName.setText(MusicUtils.getArtistName());
        mTrackNameBottom.setText(MusicUtils.getTrackName());
        mArtistNameBottom.setText(MusicUtils.getArtistName());
        mTotalTime.setText(MusicUtils.makeTimeString(this, MusicUtils.duration() / 1000));
        AlbumArtFragment cur =(AlbumArtFragment) mAlbumArtPagerAdapter.getItem(mAlbumArtPager.getCurrentItem());
        mImageFetcher.loadCurrentArtwork(cur.albumArt, ImageWorker.ImageSource.MAIN_ARTWORK);
        mImageFetcher.loadCurrentArtwork(mAlbumArtBottom, ImageWorker.ImageSource.BOTTOM_ACTION_BAR);
        mImageFetcher.loadCurrentArtwork(mAlbumArtSmall, ImageWorker.ImageSource.BOTTOM_ACTION_BAR);
        queueNextRefresh(1);
        if (D) Log.d(TAG,"Showing panel");
        mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
    }

    private long parseIdFromIntent(Intent intent, String longKey,
        String stringKey, long defaultId) {
        long id = intent.getLongExtra(longKey, -1);
        if (id < 0) {
            String idString = intent.getStringExtra(stringKey);
            if (idString != null) {
                try {
                    id = Long.parseLong(idString);
                } catch (NumberFormatException e) {}
            }
        }
        return id;
    }

    private void startPlayback() {
        Intent intent = getIntent();

        if (intent == null || mService == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (uri != null && uri.toString().length() > 0) {
            MusicUtils.playFile(this, uri);
            handled = true;
        } else if (Playlists.CONTENT_TYPE.equals(mimeType)) {
            long id = parseIdFromIntent(intent, "playlistId", "playlist", -1);
            if (id >= 0) {
                MusicUtils.playPlaylist(this, id);
                handled = true;
            }
        } else if (Albums.CONTENT_TYPE.equals(mimeType)) {
            long id = parseIdFromIntent(intent, "albumId", "album", -1);
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicUtils.playAlbum(this, id, position);
                handled = true;
            }
        } else if (Artists.CONTENT_TYPE.equals(mimeType)) {
            long id = parseIdFromIntent(intent, "artistId", "artist", -1);
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicUtils.playArtist(this, id, position);
                handled = true;
            }
        }

        if (handled) {
            setIntent(new Intent());
            ((QueueFragment)mPagerAdapter.getFragment(0)).refreshQueue();
        }
    }

    private void updatePlaybackControls() {
        mPlayPauseButton.updateState();
        mShuffleButton.updateShuffleState();
        mRepeatButton.updateRepeatState();
        mPlayPauseButtonPanel.updateState();
        mShuffleButtonPanel.updateShuffleState();
        mRepeatButtonPanel.updateRepeatState();
    }

    private void queueNextRefresh(final long delay) {
        if (!mIsPaused) {
            final Message message = mTimeHandler.obtainMessage(REFRESH_TIME);
            mTimeHandler.removeMessages(REFRESH_TIME);
            mTimeHandler.sendMessageDelayed(message, delay);
        }
    }

    private void scanBackward(final int repcnt, long delta) {
        if (mService == null) {
            return;
        }
        if (repcnt == 0) {
            mStartSeekPos = MusicUtils.position();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                delta = delta * 10;
            } else {
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos - delta;
            if (newpos < 0) {
                MusicUtils.previous(this);
                final long duration = MusicUtils.duration();
                mStartSeekPos += duration;
                newpos += duration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicUtils.seek(newpos);
                mLastSeekEventTime = delta;
            }
            if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();
        }
    }

    private void scanForward(final int repcnt, long delta) {
        if (mService == null) {
            return;
        }
        if (repcnt == 0) {
            mStartSeekPos = MusicUtils.position();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                delta = delta * 10;
            } else {
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos + delta;
            final long duration = MusicUtils.duration();
            if (newpos >= duration) {
                MusicUtils.next();
                mStartSeekPos -= duration;
                newpos -= duration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicUtils.seek(newpos);
                mLastSeekEventTime = delta;
            }
            if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();
        }
    }

    private void refreshCurrentTimeText(final long pos) {
        mCurrentTime.setText(MusicUtils.makeTimeString(this, pos / 1000));
    }

    private long refreshCurrentTime() {
        if (mService == null) {
            return 500;
        }
        try {
            final long pos = mPosOverride < 0 ? MusicUtils.position() : mPosOverride;
            if (pos >= 0 && MusicUtils.duration() > 0) {
                refreshCurrentTimeText(pos);
                final int progress = (int)(1000 * pos / MusicUtils.duration());
                mProgress.setProgress(progress);
                mProgressTop.setProgress(progress);
                if (mFromTouch) {
                    return 500;
                } else if (MusicUtils.isPlaying()) {
                    mCurrentTime.setVisibility(View.VISIBLE);
                } else {
                    final int vis = mCurrentTime.getVisibility();
                    mCurrentTime.setVisibility(vis == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                    return 500;
                }
            } else {
                mCurrentTime.setText("--:--");
                mProgress.setProgress(1000);
                mProgressTop.setProgress(1000);
            }
            final long remaining = 1000 - pos % 1000;
            int width = mProgress.getWidth();
            if (width == 0) {
                width = 320;
            }
            final long smoothrefreshtime = MusicUtils.duration() / width;
            if (smoothrefreshtime > remaining) {
                return remaining;
            }
            if (smoothrefreshtime < 20) {
                return 20;
            }
            return smoothrefreshtime;
        } catch (final Exception ignored) {

        }
        return 500;
    }

    private void fade(final View v, final float alpha) {
        final ObjectAnimator fade = ObjectAnimator.ofFloat(v, "alpha", alpha);
        fade.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.accelerate_decelerate_interpolator));
        fade.setDuration(400);
        fade.start();
    }

    private void hideQueue() {
        mPageContainer.setVisibility(View.INVISIBLE);
        mAudioPlayerHeader.setOnClickListener(mOpenAlbumProfile);
        queueButton.setImageResource(R.drawable.ic_playlist_button_thick_default);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE  && BassoUtils.isSW600P(AudioPlayerActivity.this)){
            mEqualizerLayout.setVisibility(View.VISIBLE);
            fade(mPageContainer, 0f);
            fade(mEqualizerLayout, 1f);
            equalizerButton.setImageResource(R.drawable.ic_equalizer__pressed);
        }else{
            fade(mPageContainer, 0f);
            fade(mAlbumArtPager, 1f);
        }
    }

    public void showQueue() {
        mPageContainer.setVisibility(View.VISIBLE);
        mAudioPlayerHeader.setOnClickListener(mScrollToCurrentSong);
        queueButton.setImageResource(R.drawable.ic_playlist_button_thick_pressed);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE  && BassoUtils.isSW600P(AudioPlayerActivity.this)){
            mEqualizerLayout.setVisibility(View.INVISIBLE);
            fade(mEqualizerLayout, 0f);
            fade(mPageContainer, 1f);
            equalizerButton.setImageResource(R.drawable.ic_equalizer_default);
        } else {
            fade(mAlbumArtPager, 0f);
            fade(mPageContainer, 1f);
        }
    }


    private void showEqualizer(){
        mEqualizerLayout.setVisibility(View.VISIBLE);
        mAudioPlayerHeader.setOnClickListener(mOpenAlbumProfile);
        equalizerButton.setImageResource(R.drawable.ic_equalizer__pressed);
        fade(mAlbumArtPager, 0f);
        fade(mEqualizerLayout, 1f);
    }

    private void hideEqualizer(){
        mEqualizerLayout.setVisibility(View.INVISIBLE);
        mAudioPlayerHeader.setOnClickListener(mOpenAlbumProfile);
        equalizerButton.setImageResource(R.drawable.ic_equalizer_default);
        fade(mEqualizerLayout, 0f);
        fade(mAlbumArtPager, 1f);
    }

    private void shareCurrentTrack() {
        if (MusicUtils.getTrackName() == null || MusicUtils.getArtistName() == null) {
            return;
        }
        final Intent shareIntent = new Intent();
        final String shareMessage = getString(R.string.now_listening_to,
                MusicUtils.getTrackName(), MusicUtils.getArtistName());

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_track_using)));
    }

    private final RepeatingImageButton.RepeatListener mRewindListener = new RepeatingImageButton.RepeatListener() {

        @Override
        public void onRepeat(final View v, final long howlong, final int repcnt) {
            scanBackward(repcnt, howlong);
        }
    };

    private final RepeatingImageButton.RepeatListener mFastForwardListener = new RepeatingImageButton.RepeatListener() {

        @Override
        public void onRepeat(final View v, final long howlong, final int repcnt) {
            scanForward(repcnt, howlong);
        }
    };

    private final OnClickListener mToggleHiddenPanel = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            if (mPageContainer.getVisibility() == View.VISIBLE) {
                mAudioPlayerHeader.setOnClickListener(mOpenAlbumProfile);
            } else {
                if(mEqualizerLayout.getVisibility() == View.VISIBLE) {
                    mEqualizerLayout.setVisibility(View.GONE);
                    equalizerButton.setImageResource(R.drawable.ic_equalizer_default);
                }
                mAudioPlayerHeader.setOnClickListener(mScrollToCurrentSong);
            }
        }
    };

    private final OnClickListener mOpenAlbumProfile = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            NavUtils.openAlbumProfile(AudioPlayerActivity.this, MusicUtils.getAlbumName(),
                    MusicUtils.getArtistName(), MusicUtils.getCurrentAlbumId());
        }
    };

    private final OnClickListener mScrollToCurrentSong = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            ((QueueFragment)mPagerAdapter.getFragment(0)).scrollToCurrentSong();
        }
    };

    public void updateLyrics(){
        if(mLyricsPagerAdapter!= null) {
            LyricsFragment lyricsFragment = (LyricsFragment) mLyricsPagerAdapter.getFragment(0);
            if(lyricsFragment != null)
                lyricsFragment.fetchLyrics(true);
        }
    }
    public void updateArt(){
        if(mAlbumArtPagerAdapter!= null) {
            AlbumArtFragment cur = (AlbumArtFragment) mAlbumArtPagerAdapter.getItem(mAlbumArtPager.getCurrentItem());
            if(cur!=null)
                mImageFetcher.loadCurrentArtwork(cur.albumArt, ImageWorker.ImageSource.MAIN_ARTWORK);
        }
    }

    private static final class TimeHandler extends Handler {

        private final WeakReference<AudioPlayerActivity> mAudioPlayer;

        public TimeHandler(final AudioPlayerActivity player) {
            mAudioPlayer = new WeakReference<AudioPlayerActivity>(player);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case REFRESH_TIME:
                    final long next = mAudioPlayer.get().refreshCurrentTime();
                    mAudioPlayer.get().queueNextRefresh(next);
                    break;
                default:
                    break;
            }
        }
    };

    private static final class PlaybackStatus extends BroadcastReceiver {

        private final WeakReference<AudioPlayerActivity> mReference;

        public PlaybackStatus(final AudioPlayerActivity activity) {
            mReference = new WeakReference<AudioPlayerActivity>(activity);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action.equals(MusicPlaybackService.META_CHANGED)) {
                mReference.get().updateNowPlayingInfo();
                mReference.get().invalidateOptionsMenu();
                mReference.get().updateCurrentPlayingWear();
            } else if (action.equals(MusicPlaybackService.PLAYSTATE_CHANGED)) {
                mReference.get().mPlayPauseButton.updateState();
                mReference.get().mPlayPauseButtonPanel.updateState();
                mReference.get().updateWearPlaystate();
            } else if (action.equals(MusicPlaybackService.REPEATMODE_CHANGED)
                    || action.equals(MusicPlaybackService.SHUFFLEMODE_CHANGED)) {
                mReference.get().mRepeatButton.updateRepeatState();
                mReference.get().mShuffleButton.updateShuffleState();
                mReference.get().mRepeatButtonPanel.updateRepeatState();
                mReference.get().sendRepeatState();
                mReference.get().sendShuffleState();
                mReference.get().mShuffleButtonPanel.updateShuffleState();
            } else if(action.equals("com.basso.basso.TOGGLE_LYRICS")) {
                mReference.get().toggleLyrics();
            } else if(action.equals("com.basso.basso.UPDATE_LYRICS")){
                mReference.get().updateLyrics();
            } else if(action.equals("com.basso.basso.UPDATE_ART")){
                mReference.get().updateArt();
            } else if(action.equals(MusicPlaybackService.SERVICE_DEAD)){
                mReference.get().cleanAll();
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener( googleClient, this );
        isAlive = true;
        updateWear();
        sendShuffleState();
        sendBalance();
        sendRepeatState();
        updateWearPlaystate();
        updateCurrentPlayingWear();
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    private void initGoogleApiClient() {
        googleClient = new GoogleApiClient.Builder( this )
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void updateWearPlaystate(){
        if(MusicUtils.isPlaying()){
            new SendToDataLayerThread("/playstateNotif", "pause").start();
            new SendToDataLayerThread("/playstateData", "pause").start();
            new SendToDataLayerThread("/playstateMain", "pause").start();
        }else{
            new SendToDataLayerThread("/playstateNotif", "play").start();
            new SendToDataLayerThread("/playstateData", "play").start();
            new SendToDataLayerThread("/playstateMain", "play").start();
        }
    }

    public void updateWear(){
        try {
            JSONObject tracks = MusicUtils.getJSONTracks(getApplicationContext());
            new SendToDataLayerThread("/tracks", tracks.toString()).start();
            JSONObject albums = MusicUtils.getJSONAlbums(getApplicationContext());
            new SendToDataLayerThread("/albums", albums.toString()).start();
            JSONObject artists = MusicUtils.getJSONArtists(getApplicationContext());
            new SendToDataLayerThread("/artists", artists.toString()).start();
            JSONObject playlists = MusicUtils.getJSONPlaylists(getApplicationContext());
            new SendToDataLayerThread("/playlists", playlists.toString()).start();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    public void updateCurrentPlayingWear(){
        new AsyncTask<Void, Void, Void>(){
            protected Void doInBackground(Void...params){
                if (googleClient.isConnected() && mService != null) {
                    if (D) Log.d(TAG,"Updating current wear called");
                    try {
                        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/update_current");
                        putDataMapRequest.getDataMap().putString("/track_title", mService.getTrackName());
                        putDataMapRequest.getDataMap().putString("/track_artist", mService.getArtistName());
                        putDataMapRequest.getDataMap().putString("/track_album", mService.getAlbumName());
                        putDataMapRequest.getDataMap().putLong("time", new Date().getTime());
                        Bitmap icon = mImageFetcher.getArtwork();
                        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        if(icon != null) {
                            icon.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                            Asset asset = Asset.createFromBytes(byteStream.toByteArray());
                            putDataMapRequest.getDataMap().putAsset("/track_cover", asset);
                        }
                        PutDataRequest request = putDataMapRequest.asPutDataRequest();

                        Wearable.DataApi.putDataItem(googleClient, request)
                                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {
                                        if(D) Log.d("Current updating...", "putDataItem status: "
                                                + dataItemResult.getStatus().toString());
                                    }
                                });

                    }catch (RemoteException ex){}
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void smthg){

            }
        }.execute();
    }

    class SendToDataLayerThread extends Thread {
        String path;
        String message;

        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            if(D) Log.v("myTagMainLayer","Google client status " + googleClient.isConnected());

            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if(path.equals("/dismissNotification")) {
                    if(D) Log.v("myTagMainLayer", "Notification dismiss sent");
                    if(result.getStatus().isSuccess()){
                        if(googleClient!=null && googleClient.isConnected()){
                            googleClient.disconnect();
                        }
                    }
                }
                if (result.getStatus().isSuccess()) {
                    if(D) Log.v("myTag", "Message: sent to: " + node.getDisplayName());
                }
                else {
                    if(D) Log.v("myTag", "ERROR: failed to send Message");
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if( messageEvent.getPath().equals("/getAlbum") ) {
            try {
                new SendToDataLayerThread(new String(messageEvent.getData()), MusicUtils.getJSONSongsFromAlbum(this, new String(messageEvent.getData())).toString()).start();
                if (D) Log.d(TAG,"Album tracks sent");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if( messageEvent.getPath().equals("/getArtist") ) {
            try {
                new SendToDataLayerThread(new String(messageEvent.getData()), MusicUtils.getJSONSongsFromArtist(this, new String(messageEvent.getData())).toString()).start();
                if (D) Log.d(TAG,"Artist tracks sent");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if( messageEvent.getPath().equals("/getPlaylist") ) {
            try {
                new SendToDataLayerThread(new String(messageEvent.getData()), MusicUtils.getJSONSongsFromPlaylist(this, new String(messageEvent.getData())).toString()).start();
                if (D) Log.d(TAG,"Playlist tracks sent");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(messageEvent.getPath().equals("/getTracks")){
            try {
                JSONObject tracks = MusicUtils.getJSONTracks(getApplicationContext());
                new SendToDataLayerThread("/tracks", tracks.toString()).start();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(messageEvent.getPath().equals("playalbum")){
            String albumIdAndPosition = new String(messageEvent.getData());
            String[] tokens = albumIdAndPosition.split("position");
            MusicUtils.playAlbum(this, Long.parseLong(tokens[0]), Integer.parseInt(tokens[1]));
        }
        if(messageEvent.getPath().equals("playartist")){
            String artistIdAndPosition = new String(messageEvent.getData());
            if (D) Log.d(TAG,"Play artist received");
            String[] tokens = artistIdAndPosition.split("position");
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[] {"*"},MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ARTIST_KEY + " = '" + tokens[0].replace("'","''")+"'", null , null);
            if(cursor.getCount() != 0){
                cursor.moveToFirst();
                long artist_id = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
                cursor.close();
                MusicUtils.playAll(this, MusicUtils.getSongListForCursor(ArtistSongLoader.makeArtistSongCursor(this, artist_id)), Integer.parseInt(tokens[1]), false);
            }
        }
        if(messageEvent.getPath().equals("playsong")){
            String position = new String(messageEvent.getData());
            int pos = Integer.parseInt(position);
            MusicUtils.playAll(this, MusicUtils.getSongListForCursor(SongLoader.makeSongCursor(this)), pos, false);
        }
        if(messageEvent.getPath().equals("playplaylist")){
            String playlistIdAndPosition = new String(messageEvent.getData());
            String[] tokens = playlistIdAndPosition.split("position");
            MusicUtils.playAll(this, MusicUtils.getSongListForCursor(PlaylistSongLoader.makePlaylistSongCursor(this, Long.parseLong(tokens[0]))), Integer.parseInt(tokens[1]), false);
        }
        if(messageEvent.getPath().equals("playOrPause")){
            MusicUtils.playOrPause();
        }
        if(messageEvent.getPath().equals("nextTrack")){
            MusicUtils.next();
        }
        if(messageEvent.getPath().equals("prevTrack")){
            MusicUtils.previous(getApplicationContext());
        }
        if(messageEvent.getPath().equals("/getAlbums")){
            try {
                JSONObject albums = MusicUtils.getJSONAlbums(getApplicationContext());
                new SendToDataLayerThread("/albums", albums.toString()).start();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        if(messageEvent.getPath().equals("/getArtists")){
            try {
                JSONObject artists = MusicUtils.getJSONArtists(getApplicationContext());
                new SendToDataLayerThread("/artists", artists.toString()).start();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        if(messageEvent.getPath().equals("/getPLaylists")){
            try {
                JSONObject playlists = MusicUtils.getJSONPlaylists(getApplicationContext());
                new SendToDataLayerThread("/playlists", playlists.toString()).start();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(messageEvent.getPath().equals("getBalance")){
            sendBalance();
        }

        if(messageEvent.getPath().equals("getPlaystate")){
            if(MusicUtils.isPlaying()){
                new SendToDataLayerThread("setPlaystate", "pause");
            } else {
                new SendToDataLayerThread("setPlaystate","play");
            }
        }

        if(messageEvent.getPath().equals("getShuffleState")){
           sendShuffleState();
        }

        if(messageEvent.getPath().equals("getRepeatState")){
            sendRepeatState();
        }
        if(messageEvent.getPath().equals("getMusicVolume")){
            sendVolume();
        }

        if(messageEvent.getPath().equals("getTrackName")){
            new SendToDataLayerThread("setTrackName", MusicUtils.getTrackName()).start();
        }

        if(messageEvent.getPath().equals("getArtistName")){
            new SendToDataLayerThread("setArtistName", MusicUtils.getArtistName()).start();
        }
        if(messageEvent.getPath().equals("setBalance")){
            float balance = Float.parseFloat(new String(messageEvent.getData()));
            MusicUtils.setBalance(balance);
        }
        if(messageEvent.getPath().equals("setShuffleMode")){
            if(D) Log.i(TAG, "Shuffle clicked from wear");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mShuffleButtonPanel.performClick();
                }
            });
        }
        if(messageEvent.getPath().equals("setRepeatMode")){
            if(D) Log.i(TAG, "Repeat clicked from wear");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRepeatButtonPanel.performClick();
                }
            });
        }
        if(messageEvent.getPath().equals("setVolume")){
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int volume = Integer.parseInt(new String(messageEvent.getData()));
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,volume,volume);
        }
    }

    public void sendVolume(){
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxValue = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if(D) Log.i(TAG, "Music volume " + currentVolume + " / " + maxValue);
        new SendToDataLayerThread("setMusicVolume", currentVolume+"/"+maxValue).start();
    }

    public void sendBalance(){
        new SendToDataLayerThread("setBalance",getSharedPreferences("Service", 0).getFloat("balance", 1.0f)+"");
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(D) Log.i(TAG, "Volume up in main activity");
                sendVolume();
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(D) Log.i(TAG, "Volume up in main activity");
                sendVolume();
                return super.onKeyDown(keyCode, event);
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
    public void sendRepeatState(){
        if(MusicUtils.getRepeatMode() == MusicPlaybackService.REPEAT_NONE){
            new SendToDataLayerThread("setRepeatState","none").start();
        } else if(MusicUtils.getRepeatMode() == MusicPlaybackService.REPEAT_ALL){
            new SendToDataLayerThread("setRepeatState", "all").start();
        } else if(MusicUtils.getRepeatMode() == MusicPlaybackService.REPEAT_CURRENT){
            new SendToDataLayerThread("setRepeatState","current").start();
        }
    }


    public void sendShuffleState(){
        if(MusicUtils.getShuffleMode() == MusicPlaybackService.SHUFFLE_NONE){
            new SendToDataLayerThread("setShuffleState","none").start();
        }else if(MusicUtils.getShuffleMode() == MusicPlaybackService.SHUFFLE_AUTO){
            new SendToDataLayerThread("setShuffleState", "auto").start();
        } else if(MusicUtils.getShuffleMode() == MusicPlaybackService.SHUFFLE_NORMAL){
            new SendToDataLayerThread("setShuffleState","normal").start();
        }
    }

    private class AlbumArtPageListener extends ViewPager.SimpleOnPageChangeListener {
        public void onPageScrollStateChanged(int state) {
            if(state == ViewPager.SCROLL_STATE_IDLE){
                int cur = mAlbumArtPager.getCurrentItem();
                if(cur == 0){
                    mAlbumArtPagerAdapter.addFragmentTo(new AlbumArtFragment(), 0);
                    mAlbumArtPagerAdapter.removeItem(3);
                    mAlbumArtPager.setAdapter(mAlbumArtPagerAdapter);
                    mAlbumArtPager.setCurrentItem(1);
                    if (MusicUtils.mService == null)
                        return;
                    try {
                        MusicUtils.mService.prev();
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
                else if ( cur == 2 ){
                    mAlbumArtPagerAdapter.addFragmentTo(new AlbumArtFragment(), 3);
                    mAlbumArtPagerAdapter.removeItem(0);
                    mAlbumArtPager.setAdapter(mAlbumArtPagerAdapter);
                    mAlbumArtPager.setCurrentItem(1);
                    if (MusicUtils.mService == null)
                        return;
                    try {
                        MusicUtils.mService.next();
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
