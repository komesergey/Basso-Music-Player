package com.basso.basso.ui.fragments.profile;

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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.adapters.ArtistAlbumAdapter;
import com.basso.basso.loaders.ArtistAlbumLoader;
import com.basso.basso.menu.CreateNewPlaylist;
import com.basso.basso.menu.DeleteDialog;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.model.Album;
import com.basso.basso.model.Song;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.NavUtils;
import com.basso.basso.widgets.ProfileTabCarousel;
import com.basso.basso.widgets.VerticalScrollListener;
import com.basso.basso.widgets.VerticalScrollListener.ScrollableHeader;

import java.util.ArrayList;
import java.util.List;

public class ArtistAlbumFragment extends Fragment implements LoaderCallbacks<List<Album>>, OnItemClickListener {

    private static final int GROUP_ID = 10;

    private static final int LOADER = 0;

    private ArtistAlbumAdapter mAdapter;

    private ListView mListView;

    private MyContentObserver contentObserver;

    private long[] mAlbumList;

    private Album mAlbum;

    private ProfileTabCarousel mProfileTabCarousel;

    public ArtistAlbumFragment() {}

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mProfileTabCarousel = (ProfileTabCarousel)activity.findViewById(R.id.acivity_profile_base_tab_carousel);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ArtistAlbumAdapter(getActivity(), R.layout.list_item_normal);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.list_base, null);
        mListView = (ListView)rootView.findViewById(R.id.list_base);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);
        mListView.setAdapter(mAdapter);
        mListView.setRecyclerListener(new RecycleHolder());
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(new VerticalScrollListener(mScrollableHeader, mProfileTabCarousel, 2));
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setFastScrollEnabled(false);
        mListView.setPadding(0, 0, 0, 0);
        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        Handler handler = new Handler();
        contentObserver = new MyContentObserver(handler);
        getActivity().getContentResolver().registerContentObserver(Uri.parse("content://media"), true, contentObserver);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            getLoaderManager().initLoader(LOADER, arguments, this);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(contentObserver != null) getActivity().getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.flush();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(getArguments() != null ? getArguments() : new Bundle());
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        mAlbum = mAdapter.getItem(info.position - 1);
        mAlbumList = MusicUtils.getSongListForAlbum(getActivity(), mAlbum.mAlbumId);
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE, getString(R.string.context_menu_play_selection));
        menu.add(GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE, getString(R.string.add_to_queue));
        final SubMenu subMenu = menu.addSubMenu(GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), GROUP_ID, subMenu, false);
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
                case FragmentMenuItems.PLAYLIST_SELECTED:
                    final long id = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(getActivity(), mAlbumList, id);
                    return true;
                case FragmentMenuItems.DELETE:
                    DeleteDialog.newInstance(mAlbum.mAlbumName, mAlbumList, null).show(getFragmentManager(), "DeleteDialog");
                    refresh();
                    return true;
                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        if (position == 0) {
            return;
        }
        mAlbum = mAdapter.getItem(position - 1);
        NavUtils.openAlbumProfile(getActivity(), mAlbum.mAlbumName, mAlbum.mArtistName, mAlbum.mAlbumId);
        getActivity().finish();
    }

    @Override
    public Loader<List<Album>> onCreateLoader(final int id, final Bundle args) {
        return new ArtistAlbumLoader(getActivity(), args.getLong(Config.ID));
    }

    @Override
    public void onLoadFinished(final Loader<List<Album>> loader, final List<Album> data) {
        if (data.isEmpty()) {
            mAdapter.unload();
            mAdapter.setCount(new ArrayList<Album>());
            mAdapter.notifyDataSetChanged();
            return;
        }

        mAdapter.unload();
        mAdapter.setCount(data);
        for (final Album album : data) {
            mAdapter.add(album);
        }
    }

    @Override
    public void onLoaderReset(final Loader<List<Album>> loader) {
        mAdapter.unload();
    }

    private final ScrollableHeader mScrollableHeader = new ScrollableHeader() {

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
    };

    public void refresh() {
        mListView.setSelection(0);
        SystemClock.sleep(10);
        mAdapter.notifyDataSetChanged();
        getLoaderManager().restartLoader(LOADER, getArguments(), this);
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
            getLoaderManager().restartLoader(ArtistAlbumFragment.LOADER, getArguments(), ArtistAlbumFragment.this);
        }
    }
}