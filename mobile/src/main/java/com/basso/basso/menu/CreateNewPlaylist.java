package com.basso.basso.menu;

import android.app.Dialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import com.basso.basso.R;
import com.basso.basso.format.Capitalize;
import com.basso.basso.utils.MusicUtils;

public class CreateNewPlaylist extends BasePlaylistDialog {

    private long[] mPlaylistList = new long[] {};

    public static CreateNewPlaylist getInstance(final long[] list) {
        final CreateNewPlaylist frag = new CreateNewPlaylist();
        final Bundle args = new Bundle();
        args.putLongArray("playlist_list", list);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onSaveInstanceState(final Bundle outcicle) {
        outcicle.putString("defaultname", mPlaylist.getText().toString());
    }

    @Override
    public void initObjects(final Bundle savedInstanceState) {
        mPlaylistList = getArguments().getLongArray("playlist_list");
        mDefaultname = savedInstanceState != null ? savedInstanceState.getString("defaultname")
                : makePlaylistName();
        if (mDefaultname == null) {
            getDialog().dismiss();
            return;
        }
        final String prromptformat = getString(R.string.create_playlist_prompt);
        mPrompt = String.format(prromptformat, mDefaultname);
    }

    @Override
    public void onSaveClick() {
        final String playlistName = mPlaylist.getText().toString();
        if (playlistName != null && playlistName.length() > 0) {
            final int playlistId = (int)MusicUtils.getIdForPlaylist(getActivity(),
                    playlistName);
            if (playlistId >= 0) {
                MusicUtils.clearPlaylist(getActivity(), playlistId);
                MusicUtils.addToPlaylist(getActivity(), mPlaylistList, playlistId);
            } else {
                final long newId = MusicUtils.createPlaylist(getActivity(),
                        Capitalize.capitalize(playlistName));
                MusicUtils.addToPlaylist(getActivity(), mPlaylistList, newId);
            }
            closeKeyboard();
            getDialog().dismiss();
        }
    }

    @Override
    public void onTextChangedListener() {
        final String playlistName = mPlaylist.getText().toString();
        mSaveButton = mPlaylistDialog.getButton(Dialog.BUTTON_POSITIVE);
        if (mSaveButton == null) {
            return;
        }
        if (playlistName.trim().length() == 0) {
            mSaveButton.setEnabled(false);
        } else {
            mSaveButton.setEnabled(true);
            if (MusicUtils.getIdForPlaylist(getActivity(), playlistName) >= 0) {
                mSaveButton.setText(R.string.overwrite);
            } else {
                mSaveButton.setText(R.string.save);
            }
        }
    }

    private String makePlaylistName() {
        final String template = getString(R.string.new_playlist_name_template);
        int num = 1;
        final String[] projection = new String[] {
            MediaStore.Audio.Playlists.NAME
        };
        final ContentResolver resolver = getActivity().getContentResolver();
        final String selection = MediaStore.Audio.Playlists.NAME + " != ''";
        Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projection,
                selection, null, MediaStore.Audio.Playlists.NAME);
        if (cursor == null) {
            return null;
        }

        String suggestedname;
        suggestedname = String.format(template, num++);
        boolean done = false;
        while (!done) {
            done = true;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final String playlistname = cursor.getString(0);
                if (playlistname.compareToIgnoreCase(suggestedname) == 0) {
                    suggestedname = String.format(template, num++);
                    done = false;
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        cursor = null;
        return suggestedname;
    }
}
