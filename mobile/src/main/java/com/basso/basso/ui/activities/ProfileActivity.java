package com.basso.basso.ui.activities;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.adapters.PagerAdapter;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.ui.fragments.profile.AlbumSongFragment;
import com.basso.basso.ui.fragments.profile.ArtistAlbumFragment;
import com.basso.basso.ui.fragments.profile.ArtistSongFragment;
import com.basso.basso.ui.fragments.profile.FavoriteFragment;
import com.basso.basso.ui.fragments.profile.GenreSongFragment;
import com.basso.basso.ui.fragments.profile.InfoAlbumFragment;
import com.basso.basso.ui.fragments.profile.InfoArtistFragment;
import com.basso.basso.ui.fragments.profile.LastAddedFragment;
import com.basso.basso.ui.fragments.profile.PlaylistSongFragment;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.PreferenceUtils;
import com.basso.basso.utils.SortOrder;
import com.basso.basso.widgets.ProfileTabCarousel;
import com.basso.basso.widgets.ProfileTabCarousel.Listener;

public class ProfileActivity extends BaseActivity implements OnPageChangeListener, Listener {

    private static final int NEW_PHOTO = 1;

    private Bundle mArguments;

    private ViewPager mViewPager;

    private PagerAdapter mPagerAdapter;

    private ProfileTabCarousel mTabCarousel;

    private String mType;

    private String mArtistName;

    private ColorDrawable newColor;

    private String mProfileName;

    private ImageFetcher mImageFetcher;

    private PreferenceUtils mPreferences;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        this.getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.newColor = new ColorDrawable(getResources().getColor(R.color.action_bar_background));
        this.newColor.setAlpha(0);
        mPreferences = PreferenceUtils.getInstance(this);
        mImageFetcher = BassoUtils.getImageFetcher(this);
        mArguments = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mType = mArguments.getString(Config.MIME_TYPE);
        mProfileName = mArguments.getString(Config.NAME);
        if (isArtist() || isAlbum()) {
            mArtistName = mArguments.getString(Config.ARTIST_NAME);
        }
        mPagerAdapter = new PagerAdapter(this);
        mTabCarousel = (ProfileTabCarousel)findViewById(R.id.acivity_profile_base_tab_carousel);
        mTabCarousel.reset();
        final ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(newColor);
        }
        if (isArtist()) {
            mTabCarousel.setArtistProfileHeader(this, mArtistName);
            mPagerAdapter.add(InfoArtistFragment.class, mArguments);
            mPagerAdapter.add(ArtistSongFragment.class, mArguments);
            mPagerAdapter.add(ArtistAlbumFragment.class, mArguments);
            mResources.setTitle(mArtistName);
        } else if (isAlbum()) {
            mTabCarousel.setAlbumProfileHeader(this, mProfileName, mArtistName);
            mPagerAdapter.add(InfoAlbumFragment.class, mArguments);
            mPagerAdapter.add(AlbumSongFragment.class, mArguments);
            mResources.setTitle(mProfileName);
            mResources.setSubtitle(mArguments.getString(Config.ALBUM_YEAR));
        } else if (isFavorites()) {
            mTabCarousel.setPlaylistOrGenreProfileHeader(this, mProfileName);
            mPagerAdapter.add(FavoriteFragment.class, null);
            mResources.setTitle(mProfileName);
        } else if (isLastAdded()) {
            mTabCarousel.setPlaylistOrGenreProfileHeader(this, mProfileName);
            mPagerAdapter.add(LastAddedFragment.class, null);
            mResources.setTitle(mProfileName);
        } else if (isPlaylist()) {
            mTabCarousel.setPlaylistOrGenreProfileHeader(this, mProfileName);
            mPagerAdapter.add(PlaylistSongFragment.class, mArguments);
            mResources.setTitle(mProfileName);
        } else if (isGenre()) {
            mTabCarousel.setPlaylistOrGenreProfileHeader(this, mProfileName);
            mPagerAdapter.add(GenreSongFragment.class, mArguments);
            mResources.setTitle(mProfileName);
        }

        mViewPager = (ViewPager)findViewById(R.id.acivity_profile_base_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
        mViewPager.setOnPageChangeListener(this);
        mTabCarousel.setListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mImageFetcher.flush();
    }

    @Override
    public int setContentView() {
        return R.layout.activity_profile_base;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mResources.setAddToHomeScreenIcon(menu);
        final MenuItem shuffle = menu.findItem(R.id.menu_shuffle);
        String title = null;
        if (isFavorites() || isLastAdded() || isPlaylist()) {
            title = getString(R.string.menu_play_all);
        } else {
            title = getString(R.string.menu_shuffle);
        }
        shuffle.setTitle(title);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.add_to_homescreen, menu);
        getMenuInflater().inflate(R.menu.shuffle, menu);
        if (isArtistSongPage()) {
            getMenuInflater().inflate(R.menu.artist_song_sort_by, menu);
        } else if (isArtistAlbumPage()) {
            getMenuInflater().inflate(R.menu.artist_album_sort_by, menu);
        } else if (isAlbum()) {
            getMenuInflater().inflate(R.menu.album_song_sort_by, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isAlbum()) {
                    goBack();
                } else {
                    goBack();
                }
                return true;
            case R.id.menu_add_to_homescreen: {
                final String name = isArtist() ? mArtistName : mProfileName;
                final Long id = mArguments.getLong(Config.ID);
                BassoUtils.createShortcutIntent(name, mArtistName, id, mType, this);
                return true;
            }
            case R.id.menu_shuffle: {
                final long id = mArguments.getLong(Config.ID);
                long[] list = null;
                if (isArtist()) {
                    list = MusicUtils.getSongListForArtist(this, id);
                } else if (isAlbum()) {
                    list = MusicUtils.getSongListForAlbum(this, id);
                } else if (isGenre()) {
                    list = MusicUtils.getSongListForGenre(this, id);
                }
                if (isPlaylist()) {
                    MusicUtils.playPlaylist(this, id);
                } else if (isFavorites()) {
                    MusicUtils.playFavorites(this);
                } else if (isLastAdded()) {
                    MusicUtils.playLastAdded(this);
                } else {
                    if (list != null && list.length > 0) {
                        MusicUtils.playAll(this, list, 0, true);
                    }
                }
                return true;
            }
            case R.id.menu_sort_by_az:
                if (isArtistSongPage()) {
                    mPreferences.setArtistSongSortOrder(SortOrder.ArtistSongSortOrder.SONG_A_Z);
                    getArtistSongFragment().refresh();
                } else if (isArtistAlbumPage()) {
                    mPreferences.setArtistAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
                    getArtistAlbumFragment().refresh();
                } else {
                    mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_A_Z);
                    getAlbumSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_za:
                if (isArtistSongPage()) {
                    mPreferences.setArtistSongSortOrder(SortOrder.ArtistSongSortOrder.SONG_Z_A);
                    getArtistSongFragment().refresh();
                } else if (isArtistAlbumPage()) {
                    mPreferences.setArtistAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_Z_A);
                    getArtistAlbumFragment().refresh();
                } else {
                    mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_Z_A);
                    getAlbumSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_album:
                if (isArtistSongPage()) {
                    mPreferences.setArtistSongSortOrder(SortOrder.ArtistSongSortOrder.SONG_ALBUM);
                    getArtistSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_year:
                if (isArtistSongPage()) {
                    mPreferences.setArtistSongSortOrder(SortOrder.ArtistSongSortOrder.SONG_YEAR);
                    getArtistSongFragment().refresh();
                } else if (isArtistAlbumPage()) {
                    mPreferences.setArtistAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR);
                    getArtistAlbumFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_duration:
                if (isArtistSongPage()) {
                    mPreferences
                            .setArtistSongSortOrder(SortOrder.ArtistSongSortOrder.SONG_DURATION);
                    getArtistSongFragment().refresh();
                } else {
                    mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_DURATION);
                    getAlbumSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_date_added:
                if (isArtistSongPage()) {
                    mPreferences.setArtistSongSortOrder(SortOrder.ArtistSongSortOrder.SONG_DATE);
                    getArtistSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_track_list:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
                getAlbumSongFragment().refresh();
                return true;
            case R.id.menu_sort_by_filename:
                if(isArtistSongPage()) {
                    mPreferences.setArtistSongSortOrder(
                            SortOrder.ArtistSongSortOrder.SONG_FILENAME);
                    getArtistSongFragment().refresh();
                }
                else {
                    mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_FILENAME);
                    getAlbumSongFragment().refresh();
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(mArguments);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset,
            final int positionOffsetPixels) {
        if (mViewPager.isFakeDragging()) {
            return;
        }
        final int scrollToX = (int)((position + positionOffset) * mTabCarousel
                .getAllowedHorizontalScrollLength());
        mTabCarousel.scrollTo(scrollToX, 0);
    }

    @Override
    public void onPageSelected(final int position) {
        mTabCarousel.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mTabCarousel.restoreYCoordinate(250, mViewPager.getCurrentItem());
        }
    }

    @Override
    public void onTouchDown() {
        mViewPager.beginFakeDrag();
    }

    @Override
    public void onTouchUp() {
        if (mViewPager.isFakeDragging()) {
            mViewPager.endFakeDrag();
        }
    }

    @Override
    public void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        if (mViewPager.isFakeDragging()) {
            mViewPager.fakeDragBy(oldl - l);
        }
    }

    @Override
    public void onTabSelected(final int position) {
        mViewPager.setCurrentItem(position);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_PHOTO) {
            if (resultCode == RESULT_OK) {
                final Uri selectedImage = data.getData();
                final String[] filePathColumn = {
                    MediaStore.Images.Media.DATA
                };

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null,
                        null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    final String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    cursor = null;

                    String key = mProfileName;
                    if (isArtist()) {
                        key = mArtistName;
                    } else if (isAlbum()) {
                        key = ImageFetcher.generateAlbumCacheKey(mProfileName, mArtistName);
                    }

                    final Bitmap bitmap = ImageFetcher.decodeSampledBitmapFromFile(picturePath);
                    mImageFetcher.addBitmapToCache(key, bitmap);
                    if (isAlbum()) {
                        mTabCarousel.getAlbumArt().setImageBitmap(bitmap);
                    } else {
                        mTabCarousel.getPhoto().setImageBitmap(bitmap);
                    }
                }
            } else {
                selectOldPhoto();
            }
        }
    }

    public void selectNewPhoto() {
        removeFromCache();
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        startActivityForResult(intent, NEW_PHOTO);
    }

    public void selectOldPhoto() {
        removeFromCache();
        if (isArtist()) {
            mTabCarousel.setArtistProfileHeader(this, mArtistName);
        } else if (isAlbum()) {
            mTabCarousel.setAlbumProfileHeader(this, mProfileName, mArtistName);
        } else {
            mTabCarousel.setPlaylistOrGenreProfileHeader(this, mProfileName);
        }
    }

    public void fetchAlbumArt() {
        removeFromCache();
        mTabCarousel.fetchAlbumPhoto(this, mProfileName, mArtistName);
    }

    public void googleSearch() {
        String query = mProfileName;
        if (isArtist()) {
            query = mArtistName;
        } else if (isAlbum()) {
            query = mProfileName + " " + mArtistName;
        }
        final Intent googleSearch = new Intent(Intent.ACTION_WEB_SEARCH);
        googleSearch.putExtra(SearchManager.QUERY, query);
        startActivity(googleSearch);
    }

    private void removeFromCache() {
        String key = mProfileName;
        if (isArtist()) {
            key = mArtistName;
        } else if (isAlbum()) {
            key = ImageFetcher.generateAlbumCacheKey(mProfileName, mArtistName);
        }
        mImageFetcher.removeFromCache(key);
        SystemClock.sleep(80);
    }

    private void goBack() {
        finish();
    }

    private final boolean isArtist() {
        return mType.equals(MediaStore.Audio.Artists.CONTENT_TYPE);
    }

    private final boolean isAlbum() {
        return mType.equals(MediaStore.Audio.Albums.CONTENT_TYPE);
    }

    private final boolean isGenre() {
        return mType.equals(MediaStore.Audio.Genres.CONTENT_TYPE);
    }

    private final boolean isPlaylist() {
        return mType.equals(MediaStore.Audio.Playlists.CONTENT_TYPE);
    }

    private final boolean isFavorites() {
        return mType.equals(getString(R.string.playlist_favorites));
    }

    private final boolean isLastAdded() {
        return mType.equals(getString(R.string.playlist_last_added));
    }

    private boolean isArtistSongPage() {
        return isArtist() && mViewPager.getCurrentItem() == 1;
    }

    private boolean isArtistAlbumPage() {
        return isArtist() && mViewPager.getCurrentItem() == 2;
    }

    private ArtistSongFragment getArtistSongFragment() {
        return (ArtistSongFragment)mPagerAdapter.getFragment(1);
    }

    private ArtistAlbumFragment getArtistAlbumFragment() {
        return (ArtistAlbumFragment)mPagerAdapter.getFragment(2);
    }

    private AlbumSongFragment getAlbumSongFragment() {
        return (AlbumSongFragment)mPagerAdapter.getFragment(1);
    }
}
