package com.basso.basso.menu;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;

import com.basso.basso.R;
import com.basso.basso.format.Capitalize;
import com.basso.basso.utils.MusicUtils;

public class RenamePlaylist extends BasePlaylistDialog {

    private String mOriginalName;

    private long mRenameId;

    public static RenamePlaylist getInstance(final Long id) {
        final RenamePlaylist frag = new RenamePlaylist();
        final Bundle args = new Bundle();
        args.putLong("rename", id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onSaveInstanceState(final Bundle outcicle) {
        outcicle.putString("defaultname", mPlaylist.getText().toString());
        outcicle.putLong("rename", mRenameId);
    }

    @Override
    public void initObjects(final Bundle savedInstanceState) {
        mRenameId = savedInstanceState != null ? savedInstanceState.getLong("rename")
                : getArguments().getLong("rename", -1);
        mOriginalName = getPlaylistNameFromId(mRenameId);
        mDefaultname = savedInstanceState != null ? savedInstanceState.getString("defaultname")
                : mOriginalName;
        if (mRenameId < 0 || mOriginalName == null || mDefaultname == null) {
            getDialog().dismiss();
            return;
        }
        final String promptformat = getString(R.string.create_playlist_prompt);
        mPrompt = String.format(promptformat, mOriginalName, mDefaultname);
    }

    @Override
    public void onSaveClick() {
        final String playlistName = mPlaylist.getText().toString();
        if (playlistName != null && playlistName.length() > 0) {
            final ContentResolver resolver = getActivity().getContentResolver();
            final ContentValues values = new ContentValues(1);
            values.put(Audio.Playlists.NAME, Capitalize.capitalize(playlistName));
            resolver.update(Audio.Playlists.EXTERNAL_CONTENT_URI, values,
                    MediaStore.Audio.Playlists._ID + "=?", new String[] {
                        String.valueOf(mRenameId)
                    });
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

    private String getPlaylistNameFromId(final long id) {
        Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, new String[] {
                    MediaStore.Audio.Playlists.NAME
                }, MediaStore.Audio.Playlists._ID + "=?", new String[] {
                    String.valueOf(id)
                }, MediaStore.Audio.Playlists.NAME);
        String playlistName = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                playlistName = cursor.getString(0);
            }
        }
        cursor.close();
        cursor = null;
        return playlistName;
    }

}
