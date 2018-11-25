package com.basso.basso.songs;

public final class LyricsProviderFactory {

    public LyricsProviderFactory() {}

    public static final LyricsProvider getOfflineProvider(String filePath) {
        return new OfflineLyricsProvider(filePath);
    }
    public static final LyricsProvider getMainOnlineProvider() {
        return new LyricsWikiProvider();
    }
}