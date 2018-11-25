package com.basso.basso.ui.fragments.profile;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
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

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.adapters.ProfileSongAdapter;
import com.basso.basso.dragdrop.DragSortListView;
import com.basso.basso.dragdrop.DragSortListView.DragScrollProfile;
import com.basso.basso.dragdrop.DragSortListView.DropListener;
import com.basso.basso.dragdrop.DragSortListView.RemoveListener;
import com.basso.basso.loaders.PlaylistSongLoader;
import com.basso.basso.menu.CreateNewPlaylist;
import com.basso.basso.menu.DeleteDialog;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.model.Song;
import com.basso.basso.provider.FavoritesStore;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.ui.activities.Tagger;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.NavUtils;
import com.basso.basso.widgets.ProfileTabCarousel;
import com.basso.basso.widgets.VerticalScrollListener;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongFragment extends Fragment implements LoaderCallbacks<List<Song>>, OnItemClickListener, DropListener, RemoveListener, DragScrollProfile {

    private static final int GROUP_ID = 8;

    private static final int LOADER = 0;

    private ProfileSongAdapter mAdapter;

    private DragSortListView mListView;

    private Song mSong;

    private int mSelectedPosition;

    private MyContentObserver contentObserver;

    private long mSelectedId;

    private String mSongName, mAlbumName, mArtistName;

    private ProfileTabCarousel mProfileTabCarousel;

    private long mPlaylistId;

    public PlaylistSongFragment() {}

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mProfileTabCarousel = (ProfileTabCarousel)activity.findViewById(R.id.acivity_profile_base_tab_carousel);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ProfileSongAdapter(
                getActivity(),
                R.layout.edit_track_list_item,
                ProfileSongAdapter.DISPLAY_PLAYLIST_SETTING
        );
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.list_base, null);
        mListView = (DragSortListView)rootView.findViewById(R.id.list_base);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);
        mListView.setAdapter(mAdapter);
        mListView.setRecyclerListener(new RecycleHolder());
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setDropListener(this);
        mListView.setRemoveListener(this);
        mListView.setDragScrollProfile(this);
        mListView.setOnScrollListener(new VerticalScrollListener(null, mProfileTabCarousel, 0));
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
            mPlaylistId = arguments.getLong(Config.ID);
            getLoaderManager().initLoader(LOADER, arguments, this);
        }
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
        mSelectedPosition = info.position - 1;
        mSong = mAdapter.getItem(mSelectedPosition);
        mSelectedId = mSong.mSongId;
        mSongName = mSong.mSongName;
        mAlbumName = mSong.mAlbumName;
        mArtistName = mSong.mArtistName;
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE, getString(R.string.context_menu_play_selection));
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_NEXT, Menu.NONE, getString(R.string.context_menu_play_next));
        menu.add(GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE, getString(R.string.add_to_queue));
        final SubMenu subMenu = menu.addSubMenu(GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), GROUP_ID, subMenu, true);
        menu.add(GROUP_ID, FragmentMenuItems.MORE_BY_ARTIST, Menu.NONE, getString(R.string.context_menu_more_by_artist));
        menu.add(GROUP_ID, FragmentMenuItems.USE_AS_RINGTONE, Menu.NONE, getString(R.string.context_menu_use_as_ringtone));
        menu.add(GROUP_ID, FragmentMenuItems.REMOVE_FROM_PLAYLIST, Menu.NONE, getString(R.string.context_menu_remove_from_playlist));
        menu.add(GROUP_ID, FragmentMenuItems.EDIT_TAGS, Menu.NONE, getString(R.string.edit_tags));
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
                case FragmentMenuItems.ADD_TO_FAVORITES:
                    FavoritesStore.getInstance(getActivity()).addSongId(mSelectedId, mSongName, mAlbumName, mArtistName);
                    return true;
                case FragmentMenuItems.NEW_PLAYLIST:
                    CreateNewPlaylist.getInstance(new long[] {mSelectedId}).show(getFragmentManager(), "CreatePlaylist");
                    return true;
                case FragmentMenuItems.PLAYLIST_SELECTED:
                    final long playlistId = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(getActivity(), new long[] {mSelectedId}, playlistId);
                    return true;
                case FragmentMenuItems.MORE_BY_ARTIST:
                    NavUtils.openArtistProfile(getActivity(), mArtistName);
                    return true;
                case FragmentMenuItems.USE_AS_RINGTONE:
                    MusicUtils.setRingtone(getActivity(), mSelectedId);
                    return true;
                case FragmentMenuItems.EDIT_TAGS:
                    Intent intent = new Intent(getActivity(), Tagger.class);
                    intent.putExtra("path", BassoUtils.pathFromSongId(getActivity(), mSelectedId));
                    startActivity(intent);
                    return true;
                case FragmentMenuItems.DELETE:
                    DeleteDialog.newInstance(mSong.mSongName, new long[] {mSelectedId}, null).show(getFragmentManager(), "DeleteDialog");
                    SystemClock.sleep(10);
                    mAdapter.notifyDataSetChanged();
                    getLoaderManager().restartLoader(LOADER, null, this);
                    return true;
                case FragmentMenuItems.REMOVE_FROM_PLAYLIST:
                    mAdapter.remove(mSong);
                    mAdapter.notifyDataSetChanged();
                    MusicUtils.removeFromPlaylist(getActivity(), mSong.mSongId, mPlaylistId);
                    getLoaderManager().restartLoader(LOADER, null, this);
                    return true;
                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        MusicUtils.playAllFromUserItemClick(getActivity(), mAdapter, position);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(final int id, final Bundle args) {
        return new PlaylistSongLoader(getActivity(), mPlaylistId);
    }

    @Override
    public void onLoadFinished(final Loader<List<Song>> loader, final List<Song> data) {
        if (data.isEmpty()) {
            mAdapter.unload();
            mAdapter.setCount(new ArrayList<Song>());
            mAdapter.notifyDataSetChanged();
            return;
        }
        mAdapter.unload();
        mAdapter.setCount(data);
        for (final Song song : data) {
            mAdapter.add(song);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(final Loader<List<Song>> loader) {
        mAdapter.unload();
    }

    @Override
    public float getSpeed(final float w, final long t) {
        if (w > 0.8f) {
            return mAdapter.getCount() / 0.001f;
        } else {
            return 10.0f * w;
        }
    }

    @Override
    public void remove(final int which) {
        mSong = mAdapter.getItem(which - 1);
        mAdapter.remove(mSong);
        mAdapter.notifyDataSetChanged();
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", mPlaylistId);
        getActivity().getContentResolver().delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=" + mSong.mSongId, null);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(contentObserver != null) getActivity().getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    public void drop(final int from, final int to) {
        if (from == 0 || to == 0) {
            mAdapter.notifyDataSetChanged();
            return;
        }
        final int realFrom = from - 1;
        final int realTo = to - 1;
        mSong = mAdapter.getItem(realFrom);
        mAdapter.remove(mSong);
        mAdapter.insert(mSong, realTo);
        mAdapter.notifyDataSetChanged();
        MediaStore.Audio.Playlists.Members.moveItem(getActivity().getContentResolver(),
                mPlaylistId, realFrom, realTo);
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
            System.out.println("Playlist song updated");
            getLoaderManager().restartLoader(PlaylistSongFragment.LOADER, null, PlaylistSongFragment.this);
        }
    }
}
