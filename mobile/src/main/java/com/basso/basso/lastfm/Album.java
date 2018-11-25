package com.basso.basso.lastfm;

import android.content.Context;

import com.basso.basso.Config;

import java.util.HashMap;
import java.util.Map;

public class Album extends MusicEntry {

    protected final static ItemFactory<Album> FACTORY = new AlbumFactory();

    private String artist;

    private Album(final String name, final String url, final String artist) {
        super(name, url);
        this.artist = artist;
    }

    public final static Album getInfo(final Context context, final String artist,
            final String albumOrMbid) {
        return getInfo(context, artist, albumOrMbid, null, Config.LASTFM_API_KEY);
    }

    public final static Album getInfo(final Context context, final String artist,
            final String albumOrMbid, final String username, final String apiKey) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("artist", artist);
        params.put("album", albumOrMbid);
        MapUtilities.nullSafePut(params, "username", username);
        final Result result = Caller.getInstance(context).call("album.getInfo", apiKey, params);
        return ResponseBuilder.buildItem(result, Album.class);
    }

    private final static class AlbumFactory implements ItemFactory<Album> {

        @Override
        public Album createItemFromElement(final DomElement element) {
            if (element == null) {
                return null;
            }
            final Album album = new Album(null, null, null);
            MusicEntry.loadStandardInfo(album, element);
            if (element.hasChild("artist")) {
                album.artist = element.getChild("artist").getChildText("name");
                if (album.artist == null) {
                    album.artist = element.getChildText("artist");
                }
            }
            return album;
        }
    }
}