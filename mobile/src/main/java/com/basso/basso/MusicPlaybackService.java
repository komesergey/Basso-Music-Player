package com.basso.basso;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.basso.basso.appwidgets.AppWidgetLarge;
import com.basso.basso.appwidgets.AppWidgetLargeAlternate;
import com.basso.basso.appwidgets.AppWidgetSmall;
import com.basso.basso.appwidgets.RecentWidgetProvider;
import com.basso.basso.cache.ImageCache;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.provider.FavoritesStore;
import com.basso.basso.provider.RecentStore;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.MusicUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

import basso.IBassoService;

@SuppressLint("NewApi")
public class MusicPlaybackService extends Service {
    private static final String TAG = "MusicPlaybackService";
    private static final boolean D = false;

    public static final String PLAYSTATE_CHANGED = "com.basso.basso.playstatechanged";
    public static final String SERVICE_DEAD = "com.basso.basso.done";
    public static final String POSITION_CHANGED = "com.android.basso.positionchanged";
    public static final String META_CHANGED = "com.basso.basso.metachanged";
    public static final String QUEUE_CHANGED = "com.basso.basso.queuechanged";
    public static final String REPEATMODE_CHANGED = "com.basso.basso.repeatmodechanged";
    public static final String SHUFFLEMODE_CHANGED = "com.basso.basso.shufflemodechanged";
    public static final String Basso_PACKAGE_NAME = "com.basso.basso";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";
    public static final String SERVICECMD = "com.basso.basso.musicservicecommand";
    public static final String TOGGLEPAUSE_ACTION = "com.basso.basso.togglepause";
    public static final String PAUSE_ACTION = "com.basso.basso.pause";
    public static final String STOP_ACTION = "com.basso.basso.stop";
    public static final String PREVIOUS_ACTION = "com.basso.basso.previous";
    public static final String NEXT_ACTION = "com.basso.basso.next";
    public static final String REPEAT_ACTION = "com.basso.basso.repeat";
    public static final String SHUFFLE_ACTION = "com.basso.basso.shuffle";
    public static final String FOREGROUND_STATE_CHANGED = "com.basso.basso.fgstatechanged";
    public static final String NOW_IN_FOREGROUND = "nowinforeground";
    public static final String FROM_MEDIA_BUTTON = "frommediabutton";
    public static final String REFRESH = "com.basso.basso.refresh";
    private static final String SHUTDOWN = "com.basso.basso.shutdown";
    public static final String UPDATE_LOCKSCREEN = "com.basso.basso.updatelockscreen";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String CMDNOTIF = "buttonId";
    private static final int IDCOLIDX = 0;
    public static final int NOW = 1;
    public static final int NEXT = 2;
    public static final int LAST = 3;
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;
    private static final int TRACK_ENDED = 1;
    private static final int TRACK_WENT_TO_NEXT = 2;
    private static final int RELEASE_WAKELOCK = 3;
    private static final int SERVER_DIED = 4;
    private static final int FOCUSCHANGE = 5;
    private static final int FADEDOWN = 6;
    private static final int FADEUP = 7;
    private static final int IDLE_DELAY = 60000;
    private static final long REWIND_INSTEAD_PREVIOUS_THRESHOLD = 3000;
    private static final int MAX_HISTORY_SIZE = 100;
    private static final String[] PROJECTION = new String[] {
            "audio._id AS _id", MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
    };
    private static final String[] ALBUM_PROJECTION = new String[] {
            MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.LAST_YEAR
    };
    private static final LinkedList<Integer> mHistory = Lists.newLinkedList();
    private static final Shuffler mShuffler = new Shuffler();
    private static final char HEX_DIGITS[] = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    private final IBinder mBinder = new ServiceStub(this);
    private final AppWidgetSmall mAppWidgetSmall = AppWidgetSmall.getInstance();
    private final AppWidgetLarge mAppWidgetLarge = AppWidgetLarge.getInstance();
    private final AppWidgetLargeAlternate mAppWidgetLargeAlternate = AppWidgetLargeAlternate.getInstance();
    private final RecentWidgetProvider mRecentWidgetProvider = RecentWidgetProvider.getInstance();
    private MultiPlayer mPlayer;
    private String mFileToPlay;
    private WakeLock mWakeLock;
    private AlarmManager mAlarmManager;
    private PendingIntent mShutdownIntent;
    private boolean mShutdownScheduled;
    private Cursor mCursor;
    private Cursor mAlbumCursor;
    private AudioManager mAudioManager;
    private SharedPreferences mPreferences;
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private boolean mQueueIsSaveable = true;
    private boolean mPausedByTransientLossOfFocus = false;
    private boolean mAnyActivityInForeground = false;
    private RemoteControlClient mRemoteControlClient;
    private ComponentName mMediaButtonReceiverComponent;
    private int mCardId;
    private int mPlayListLen = 0;
    private int mPlayPos = -1;
    private int mNextPlayPos = -1;
    private int mOpenFailedCounter = 0;
    private int mMediaMountedCount = 0;
    private int mShuffleMode = SHUFFLE_NONE;
    private int mRepeatMode = REPEAT_NONE;
    private int mServiceStartId = -1;
    private long[] mPlayList = null;
    private long[] mAutoShuffleList = null;
    private MusicPlayerHandler mPlayerHandler;
    private BroadcastReceiver mUnmountReceiver = null;
    private ImageFetcher mImageFetcher;
    private NotificationHelper mNotificationHelper;
    private RecentStore mRecentsCache;
    private FavoritesStore mFavoritesCache;

    @Override
    public IBinder onBind(final Intent intent) {
        if (D) Log.d(TAG, "Service bound, intent = " + intent);
        cancelShutdown();
        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        if (D) Log.d(TAG, "Service unbound");
        mServiceInUse = false;
        saveQueue(true);

        if (mIsSupposedToBePlaying || mPausedByTransientLossOfFocus) {
            return true;
        } else if (mPlayListLen > 0 || mPlayerHandler.hasMessages(TRACK_ENDED)) {
            scheduleDelayedShutdown();
            return true;
        }
        stopSelf(mServiceStartId);
        return true;
    }

    @Override
    public void onRebind(final Intent intent) {
        cancelShutdown();
        mServiceInUse = true;
    }

    @Override
    public void onCreate() {
        if (D) Log.d(TAG, "Creating service");
        super.onCreate();
        mRecentsCache = RecentStore.getInstance(this);
        mFavoritesCache = FavoritesStore.getInstance(this);
        mNotificationHelper = new NotificationHelper(this);
        mImageFetcher = ImageFetcher.getInstance(this);
        mImageFetcher.setImageCache(ImageCache.getInstance(this));
        final HandlerThread thread = new HandlerThread("MusicPlayerHandler", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mPlayerHandler = new MusicPlayerHandler(this, thread.getLooper());
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);
        setUpRemoteControlClient();
        mPreferences = getSharedPreferences("Service", 0);
        mCardId = getCardId();
        registerExternalStorageListener();
        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICECMD);
        filter.addAction(TOGGLEPAUSE_ACTION);
        filter.addAction(PAUSE_ACTION);
        filter.addAction(STOP_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);
        filter.addAction(REPEAT_ACTION);
        filter.addAction(SHUFFLE_ACTION);
        registerReceiver(mIntentReceiver, filter);
        final PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);
        final Intent shutdownIntent = new Intent(this, MusicPlaybackService.class);
        shutdownIntent.setAction(SHUTDOWN);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mShutdownIntent = PendingIntent.getService(this, 0, shutdownIntent, 0);
        scheduleDelayedShutdown();
        reloadQueue();
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
    }

    private void setUpRemoteControlClient() {
        final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mMediaButtonReceiverComponent);
        mRemoteControlClient = new RemoteControlClient(
                PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
        mAudioManager.registerRemoteControlClient(mRemoteControlClient);

        int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_STOP;

        if (BassoUtils.hasJellyBeanMR2()) {
            flags |= RemoteControlClient.FLAG_KEY_MEDIA_POSITION_UPDATE;
            mRemoteControlClient.setOnGetPlaybackPositionListener(new RemoteControlClient.OnGetPlaybackPositionListener() {
                @Override
                public long onGetPlaybackPosition() {
                    return position();
                }
            });
            mRemoteControlClient.setPlaybackPositionUpdateListener(new RemoteControlClient.OnPlaybackPositionUpdateListener() {
                @Override
                public void onPlaybackPositionUpdate(long newPositionMs) {
                    seek(newPositionMs);
                }
            });
        }

        mRemoteControlClient.setTransportControlFlags(flags);
    }

    @Override
    public void onDestroy() {
        if (D) Log.d(TAG, "Destroying service");
        super.onDestroy();
        final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);
        mAlarmManager.cancel(mShutdownIntent);
        mPlayerHandler.removeCallbacksAndMessages(null);
        mPlayer.release();
        mPlayer = null;
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
        mPlayerHandler.removeCallbacksAndMessages(null);
        closeCursor();
        unregisterReceiver(mIntentReceiver);
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
        mWakeLock.release();
        if (D) Log.d(TAG, "End destroying service");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (D) Log.d(TAG, "Got new intent " + intent + ", startId = " + startId);
        mServiceStartId = startId;

        if (intent != null) {
            final String action = intent.getAction();

            if (intent.hasExtra(NOW_IN_FOREGROUND)) {
                mAnyActivityInForeground = intent.getBooleanExtra(NOW_IN_FOREGROUND, false);
                saveQueue(true);
                updateNotification();
            }

            if (SHUTDOWN.equals(action)) {
                mShutdownScheduled = false;
                releaseServiceUiAndStop();
                return START_NOT_STICKY;
            }

            handleCommandIntent(intent);
        }
        scheduleDelayedShutdown();

        if (intent != null && intent.getBooleanExtra(FROM_MEDIA_BUTTON, false)) {
            MediaButtonIntentReceiver.completeWakefulIntent(intent);
        }

        return START_STICKY;
    }

    private void releaseServiceUiAndStop() {
        if (isPlaying()
                || mPausedByTransientLossOfFocus
                || mPlayerHandler.hasMessages(TRACK_ENDED)) {
            return;
        }

        if (D) Log.d(TAG, "Nothing is playing anymore, releasing notification");
        mNotificationHelper.killNotification();
        mAudioManager.abandonAudioFocus(mAudioFocusListener);

        if (!mServiceInUse) {
            saveQueue(true);
            stopSelf(mServiceStartId);
        }
    }

    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        final String command = SERVICECMD.equals(action) ? intent.getStringExtra(CMDNAME) : null;

        if (D) Log.d(TAG, "handleCommandIntent: action = " + action + ", command = " + command);

        if (CMDNEXT.equals(command) || NEXT_ACTION.equals(action)) {
            gotoNext(true);
        } else if (CMDPREVIOUS.equals(command) || PREVIOUS_ACTION.equals(action)) {
            if (position() < REWIND_INSTEAD_PREVIOUS_THRESHOLD) {
                prev();
            } else {
                seek(0);
                play();
            }
        } else if (CMDTOGGLEPAUSE.equals(command) || TOGGLEPAUSE_ACTION.equals(action)) {
            if (isPlaying()) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else {
                play();
            }
        } else if (CMDPAUSE.equals(command) || PAUSE_ACTION.equals(action)) {
            pause();
            mPausedByTransientLossOfFocus = false;
        } else if (CMDPLAY.equals(command)) {
            play();
        } else if (CMDSTOP.equals(command) || STOP_ACTION.equals(action)) {
            pause();
            mPausedByTransientLossOfFocus = false;
            seek(0);
            releaseServiceUiAndStop();
        } else if (REPEAT_ACTION.equals(action)) {
            cycleRepeat();
        } else if (SHUFFLE_ACTION.equals(action)) {
            cycleShuffle();
        }
    }

    private void updateNotification() {
        if (!mAnyActivityInForeground && isPlaying()) {
            mNotificationHelper.buildNotification(getAlbumName(), getArtistName(),
                    getTrackName(), getAlbumId(), getAlbumArt(), isPlaying());
        } else if (mAnyActivityInForeground) {
            mNotificationHelper.killNotification();
        }
    }

    private int getCardId() {
        final ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://media/external/fs_id"), null, null,
                null, null);
        int mCardId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            mCardId = cursor.getInt(0);
            cursor.close();
            cursor = null;
        }
        return mCardId;
    }

    public void closeExternalStorageFiles(final String storagePath) {
        stop(true);
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
    }

    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        saveQueue(true);
                        mQueueIsSaveable = false;
                        closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mMediaMountedCount++;
                        mCardId = getCardId();
                        reloadQueue();
                        mQueueIsSaveable = true;
                        notifyChange(QUEUE_CHANGED);
                        notifyChange(META_CHANGED);
                    }
                }
            };
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, filter);
        }
    }

    private void scheduleDelayedShutdown() {
        if (D) Log.v(TAG, "Scheduling shutdown in " + IDLE_DELAY + " ms");
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + IDLE_DELAY, mShutdownIntent);
        mShutdownScheduled = true;
    }

    private void cancelShutdown() {
        if (D) Log.d(TAG, "Cancelling delayed shutdown, scheduled = " + mShutdownScheduled);
        if (mShutdownScheduled) {
            mAlarmManager.cancel(mShutdownIntent);
            mShutdownScheduled = false;
        }
    }

    private void stop(final boolean goToIdle) {
        if (D) Log.d(TAG, "Stopping playback, goToIdle = " + goToIdle);
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }
        mFileToPlay = null;
        closeCursor();
        if (goToIdle) {
            scheduleDelayedShutdown();
            mIsSupposedToBePlaying = false;
        } else {
            stopForeground(false);
        }
    }

    private int removeTracksInternal(int first, int last) {
        synchronized (this) {
            if (last < first) {
                return 0;
            } else if (first < 0) {
                first = 0;
            } else if (last >= mPlayListLen) {
                last = mPlayListLen - 1;
            }

            boolean gotonext = false;
            if (first <= mPlayPos && mPlayPos <= last) {
                mPlayPos = first;
                gotonext = true;
            } else if (mPlayPos > last) {
                mPlayPos -= last - first + 1;
            }
            final int num = mPlayListLen - last - 1;
            for (int i = 0; i < num; i++) {
                mPlayList[first + i] = mPlayList[last + 1 + i];
            }
            mPlayListLen -= last - first + 1;

            if (gotonext) {
                if (mPlayListLen == 0) {
                    stop(true);
                    mPlayPos = -1;
                    closeCursor();
                } else {
                    if (mShuffleMode != SHUFFLE_NONE) {
                        mPlayPos = getNextPosition(true);
                    } else if (mPlayPos >= mPlayListLen) {
                        mPlayPos = 0;
                    }
                    final boolean wasPlaying = isPlaying();
                    stop(false);
                    openCurrentAndNext();
                    if (wasPlaying) {
                        play();
                    }
                }
                notifyChange(META_CHANGED);
            }
            return last - first + 1;
        }
    }

    private void addToPlayList(final long[] list, int position) {
        final int addlen = list.length;
        if (position < 0) {
            mPlayListLen = 0;
            position = 0;
        }
        ensurePlayListCapacity(mPlayListLen + addlen);
        if (position > mPlayListLen) {
            position = mPlayListLen;
        }

        final int tailsize = mPlayListLen - position;
        for (int i = tailsize; i > 0; i--) {
            mPlayList[position + i] = mPlayList[position + i - addlen];
        }

        for (int i = 0; i < addlen; i++) {
            mPlayList[position + i] = list[i];
        }
        mPlayListLen += addlen;
        if (mPlayListLen == 0) {
            closeCursor();
            notifyChange(META_CHANGED);
        }
    }

    private void updateCursor(final long trackId) {
        updateCursor("_id=" + trackId, null);
    }

    private void updateCursor(final String selection, final String[] selectionArgs) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION, selection, selectionArgs);
        }
        updateAlbumCursor();
    }

    private void updateCursor(final Uri uri) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(uri, PROJECTION, null, null);
        }
        updateAlbumCursor();
    }

    private void updateAlbumCursor() {
        long albumId = getAlbumId();
        if (albumId >= 0) {
            mAlbumCursor = openCursorAndGoToFirst(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    ALBUM_PROJECTION, "_id=" + albumId, null);
        } else {
            mAlbumCursor = null;
        }
    }

    private Cursor openCursorAndGoToFirst(Uri uri, String[] projection,
            String selection, String[] selectionArgs) {
        Cursor c = getContentResolver().query(uri, projection,
                selection, selectionArgs, null, null);
        if (c == null) {
            return null;
        }
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;
     }

    private void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (mAlbumCursor != null) {
            mAlbumCursor.close();
            mAlbumCursor = null;
        }
    }

    private void openCurrentAndNext() {
        openCurrentAndMaybeNext(true);
    }

    private void openCurrentAndMaybeNext(final boolean openNext) {
        synchronized (this) {
            closeCursor();

            if (mPlayListLen == 0) {
                return;
            }
            stop(false);

            updateCursor(mPlayList[mPlayPos]);
            while (true) {
                if (mCursor != null && !mCursor.isClosed()
                        && openFile(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                                + mCursor.getLong(IDCOLIDX))) {
                    break;
                }
                closeCursor();
                if (mOpenFailedCounter++ < 10 && mPlayListLen > 1) {
                    final int pos = getNextPosition(false);
                    if (pos < 0) {
                        scheduleDelayedShutdown();
                        if (mIsSupposedToBePlaying) {
                            mIsSupposedToBePlaying = false;
                            notifyChange(PLAYSTATE_CHANGED);
                        }
                        return;
                    }
                    mPlayPos = pos;
                    stop(false);
                    mPlayPos = pos;
                    updateCursor(mPlayList[mPlayPos]);
                } else {
                    mOpenFailedCounter = 0;
                    if(D) Log.w(TAG, "Failed to open file for playback");
                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.service_start_error_msg), Toast.LENGTH_SHORT).show();
                        }
                    });
                    scheduleDelayedShutdown();
                    if (mIsSupposedToBePlaying) {
                        mIsSupposedToBePlaying = false;
                        notifyChange(PLAYSTATE_CHANGED);
                    }
                    return;
                }
            }
            if (openNext) {
                setNextTrack();
            }
        }
    }

    private int getNextPosition(final boolean force) {
        if (!force && mRepeatMode == REPEAT_CURRENT) {
            if (mPlayPos < 0) {
                return 0;
            }
            return mPlayPos;
        } else if (mShuffleMode == SHUFFLE_NORMAL) {
            if (mPlayPos >= 0) {
                mHistory.add(mPlayPos);
            }
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            final int numTracks = mPlayListLen;
            final int[] tracks = new int[numTracks];
            for (int i = 0; i < numTracks; i++) {
                tracks[i] = i;
            }

            final int numHistory = mHistory.size();
            int numUnplayed = numTracks;
            for (int i = 0; i < numHistory; i++) {
                final int idx = mHistory.get(i).intValue();
                if (idx < numTracks && tracks[idx] >= 0) {
                    numUnplayed--;
                    tracks[idx] = -1;
                }
            }
            if (numUnplayed <= 0) {
                if (mRepeatMode == REPEAT_ALL || force) {
                    numUnplayed = numTracks;
                    for (int i = 0; i < numTracks; i++) {
                        tracks[i] = i;
                    }
                } else {
                    return -1;
                }
            }
            int skip = 0;
            if (mShuffleMode == SHUFFLE_NORMAL || mShuffleMode == SHUFFLE_AUTO) {
                skip = mShuffler.nextInt(numUnplayed);
            }
            int cnt = -1;
            while (true) {
                while (tracks[++cnt] < 0) {
                    ;
                }
                skip--;
                if (skip < 0) {
                    break;
                }
            }
            return cnt;
        } else if (mShuffleMode == SHUFFLE_AUTO) {
            doAutoShuffleUpdate();
            return mPlayPos + 1;
        } else {
            if (mPlayPos >= mPlayListLen - 1) {
                if (mRepeatMode == REPEAT_NONE && !force) {
                    return -1;
                } else if (mRepeatMode == REPEAT_ALL || force) {
                    return 0;
                }
                return -1;
            } else {
                return mPlayPos + 1;
            }
        }
    }

    private void setNextTrack() {
        mNextPlayPos = getNextPosition(false);
        if (D) Log.d(TAG, "setNextTrack: next play position = " + mNextPlayPos);
        if (mNextPlayPos >= 0 && mPlayList != null) {
            final long id = mPlayList[mNextPlayPos];
            mPlayer.setNextDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);
        } else {
            mPlayer.setNextDataSource(null);
        }
    }

    private boolean makeAutoShuffleList() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {
                        MediaStore.Audio.Media._ID
                    }, MediaStore.Audio.Media.IS_MUSIC + "=1", null, null);
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
            final int len = cursor.getCount();
            final long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                cursor.moveToNext();
                list[i] = cursor.getLong(0);
            }
            mAutoShuffleList = list;
            return true;
        } catch (final RuntimeException e) {
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    private void doAutoShuffleUpdate() {
        boolean notify = false;
        if (mPlayPos > 10) {
            removeTracks(0, mPlayPos - 9);
            notify = true;
        }
        final int toAdd = 7 - (mPlayListLen - (mPlayPos < 0 ? -1 : mPlayPos));
        for (int i = 0; i < toAdd; i++) {
            int lookback = mHistory.size();
            int idx = -1;
            while (true) {
                idx = mShuffler.nextInt(mAutoShuffleList.length);
                if (!wasRecentlyUsed(idx, lookback)) {
                    break;
                }
                lookback /= 2;
            }
            mHistory.add(idx);
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            ensurePlayListCapacity(mPlayListLen + 1);
            mPlayList[mPlayListLen++] = mAutoShuffleList[idx];
            notify = true;
        }
        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    private boolean wasRecentlyUsed(final int idx, int lookbacksize) {
        if (lookbacksize == 0) {
            return false;
        }
        final int histsize = mHistory.size();
        if (histsize < lookbacksize) {
            lookbacksize = histsize;
        }
        final int maxidx = histsize - 1;
        for (int i = 0; i < lookbacksize; i++) {
            final long entry = mHistory.get(maxidx - i);
            if (entry == idx) {
                return true;
            }
        }
        return false;
    }

    private void ensurePlayListCapacity(final int size) {
        if (mPlayList == null || size > mPlayList.length) {
            final long[] newlist = new long[size * 2];
            final int len = mPlayList != null ? mPlayList.length : mPlayListLen;
            for (int i = 0; i < len; i++) {
                newlist[i] = mPlayList[i];
            }
            mPlayList = newlist;
        }
    }

    private void notifyChange(final String what) {
        if (D) Log.d(TAG, "notifyChange: what = " + what);
        updateRemoteControlClient(what);
        if (what.equals(POSITION_CHANGED)) {
            return;
        }
        final Intent intent = new Intent(what);
        intent.putExtra("id", getAudioId());
        intent.putExtra("artist", getArtistName());
        intent.putExtra("album", getAlbumName());
        intent.putExtra("track", getTrackName());
        intent.putExtra("playing", isPlaying());
        intent.putExtra("isfavorite", isFavorite());
        sendStickyBroadcast(intent);

        final Intent musicIntent = new Intent(intent);
        musicIntent.setAction(what.replace(Basso_PACKAGE_NAME, MUSIC_PACKAGE_NAME));
        sendStickyBroadcast(musicIntent);

        if (what.equals(META_CHANGED)) {
            if (mFavoritesCache.getSongId(getAudioId()) != null) {
                mFavoritesCache.addSongId(getAudioId(), getTrackName(), getAlbumName(),
                        getArtistName());
            }
            mRecentsCache.addAlbumId(getAlbumId(), getAlbumName(), getArtistName(),
                    MusicUtils.getSongCountForAlbum(this, getAlbumId()),
                    MusicUtils.getReleaseDateForAlbum(this, getAlbumId()));
            getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } else if (what.equals(QUEUE_CHANGED)) {
            saveQueue(true);
            if (isPlaying()) {
                setNextTrack();
            }
        } else {
            saveQueue(false);
        }

        if (what.equals(PLAYSTATE_CHANGED)) {
            mNotificationHelper.updatePlayState(isPlaying());
        }
        mAppWidgetSmall.notifyChange(this, what);
        mAppWidgetLarge.notifyChange(this, what);
        mAppWidgetLargeAlternate.notifyChange(this, what);
        mRecentWidgetProvider.notifyChange(this, what);
    }

    private void updateRemoteControlClient(final String what) {
        int playState = mIsSupposedToBePlaying
                ? RemoteControlClient.PLAYSTATE_PLAYING
                : RemoteControlClient.PLAYSTATE_PAUSED;

        if (BassoUtils.hasJellyBeanMR2()
                && (what.equals(PLAYSTATE_CHANGED) || what.equals(POSITION_CHANGED))) {
            mRemoteControlClient.setPlaybackState(playState, position(), 1.0f);
        } else if (what.equals(PLAYSTATE_CHANGED)) {
            mRemoteControlClient.setPlaybackState(playState);
        } else if (what.equals(META_CHANGED) || what.equals(QUEUE_CHANGED)) {
            Bitmap albumArt = getAlbumArt();
            if (albumArt != null) {
                Bitmap.Config config = albumArt.getConfig();
                if (config == null) {
                    config = Bitmap.Config.ARGB_8888;
                }
                albumArt = albumArt.copy(config, false);
            }
            mRemoteControlClient
                    .editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, getArtistName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
                            getAlbumArtistName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getAlbumName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, getTrackName())
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration())
                    .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, albumArt)
                    .apply();

            if (BassoUtils.hasJellyBeanMR2()) {
                mRemoteControlClient.setPlaybackState(playState, position(), 1.0f);
            }
        }
    }

    private void saveQueue(final boolean full) {
        if (!mQueueIsSaveable) {
            return;
        }

        final SharedPreferences.Editor editor = mPreferences.edit();
        if (full) {
            final StringBuilder q = new StringBuilder();
            int len = mPlayListLen;
            for (int i = 0; i < len; i++) {
                long n = mPlayList[i];
                if (n < 0) {
                    continue;
                } else if (n == 0) {
                    q.append("0;");
                } else {
                    while (n != 0) {
                        final int digit = (int)(n & 0xf);
                        n >>>= 4;
                        q.append(HEX_DIGITS[digit]);
                    }
                    q.append(";");
                }
            }
            editor.putString("queue", q.toString());
            editor.putInt("cardid", mCardId);
            if (mShuffleMode != SHUFFLE_NONE) {
                len = mHistory.size();
                q.setLength(0);
                for (int i = 0; i < len; i++) {
                    int n = mHistory.get(i);
                    if (n == 0) {
                        q.append("0;");
                    } else {
                        while (n != 0) {
                            final int digit = n & 0xf;
                            n >>>= 4;
                            q.append(HEX_DIGITS[digit]);
                        }
                        q.append(";");
                    }
                }
                editor.putString("history", q.toString());
            }
        }
        editor.putInt("curpos", mPlayPos);
        if (mPlayer.isInitialized()) {
            editor.putLong("seekpos", mPlayer.position());
        }
        editor.putInt("repeatmode", mRepeatMode);
        editor.putInt("shufflemode", mShuffleMode);
        editor.apply();
    }

    private void reloadQueue() {
        String q = null;
        int id = mCardId;
        if (mPreferences.contains("cardid")) {
            id = mPreferences.getInt("cardid", ~mCardId);
        }
        if (id == mCardId) {
            q = mPreferences.getString("queue", "");
        }
        int qlen = q != null ? q.length() : 0;
        if (qlen > 1) {
            int plen = 0;
            int n = 0;
            int shift = 0;
            for (int i = 0; i < qlen; i++) {
                final char c = q.charAt(i);
                if (c == ';') {
                    ensurePlayListCapacity(plen + 1);
                    mPlayList[plen] = n;
                    plen++;
                    n = 0;
                    shift = 0;
                } else {
                    if (c >= '0' && c <= '9') {
                        n += c - '0' << shift;
                    } else if (c >= 'a' && c <= 'f') {
                        n += 10 + c - 'a' << shift;
                    } else {
                        plen = 0;
                        break;
                    }
                    shift += 4;
                }
            }
            mPlayListLen = plen;
            final int pos = mPreferences.getInt("curpos", 0);
            if (pos < 0 || pos >= mPlayListLen) {
                mPlayListLen = 0;
                return;
            }
            mPlayPos = pos;
            updateCursor(mPlayList[mPlayPos]);
            if (mCursor == null) {
                SystemClock.sleep(3000);
                updateCursor(mPlayList[mPlayPos]);
            }
            synchronized (this) {
                closeCursor();
                mOpenFailedCounter = 20;
                openCurrentAndNext();
            }
            if (!mPlayer.isInitialized()) {
                mPlayListLen = 0;
                return;
            }

            final long seekpos = mPreferences.getLong("seekpos", 0);
            seek(seekpos >= 0 && seekpos < duration() ? seekpos : 0);

            if (D) {
                Log.d(TAG, "restored queue, currently at position "
                        + position() + "/" + duration()
                        + " (requested " + seekpos + ")");
            }

            int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);
            if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
                repmode = REPEAT_NONE;
            }
            mRepeatMode = repmode;

            int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
            if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
                shufmode = SHUFFLE_NONE;
            }
            if (shufmode != SHUFFLE_NONE) {
                q = mPreferences.getString("history", "");
                qlen = q != null ? q.length() : 0;
                if (qlen > 1) {
                    plen = 0;
                    n = 0;
                    shift = 0;
                    mHistory.clear();
                    for (int i = 0; i < qlen; i++) {
                        final char c = q.charAt(i);
                        if (c == ';') {
                            if (n >= mPlayListLen) {
                                mHistory.clear();
                                break;
                            }
                            mHistory.add(n);
                            n = 0;
                            shift = 0;
                        } else {
                            if (c >= '0' && c <= '9') {
                                n += c - '0' << shift;
                            } else if (c >= 'a' && c <= 'f') {
                                n += 10 + c - 'a' << shift;
                            } else {
                                mHistory.clear();
                                break;
                            }
                            shift += 4;
                        }
                    }
                }
            }
            if (shufmode == SHUFFLE_AUTO) {
                if (!makeAutoShuffleList()) {
                    shufmode = SHUFFLE_NONE;
                }
            }
            mShuffleMode = shufmode;
        }
    }

    public boolean openFile(final String path) {
        if (D) Log.d(TAG, "openFile: path = " + path);
        synchronized (this) {
            if (path == null) {
                return false;
            }
            if (mCursor == null) {
                Uri uri = Uri.parse(path);
                long id = -1;
                try  {
                    id = Long.valueOf(uri.getLastPathSegment());
                } catch (NumberFormatException ex) {
                }

                if (id != -1 && path.startsWith(MediaStore.Audio.Media.
                        EXTERNAL_CONTENT_URI.toString())) {
                    updateCursor(uri);

                } else if (id != -1 && path.startsWith(MediaStore.Files.getContentUri(
                        "external").toString())) {
                    updateCursor(id);

                } else {
                    String where = MediaStore.Audio.Media.DATA + "=?";
                    String[] selectionArgs = new String[] {path};
                    updateCursor(where, selectionArgs);
                }
                try {
                    if (mCursor != null) {
                        ensurePlayListCapacity(1);
                        mPlayListLen = 1;
                        mPlayList[0] = mCursor.getLong(IDCOLIDX);
                        mPlayPos = 0;
                    }
                } catch (final UnsupportedOperationException ex) {
                }
            }
            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (mPlayer.isInitialized()) {
                mOpenFailedCounter = 0;
                return true;
            }
            stop(true);
            return false;
        }
    }

    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    public int getShuffleMode() {
        return mShuffleMode;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public int removeTrack(final long id) {
        int numremoved = 0;
        synchronized (this) {
            for (int i = 0; i < mPlayListLen; i++) {
                if (mPlayList[i] == id) {
                    numremoved += removeTracksInternal(i, i);
                    i--;
                }
            }
        }
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numremoved;
    }

    public int removeTracks(final int first, final int last) {
        final int numremoved = removeTracksInternal(first, last);
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numremoved;
    }

    public int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    public String getPath() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.DATA));
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ALBUM));
        }
    }

    public String getTrackName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.TITLE));
        }
    }

    public String getArtistName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ARTIST));
        }
    }

    public String getAlbumArtistName() {
        synchronized (this) {
            if (mAlbumCursor == null) {
                return null;
            }
            return mAlbumCursor.getString(mAlbumCursor.getColumnIndexOrThrow(AlbumColumns.ARTIST));
        }
    }

    public long getAlbumId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));
        }
    }

    public long getArtistId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(AudioColumns.ARTIST_ID));
        }
    }

    public long getAudioId() {
        synchronized (this) {
            if (mPlayPos >= 0 && mPlayer.isInitialized()) {
                return mPlayList[mPlayPos];
            }
        }
        return -1;
    }

    public long seek(long position) {
        if (mPlayer.isInitialized()) {
            if (position < 0) {
                position = 0;
            } else if (position > mPlayer.duration()) {
                position = mPlayer.duration();
            }
            long result = mPlayer.seek(position);
            notifyChange(POSITION_CHANGED);
            return result;
        }
        return -1;
    }

    public long position() {
        if (mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    public long duration() {
        if (mPlayer.isInitialized()) {
            return mPlayer.duration();
        }
        return -1;
    }

    public long[] getQueue() {
        synchronized (this) {
            final int len = mPlayListLen;
            final long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                list[i] = mPlayList[i];
            }
            return list;
        }
    }

    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    public boolean isFavorite() {
        if (mFavoritesCache != null) {
            synchronized (this) {
                final Long id = mFavoritesCache.getSongId(getAudioId());
                return id != null ? true : false;
            }
        }
        return false;
    }

    public void open(final long[] list, final int position) {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;
            }
            final long oldId = getAudioId();
            final int listlength = list.length;
            boolean newlist = true;
            if (mPlayListLen == listlength) {
                newlist = false;
                for (int i = 0; i < listlength; i++) {
                    if (list[i] != mPlayList[i]) {
                        newlist = true;
                        break;
                    }
                }
            }
            if (newlist) {
                addToPlayList(list, -1);
                notifyChange(QUEUE_CHANGED);
            }
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mShuffler.nextInt(mPlayListLen);
            }
            mHistory.clear();
            openCurrentAndNext();
            if (oldId != getAudioId()) {
                notifyChange(META_CHANGED);
            }
        }
    }

    public void stop() {
        stop(true);
    }

    public void play() {
        int status = mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (D) Log.d(TAG, "Starting playback: audio focus request status = " + status);

        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }

        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName()));

        if (mPlayer.isInitialized()) {
            setNextTrack();

            final long duration = mPlayer.duration();
            if (mRepeatMode != REPEAT_CURRENT && duration > 2000
                    && mPlayer.position() >= duration - 2000) {
                gotoNext(true);
            }

            mPlayer.start();
            mPlayerHandler.removeMessages(FADEDOWN);
            mPlayerHandler.sendEmptyMessage(FADEUP);

            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyChange(PLAYSTATE_CHANGED);
            }

            cancelShutdown();
            updateNotification();
        } else if (mPlayListLen <= 0) {
            setShuffleMode(SHUFFLE_AUTO);
        }
    }

    public void pause() {
        if (D) Log.d(TAG, "Pausing playback");
        synchronized (this) {
            mPlayerHandler.removeMessages(FADEUP);
            if (mIsSupposedToBePlaying) {
                mPlayer.pause();
                scheduleDelayedShutdown();
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
            }
        }
    }

    public void gotoNext(final boolean force) {
        if (D) Log.d(TAG, "Going to next track");
        synchronized (this) {
            if (mPlayListLen <= 0) {
                if (D) Log.d(TAG, "No play queue");
                scheduleDelayedShutdown();
                return;
            }
            final int pos = getNextPosition(force);
            if (pos < 0) {
                scheduleDelayedShutdown();
                if (mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                }
                return;
            }
            mPlayPos = pos;
            stop(false);
            mPlayPos = pos;
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
        }
    }

    public void prev() {
        if (D) Log.d(TAG, "Going to previous track");
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_NORMAL) {
                final int histsize = mHistory.size();
                if (histsize == 0) {
                    return;
                }
                final Integer pos = mHistory.remove(histsize - 1);
                mPlayPos = pos.intValue();
            } else {
                if (mPlayPos > 0) {
                    mPlayPos--;
                } else {
                    mPlayPos = mPlayListLen - 1;
                }
            }
            stop(false);
            openCurrent();
            play();
            notifyChange(META_CHANGED);
        }
    }

    private void openCurrent() {
        openCurrentAndMaybeNext(false);
    }

    public void toggleFavorite() {
        if (mFavoritesCache != null) {
            synchronized (this) {
                mFavoritesCache.toggleSong(getAudioId(), getTrackName(), getAlbumName(),
                        getArtistName());
            }
        }
    }

    public void moveQueueItem(int index1, int index2) {
        synchronized (this) {
            if (index1 >= mPlayListLen) {
                index1 = mPlayListLen - 1;
            }
            if (index2 >= mPlayListLen) {
                index2 = mPlayListLen - 1;
            }
            if (index1 < index2) {
                final long tmp = mPlayList[index1];
                for (int i = index1; i < index2; i++) {
                    mPlayList[i] = mPlayList[i + 1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                    mPlayPos--;
                }
            } else if (index2 < index1) {
                final long tmp = mPlayList[index1];
                for (int i = index1; i > index2; i--) {
                    mPlayList[i] = mPlayList[i - 1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                    mPlayPos++;
                }
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void setRepeatMode(final int repeatmode) {
        synchronized (this) {
            mRepeatMode = repeatmode;
            setNextTrack();
            saveQueue(false);
            notifyChange(REPEATMODE_CHANGED);
        }
    }

    public void setShuffleMode(final int shufflemode) {
        synchronized (this) {
            if (mShuffleMode == shufflemode && mPlayListLen > 0) {
                return;
            }
            mShuffleMode = shufflemode;
            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlayListLen = 0;
                    doAutoShuffleUpdate();
                    mPlayPos = 0;
                    openCurrentAndNext();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                } else {
                    mShuffleMode = SHUFFLE_NONE;
                }
            }
            saveQueue(false);
            notifyChange(SHUFFLEMODE_CHANGED);
        }
    }

    public void setQueuePosition(final int index) {
        synchronized (this) {
            stop(false);
            mPlayPos = index;
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }

    public void enqueue(final long[] list, final int action) {
        synchronized (this) {
            if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
                addToPlayList(list, mPlayPos + 1);
                notifyChange(QUEUE_CHANGED);
            } else {
                addToPlayList(list, Integer.MAX_VALUE);
                notifyChange(QUEUE_CHANGED);
                if (action == NOW) {
                    mPlayPos = mPlayListLen - list.length;
                    openCurrentAndNext();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                }
            }
            if (mPlayPos < 0) {
                mPlayPos = 0;
                openCurrentAndNext();
                play();
                notifyChange(META_CHANGED);
            }
        }
    }

    private void cycleRepeat() {
        if (mRepeatMode == REPEAT_NONE) {
            setRepeatMode(REPEAT_ALL);
        } else if (mRepeatMode == REPEAT_ALL) {
            setRepeatMode(REPEAT_CURRENT);
            if (mShuffleMode != SHUFFLE_NONE) {
                setShuffleMode(SHUFFLE_NONE);
            }
        } else {
            setRepeatMode(REPEAT_NONE);
        }
    }

    private void cycleShuffle() {
        if (mShuffleMode == SHUFFLE_NONE) {
            setShuffleMode(SHUFFLE_NORMAL);
            if (mRepeatMode == REPEAT_CURRENT) {
                setRepeatMode(REPEAT_ALL);
            }
        } else if (mShuffleMode == SHUFFLE_NORMAL || mShuffleMode == SHUFFLE_AUTO) {
            setShuffleMode(SHUFFLE_NONE);
        }
    }

    public short getNumberOfBands(){
        return mPlayer.getNumberOfBands();
    }

    public short getBandLevelRange(int level){
        return mPlayer.getBandLevelRange(level);
    }

    public int getNumberOfPresets(){
        return mPlayer.getNumberOfPresets();
    }

    public void enableEqualizer(){
        mPlayer.enableEqualizer();
    }

    public void disableEqualizer(){
        mPlayer.disableEqualizer();
    }

    public String getPresetName(short i){
        return mPlayer.getPresetName(i);
    }

    public int getBandLevel(short band){
        return mPlayer.getBandLevel(band);
    }

    public void setBandLeve(short band, short level){
        mPlayer.setBandLeve(band, level);
    }

    public short getCurrentPreset(){
        return mPlayer.getCurrentPreset();
    }

    public void usePreset(short preset){
        mPlayer.usePreset(preset);
    }

    public int getCenterFreq(short band) { return mPlayer.getCenterFreq(band);}

    public void setBalance(float balance) { mPlayer.setBalance(balance);}

    public void setLeftChannelVolume(float vol){mPlayer.setLeftChannelVolume(vol);}

    public void setRightChannelVolume(float vol){mPlayer.setRightChannelVolume(vol);}

    public float getLeftVolume(){return mPlayer.getLeftVolume();}

    public float getRightVolume(){return mPlayer.getRightVolume();}

    public void deletePreset(int position){mPlayer.deletePreset(position);}

    public void updatePref(){mPlayer.updatePref(this);}

    public void savePreset(String result){mPlayer.savePreset(result);}

    public boolean isEqualizerEnabled(){return mPlayer.isEqualizerEnabled();}

    public void setEqualizerEnabled(boolean enabled){mPlayer.setEqualizerEnabled(enabled);}

    public Bitmap getAlbumArt() {
        final Bitmap bitmap = mImageFetcher.getArtwork(getAlbumName(), getAlbumId(), getArtistName());
        return bitmap;
    }

    public void refresh() {
        notifyChange(REFRESH);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String command = intent.getStringExtra(CMDNAME);

            if (AppWidgetSmall.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] small = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetSmall.performUpdate(MusicPlaybackService.this, small);
            } else if (AppWidgetLarge.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] large = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetLarge.performUpdate(MusicPlaybackService.this, large);
            } else if (AppWidgetLargeAlternate.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] largeAlt = intent
                        .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetLargeAlternate.performUpdate(MusicPlaybackService.this, largeAlt);
            } else if (RecentWidgetProvider.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] recent = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mRecentWidgetProvider.performUpdate(MusicPlaybackService.this, recent);
            } else {
                handleCommandIntent(intent);
            }
        }
    };

    private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(final int focusChange) {
            mPlayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };

    private static final class MusicPlayerHandler extends Handler {
        private final WeakReference<MusicPlaybackService> mService;
        private float mCurrentVolume = 1.0f;

        public MusicPlayerHandler(final MusicPlaybackService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<MusicPlaybackService>(service);
        }

        @Override
        public void handleMessage(final Message msg) {
            final MusicPlaybackService service = mService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= .05f;
                    if (mCurrentVolume > .2f) {
                        sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    mCurrentVolume += .01f;
                    if (mCurrentVolume < 1.0f) {
                        sendEmptyMessageDelayed(FADEUP, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    if (service.isPlaying()) {
                        service.gotoNext(true);
                    } else {
                        service.openCurrentAndNext();
                    }
                    break;
                case TRACK_WENT_TO_NEXT:
                    service.mPlayPos = service.mNextPlayPos;
                    if (service.mCursor != null) {
                        service.mCursor.close();
                    }
                    service.updateCursor(service.mPlayList[service.mPlayPos]);
                    service.notifyChange(META_CHANGED);
                    service.updateNotification();
                    service.setNextTrack();
                    break;
                case TRACK_ENDED:
                    if (service.mRepeatMode == REPEAT_CURRENT) {
                        service.seek(0);
                        service.play();
                    } else {
                        service.gotoNext(false);
                    }
                    break;
                case RELEASE_WAKELOCK:
                    service.mWakeLock.release();
                    break;
                case FOCUSCHANGE:
                    if (D) Log.d(TAG, "Received audio focus change event " + msg.arg1);
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            if (service.isPlaying()) {
                                service.mPausedByTransientLossOfFocus =
                                    msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                            }
                            service.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            removeMessages(FADEUP);
                            sendEmptyMessage(FADEDOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (!service.isPlaying()
                                    && service.mPausedByTransientLossOfFocus) {
                                service.mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                service.mPlayer.setVolume(mCurrentVolume);
                                service.play();
                            } else {
                                removeMessages(FADEDOWN);
                                sendEmptyMessage(FADEUP);
                            }
                            break;
                        default:
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static final class Shuffler {

        private final LinkedList<Integer> mHistoryOfNumbers = new LinkedList<Integer>();

        private final TreeSet<Integer> mPreviousNumbers = new TreeSet<Integer>();

        private final Random mRandom = new Random();

        private int mPrevious;

        public Shuffler() {
            super();
        }

        public int nextInt(final int interval) {
            int next;
            do {
                next = mRandom.nextInt(interval);
            } while (next == mPrevious && interval > 1
                    && !mPreviousNumbers.contains(Integer.valueOf(next)));
            mPrevious = next;
            mHistoryOfNumbers.add(mPrevious);
            mPreviousNumbers.add(mPrevious);
            cleanUpHistory();
            return next;
        }

        private void cleanUpHistory() {
            if (!mHistoryOfNumbers.isEmpty() && mHistoryOfNumbers.size() >= MAX_HISTORY_SIZE) {
                for (int i = 0; i < Math.max(1, MAX_HISTORY_SIZE / 2); i++) {
                    mPreviousNumbers.remove(mHistoryOfNumbers.removeFirst());
                }
            }
        }
    };

    private static final class MultiPlayer implements MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener {

        private Equalizer mEqualizer;

        private final WeakReference<MusicPlaybackService> mService;

        private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

        private MediaPlayer mNextMediaPlayer;

        private SharedPreferences mPreferences;

        private Handler mHandler;

        private boolean mEqualizerEnabled = true;

        private boolean mIsInitialized = false;

        private JSONArray customEqualizerPresets;

        private float leftVolume;

        private float rightVolume;

        private float balance;

        public void setEqualizerPreset(){
            int selectedPreset = mPreferences.getInt("active_preset", 0);
            if (D) Log.d(TAG,"Active preset in service " + selectedPreset);
            try{
                String customPresetsString = mPreferences.getString("equalizer_presets", "no_presets");
                if(!customPresetsString.equals("no_presets")){
                    customEqualizerPresets =  new JSONArray(customPresetsString);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

            if(selectedPreset < mEqualizer.getNumberOfPresets()){
                mEqualizer.usePreset((short)selectedPreset);
            } else {
                if(customEqualizerPresets != null){
                    try {
                        int pos = selectedPreset - mEqualizer.getNumberOfPresets();
                        if(pos >= 0 && pos < customEqualizerPresets.length() ){
                            JSONObject preset = customEqualizerPresets.getJSONObject(pos);
                            short numberFrequencyBands = (short)mEqualizer.getNumberOfBands();
                            for (short i = 0; i < numberFrequencyBands; i++) {
                                short equalizerBandIndex = i;
                                mEqualizer.setBandLevel(equalizerBandIndex, (short)preset.getInt("" + i));
                            }
                        }
                    }catch (JSONException ex){}
                }
            }
        }

        public void setBalancePreset(){
            float balance = mPreferences.getFloat("balance", 1.0f);
            setBalance(balance);
        }

        public void updatePref(final MusicPlaybackService service){
            if (D) Log.d(TAG,"Update pref called");
            mPreferences = service.getSharedPreferences("Service", 0);
            try{
                String customPresetsString = mPreferences.getString("equalizer_presets", "no_presets");
                if (D) Log.d(TAG,"Updated prefs: " + customPresetsString);
                if(!customPresetsString.equals("no_presets")){
                    customEqualizerPresets =  new JSONArray(customPresetsString);
                    if (D) Log.d(TAG,"Updated custom presets " + customEqualizerPresets);
                }else{
                    customEqualizerPresets = null;
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        public MultiPlayer(final MusicPlaybackService service) {
            mService = new WeakReference<MusicPlaybackService>(service);
            mEqualizer = new Equalizer(0, getAudioSessionId());
            mPreferences = service.getSharedPreferences("Service", 0);

            setEqualizerPreset();
            setBalancePreset();
            mEqualizerEnabled = mPreferences.getBoolean("equalizer_enabled", false);
            mEqualizer.setEnabled(mEqualizerEnabled);
            if(D) Log.i(TAG, "Equalizer enabled " + mEqualizer.getEnabled() + " with id " + getAudioSessionId());
            mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
        }

        public void setEqualizerEnabled(boolean enabled){
            mEqualizerEnabled = enabled;
            mPreferences.edit().putBoolean("equalizer_enabled", enabled).commit();
            mEqualizer.setEnabled(enabled);
        }
        public boolean isEqualizerEnabled(){
            return mPreferences.getBoolean("equalizer_enabled", false);
        }

        public void setDataSource(final String path) {
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }

        public short getNumberOfBands(){
            return mEqualizer.getNumberOfBands();
        }

        public short getBandLevelRange(int level){
            return mEqualizer.getBandLevelRange()[level];
        }

        public int getNumberOfPresets(){
            return mEqualizer.getNumberOfPresets();
        }

        public void enableEqualizer(){
            mEqualizer.setEnabled(true);
        }

        public void disableEqualizer(){
            mEqualizer.setEnabled(false);
        }

        public String getPresetName(short i){
            return mEqualizer.getPresetName(i);
        }

        public int getBandLevel(short band){
            return mEqualizer.getBandLevel(band);
        }

        public void setBandLeve(short band, short level){
            mEqualizer.setBandLevel(band, level);
        }

        public short getCurrentPreset(){
            return mEqualizer.getCurrentPreset();
        }

        public void usePreset(short preset){

            if(preset < mEqualizer.getNumberOfPresets()){
                mEqualizer.usePreset((short) preset);
                SharedPreferences.Editor editor = mPreferences.edit();
                if (D) Log.d(TAG,"Active preset " + preset);
                editor.putInt("active_preset", preset);
                editor.commit();
            } else {
                try {
                    if (D) Log.d(TAG,"Custom presets length " + customEqualizerPresets + (customEqualizerPresets != null ? customEqualizerPresets.length() : null ));
                    if(customEqualizerPresets != null) {
                        int pos = preset - mEqualizer.getNumberOfPresets();
                        if(pos < customEqualizerPresets.length()) {
                            JSONObject presetJSON = customEqualizerPresets.getJSONObject(pos);
                            if (D) Log.d(TAG,"Preset with name " + presetJSON.getString("name") + " clicked");
                            short numberFrequencyBands = (short) mEqualizer.getNumberOfBands();

                            for (short i = 0; i < numberFrequencyBands; i++) {
                                short equalizerBandIndex = i;
                                mEqualizer.setBandLevel(equalizerBandIndex, (short) presetJSON.getInt("" + i));
                            }
                            SharedPreferences.Editor editor = mPreferences.edit();
                            if (D) Log.d(TAG,"Active preset " + preset);
                            editor.putInt("active_preset", preset);
                            editor.commit();
                        }
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }

        public void savePreset(String result){
            mPreferences.edit().putString("equalizer_presets", result).commit();
        }
        public int getCenterFreq(short band){return mEqualizer.getCenterFreq(band); }

        public void deletePreset(int position){
            try {
                int pos = position - mEqualizer.getNumberOfPresets();
                if (customEqualizerPresets != null) {
                    JSONArray newJSonArray = new JSONArray();
                    boolean deleted = false;
                    for (int i = 0; i < customEqualizerPresets.length() - 1; i++) {
                        if (i != pos && !deleted)
                            newJSonArray.put(i, customEqualizerPresets.getJSONObject(i));
                        if (i == pos || deleted) {
                            deleted = true;
                            newJSonArray.put(i, customEqualizerPresets.getJSONObject(i + 1));
                        }
                    }
                    customEqualizerPresets = newJSonArray;
                    SharedPreferences.Editor editor = mPreferences.edit();
                    if (customEqualizerPresets.length() == 0) {
                        if (D) Log.d(TAG,"All custom presets are deleted");
                        editor.remove("equalizer_presets");
                    } else
                        editor.putString("equalizer_presets", customEqualizerPresets.toString());
                    editor.commit();
                }
            }catch (JSONException ex){}
        }

        private boolean setDataSourceImpl(final MediaPlayer player, final String path) {
            try {
                player.reset();
                player.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    player.setDataSource(mService.get(), Uri.parse(path));
                } else {
                    player.setDataSource(path);
                }
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepare();
            } catch (final IOException todo) {
                return false;
            } catch (final IllegalArgumentException todo) {
                return false;
            }
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mService.get().getPackageName());
            mService.get().sendBroadcast(intent);
            return true;
        }

        public void setNextDataSource(final String path) {
            try {
                mCurrentMediaPlayer.setNextMediaPlayer(null);
            } catch (IllegalArgumentException e) {
                if(D) Log.i(TAG, "Next media player is current one, continuing");
            } catch (IllegalStateException e) {
                if(D) Log.e(TAG, "Media player not initialized!");
                return;
            }
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
            if (path == null) {
                return;
            }
            mNextMediaPlayer = new MediaPlayer();
            mNextMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
            if (setDataSourceImpl(mNextMediaPlayer, path)) {
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
            } else {
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer.release();
                    mNextMediaPlayer = null;
                }
            }
        }

        public void setHandler(final Handler handler) {
            mHandler = handler;
        }

        public boolean isInitialized() {
            return mIsInitialized;
        }

        public void start() {
            mCurrentMediaPlayer.start();
        }

        public void stop() {
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
        }

        public void release() {
            stop();
            mCurrentMediaPlayer.release();
            mEqualizer.release();
        }

        public void pause() {
            mCurrentMediaPlayer.pause();
        }

        public long duration() {
            return mCurrentMediaPlayer.getDuration();
        }

        public long position() {
            return mCurrentMediaPlayer.getCurrentPosition();
        }

        public long seek(final long whereto) {
            mCurrentMediaPlayer.seekTo((int)whereto);
            return whereto;
        }

        public void setBalance(float balance) {
            if (D) Log.d(TAG,"Balance " + balance);
            this.balance = balance;
            mPreferences.edit().putFloat("balance", balance).commit();
            if (balance < 1.0f) {
                float masterVolume = 1.0f;
                if (D) Log.d(TAG,"Left volume " + masterVolume + ", right volume " + masterVolume * balance);
                setLeftChannelVolume(masterVolume);
                setRightChannelVolume(masterVolume * balance);
            } else {
                float masterVolume = 1.0f;
                if (D) Log.d(TAG,"Right volume " + masterVolume + ", left volume " + (masterVolume * (2.0f - balance)));
                setRightChannelVolume(masterVolume);
                setLeftChannelVolume(masterVolume * (2.0f - balance));
            }
        }

        public void setVolume(final float vol) {
            leftVolume = vol;
            rightVolume = vol;
            mCurrentMediaPlayer.setVolume(vol, vol);
        }

        public void setLeftChannelVolume( float vol){
            leftVolume = vol;
            mCurrentMediaPlayer.setVolume(leftVolume, rightVolume);
        }

        public void setRightChannelVolume( float vol){
            rightVolume = vol;
            mCurrentMediaPlayer.setVolume(leftVolume, rightVolume);
        }

        public float getLeftVolume(){
            return leftVolume;
        }

        public float getRightVolume(){
            return rightVolume;
        }

        public void setAudioSessionId(final int sessionId) {
            mCurrentMediaPlayer.setAudioSessionId(sessionId);
        }

        public int getAudioSessionId() {
            return mCurrentMediaPlayer.getAudioSessionId();
        }

        @Override
        public boolean onError(final MediaPlayer mp, final int what, final int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInitialized = false;
                    mCurrentMediaPlayer.release();
                    mCurrentMediaPlayer = new MediaPlayer();
                    mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                    return true;
                default:
                    break;
            }
            return false;
        }

        @Override
        public void onCompletion(final MediaPlayer mp) {
            if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
                mCurrentMediaPlayer.release();
                mCurrentMediaPlayer = mNextMediaPlayer;
                mNextMediaPlayer = null;
                mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
            } else {
                mService.get().mWakeLock.acquire(30000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        }
    }

    private static final class ServiceStub extends IBassoService.Stub {

        private final WeakReference<MusicPlaybackService> mService;

        private ServiceStub(final MusicPlaybackService service) {
            mService = new WeakReference<MusicPlaybackService>(service);
        }

        @Override
        public void openFile(final String path) throws RemoteException {
            mService.get().openFile(path);
        }

        @Override
        public void open(final long[] list, final int position) throws RemoteException {
            mService.get().open(list, position);
        }

        @Override
        public void stop() throws RemoteException {
            mService.get().stop();
        }

        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        @Override
        public void play() throws RemoteException {
            mService.get().play();
        }

        @Override
        public void prev() throws RemoteException {
            mService.get().prev();
        }

        @Override
        public void next() throws RemoteException {
            mService.get().gotoNext(true);
        }

        @Override
        public void enqueue(final long[] list, final int action) throws RemoteException {
            mService.get().enqueue(list, action);
        }

        @Override
        public void setQueuePosition(final int index) throws RemoteException {
            mService.get().setQueuePosition(index);
        }

        @Override
        public void setShuffleMode(final int shufflemode) throws RemoteException {
            mService.get().setShuffleMode(shufflemode);
        }

        @Override
        public void setRepeatMode(final int repeatmode) throws RemoteException {
            mService.get().setRepeatMode(repeatmode);
        }

        @Override
        public void moveQueueItem(final int from, final int to) throws RemoteException {
            mService.get().moveQueueItem(from, to);
        }

        @Override
        public void toggleFavorite() throws RemoteException {
            mService.get().toggleFavorite();
        }

        @Override
        public void refresh() throws RemoteException {
            mService.get().refresh();
        }

        @Override
        public boolean isFavorite() throws RemoteException {
            return mService.get().isFavorite();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override
        public long[] getQueue() throws RemoteException {
            return mService.get().getQueue();
        }

        @Override
        public long duration() throws RemoteException {
            return mService.get().duration();
        }

        @Override
        public long position() throws RemoteException {
            return mService.get().position();
        }

        @Override
        public long seek(final long position) throws RemoteException {
            return mService.get().seek(position);
        }

        @Override
        public long getAudioId() throws RemoteException {
            return mService.get().getAudioId();
        }

        @Override
        public long getArtistId() throws RemoteException {
            return mService.get().getArtistId();
        }

        @Override
        public long getAlbumId() throws RemoteException {
            return mService.get().getAlbumId();
        }

        @Override
        public String getArtistName() throws RemoteException {
            return mService.get().getArtistName();
        }

        @Override
        public String getTrackName() throws RemoteException {
            return mService.get().getTrackName();
        }

        @Override
        public String getAlbumName() throws RemoteException {
            return mService.get().getAlbumName();
        }

        @Override
        public String getPath() throws RemoteException {
            return mService.get().getPath();
        }

        @Override
        public int getQueuePosition() throws RemoteException {
            return mService.get().getQueuePosition();
        }

        @Override
        public int getShuffleMode() throws RemoteException {
            return mService.get().getShuffleMode();
        }

        @Override
        public int getRepeatMode() throws RemoteException {
            return mService.get().getRepeatMode();
        }

        @Override
        public int removeTracks(final int first, final int last) throws RemoteException {
            return mService.get().removeTracks(first, last);
        }

        @Override
        public int removeTrack(final long id) throws RemoteException {
            return mService.get().removeTrack(id);
        }

        @Override
        public int getMediaMountedCount() throws RemoteException {
            return mService.get().getMediaMountedCount();
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mService.get().getAudioSessionId();
        }

        @Override
        public int getNumberOfBands(){
            return mService.get().getNumberOfBands();
        }
        @Override
        public int getBandLevelRange(int level){
            return mService.get().getBandLevelRange(level);
        }
        @Override
        public int getNumberOfPresets(){
            return mService.get().getNumberOfPresets();
        }
        @Override
        public void enableEqualizer(){
            mService.get().enableEqualizer();
        }
        @Override
        public void disableEqualizer(){
            mService.get().disableEqualizer();
        }
        @Override
        public String getPresetName(int i){
            return mService.get().getPresetName((short)i);
        }
        @Override
        public int getBandLevel(int band){
            return mService.get().getBandLevel((short)band);
        }
        @Override
        public void setBandLeve(int band, int level){
            mService.get().setBandLeve((short)band, (short)level);
        }
        @Override
        public int getCenterFreq(int band){
            return mService.get().getCenterFreq((short)band);
        }
        @Override
        public int getCurrentPreset(){
            return mService.get().getCurrentPreset();
        }
        @Override
        public void usePreset(int preset){
            mService.get().usePreset((short)preset);
        }

        @Override
        public void setBalance(float balance){mService.get().setBalance(balance);}

        @Override
        public void setLeftChannelVolume(float vol){mService.get().setLeftChannelVolume(vol);}

        @Override
        public void setRightChannelVolume(float vol){mService.get().setRightChannelVolume(vol);}

        @Override
        public float getLeftVolumet(){return  mService.get().getRightVolume();}

        @Override
        public float getRightVoluemt(){return mService.get().getLeftVolume();}

        @Override
        public void deletePreset(int position){mService.get().deletePreset(position);}

        @Override
        public void updatePref(){mService.get().updatePref();}

        @Override
        public void savePreset(String result){mService.get().savePreset(result);}

        @Override
        public boolean isEqualizerEnabled(){return mService.get().isEqualizerEnabled();}

        @Override
        public void setEqualizerEnabled(boolean enabled){mService.get().setEqualizerEnabled(enabled);}
    }
}
