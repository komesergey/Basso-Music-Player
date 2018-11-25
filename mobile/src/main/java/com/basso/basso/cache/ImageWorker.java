package com.basso.basso.cache;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.basso.basso.R;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.ThemeUtils;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.RejectedExecutionException;


public abstract class ImageWorker {

    private static final int FADE_IN_TIME = 200;

    private final BitmapDrawable mDefaultArtwork;

    private final BitmapDrawable mDefaultArtworkBig;

    private final Resources mResources;

    private final ColorDrawable mCurrentDrawable;

    private final Drawable[] mArrayDrawable;

    private final Bitmap mDefault;

    private final Bitmap mDefaultBig;

    protected Context mContext;

    protected ImageCache mImageCache;

    protected ImageWorker(final Context context) {
        mContext = context.getApplicationContext();
        mResources = mContext.getResources();
        final ThemeUtils theme = new ThemeUtils(context);
        mDefault = ((BitmapDrawable) theme.getDrawable("default_artwork")).getBitmap();
        mDefaultBig = BassoUtils.drawTextToBitmap(mContext, ((BitmapDrawable) theme.getDrawable("ic_empty_cover_big")).getBitmap(), "NO COVER");
        mDefaultArtwork = new BitmapDrawable(mResources, mDefault);
        mDefaultArtwork.setFilterBitmap(false);
        mDefaultArtwork.setDither(false);
        mDefaultArtworkBig = new BitmapDrawable(mResources, mDefaultBig);
        mDefaultArtworkBig.setFilterBitmap(false);
        mDefaultArtworkBig.setDither(false);
        mCurrentDrawable = new ColorDrawable(mResources.getColor(R.color.transparent));
        mArrayDrawable = new Drawable[2];
        mArrayDrawable[0] = mCurrentDrawable;
    }

    public Bitmap getDefaultBig(){
        return mDefaultBig;
    }

    public void setImageCache(final ImageCache cacheCallback) {
        mImageCache = cacheCallback;
    }

    public void close() {
        if (mImageCache != null) {
            mImageCache.close();
        }
    }

    public void flush() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    public void addBitmapToCache(final String key, final Bitmap bitmap) {
        if (mImageCache != null) {
            mImageCache.addBitmapToCache(key, bitmap);
        }
    }

    public Bitmap getDefaultArtwork() {
        return mDefault;
    }

    private final class BitmapWorkerTask extends AsyncTask<String, Void, TransitionDrawable> {

        private final WeakReference<ImageView> mImageReference;

        private final ImageType mImageType;

        private final ImageSource mImageSource;

        private String mKey;

        private String mArtistName;

        private String mAlbumName;

        private long mAlbumId;

        private String mUrl;

        @SuppressWarnings("deprecation")
        public BitmapWorkerTask(final ImageView imageView, final ImageType imageType, final ImageSource imageSource) {
            mImageReference = new WeakReference<ImageView>(imageView);
            mImageType = imageType;
            mImageSource = imageSource;
        }

        @Override
        protected TransitionDrawable doInBackground(final String... params) {
            mKey = params[0];

            Bitmap bitmap = null;

            if (mKey != null && mImageCache != null && !isCancelled()
                    && getAttachedImageView() != null) {
                bitmap = mImageCache.getCachedBitmap(mKey);
            }

            mAlbumId = Long.valueOf(params[3]);

            if (bitmap == null && mImageType.equals(ImageType.ALBUM) && mAlbumId >= 0
                    && mKey != null && !isCancelled() && getAttachedImageView() != null
                    && mImageCache != null) {
                bitmap = mImageCache.getCachedArtwork(mContext, mKey, mAlbumId);
            }

            if (bitmap == null && BassoUtils.isOnline(mContext) && !isCancelled()
                    && getAttachedImageView() != null) {
                mArtistName = params[1];
                mAlbumName = params[2];
                mUrl = processImageUrl(mArtistName, mAlbumName, mImageType);
                if (mUrl != null) {
                    bitmap = processBitmap(mUrl);
                }
            }

            if (bitmap != null && mKey != null && mImageCache != null) {
                addBitmapToCache(mKey, bitmap);
            }

            if (bitmap != null) {
                final BitmapDrawable layerTwo = new BitmapDrawable(mResources, bitmap);
                layerTwo.setFilterBitmap(false);
                layerTwo.setDither(false);
                mArrayDrawable[1] = layerTwo;
                final TransitionDrawable result = new TransitionDrawable(mArrayDrawable);
                result.setCrossFadeEnabled(true);
                result.startTransition(FADE_IN_TIME);
                return result;
            }

            if(bitmap == null && mImageSource.equals(ImageSource.MAIN_ARTWORK)){
                final BitmapDrawable layerTwo = new BitmapDrawable(mResources, mDefaultBig);
                layerTwo.setFilterBitmap(false);
                layerTwo.setDither(false);
                mArrayDrawable[1] = layerTwo;
                final TransitionDrawable result = new TransitionDrawable(mArrayDrawable);
                result.setCrossFadeEnabled(true);
                result.startTransition(FADE_IN_TIME);
                return result;
            }

            return null;
        }

        @Override
        protected void onPostExecute(TransitionDrawable result) {
            if (isCancelled()) {
                result = null;
            }
            final ImageView imageView = getAttachedImageView();
            if (imageView != null) {
                if(result != null)
                    imageView.setImageDrawable(result);
                else {
                    if(mImageSource.equals(ImageSource.BOTTOM_ACTION_BAR))
                        imageView.setImageDrawable(mDefaultArtwork);
                }
            }
        }

        private final ImageView getAttachedImageView() {
            final ImageView imageView = mImageReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask) {
                return imageView;
            }
            return null;
        }
    }

    public static final void cancelWork(final ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
        }
    }

    public static final boolean executePotentialWork(final Object data, final ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mKey;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static final BitmapWorkerTask getBitmapWorkerTask(final ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private static final class AsyncDrawable extends BitmapDrawable {

        private final WeakReference<BitmapWorkerTask> mBitmapWorkerTaskReference;

        public AsyncDrawable(final Resources res, final Bitmap bitmap, final BitmapWorkerTask mBitmapWorkerTask) {
            super(bitmap);
            mBitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(mBitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return mBitmapWorkerTaskReference.get();
        }
    }

    protected void loadImage(final String key, final String artistName, final String albumName,
            final long albumId, final ImageView imageView, final ImageType imageType, final ImageSource imageSource) {
        if (key == null || mImageCache == null || imageView == null) {
            return;
        }
        final Bitmap lruBitmap = mImageCache.getBitmapFromMemCache(key);
        if (lruBitmap != null && imageView != null) {
            imageView.setImageBitmap(lruBitmap);
        } else if (executePotentialWork(key, imageView)
                && imageView != null && !mImageCache.isDiskCachePaused()) {
            final BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(imageView, imageType, imageSource);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mDefaultBig, bitmapWorkerTask);
            imageView.setImageDrawable(asyncDrawable);
            try {
                BassoUtils.execute(false, bitmapWorkerTask, key, artistName, albumName, String.valueOf(albumId));
            } catch (RejectedExecutionException e) {
                imageView.setImageBitmap(getDefaultArtwork());
            }
        }
    }

    public Bitmap getArtwork(String mKey, long mAlbumId, String mArtistName, String mAlbumName, ImageType mImageType, int w, int h, boolean resize){

        Bitmap bitmap = null;
        BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
        BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptionsCache.inDither = false;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptions.inDither = false;

        if (mKey != null && mImageCache != null) {
            bitmap = mImageCache.getCachedBitmap(mKey);

        }
        if (bitmap == null  && mAlbumId >= 0 && mKey != null && mImageCache != null) {
            bitmap = mImageCache.getCachedArtwork(mContext, mKey, mAlbumId);
        }

        if (bitmap == null && BassoUtils.isOnline(mContext)) {
            String mUrl = processImageUrl(mArtistName, mAlbumName, mImageType);
            if (mUrl != null) {
                bitmap = processBitmap(mUrl);
            }
        }

        if (bitmap != null && mKey != null && mImageCache != null) {
            addBitmapToCache(mKey, bitmap);
        }

        if(bitmap == null){
            bitmap = mDefaultBig;
        }
        if(resize) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            int sampleSize = 1;
            sBitmapOptionsCache.inJustDecodeBounds = true;
            sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.ARGB_8888;

            BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length, sBitmapOptionsCache);
            int nextWidth = sBitmapOptionsCache.outWidth >> 1;
            int nextHeight = sBitmapOptionsCache.outHeight >> 1;
            while (nextWidth > w && nextHeight > h) {
                sampleSize <<= 1;
                nextWidth >>= 1;
                nextHeight >>= 1;
            }
            sBitmapOptionsCache.inSampleSize = sampleSize;
            sBitmapOptionsCache.inJustDecodeBounds = false;
            Bitmap b = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length, sBitmapOptionsCache);
            if (b != null) {
                if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                    Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                    if (tmp != b) b.recycle();
                    b = tmp;
                    return b;
                }
            }
        } else {
            return bitmap;
        }
        return null;
    }

    protected abstract Bitmap processBitmap(String key);

    protected abstract String processImageUrl(String artistName, String albumName, ImageType imageType);

    public enum ImageType {
        ARTIST, ALBUM;
    }

    public enum ImageSource {
        MAIN_ARTWORK, BOTTOM_ACTION_BAR, OTHER;
    }

}
