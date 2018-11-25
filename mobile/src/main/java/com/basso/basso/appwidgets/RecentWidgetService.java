package com.basso.basso.appwidgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.cache.ImageCache;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.provider.RecentStore;
import com.basso.basso.provider.RecentStore.RecentStoreColumns;

@TargetApi(11)
public class RecentWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new WidgetRemoteViewsFactory(getApplicationContext());
    }

    private static final class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private static final int VIEW_TYPE_COUNT = 2;

        private final Context mContext;

        private final ImageFetcher mFetcher;

        private final RecentStore mRecentsStore;

        private Cursor mCursor;

        private RemoteViews mViews;

        public WidgetRemoteViewsFactory(final Context context) {
            mContext = context;
            mFetcher = ImageFetcher.getInstance(context);
            mFetcher.setImageCache(ImageCache.getInstance(context));
            mRecentsStore = RecentStore.getInstance(context);
        }

        @Override
        public int getCount() {
            if (mCursor == null || mCursor.isClosed() || mCursor.getCount() <= 0) {
                return 0;
            }
            return mCursor.getCount();
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public RemoteViews getViewAt(final int position) {
            mCursor.moveToPosition(position);
            mViews = new RemoteViews(mContext.getPackageName(), R.layout.app_widget_recents_items);
            final long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ID));
            final String albumName = mCursor.getString(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ALBUMNAME));
            final String artist = mCursor.getString(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ARTISTNAME));
            mViews.setTextViewText(R.id.app_widget_recents_line_one, albumName);
            mViews.setTextViewText(R.id.app_widget_recents_line_two, artist);
            Bitmap bitmap = mFetcher.getCachedArtwork(albumName, artist, id);
            if (bitmap != null) {
                mViews.setImageViewBitmap(R.id.app_widget_recents_base_image, bitmap);
            } else {
                mViews.setImageViewResource(R.id.app_widget_recents_base_image, R.drawable.default_artwork);
            }
            final Intent profileIntent = new Intent();
            final Bundle profileExtras = new Bundle();
            profileExtras.putLong(Config.ID, id);
            profileExtras.putString(Config.NAME, albumName);
            profileExtras.putString(Config.ARTIST_NAME, artist);
            profileExtras.putString(RecentWidgetProvider.SET_ACTION, RecentWidgetProvider.OPEN_PROFILE);
            profileIntent.putExtras(profileExtras);
            mViews.setOnClickFillInIntent(R.id.app_widget_recents_items, profileIntent);
            final Intent playAlbum = new Intent();
            final Bundle playAlbumExtras = new Bundle();
            playAlbumExtras.putLong(Config.ID, id);
            playAlbumExtras.putString(RecentWidgetProvider.SET_ACTION, RecentWidgetProvider.PLAY_ALBUM);
            playAlbum.putExtras(playAlbumExtras);
            mViews.setOnClickFillInIntent(R.id.app_widget_recents_base_image, playAlbum);
            return mViews;
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
                mCursor = null;
            }
            mCursor = mRecentsStore.getReadableDatabase().query(
                    RecentStoreColumns.NAME,
                    new String[] {
                            RecentStoreColumns.ID + " as id", RecentStoreColumns.ID,
                            RecentStoreColumns.ALBUMNAME, RecentStoreColumns.ARTISTNAME,
                            RecentStoreColumns.ALBUMSONGCOUNT, RecentStoreColumns.ALBUMYEAR,
                            RecentStoreColumns.TIMEPLAYED
                    }, null, null, null, null, RecentStoreColumns.TIMEPLAYED + " DESC");
        }

        @Override
        public void onDestroy() {
            closeCursor();
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public void onCreate() {
        }

        private void closeCursor() {
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
                mCursor = null;
            }
        }
    }
}
