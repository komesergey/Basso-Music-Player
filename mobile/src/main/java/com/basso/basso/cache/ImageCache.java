package com.basso.basso.cache;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentCallbacks2;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.basso.basso.utils.BassoUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ImageCache {

    private static final String TAG = ImageCache.class.getSimpleName();

    private static final Uri mArtworkUri;

    private static final float MEM_CACHE_DIVIDER = 0.25f;

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 60;

    private static final CompressFormat COMPRESS_FORMAT = CompressFormat.JPEG;

    private static final int DISK_CACHE_INDEX = 0;

    private static final int COMPRESS_QUALITY = 98;

    private MemoryCache mLruCache;

    private DiskLruCache mDiskCache;

    private static ImageCache sInstance;

    public boolean mPauseDiskAccess = false;

    private Object mPauseLock = new Object();

    static {
        mArtworkUri = Uri.parse("content://media/external/audio/albumart");
    }

    public ImageCache(final Context context) {
        init(context);
    }

    public final static ImageCache getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new ImageCache(context.getApplicationContext());
        }
        return sInstance;
    }

    private void init(final Context context) {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                initDiskCache(context);
                return null;
            }
        }, (Void[]) null);
        initLruCache(context);
    }

    private synchronized void initDiskCache(final Context context) {
        if (mDiskCache == null || mDiskCache.isClosed()) {
            File diskCacheDir = getDiskCacheDir(context, TAG);
            if (diskCacheDir != null) {
                if (!diskCacheDir.exists()) {
                    diskCacheDir.mkdirs();
                }
                if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
                    try {
                        mDiskCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                    } catch (final IOException e) {
                        diskCacheDir = null;
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    public void initLruCache(final Context context) {
        final ActivityManager activityManager = (ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE);
        final int lruCacheSize = Math.round(MEM_CACHE_DIVIDER * activityManager.getMemoryClass()
                * 1024 * 1024);
        mLruCache = new MemoryCache(lruCacheSize);

        context.registerComponentCallbacks(new ComponentCallbacks2() {

            @Override
            public void onTrimMemory(final int level) {
                if (level >= TRIM_MEMORY_MODERATE) {
                    evictAll();
                } else if (level >= TRIM_MEMORY_BACKGROUND) {
                    mLruCache.trimToSize(mLruCache.size() / 2);
                }
            }

            @Override
            public void onLowMemory() {
            }

            @Override
            public void onConfigurationChanged(final Configuration newConfig) {
            }
        });
    }

    public static final ImageCache findOrCreateCache(final Activity activity) {

        final RetainFragment retainFragment = findOrCreateRetainFragment(activity.getFragmentManager());

        ImageCache cache = (ImageCache)retainFragment.getObject();

        if (cache == null) {
            cache = getInstance(activity);
            retainFragment.setObject(cache);
        }
        return cache;
    }

    public static final RetainFragment findOrCreateRetainFragment(final FragmentManager fm) {

        RetainFragment retainFragment = (RetainFragment)fm.findFragmentByTag(TAG);

        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, TAG).commit();
        }
        return retainFragment;
    }

    public void addBitmapToCache(final String data, final Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        addBitmapToMemCache(data, bitmap);

        if (mDiskCache != null) {
            final String key = hashKeyForDisk(data);
            OutputStream out = null;
            try {
                final DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
                if (snapshot == null) {
                    final DiskLruCache.Editor editor = mDiskCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(DISK_CACHE_INDEX);
                        bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, out);
                        editor.commit();
                        out.close();
                        flush();
                    }
                } else {
                    snapshot.getInputStream(DISK_CACHE_INDEX).close();
                }
            } catch (final IOException e) {
                Log.e(TAG, "addBitmapToCache - " + e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (final IllegalStateException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                }
            }
        }
    }

    public void addBitmapToMemCache(final String data, final Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }
        if (getBitmapFromMemCache(data) == null) {
            mLruCache.put(data, bitmap);
        }
    }

    public final Bitmap getBitmapFromMemCache(final String data) {
        if (data == null) {
            return null;
        }
        if (mLruCache != null) {
            final Bitmap lruBitmap = mLruCache.get(data);
            if (lruBitmap != null) {
                return lruBitmap;
            }
        }
        return null;
    }

    public final Bitmap getBitmapFromDiskCache(final String data) {
        if (data == null) {
            return null;
        }

        if (getBitmapFromMemCache(data) != null) {
            return getBitmapFromMemCache(data);
        }

        waitUntilUnpaused();
        final String key = hashKeyForDisk(data);
        if (mDiskCache != null) {
            InputStream inputStream = null;
            try {
                final DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
                if (snapshot != null) {
                    inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                    if (inputStream != null) {
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                }
            } catch (final IOException e) {
                Log.e(TAG, "getBitmapFromDiskCache - " + e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (final IOException e) {
                }
            }
        }
        return null;
    }

    public final Bitmap getCachedBitmap(final String data) {
        if (data == null) {
            return null;
        }
        Bitmap cachedImage = getBitmapFromMemCache(data);
        if (cachedImage == null) {
            cachedImage = getBitmapFromDiskCache(data);
        }
        if (cachedImage != null) {
            addBitmapToMemCache(data, cachedImage);
            return cachedImage;
        }
        return null;
    }

    public final Bitmap getCachedArtwork(final Context context, final String data, final long id) {
        if (context == null || data == null) {
            return null;
        }
        Bitmap cachedImage = getCachedBitmap(data);
        if (cachedImage == null && id >= 0) {
            cachedImage = getArtworkFromFile(context, id);
        }
        if (cachedImage != null) {
            addBitmapToMemCache(data, cachedImage);
            return cachedImage;
        }
        return null;
    }

    public final Bitmap getArtworkFromFile(final Context context, final long albumId) {
        if (albumId < 0) {
            return null;
        }
        Bitmap artwork = null;
        waitUntilUnpaused();
        try {
            final Uri uri = ContentUris.withAppendedId(mArtworkUri, albumId);
            final ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                artwork = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            }
        } catch (final Exception e) { Log.e(TAG, "getArtworkFromFile", e);};
        return artwork;
    }

    public void flush() {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                if (mDiskCache != null) {
                    try {
                        if (!mDiskCache.isClosed()) {
                            mDiskCache.flush();
                        }
                    } catch (final IOException e) {
                        Log.e(TAG, "flush - " + e);
                    }
                }
                return null;
            }
        }, (Void[]) null);
    }

    public void clearCaches() {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                try {
                    if (mDiskCache != null) {
                        mDiskCache.delete();
                        mDiskCache = null;
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "clearCaches - " + e);
                }
                evictAll();
                return null;
            }
        }, (Void[]) null);
    }

    public void close() {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                if (mDiskCache != null) {
                    try {
                        if (!mDiskCache.isClosed()) {
                            mDiskCache.close();
                            mDiskCache = null;
                        }
                    } catch (final IOException e) {
                        Log.e(TAG, "close - " + e);
                    }
                }
                return null;
            }
        }, (Void[]) null);
    }

    public void evictAll() {
        if (mLruCache != null) {
            mLruCache.evictAll();
        }
        System.gc();
    }

    public void removeFromCache(final String key) {
        if (key == null) {
            return;
        }
        if (mLruCache != null) {
            mLruCache.remove(key);
        }

        try {
            if (mDiskCache != null) {
                mDiskCache.remove(hashKeyForDisk(key));
            }
        } catch (final IOException e) {
            Log.e(TAG, "remove - " + e);
        }
        flush();
    }

    public void setPauseDiskCache(final boolean pause) {
        synchronized (mPauseLock) {
            if (mPauseDiskAccess != pause) {
                mPauseDiskAccess = pause;
                if (!pause) {
                    mPauseLock.notify();
                }
            }
        }
    }

    private void waitUntilUnpaused() {
        synchronized (mPauseLock) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                while (mPauseDiskAccess) {
                    try {
                        mPauseLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public boolean isDiskCachePaused() {
        return mPauseDiskAccess;
    }

    public static final File getDiskCacheDir(final Context context, final String uniqueName) {
        final String cachePath = getExternalCacheDir(context) != null
                                    ? getExternalCacheDir(context).getPath()
                                    : context.getCacheDir().getPath();
        return new File(cachePath, uniqueName);
    }

    public static final boolean isExternalStorageRemovable() {
        return Environment.isExternalStorageRemovable();
    }

    public static final File getExternalCacheDir(final Context context) {
        return context.getExternalCacheDir();
    }

    public static final long getUsableSpace(final File path) {
        return path.getUsableSpace();
    }

    public static final String hashKeyForDisk(final String key) {
        String cacheKey;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (final NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static final String bytesToHexString(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    public static final class RetainFragment extends Fragment {

        private Object mObject;

        public RetainFragment() {}

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public void setObject(final Object object) {
            mObject = object;
        }

        public Object getObject() {
            return mObject;
        }
    }

    public static final class MemoryCache extends LruCache<String, Bitmap> {

        public MemoryCache(final int maxSize) {
            super(maxSize);
        }

        public static final int getBitmapSize(final Bitmap bitmap) {
            return bitmap.getByteCount();
        }

        @Override
        protected int sizeOf(final String paramString, final Bitmap paramBitmap) {
            return getBitmapSize(paramBitmap);
        }
    }
}
