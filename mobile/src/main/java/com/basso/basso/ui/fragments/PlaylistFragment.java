package com.basso.basso.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.basso.basso.Config;
import com.basso.basso.MusicStateListener;
import com.basso.basso.R;
import com.basso.basso.adapters.PlaylistAdapter;
import com.basso.basso.loaders.PlaylistLoader;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.menu.RenamePlaylist;
import com.basso.basso.model.Playlist;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.ui.activities.BaseActivity;
import com.basso.basso.ui.activities.ProfileActivity;
import com.basso.basso.utils.MusicUtils;

import java.util.List;

public class PlaylistFragment extends Fragment implements LoaderCallbacks<List<Playlist>>,
        OnItemClickListener, MusicStateListener {

    private static final int GROUP_ID = 0;

    private static final int LOADER = 0;

    private PlaylistAdapter mAdapter;

    private ListView mListView;

    private MyContentObserver contentObserver;

    private Playlist mPlaylist;

    public PlaylistFragment() {}

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        ((BaseActivity)activity).setMusicStateListenerListener(this);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new PlaylistAdapter(getActivity(), R.layout.list_item_simple);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.list_base, null);
        mListView = (ListView)rootView.findViewById(R.id.list_base);
        mListView.setAdapter(mAdapter);
        mListView.setRecyclerListener(new RecycleHolder());
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(contentObserver != null) getActivity().getContentResolver().unregisterContentObserver(contentObserver);
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
        final int mPosition = info.position;
        mPlaylist = mAdapter.getItem(mPosition);
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE, R.string.context_menu_play_selection);
        menu.add(GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE, R.string.add_to_queue);
        if (info.position > 1) {
            menu.add(GROUP_ID, FragmentMenuItems.RENAME_PLAYLIST, Menu.NONE, R.string.context_menu_rename_playlist);
            menu.add(GROUP_ID, FragmentMenuItems.DELETE, Menu.NONE, R.string.context_menu_delete);
        }
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        if (item.getGroupId() == GROUP_ID) {
            final AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
            switch (item.getItemId()) {
                case FragmentMenuItems.PLAY_SELECTION:
                    if (info.position == 0) {
                        MusicUtils.playFavorites(getActivity());
                    } else if (info.position == 1) {
                        MusicUtils.playLastAdded(getActivity());
                    } else {
                        MusicUtils.playPlaylist(getActivity(), mPlaylist.mPlaylistId);
                    }
                    return true;
                case FragmentMenuItems.ADD_TO_QUEUE:
                    long[] list = null;
                    if (info.position == 0) {
                        list = MusicUtils.getSongListForFavorites(getActivity());
                    } else if (info.position == 1) {
                        list = MusicUtils.getSongListForLastAdded(getActivity());
                    } else {
                        list = MusicUtils.getSongListForPlaylist(getActivity(),
                                mPlaylist.mPlaylistId);
                    }
                    MusicUtils.addToQueue(getActivity(), list);
                    return true;
                case FragmentMenuItems.RENAME_PLAYLIST:
                    RenamePlaylist.getInstance(mPlaylist.mPlaylistId).show(
                            getFragmentManager(), "RenameDialog");
                    return true;
                case FragmentMenuItems.DELETE:
                    buildDeleteDialog().show();
                    return true;
                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        final Bundle bundle = new Bundle();
        mPlaylist = mAdapter.getItem(position);
        String playlistName;
        if (position == 0) {
            playlistName = getString(R.string.playlist_favorites);
            bundle.putString(Config.MIME_TYPE, getString(R.string.playlist_favorites));
        } else if (position == 1) {
            playlistName = getString(R.string.playlist_last_added);
            bundle.putString(Config.MIME_TYPE, getString(R.string.playlist_last_added));
        } else {
            playlistName = mPlaylist.mPlaylistName;
            bundle.putString(Config.MIME_TYPE, MediaStore.Audio.Playlists.CONTENT_TYPE);
            bundle.putLong(Config.ID, mPlaylist.mPlaylistId);
        }

        bundle.putString(Config.NAME, playlistName);

        final Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public Loader<List<Playlist>> onCreateLoader(final int id, final Bundle args) {
        return new PlaylistLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<Playlist>> loader, final List<Playlist> data) {
        if (data.isEmpty()) {
            mAdapter.unload();
            mAdapter.notifyDataSetChanged();
            return;
        }

        mAdapter.unload();
        for (final Playlist playlist : data) {
            mAdapter.add(playlist);
        }
        mAdapter.buildCache();
    }

    @Override
    public void onLoaderReset(final Loader<List<Playlist>> loader) {
        mAdapter.unload();
    }

    @Override
    public void restartLoader() {
        System.out.println("Playlists reloaded");
        getLoaderManager().restartLoader(LOADER, null, this);
    }

    @Override
    public void onMetaChanged() {
    }

    private final AlertDialog buildDeleteDialog() {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.delete_dialog_title, mPlaylist.mPlaylistName))
                .setPositiveButton(R.string.context_menu_delete, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final Uri mUri = ContentUris.withAppendedId(
                                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                                mPlaylist.mPlaylistId);
                        getActivity().getContentResolver().delete(mUri, null, null);
                        MusicUtils.refresh();
                    }
                }).setNegativeButton(R.string.cancel, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                }).setMessage(R.string.cannot_be_undone).create();
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
            getLoaderManager().restartLoader(PlaylistFragment.LOADER, null, PlaylistFragment.this);
        }
    }
}
