package com.basso.basso.utils;

import android.provider.MediaStore;

public final class SortOrder {

    public SortOrder() {
    }

    public static interface ArtistSortOrder {
        public final static String ARTIST_A_Z = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;
        public final static String ARTIST_Z_A = ARTIST_A_Z + " DESC";
        public final static String ARTIST_NUMBER_OF_SONGS = MediaStore.Audio.Artists.NUMBER_OF_TRACKS + " DESC";
        public final static String ARTIST_NUMBER_OF_ALBUMS = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS + " DESC";
    }

    public static interface AlbumSortOrder {
        public final static String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        public final static String ALBUM_Z_A = ALBUM_A_Z + " DESC";
        public final static String ALBUM_NUMBER_OF_SONGS = MediaStore.Audio.Albums.NUMBER_OF_SONGS + " DESC";
        public final static String ALBUM_ARTIST = MediaStore.Audio.Albums.ARTIST;
        public final static String ALBUM_YEAR = MediaStore.Audio.Albums.FIRST_YEAR + " DESC";
    }

    public static interface SongSortOrder {
        public final static String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        public final static String SONG_Z_A = SONG_A_Z + " DESC";
        public final static String SONG_ARTIST = MediaStore.Audio.Media.ARTIST;
        public final static String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;
        public final static String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";
        public final static String SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC";
        public final static String SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC";
        public final static String SONG_FILENAME = MediaStore.Audio.Media.DATA;
    }

    public static interface AlbumSongSortOrder {
        public final static String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        public final static String SONG_Z_A = SONG_A_Z + " DESC";
        public final static String SONG_TRACK_LIST = MediaStore.Audio.Media.TRACK + ", " + MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        public final static String SONG_DURATION = SongSortOrder.SONG_DURATION;
        public final static String SONG_FILENAME = SongSortOrder.SONG_FILENAME;
    }

    public static interface ArtistSongSortOrder {
        public final static String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        public final static String SONG_Z_A = SONG_A_Z + " DESC";
        public final static String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;
        public final static String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";
        public final static String SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC";
        public final static String SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC";
        public final static String SONG_FILENAME = SongSortOrder.SONG_FILENAME;
    }

    public static interface ArtistAlbumSortOrder {
        public final static String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        public final static String ALBUM_Z_A = ALBUM_A_Z + " DESC";
        public final static String ALBUM_NUMBER_OF_SONGS = MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS + " DESC";
        public final static String ALBUM_YEAR = MediaStore.Audio.Artists.Albums.FIRST_YEAR + " DESC";
    }
}
