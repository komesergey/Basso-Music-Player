package com.basso.basso.model;

import android.text.TextUtils;

public class Playlist {

    public long mPlaylistId;

    public String mPlaylistName;

    public Playlist(final long playlistId, final String playlistName) {
        super();
        mPlaylistId = playlistId;
        mPlaylistName = playlistName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) mPlaylistId;
        result = prime * result + (mPlaylistName == null ? 0 : mPlaylistName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Playlist other = (Playlist)obj;
        if (mPlaylistId != other.mPlaylistId) {
            return false;
        }
        return TextUtils.equals(mPlaylistName, other.mPlaylistName);
    }

    @Override
    public String toString() {
        return mPlaylistName;
    }
}
