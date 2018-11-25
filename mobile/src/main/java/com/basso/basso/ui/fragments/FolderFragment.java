package com.basso.basso.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.basso.basso.R;
import com.basso.basso.adapters.FileAdepter;
import com.basso.basso.loaders.FolderLoader;
import com.basso.basso.menu.CreateNewPlaylist;
import com.basso.basso.menu.FragmentMenuItems;
import com.basso.basso.model.FileMixed;
import com.basso.basso.provider.FavoritesStore;
import com.basso.basso.ui.activities.Tagger;
import com.basso.basso.utils.MusicUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<FileMixed>>, AdapterView.OnItemClickListener {
    private File file;
    private ListView listView;
    private TextView pathTextView;
    private FileAdepter fileAdepter;
    private static final int GROUP_ID = 1408;
    private String mSongName, mAlbumName, mArtistName;
    private int mSelectedPosition;
    private long mSelectedId;
    private FileMixed mSong;
    private static final int LOADER = 0;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        ViewGroup mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_folder, null);
        listView=(ListView) mRootView.findViewById(R.id.pathlist);
        pathTextView=(TextView) mRootView.findViewById(R.id.path);
        String root_sd = Environment.getExternalStorageDirectory().toString();
        listView.setAdapter(new FileAdepter(getActivity(), R.layout.list_item_normal, new ArrayList<FileMixed>()));
        listView.setOnCreateContextMenuListener(this);
        pathTextView.setText(root_sd);
        file = new File( root_sd ) ;
        Bundle args = new Bundle();
        args.putString("path", root_sd);
        getLoaderManager().initLoader(LOADER, args, this);
        listView.setOnItemClickListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
                                    final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        mSelectedPosition = info.position;
        mSong = fileAdepter.getItem(mSelectedPosition);
        if(!mSong.isDirectory()) {
            mSelectedId = Long.parseLong(mSong.getAUDIO_ID());
            mSongName = mSong.getSongName();
            mAlbumName = mSong.getAlbumName();
            mArtistName = mSong.getArtistName();
            menu.add(GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE, getString(R.string.context_menu_play_selection));
            menu.add(GROUP_ID, FragmentMenuItems.PLAY_NEXT, Menu.NONE, getString(R.string.context_menu_play_next));
            menu.add(GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE, getString(R.string.add_to_queue));
            final SubMenu subMenu = menu.addSubMenu(GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
            MusicUtils.makePlaylistMenu(getActivity(), GROUP_ID, subMenu, true);
            menu.add(GROUP_ID, FragmentMenuItems.USE_AS_RINGTONE, Menu.NONE, getString(R.string.context_menu_use_as_ringtone));
            menu.add(GROUP_ID, FragmentMenuItems.EDIT_TAGS, Menu.NONE, getString(R.string.edit_tags));
        }
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        if (item.getGroupId() == GROUP_ID) {
            switch (item.getItemId()) {
                case FragmentMenuItems.PLAY_SELECTION:
                    MusicUtils.playAll(getActivity(), new long[] {
                            mSelectedId
                    }, 0, false);
                    return true;
                case FragmentMenuItems.PLAY_NEXT:
                    MusicUtils.playNext(new long[] {
                            mSelectedId
                    });
                    return true;
                case FragmentMenuItems.ADD_TO_QUEUE:
                    MusicUtils.addToQueue(getActivity(), new long[] {
                            mSelectedId
                    });
                    return true;
                case FragmentMenuItems.ADD_TO_FAVORITES:
                    FavoritesStore.getInstance(getActivity()).addSongId(
                            mSelectedId, mSongName, mAlbumName, mArtistName);
                    return true;
                case FragmentMenuItems.NEW_PLAYLIST:
                    CreateNewPlaylist.getInstance(new long[]{
                            mSelectedId
                    }).show(getFragmentManager(), "CreatePlaylist");
                    return true;
                case FragmentMenuItems.PLAYLIST_SELECTED:
                    final long mPlaylistId = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(getActivity(), new long[] {
                            mSelectedId
                    }, mPlaylistId);
                    return true;
                case FragmentMenuItems.USE_AS_RINGTONE:
                    MusicUtils.setRingtone(getActivity(), mSelectedId);
                    return true;
                case FragmentMenuItems.EDIT_TAGS:
                    Intent intent = new Intent(getActivity(), Tagger.class);
                    intent.putExtra("path", mSong.getDATA());
                    startActivity(intent);
                    return true;
                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }
    public boolean onBackPressed(){
        try {
            if(file.getPath().equals(Environment.getExternalStorageDirectory().toString()))
                return false;
            String parent = file.getParent();
            file = new File(parent);
            pathTextView.setText(parent);
            Bundle args = new Bundle();
            args.putString("path", parent);
            getLoaderManager().restartLoader(LOADER, args, this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        if(fileAdepter != null){
            FileMixed fileFromAdapter= fileAdepter.getItem(position);
            if(fileFromAdapter.isDirectory()){
                file = new File(file, fileAdepter.getItem(position).getDirectoryName());
                Bundle args = new Bundle();
                args.putString("path", file.toString());
                pathTextView.setText(file.toString());
                getLoaderManager().restartLoader(LOADER, args, this);
            } else {
                MusicUtils.playAll(getActivity(), getFolderSongs(), position, false);
            }
        }
    }

    public long[] getFolderSongs(){
        if(fileAdepter != null){
            List<FileMixed>  output = fileAdepter.getData();
            long[] songs = new long[output.size()];
            for(int i = 0; i < output.size(); i++){
                songs[i] = Long.parseLong(output.get(i).getAUDIO_ID());
            }
            return songs;
        }
        return new long[0];
    }
    @Override
    public Loader<List<FileMixed>> onCreateLoader(final int id, final Bundle args) {
        return new FolderLoader(getActivity(), new File(args.getString("path")));
    }
    @Override
    public void onLoaderReset(final Loader<List<FileMixed>> loader) {

    }
    @Override
    public void onLoadFinished(final Loader<List<FileMixed>> loader, final List<FileMixed> data) {
        fileAdepter = new FileAdepter(getActivity(), R.layout.list_item_normal, data);
        listView.setAdapter(fileAdepter);
    }
}
