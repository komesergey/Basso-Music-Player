package com.basso.basso.ui.activities;

import static com.basso.basso.utils.MusicUtils.mService;

import android.app.Activity;
import android.app.ActionBar;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import basso.IBassoService;
import com.basso.basso.R;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.cache.ImageWorker;
import com.basso.basso.format.PrefixHighlighter;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.ui.MusicHolder;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.MusicUtils.ServiceToken;
import com.basso.basso.utils.NavUtils;
import com.basso.basso.utils.ThemeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class SearchActivity extends Activity implements LoaderCallbacks<Cursor>,
        OnScrollListener, OnQueryTextListener, OnItemClickListener, ServiceConnection {

    private static final int ONE = 1, TWO = 2;

    private ServiceToken mToken;

    private String mFilterString;

    private LinearLayout scrollLayout;

    private ListView mGridView;

    private SearchAdapter mAdapter;

    private SearchView mSearchView;

    private ThemeUtils mResources;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_background)));
        }
        mResources = new ThemeUtils(this);
        mResources.setOverflowStyle(this);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mToken = MusicUtils.bindToService(this, this);
        final ActionBar actionBar = getActionBar();
        mResources.themeActionBar(actionBar, getString(R.string.app_name));
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.search_list_base);
        final FrameLayout background = (FrameLayout)findViewById(R.id.list_base_container);
        background.setBackgroundColor(getResources().getColor(R.color.action_bar_background));
        final String query = getIntent().getStringExtra(SearchManager.QUERY);
        mFilterString = !TextUtils.isEmpty(query) ? query : null;
        mResources.setSubtitle("\"" + mFilterString + "\"");
        mAdapter = new SearchAdapter(this);
        mAdapter.setPrefix(mFilterString);
        mGridView = (ListView)findViewById(R.id.list_base);
        scrollLayout = (LinearLayout)findViewById(R.id.scroll_listview);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            scrollLayout.setPadding(0,getStatusBarHeight(),0,0);
        }
        mGridView.setAdapter(mAdapter);
        mGridView.setRecyclerListener(new RecycleHolder());
        mGridView.setOnScrollListener(this);
        mGridView.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        final String query = intent.getStringExtra(SearchManager.QUERY);
        mFilterString = !TextUtils.isEmpty(query) ? query : null;
        mAdapter.setPrefix(mFilterString);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        mResources.setSearchIcon(menu);
        mSearchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        mSearchView.setOnQueryTextListener(this);
        int searchIconId = mSearchView.getContext().getResources().getIdentifier("android:id/search_button", null, null);
        ImageView searchIcon = (ImageView) mSearchView.findViewById(searchIconId);
        searchIcon.setImageResource(R.drawable.ic_action_search);
        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) mSearchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.WHITE);

        int mVoiceButtonId = getResources().getIdentifier("android:id/search_voice_btn", null, null);
        ImageView voiceSearchButton = (ImageView)mSearchView.findViewById(mVoiceButtonId);
        voiceSearchButton.setImageResource(R.drawable.ic_btn_speak);

        int mCloseButtonId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeSearchButton = (ImageView)mSearchView.findViewById(mCloseButtonId);
        closeSearchButton.setImageResource(R.drawable.btn_notification_collapse);

        try {
            int queryTextViewId = getResources().getIdentifier("android:id/search_src_text", null, null);
            View autoComplete = mSearchView.findViewById(queryTextViewId);

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
        }catch (Exception ex){
            Log.e(SearchActivity.class.getCanonicalName(), ex.getMessage());
        }

        final SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        final SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        mSearchView.setSearchableInfo(searchableInfo);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        if (mService != null) {
            MusicUtils.unbindFromService(mToken);
            mToken = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = Uri.parse("content://media/external/audio/search/fancy/" + Uri.encode(mFilterString));
        final String[] projection = new String[] {
                BaseColumns._ID, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Media.TITLE, "data1", "data2"
        };
        return new CursorLoader(this, uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        if (data == null || data.isClosed() || data.getCount() <= 0) {
            final TextView empty = (TextView)findViewById(R.id.empty);
            empty.setText(getString(R.string.empty_search));
            mGridView.setEmptyView(empty);
            return;
        }
        mAdapter.swapCursor(data);
    }
    public int getStatusBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING
                || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            mAdapter.setPauseDiskCache(true);
        } else {
            mAdapter.setPauseDiskCache(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }
        if (mSearchView != null) {
            final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();
        }
        mResources.setSubtitle("\"" + mFilterString + "\"");
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (TextUtils.isEmpty(newText)) {
            return false;
        }
        mFilterString = !TextUtils.isEmpty(newText) ? newText : null;
        mAdapter.setPrefix(mFilterString);
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        if (cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return;
        }

        final String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));

        if ("artist".equals(mimeType)) {
            NavUtils.openArtistProfile(this,
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)));
        } else if ("album".equals(mimeType)) {
            NavUtils.openAlbumProfile(this,
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)));
        } else if (position >= 0 && id >= 0) {
            final long[] list = new long[] {
                id
            };
            MusicUtils.playAll(this, list, 0, false);
        }

        cursor.close();
        cursor = null;
        finish();
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        mService = IBassoService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mService = null;
    }

    private static final class SearchAdapter extends CursorAdapter {

        private static final int VIEW_TYPE_COUNT = 2;

        private final ImageFetcher mImageFetcher;

        private final PrefixHighlighter mHighlighter;

        private char[] mPrefix;

        public SearchAdapter(final Activity context) {
            super(context, null, false);
            mImageFetcher = BassoUtils.getImageFetcher(context);
            mHighlighter = new PrefixHighlighter(context);
        }

        @Override
        public void bindView(final View convertView, final Context context, final Cursor cursor) {
            MusicHolder holder = (MusicHolder)convertView.getTag();
            if (holder == null) {
                holder = new MusicHolder(convertView);
                convertView.setTag(holder);
            }
            final String mimetype = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
            if (mimetype.equals("artist")) {
                holder.mImage.get().setScaleType(ScaleType.CENTER_CROP);
                final String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
                holder.mLineOne.get().setText(artist);
                final int albumCount = cursor.getInt(cursor.getColumnIndexOrThrow("data1"));
                holder.mLineTwo.get().setText(MusicUtils.makeLabel(context, R.plurals.Nalbums, albumCount));
                mImageFetcher.loadArtistImage(artist, holder.mImage.get(), ImageWorker.ImageSource.OTHER);
                mHighlighter.setText(holder.mLineOne.get(), artist, mPrefix);
            } else if (mimetype.equals("album")) {
                holder.mImage.get().setScaleType(ScaleType.FIT_XY);
                final long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
                final String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                holder.mLineOne.get().setText(album);
                final String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
                holder.mLineTwo.get().setText(artist);
                mImageFetcher.loadAlbumImage(artist, album, id, holder.mImage.get(), ImageWorker.ImageSource.OTHER);
                mHighlighter.setText(holder.mLineOne.get(), album, mPrefix);
            } else if (mimetype.startsWith("audio/") || mimetype.equals("application/ogg")
                    || mimetype.equals("application/x-ogg")) {
                holder.mImage.get().setScaleType(ScaleType.FIT_XY);
                holder.mImage.get().setImageResource(R.drawable.header_temp);
                final String track = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                holder.mLineOne.get().setText(track);
                final String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                holder.mLineTwo.get().setText(album);
                mHighlighter.setText(holder.mLineOne.get(), track, mPrefix);
            }
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            return ((Activity)context).getLayoutInflater().inflate(
                    R.layout.list_item_normal, parent, false);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        public void setPauseDiskCache(final boolean pause) {
            if (mImageFetcher != null) {
                mImageFetcher.setPauseDiskCache(pause);
            }
        }

        public void setPrefix(final CharSequence prefix) {
            if (!TextUtils.isEmpty(prefix)) {
                mPrefix = prefix.toString().toUpperCase(Locale.getDefault()).toCharArray();
            } else {
                mPrefix = null;
            }
        }
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem,
            final int visibleItemCount, final int totalItemCount) {
    }

}
