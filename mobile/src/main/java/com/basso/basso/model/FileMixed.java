package com.basso.basso.model;

import android.text.TextUtils;

public class FileMixed {

    private String DATA;
    private String AUDIO_ID;
    private String directoryName;
    private String TRACK;
    private String albumName;
    private String artistName;
    private String songName;
    private boolean isDirectory;
    public long id;
    private String directoryPath;

    public FileMixed(long id, String DATA, String AUDIO_ID, String directoryName,String track, String albumName, String artistName, String songName,  String directoryPath, boolean isDirectory){
        this.id = id;
        if(!isDirectory){
            this.DATA = DATA;
            this.AUDIO_ID = AUDIO_ID;
            this.TRACK = track;
            this.albumName = albumName;
            this.artistName = artistName;
            this.songName = songName;
            this.isDirectory = false;
        } else {
            this.directoryName = directoryName;
            this.directoryPath = directoryPath;
            this.isDirectory = true;
        }
    }

    public String getAlbumName(){return albumName;}

    public String getArtistName() {return artistName;}

    public String getSongName() {return songName;}

    public String getTRACK(){ return TRACK;}

    public String getDATA() {
        return DATA;
    }

    public String getAUDIO_ID(){
        return AUDIO_ID;
    }

    public String getDirectoryName(){
        return directoryName;
    }

    public String getDirectoryPath(){
        return directoryPath;
    }

    public boolean isDirectory(){
        return isDirectory;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (DATA == null ? 0 : DATA.hashCode());
        result = prime * result + (AUDIO_ID == null ? 0 : AUDIO_ID.hashCode());
        result = prime * result + (directoryName == null ? 0 : directoryName.hashCode());
        result = prime * result + (directoryPath == null ? 0 : directoryPath.hashCode());
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
        final FileMixed other = (FileMixed)obj;
        if (TextUtils.equals(DATA, other.DATA)) {
            return false;
        }
        if (!TextUtils.equals(AUDIO_ID, other.AUDIO_ID)) {
            return false;
        }
        if (!TextUtils.equals(directoryName, other.directoryName)) {
            return false;
        }
        if (!TextUtils.equals(directoryPath, other.directoryPath)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return DATA;
    }
}
