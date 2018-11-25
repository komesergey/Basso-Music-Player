package com.basso.basso.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.cache.ImageCache;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.lastfm.Album;
import com.basso.basso.lastfm.Artist;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.ui.activities.ShortcutActivity;

import java.lang.ref.WeakReference;

public final class BassoUtils {

    private static final int BRIGHTNESS_THRESHOLD = 130;

    private static final String EXCLUDED = "User-contributed text is available under the Creative Commons By-SA License and may also be available under the GNU FDL.";

    private static boolean D = false;

    public BassoUtils() {
    }

    public static final boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static final boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static final boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static final boolean hasLollipop(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static final boolean isTablet(final Context context) {
        final int layout = context.getResources().getConfiguration().screenLayout;
        return (layout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static final boolean isSW600P(final Activity activity){
        int smallestWidth = activity.getResources().getConfiguration().smallestScreenWidthDp;
        if (smallestWidth >= 600) {
            return true;
        } else {
            return false;
        }
    }

    public static final String pathFromSongId(Context context, long id){
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"*"}, MediaStore.Audio.AudioColumns.IS_MUSIC + "= 1 AND " + BaseColumns._ID + " = " + id, null, null);
        if(cursor.getCount() == 0)
            return null;
        if(cursor.moveToFirst()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            cursor.close();
            return path;
        } else {
            cursor.close();
            return null;
        }
    }

    public static final boolean isLandscape(final Context context) {
        final int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @SuppressLint("NewApi")
    public static <T> void execute(final boolean forceSerial, final AsyncTask<T, ?, ?> task,
            final T... args) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) {
            throw new UnsupportedOperationException(
                    "This class can only be used on API 4 and newer.");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || forceSerial) {
            task.execute(args);
        } else {
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, args);
        }
    }

    public static final boolean isOnline(final Context context) {
        if (context == null) {
            return false;
        }
        RecycleHolder.m(null, null);
        boolean state = false;
        final boolean onlyOnWifi = PreferenceUtils.getInstance(context).onlyOnWifi();
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null) {
            state = wifiNetwork.isConnectedOrConnecting();
        }
        final NetworkInfo mbobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mbobileNetwork != null) {
            if (!onlyOnWifi) {
                state = mbobileNetwork.isConnectedOrConnecting();
            }
        }
        final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (!onlyOnWifi) {
                state = activeNetwork.isConnectedOrConnecting();
            }
        }
        return state;
    }

    public static void showCheatSheet(final View view) {
        final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        view.getLocationOnScreen(screenPos);
        view.getWindowVisibleDisplayFrame(displayFrame);
        final Context context = view.getContext();
        final int viewWidth = view.getWidth();
        final int viewHeight = view.getHeight();
        final int viewCenterX = screenPos[0] + viewWidth / 2;
        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        final int estimatedToastHeight = (int)(48 * context.getResources().getDisplayMetrics().density);
        final Toast cheatSheet = Toast.makeText(context, view.getContentDescription(), Toast.LENGTH_SHORT);
        final boolean showBelow = screenPos[1] < estimatedToastHeight;
        if (showBelow) {
            cheatSheet.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, viewCenterX
                    - screenWidth / 2, screenPos[1] - displayFrame.top + viewHeight);
        } else {
            cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, viewCenterX
                    - screenWidth / 2, displayFrame.bottom - screenPos[1]);
        }
        cheatSheet.show();
    }

    public static final boolean isColorDark(final int color) {
        return (30 * Color.red(color) + 59 * Color.green(color) + 11 * Color.blue(color)) / 100 <= BRIGHTNESS_THRESHOLD;
    }

    @SuppressLint("NewApi")
    public static void doAfterLayout(final View view, final Runnable runnable) {
        final OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (hasJellyBean()) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                runnable.run();
            }
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    public static final ImageFetcher getImageFetcher(final Activity activity) {
        final ImageFetcher imageFetcher = ImageFetcher.getInstance(activity);
        imageFetcher.setImageCache(ImageCache.findOrCreateCache(activity));
        return imageFetcher;
    }

    public static void createShortcutIntent(final String displayName, final String artistName,
            final Long id, final String mimeType, final Activity context) {
        try {
            final ImageFetcher fetcher = getImageFetcher(context);
            Bitmap bitmap = null;
            if (mimeType.equals(MediaStore.Audio.Albums.CONTENT_TYPE)) {
                bitmap = fetcher.getCachedBitmap(ImageFetcher.generateAlbumCacheKey(displayName, artistName));
            } else {
                bitmap = fetcher.getCachedBitmap(displayName);
            }
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_artwork);
            }
            final Intent shortcutIntent = new Intent(context, ShortcutActivity.class);
            shortcutIntent.setAction(Intent.ACTION_VIEW);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            shortcutIntent.putExtra(Config.ID, id);
            shortcutIntent.putExtra(Config.NAME, displayName);
            shortcutIntent.putExtra(Config.MIME_TYPE, mimeType);
            final Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapUtils.resizeAndCropCenter(bitmap, 96));
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, displayName);
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            context.sendBroadcast(intent);
            Toast.makeText(context, context.getString(R.string.pinned_to_home_screen, displayName), Toast.LENGTH_SHORT).show();
        } catch (final Exception e) {
            if(D) Log.e("BassoUtils", "createShortcutIntent", e);
            Toast.makeText(context, context.getString(R.string.could_not_be_pinned_to_home_screen, displayName), Toast.LENGTH_SHORT).show();
        }
    }

    public static void removeHardwareAccelerationSupport(View v) {
        if (v.getLayerType() != View.LAYER_TYPE_SOFTWARE) {
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
   }

    public static void setAlbumInfo(final Context context, final String artist, final String album, TextView textView){
        final WeakReference<TextView> textViewWeakReference = new WeakReference<TextView>(textView);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void...params){
                String result = null;
                Album info = Album.getInfo(context, artist, album);
                if(info!=null){
                    result = info.getWikiSummary();
                }
                return result;
            }
            @Override
            protected void onPostExecute(String result){
                if(result != null){
                    textViewWeakReference.get().setText(Html.fromHtml(result));
                } else
                    textViewWeakReference.get().setText(context.getString(R.string.no_information));
            }
        }.execute();
    }

    public static void setArtistInfo(final Context context, final String artist, TextView textView){
        final WeakReference<TextView> textViewWeakReference = new WeakReference<TextView>(textView);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String correctedArtist = null;
                Artist corrected = Artist.getCorrection(context,artist);
                if(corrected != null){
                    correctedArtist = corrected.getName();
                }
                Artist result;
                if(correctedArtist != null) {
                    result = Artist.getInfo(context, correctedArtist);
                }else {
                    result = Artist.getInfo(context, artist);
                }

                if(result != null){
                    return result.getWikiSummary();
                } else {
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result) {
                if(result != null && !TextUtils.isEmpty(result)) {
                    result = result.replace(EXCLUDED, "");
                    int lastFmLink = result.lastIndexOf("<a ");
                    if(lastFmLink != -1){
                        StringBuilder resultString = new StringBuilder(result).insert(lastFmLink, "<br>");
                        result = resultString.toString();
                    }
                    textViewWeakReference.get().setText(Html.fromHtml(result));
                }else
                    textViewWeakReference.get().setText(context.getString(R.string.no_information));
            }
        }.execute();
    }

    public static Bitmap drawTextToBitmap(Context mContext,  Bitmap bitmap,  String mText) {
        try {
            Resources resources = mContext.getResources();
            float scale = resources.getDisplayMetrics().densityDpi/160f;
            android.graphics.Bitmap.Config bitmapConfig =   bitmap.getConfig();
            if(bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            bitmap = bitmap.copy(bitmapConfig, true);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(resources.getColor(R.color.action_bar_background));
            paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            int size =(int) ((bitmap.getWidth() - 20) / (scale * mText.length()));
            paint.setTextSize((int) (size * scale));
            Rect bounds = new Rect();
            paint.getTextBounds(mText, 0, mText.length(), bounds);
            int x = bitmap.getWidth()/2 - bounds.width()/2;
            int y = bitmap.getHeight()/2 + bounds.height()/2;
            canvas.drawText(mText, x, y, paint);
            return bitmap;
        } catch (Exception e) {
            return null;
        }

    }
}
