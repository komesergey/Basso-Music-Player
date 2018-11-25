package com.basso.basso.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.adapters.GenreAdapter;
import com.basso.basso.loaders.GenreLoader;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.model.Genre;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.ui.activities.ProfileActivity;
import com.basso.basso.utils.MusicUtils;

import java.util.List;

public class GenreFragment extends Fragment implements LoaderCallbacks<List<Genre>>,
        OnItemClickListener {

    private static final int GROUP_ID = 5;

    private static final int LOADER = 0;

    private ViewGroup mRootView;

    private GenreAdapter mAdapter;

    private ListView mListView;

    private long[] mGenreList;

    private Genre mGenre;

    public GenreFragment() {}

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new GenreAdapter(getActivity(), R.layout.list_item_simple);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mRootView = (ViewGroup)inflater.inflate(R.layout.list_base, null);
        mListView = (ListView)mRootView.findViewById(R.id.list_base);
        mListView.setAdapter(mAdapter);
        mListView.setRecyclerListener(new RecycleHolder());
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(LOADER, null, this);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        mGenre = mAdapter.getItem(info.position);
        mGenreList = MusicUtils.getSongListForGenre(getActivity(), mGenre.mGenreId);
        menu.add(GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE, R.string.context_menu_play_selection);
        menu.add(GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE, R.string.add_to_queue);
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        if (item.getGroupId() == GROUP_ID) {
            switch (item.getItemId()) {
                case FragmentMenuItems.PLAY_SELECTION:
                    MusicUtils.playAll(getActivity(), mGenreList, 0, false);
                    return true;
                case FragmentMenuItems.ADD_TO_QUEUE:
                    MusicUtils.addToQueue(getActivity(), mGenreList);
                    return true;
                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        mGenre = mAdapter.getItem(position);
        final Bundle bundle = new Bundle();
        bundle.putLong(Config.ID, mGenre.mGenreId);
        bundle.putString(Config.MIME_TYPE, MediaStore.Audio.Genres.CONTENT_TYPE);
        bundle.putString(Config.NAME, mGenre.mGenreName);
        final Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public Loader<List<Genre>> onCreateLoader(final int id, final Bundle args) {
        return new GenreLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<Genre>> loader, final List<Genre> data) {
        if (data.isEmpty()) {
            mAdapter.unload();
            mAdapter.notifyDataSetChanged();
            final TextView empty = (TextView)mRootView.findViewById(R.id.empty);
            empty.setText(getString(R.string.empty_music));
            mListView.setEmptyView(empty);
            return;
        }

        mAdapter.unload();
        for (final Genre genre : data) {
            mAdapter.add(genre);
        }
        mAdapter.buildCache();
    }

    @Override
    public void onLoaderReset(final Loader<List<Genre>> loader) {
        mAdapter.unload();
    }
}
