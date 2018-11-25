package com.basso.basso.model;

import android.text.TextUtils;

public class Genre {

    public long mGenreId;

    public String mGenreName;

    public Genre(final long genreId, final String genreName) {
        super();
        mGenreId = genreId;
        mGenreName = genreName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) mGenreId;
        result = prime * result + (mGenreName == null ? 0 : mGenreName.hashCode());
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
        final Genre other = (Genre)obj;
        if (mGenreId != other.mGenreId) {
            return false;
        }
        return TextUtils.equals(mGenreName, other.mGenreName);
    }

    @Override
    public String toString() {
        return mGenreName;
    }
}
