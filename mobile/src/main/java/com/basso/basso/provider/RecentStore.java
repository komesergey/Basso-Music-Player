package com.basso.basso.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.basso.basso.ui.activities.ProfileActivity;

public class RecentStore extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    public static final String DATABASENAME = "albumhistory.db";

    private static RecentStore sInstance = null;

    public RecentStore(final Context context) {
        super(context, DATABASENAME, null, VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStoreColumns.NAME + " ("
                + RecentStoreColumns.ID + " LONG NOT NULL," + RecentStoreColumns.ALBUMNAME
                + " TEXT NOT NULL," + RecentStoreColumns.ARTISTNAME + " TEXT NOT NULL,"
                + RecentStoreColumns.ALBUMSONGCOUNT + " TEXT NOT NULL,"
                + RecentStoreColumns.ALBUMYEAR + " TEXT," + RecentStoreColumns.TIMEPLAYED
                + " LONG NOT NULL);");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME);
        onCreate(db);
    }

    public static final synchronized RecentStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new RecentStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public void addAlbumId(final Long albumId, final String albumName, final String artistName,
            final String songCount, final String albumYear) {
        if (albumId == null || albumName == null || artistName == null || songCount == null) {
            return;
        }

        final SQLiteDatabase database = getWritableDatabase();
        final ContentValues values = new ContentValues(6);

        database.beginTransaction();

        values.put(RecentStoreColumns.ID, albumId);
        values.put(RecentStoreColumns.ALBUMNAME, albumName);
        values.put(RecentStoreColumns.ARTISTNAME, artistName);
        values.put(RecentStoreColumns.ALBUMSONGCOUNT, songCount);
        values.put(RecentStoreColumns.ALBUMYEAR, albumYear);
        values.put(RecentStoreColumns.TIMEPLAYED, System.currentTimeMillis());

        database.delete(RecentStoreColumns.NAME, RecentStoreColumns.ID + " = ?", new String[] {
            String.valueOf(albumId)
        });
        database.insert(RecentStoreColumns.NAME, null, values);
        database.setTransactionSuccessful();
        database.endTransaction();

    }

    public String getAlbumName(final String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        final SQLiteDatabase database = getReadableDatabase();
        final String[] projection = new String[] {
                RecentStoreColumns.ID, RecentStoreColumns.ALBUMNAME, RecentStoreColumns.ARTISTNAME,
                RecentStoreColumns.TIMEPLAYED
        };
        final String selection = RecentStoreColumns.ARTISTNAME + "=?";
        final String[] having = new String[] {key};
        Cursor cursor = database.query(RecentStoreColumns.NAME, projection, selection, having,
                null, null, RecentStoreColumns.TIMEPLAYED + " DESC", null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            final String album = cursor.getString(cursor
                    .getColumnIndexOrThrow(RecentStoreColumns.ALBUMNAME));
            cursor.close();
            cursor = null;
            return album;
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }

        return null;
    }

    public void deleteDatabase() {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(RecentStoreColumns.NAME, null, null);
    }

    public void removeItem(final long albumId) {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(RecentStoreColumns.NAME, RecentStoreColumns.ID + " = ?", new String[] {
            String.valueOf(albumId)
        });
    }

    public interface RecentStoreColumns {

        public static final String NAME = "albumhistory";

        public static final String ID = "albumid";

        public static final String ALBUMNAME = "itemname";

        public static final String ARTISTNAME = "artistname";

        public static final String ALBUMSONGCOUNT = "albumsongcount";

        public static final String ALBUMYEAR = "albumyear";

        public static final String TIMEPLAYED = "timeplayed";
    }
}
