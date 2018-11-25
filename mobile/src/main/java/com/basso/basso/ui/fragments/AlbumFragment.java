package com.basso.basso.ui.fragments;

import static com.basso.basso.utils.PreferenceUtils.ALBUM_LAYOUT;

import android.app.Activity;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.basso.basso.MusicStateListener;
import com.basso.basso.R;
import com.basso.basso.adapters.AlbumAdapter;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.loaders.AlbumLoader;
import com.basso.basso.menu.CreateNewPlaylist;
import com.basso.basso.menu.DeleteDialog;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.model.Album;
import com.basso.basso.model.Song;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.ui.activities.BaseActivity;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.NavUtils;
import com.basso.basso.utils.PreferenceUtils;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment implements LoaderCallbacks<List<Album>>,
        OnScrollListener, OnItemClickListener, MusicStateListener {

    private static final int GROUP_ID = 3;

    private static final int ONE = 1, TWO = 2, FOUR = 4;

    private static final int LOADER = 0;

    private ViewGroup mRootView;

    private AlbumAdapter mAdapter;

    private GridView mGridView;

    private ListView mListView;

    private MyContentObserver contentObserver;

    private long[] mAlbumList;

    private Album mAlbum;

    private boolean mShouldRefresh = false;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        ((BaseActivity)activity).setMusicStateListenerListener(this);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layout = R.layout.list_item_normal;
        if (isSimpleLayout()) {
            layout = R.layout.list_item_normal;
        } else if (isDetailedLayout()) {
            layout = R.layout.list_item_detailed;
        } else {
            layout = R.layout.grid_items_normal;
        }
        mAdapter = new AlbumAdapter(getActivity(), layout);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (isSimpleLayout()) {
            mRootView = (ViewGroup)inflater.inflate(R.layout.list_base, null);
            initListView();
        } else {
            mRootView = (ViewGroup)inflater.inflate(R.layout.grid_base, null);
            initGridView();
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        Handler handler = new Handler();
        contentObserver = new MyContentObserver(handler);
        getActivity().getContentResolver().registerContentObserver(Uri.parse("content://media"), true, contentObserver);
        getLoaderManager().initLoader(LOADER, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.flush();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        mAlbum = mAdapter.getItem(info.position);
        mAlbumList = MusicUtils.getSongListForAlbum(getActivity(), mAlbum.mAlbumId);
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE, getString(R.string.context_menu_play_selection));
        menu.add(GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE, getString(R.string.add_to_queue));
        final SubMenu subMenu = menu.addSubMenu(GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), GROUP_ID, subMenu, false);
        menu.add(GROUP_ID, FragmentMenuItems.MORE_BY_ARTIST, Menu.NONE, getString(R.string.context_menu_more_by_artist));
        menu.add(GROUP_ID, FragmentMenuItems.DELETE, Menu.NONE, getString(R.string.context_menu_delete));
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        if (item.getGroupId() == GROUP_ID) {
            switch (item.getItemId()) {
                case FragmentMenuItems.PLAY_SELECTION:
                    MusicUtils.playAll(getActivity(), mAlbumList, 0, false);
                    return true;
                case FragmentMenuItems.ADD_TO_QUEUE:
                    MusicUtils.addToQueue(getActivity(), mAlbumList);
                    return true;
                case FragmentMenuItems.NEW_PLAYLIST:
                    CreateNewPlaylist.getInstance(mAlbumList).show(getFragmentManager(), "CreatePlaylist");
                    return true;
                case FragmentMenuItems.MORE_BY_ARTIST:
                    NavUtils.openArtistProfile(getActivity(), mAlbum.mArtistName);
                    return true;
                case FragmentMenuItems.PLAYLIST_SELECTED:
                    final long id = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(getActivity(), mAlbumList, id);
                    return true;
                case FragmentMenuItems.DELETE:
                    mShouldRefresh = true;
                    final String album = mAlbum.mAlbumName;
                    DeleteDialog.newInstance(album, mAlbumList, ImageFetcher.generateAlbumCacheKey(album,mAlbum.mArtistName))
                            .show(getFragmentManager(), "DeleteDialog");
                    return true;
                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(contentObserver != null) getActivity().getContentResolver().unregisterContentObserver(contentObserver);
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
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        mAlbum = mAdapter.getItem(position);
        NavUtils.openAlbumProfile(getActivity(), mAlbum.mAlbumName, mAlbum.mArtistName, mAlbum.mAlbumId);
    }

    @Override
    public Loader<List<Album>> onCreateLoader(final int id, final Bundle args) {
        return new AlbumLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<Album>> loader, final List<Album> data) {
        if (data.isEmpty()) {
            mAdapter.unload();
            mAdapter.notifyDataSetChanged();
            final TextView empty = (TextView)mRootView.findViewById(R.id.empty);
            empty.setText(getString(R.string.empty_music));
            if (isSimpleLayout()) {
                mListView.setEmptyView(empty);
            } else {
                mGridView.setEmptyView(empty);
            }
            return;
        }

        mAdapter.unload();
        for (final Album album : data) {
            mAdapter.add(album);
        }
        mAdapter.buildCache();
    }

    @Override
    public void onLoaderReset(final Loader<List<Album>> loader) {
        mAdapter.unload();
    }

    public void scrollToCurrentAlbum() {
        final int currentAlbumPosition = getItemPositionByAlbum();
        if (currentAlbumPosition != 0) {
            if (isSimpleLayout()) {
                mListView.setSelection(currentAlbumPosition);
            } else {
                mGridView.setSelection(currentAlbumPosition);
            }
        }
    }

    private int getItemPositionByAlbum() {
        final long albumId = MusicUtils.getCurrentAlbumId();
        if (mAdapter == null) {
            return 0;
        }
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mAdapter.getItem(i).mAlbumId == albumId) {
                return i;
            }
        }
        return 0;
    }

    public void refresh() {
        SystemClock.sleep(10);
        getLoaderManager().restartLoader(LOADER, null, this);
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
    }

    @Override
    public void restartLoader() {
        if (mShouldRefresh) {
            getLoaderManager().restartLoader(LOADER, null, this);
        }
        mShouldRefresh = false;
    }

    @Override
    public void onMetaChanged() {
    }

    private void initAbsListView(final AbsListView list) {
        list.setRecyclerListener(new RecycleHolder());
        list.setOnCreateContextMenuListener(this);
        list.setOnItemClickListener(this);
        list.setOnScrollListener(this);
    }

    private void initListView() {
        mListView = (ListView)mRootView.findViewById(R.id.list_base);
        mListView.setAdapter(mAdapter);
        initAbsListView(mListView);
        mAdapter.setTouchPlay(true);
    }

    private void initGridView() {
        mGridView = (GridView)mRootView.findViewById(R.id.grid_base);
        mGridView.setAdapter(mAdapter);
        initAbsListView(mGridView);
        if (BassoUtils.isLandscape(getActivity()) && !BassoUtils.isSW600P(getActivity())) {
            if (isDetailedLayout()) {
                mAdapter.setLoadExtraData(true);
                mGridView.setNumColumns(TWO);
            } else {
                mGridView.setNumColumns(FOUR);
            }
        } else if(!BassoUtils.isSW600P(getActivity())) {
            if (isDetailedLayout()) {
                mAdapter.setLoadExtraData(true);
                mGridView.setNumColumns(ONE);
            } else {
                mGridView.setNumColumns(TWO);
            }
        }

        if (BassoUtils.isLandscape(getActivity()) && BassoUtils.isSW600P(getActivity())) {
            if (isDetailedLayout()) {
                mAdapter.setLoadExtraData(true);
                mGridView.setNumColumns(TWO);
            } else {
                mGridView.setNumColumns(FOUR);
            }
        } else if(BassoUtils.isSW600P(getActivity())) {
            if (isDetailedLayout()) {
                mAdapter.setLoadExtraData(true);
                mGridView.setNumColumns(ONE);
            } else {
                mGridView.setNumColumns(FOUR);
            }
        }
    }

    private boolean isSimpleLayout() {
        return PreferenceUtils.getInstance(getActivity()).isSimpleLayout(ALBUM_LAYOUT,
                getActivity());
    }

    private boolean isDetailedLayout() {
        return PreferenceUtils.getInstance(getActivity()).isDetailedLayout(ALBUM_LAYOUT,
                getActivity());
    }

    private class MyContentObserver extends ContentObserver {
        MyContentObserver(Handler handler) {
            super(handler);
        }
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange)
        {
            onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange,  Uri uri) {
            super.onChange(selfChange);
            getLoaderManager().restartLoader(AlbumFragment.LOADER, null, AlbumFragment.this);
        }
    }
}
