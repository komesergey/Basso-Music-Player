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
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.basso.basso.R;
import com.basso.basso.adapters.ProfileSongAdapter;
import com.basso.basso.loaders.FavoritesLoader;
import com.basso.basso.menu.CreateNewPlaylist;
import com.basso.basso.menu.DeleteDialog;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.model.Song;
import com.basso.basso.provider.FavoritesStore;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.NavUtils;
import com.basso.basso.widgets.ProfileTabCarousel;
import com.basso.basso.widgets.VerticalScrollListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment implements LoaderCallbacks<List<Song>>, OnItemClickListener {

    private static final int GROUP_ID = 6;

    private static final int LOADER = 0;

    private ViewGroup mRootView;

    private ProfileSongAdapter mAdapter;

    private ListView mListView;

    private MyContentObserver contentObserver;

    private Song mSong;

    private int mSelectedPosition;

    private long mSelectedId;

    private String mArtistName;

    private ProfileTabCarousel mProfileTabCarousel;

    public FavoriteFragment() {}

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mProfileTabCarousel = (ProfileTabCarousel)activity.findViewById(R.id.acivity_profile_base_tab_carousel);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ProfileSongAdapter(getActivity(), R.layout.list_item_simple, ProfileSongAdapter.DISPLAY_PLAYLIST_SETTING);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mRootView = (ViewGroup)inflater.inflate(R.layout.list_base, null);
        mListView = (ListView)mRootView.findViewById(R.id.list_base);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);
        mListView.setAdapter(mAdapter);
        mListView.setRecyclerListener(new RecycleHolder());
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(new VerticalScrollListener(null, mProfileTabCarousel, 0));
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setFastScrollEnabled(false);
        mListView.setPadding(0, 0, 0, 0);
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
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        mSelectedPosition = info.position - 1;
        mSong = mAdapter.getItem(mSelectedPosition);
        mSelectedId = mSong.mSongId;
        mArtistName = mSong.mArtistName;
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE, getString(R.string.context_menu_play_selection));
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_NEXT, Menu.NONE, getString(R.string.context_menu_play_next));
        menu.add(GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE, getString(R.string.add_to_queue));
        final SubMenu subMenu = menu.addSubMenu(GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), GROUP_ID, subMenu, false);
        menu.add(GROUP_ID, FragmentMenuItems.MORE_BY_ARTIST, Menu.NONE, getString(R.string.context_menu_more_by_artist));
        menu.add(GROUP_ID, FragmentMenuItems.USE_AS_RINGTONE, Menu.NONE, getString(R.string.context_menu_use_as_ringtone));
        menu.add(GROUP_ID, FragmentMenuItems.REMOVE_FROM_FAVORITES, Menu.NONE, getString(R.string.remove_from_favorites));
        menu.add(GROUP_ID, FragmentMenuItems.DELETE, Menu.NONE, getString(R.string.context_menu_delete));
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        if (item.getGroupId() == GROUP_ID) {
            switch (item.getItemId()) {
                case FragmentMenuItems.PLAY_SELECTION:
                    MusicUtils.playAll(getActivity(), new long[] {mSelectedId}, 0, false);
                    return true;
                case FragmentMenuItems.PLAY_NEXT:
                    MusicUtils.playNext(new long[] {mSelectedId});
                    return true;
                case FragmentMenuItems.ADD_TO_QUEUE:
                    MusicUtils.addToQueue(getActivity(), new long[] {mSelectedId});
                    return true;
                case FragmentMenuItems.NEW_PLAYLIST:
                    CreateNewPlaylist.getInstance(new long[] {mSelectedId}).show(getFragmentManager(), "CreatePlaylist");
                    return true;
                case FragmentMenuItems.PLAYLIST_SELECTED:
                    final long mPlaylistId = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(getActivity(), new long[] {mSelectedId}, mPlaylistId);
                    return true;
                case FragmentMenuItems.MORE_BY_ARTIST:
                    NavUtils.openArtistProfile(getActivity(), mArtistName);
                    return true;
                case FragmentMenuItems.USE_AS_RINGTONE:
                    MusicUtils.setRingtone(getActivity(), mSelectedId);
                    return true;
                case FragmentMenuItems.REMOVE_FROM_FAVORITES:
                    FavoritesStore.getInstance(getActivity()).removeItem(mSelectedId);
                    getLoaderManager().restartLoader(LOADER, null, this);
                    getActivity().getContentResolver().notifyChange(Uri.parse("content://media"), null);
                    return true;
                case FragmentMenuItems.DELETE:
                    DeleteDialog.newInstance(mSong.mSongName, new long[] {mSelectedId}, null).show(getFragmentManager(), "DeleteDialog");
                    SystemClock.sleep(10);
                    mAdapter.notifyDataSetChanged();
                    getLoaderManager().restartLoader(LOADER, null, this);
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
        if(contentObserver != null)
            getActivity().getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        MusicUtils.playAllFromUserItemClick(getActivity(), mAdapter, position);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(final int id, final Bundle args) {
        return new FavoritesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<Song>> loader, final List<Song> data) {
        if (data.isEmpty()) {
            mAdapter.unload();
            mAdapter.setCount(new ArrayList<Song>());
            mAdapter.notifyDataSetChanged();
            final TextView empty = (TextView)mRootView.findViewById(R.id.empty);
            empty.setText(getString(R.string.empty_favorite));
            empty.setPadding(0,mProfileTabCarousel.getTabHeight(),0,0);
            mListView.setEmptyView(empty);
            return;
        }

        mAdapter.unload();
        mAdapter.setCount(data);
        for (final Song song : data) {
            mAdapter.add(song);
        }
    }

    @Override
    public void onLoaderReset(final Loader<List<Song>> loader) {
        mAdapter.unload();
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
            getLoaderManager().restartLoader(FavoriteFragment.LOADER, null, FavoriteFragment.this);
        }
    }
}
