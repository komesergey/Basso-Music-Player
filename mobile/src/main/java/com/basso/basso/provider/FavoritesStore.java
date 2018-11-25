package com.basso.basso.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesStore extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    public static final String DATABASENAME = "favorites.db";

    private static FavoritesStore sInstance = null;

    public FavoritesStore(final Context context) {
        super(context, DATABASENAME, null, VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FavoriteColumns.NAME + " (" + FavoriteColumns.ID
                + " LONG NOT NULL," + FavoriteColumns.SONGNAME + " TEXT NOT NULL,"
                + FavoriteColumns.ALBUMNAME + " TEXT NOT NULL," + FavoriteColumns.ARTISTNAME
                + " TEXT NOT NULL," + FavoriteColumns.PLAYCOUNT + " LONG NOT NULL);");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteColumns.NAME);
        onCreate(db);
    }

    public static final synchronized FavoritesStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new FavoritesStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public void addSongId(final Long songId, final String songName, final String albumName,
            final String artistName) {
        if (songId == null || songName == null || albumName == null || artistName == null) {
            return;
        }

        final Long playCount = getPlayCount(songId);
        final SQLiteDatabase database = getWritableDatabase();
        final ContentValues values = new ContentValues(5);

        database.beginTransaction();

        values.put(FavoriteColumns.ID, songId);
        values.put(FavoriteColumns.SONGNAME, songName);
        values.put(FavoriteColumns.ALBUMNAME, albumName);
        values.put(FavoriteColumns.ARTISTNAME, artistName);
        values.put(FavoriteColumns.PLAYCOUNT, playCount != 0 ? playCount + 1 : 1);

        database.delete(FavoriteColumns.NAME, FavoriteColumns.ID + " = ?", new String[] {
            String.valueOf(songId)
        });
        database.insert(FavoriteColumns.NAME, null, values);
        database.setTransactionSuccessful();
        database.endTransaction();

    }

    public Long getSongId(final Long songId) {
        if (songId <= -1) {
            return null;
        }

        final SQLiteDatabase database = getReadableDatabase();
        final String[] projection = new String[] {
                FavoriteColumns.ID, FavoriteColumns.SONGNAME, FavoriteColumns.ALBUMNAME,
                FavoriteColumns.ARTISTNAME, FavoriteColumns.PLAYCOUNT
        };
        final String selection = FavoriteColumns.ID + "=?";
        final String[] having = new String[] {
            String.valueOf(songId)
        };
        Cursor cursor = database.query(FavoriteColumns.NAME, projection, selection, having, null,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final Long id = cursor.getLong(cursor.getColumnIndexOrThrow(FavoriteColumns.ID));
            cursor.close();
            cursor = null;
            return id;
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return null;
    }

    public Long getPlayCount(final Long songId) {
        if (songId <= -1) {
            return null;
        }

        final SQLiteDatabase database = getReadableDatabase();
        final String[] projection = new String[] {
                FavoriteColumns.ID, FavoriteColumns.SONGNAME, FavoriteColumns.ALBUMNAME,
                FavoriteColumns.ARTISTNAME, FavoriteColumns.PLAYCOUNT
        };
        final String selection = FavoriteColumns.ID + "=?";
        final String[] having = new String[] {
            String.valueOf(songId)
        };
        Cursor cursor = database.query(FavoriteColumns.NAME, projection, selection, having, null,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final Long playCount = cursor.getLong(cursor
                    .getColumnIndexOrThrow(FavoriteColumns.PLAYCOUNT));
            cursor.close();
            cursor = null;
            return playCount;
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

        return (long)0;
    }

    public static void deleteDatabase(final Context context) {
        context.deleteDatabase(DATABASENAME);
    }

    public void toggleSong(final Long songId, final String songName, final String albumName,
            final String artistName) {
        if (getSongId(songId) == null) {
            addSongId(songId, songName, albumName, artistName);
        } else {
            removeItem(songId);
        }
    }

    public void removeItem(final Long songId) {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(FavoriteColumns.NAME, FavoriteColumns.ID + " = ?", new String[] {
            String.valueOf(songId)
        });
    }

    public interface FavoriteColumns {

        public static final String NAME = "favorites";

        public static final String ID = "songid";

        public static final String SONGNAME = "songname";

        public static final String ALBUMNAME = "albumname";

        public static final String ARTISTNAME = "artistname";

        public static final String PLAYCOUNT = "playcount";
    }
}
