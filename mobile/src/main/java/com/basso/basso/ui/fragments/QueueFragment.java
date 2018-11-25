package com.basso.basso.ui.fragments;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.basso.basso.R;
import com.basso.basso.adapters.SongAdapter;
import com.basso.basso.dragdrop.DragSortListView;
import com.basso.basso.dragdrop.DragSortListView.DragScrollProfile;
import com.basso.basso.dragdrop.DragSortListView.DropListener;
import com.basso.basso.dragdrop.DragSortListView.RemoveListener;
import com.basso.basso.loaders.NowPlayingCursor;
import com.basso.basso.loaders.QueueLoader;
import com.basso.basso.menu.CreateNewPlaylist;
import com.basso.basso.menu.DeleteDialog;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.model.Song;
import com.basso.basso.provider.FavoritesStore;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.NavUtils;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;

public class QueueFragment extends Fragment implements LoaderCallbacks<List<Song>>,
        OnItemClickListener, DropListener, RemoveListener, DragScrollProfile {

    private static final int GROUP_ID = 13;

    private static final int LOADER = 0;

    private SongAdapter mAdapter;

    private DragSortListView mListView;

    private Song mSong;

    private int mSelectedPosition;

    private MyContentObserver contentObserver;

    private long mSelectedId;

    private String mSongName, mAlbumName, mArtistName;

    public QueueFragment() {
    }
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SongAdapter(getActivity(), R.layout.edit_track_list_item);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.list_base, null);
        mListView = (DragSortListView)rootView.findViewById(R.id.list_base);
        mListView.setAdapter(mAdapter);
        mListView.setRecyclerListener(new RecycleHolder());
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setDropListener(this);
        mListView.setRemoveListener(this);
        mListView.setDragScrollProfile(this);
        return rootView;
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
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        mSelectedPosition = info.position;
        mSong = mAdapter.getItem(mSelectedPosition);
        mSelectedId = mSong.mSongId;
        mSongName = mSong.mSongName;
        mAlbumName = mSong.mAlbumName;
        mArtistName = mSong.mArtistName;
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_NEXT, Menu.NONE, getString(R.string.context_menu_play_next));
        final SubMenu subMenu = menu.addSubMenu(GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), GROUP_ID, subMenu, true);
        menu.add(GROUP_ID, FragmentMenuItems.REMOVE_FROM_QUEUE, Menu.NONE, getString(R.string.remove_from_queue));
        menu.add(GROUP_ID, FragmentMenuItems.MORE_BY_ARTIST, Menu.NONE, getString(R.string.context_menu_more_by_artist));
        menu.add(GROUP_ID, FragmentMenuItems.USE_AS_RINGTONE, Menu.NONE, getString(R.string.context_menu_use_as_ringtone));
        menu.add(GROUP_ID, FragmentMenuItems.DELETE, Menu.NONE, getString(R.string.context_menu_delete));
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        if (item.getGroupId() == GROUP_ID) {
            switch (item.getItemId()) {
                case FragmentMenuItems.PLAY_NEXT:
                    NowPlayingCursor queue = (NowPlayingCursor)QueueLoader
                            .makeQueueCursor(getActivity());
                    queue.removeItem(mSelectedPosition);
                    queue.close();
                    queue = null;
                    MusicUtils.playNext(new long[] {
                        mSelectedId
                    });
                    refreshQueue();
                    return true;
                case FragmentMenuItems.REMOVE_FROM_QUEUE:
                    MusicUtils.removeTrack(mSelectedId);
                    refreshQueue();
                    return true;
                case FragmentMenuItems.ADD_TO_FAVORITES:
                    FavoritesStore.getInstance(getActivity()).addSongId(
                            mSelectedId, mSongName, mAlbumName, mArtistName);
                    return true;
                case FragmentMenuItems.NEW_PLAYLIST:
                    CreateNewPlaylist.getInstance(new long[] {
                        mSelectedId
                    }).show(getFragmentManager(), "CreatePlaylist");
                    return true;
                case FragmentMenuItems.PLAYLIST_SELECTED:
                    final long mPlaylistId = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(getActivity(), new long[] {
                        mSelectedId
                    }, mPlaylistId);
                    return true;
                case FragmentMenuItems.MORE_BY_ARTIST:
                    NavUtils.openArtistProfile(getActivity(), mArtistName);
                    return true;
                case FragmentMenuItems.USE_AS_RINGTONE:
                    MusicUtils.setRingtone(getActivity(), mSelectedId);
                    return true;
                case FragmentMenuItems.DELETE:
                    DeleteDialog.newInstance(mSong.mSongName, new long[] {
                        mSelectedId
                    }, null).show(getFragmentManager(), "DeleteDialog");
                    return true;
                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        MusicUtils.setQueuePosition(position);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(final int id, final Bundle args) {
        return new QueueLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<Song>> loader, final List<Song> data) {
        if (data.isEmpty()) {
            mAdapter.unload();
            mAdapter.notifyDataSetChanged();
            return;
        }

        mAdapter.unload();
        for (final Song song : data) {
            mAdapter.add(song);
        }
        mAdapter.buildCache();
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
        mSong = mAdapter.getItem(which);
        mAdapter.remove(mSong);
        mAdapter.notifyDataSetChanged();
        MusicUtils.removeTrack(mSong.mSongId);
        mAdapter.buildCache();
    }

    @Override
    public void drop(final int from, final int to) {
        mSong = mAdapter.getItem(from);
        mAdapter.remove(mSong);
        mAdapter.insert(mSong, to);
        mAdapter.notifyDataSetChanged();
        MusicUtils.moveQueueItem(from, to);
        mAdapter.buildCache();
    }

    public void scrollToCurrentSong() {
        final int currentSongPosition = getItemPositionBySong();
        if (currentSongPosition != 0) {
            mListView.setSelection(currentSongPosition);
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(contentObserver != null)
            getActivity().getContentResolver().unregisterContentObserver(contentObserver);
    }

    private int getItemPositionBySong() {
        final long trackId = MusicUtils.getCurrentAudioId();
        if (mAdapter == null) {
            return 0;
        }
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mAdapter.getItem(i).mSongId == trackId) {
                return i;
            }
        }
        return 0;
    }

    public void refreshQueue() {
        if (isAdded()) {
            getLoaderManager().restartLoader(LOADER, null, this);
        }
    }

    private class MyContentObserver extends ContentObserver {
        MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {return true;}

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange,  Uri uri) {
            super.onChange(selfChange);
            getLoaderManager().restartLoader(QueueFragment.LOADER, null, QueueFragment.this);
        }
    }
}