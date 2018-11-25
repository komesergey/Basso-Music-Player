package com.basso.basso.songs;

public interface LyricsProvider {
    public String getLyrics(String artist, String song);
    public String getProviderName();
}