package com.basso.basso.ui.fragments.phone;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.basso.basso.R;
import com.basso.basso.adapters.PagerAdapter;
import com.basso.basso.adapters.PagerAdapter.MusicFragments;
import com.basso.basso.ui.fragments.AlbumFragment;
import com.basso.basso.ui.fragments.ArtistFragment;
import com.basso.basso.ui.fragments.FolderFragment;
import com.basso.basso.ui.fragments.SongFragment;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.NavUtils;
import com.basso.basso.utils.PreferenceUtils;
import com.basso.basso.utils.SortOrder;
import com.basso.basso.utils.ThemeUtils;
import com.astuetz.PagerSlidingTabStrip;
import com.viewpagerindicator.TitlePageIndicator.OnCenterItemClickListener;

public class MusicBrowserPhoneFragment extends Fragment implements
        OnCenterItemClickListener {

    private ViewPager mViewPager;

    private PagerAdapter mPagerAdapter;

    private ThemeUtils mResources;

    private PreferenceUtils mPreferences;

    public MusicBrowserPhoneFragment() {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceUtils.getInstance(getActivity());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_music_browser_phone, container, false);
        mPagerAdapter = new PagerAdapter(getActivity());
        final MusicFragments[] mFragments = MusicFragments.values();
        for (final MusicFragments mFragment : mFragments) {
            mPagerAdapter.add(mFragment.getFragmentClass(), null);
        }
        mViewPager = (ViewPager)rootView.findViewById(R.id.fragment_home_phone_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
        mViewPager.setCurrentItem(mPreferences.getStartPage());
        final PagerSlidingTabStrip pageIndicator = (PagerSlidingTabStrip)rootView.findViewById(R.id.fragment_home_phone_pager_titles);
        pageIndicator.setViewPager(mViewPager);
        pageIndicator.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL), Typeface.NORMAL);
        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mResources = new ThemeUtils(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreferences.setStartPage(mViewPager.getCurrentItem());
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.shuffle, menu);
        if (isRecentPage()) {
            inflater.inflate(R.menu.view_as, menu);
        } else if (isArtistPage()) {
            inflater.inflate(R.menu.artist_sort_by, menu);
            inflater.inflate(R.menu.view_as, menu);
        } else if (isAlbumPage()) {
            inflater.inflate(R.menu.album_sort_by, menu);
            inflater.inflate(R.menu.view_as, menu);
        } else if (isSongPage()) {
            inflater.inflate(R.menu.song_sort_by, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_shuffle:
                MusicUtils.shuffleAll(getActivity());
                return true;
            case R.id.menu_sort_by_az:
                if (isArtistPage()) {
                    mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_A_Z);
                    getArtistFragment().refresh();
                } else if (isAlbumPage()) {
                    mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                    getAlbumFragment().refresh();
                } else if (isSongPage()) {
                    mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                    getSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_za:
                if (isArtistPage()) {
                    mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_Z_A);
                    getArtistFragment().refresh();
                } else if (isAlbumPage()) {
                    mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_Z_A);
                    getAlbumFragment().refresh();
                } else if (isSongPage()) {
                    mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                    getSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_artist:
                if (isAlbumPage()) {
                    mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_ARTIST);
                    getAlbumFragment().refresh();
                } else if (isSongPage()) {
                    mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                    getSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_album:
                if (isSongPage()) {
                    mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                    getSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_year:
                if (isAlbumPage()) {
                    mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_YEAR);
                    getAlbumFragment().refresh();
                } else if (isSongPage()) {
                    mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                    getSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_duration:
                if (isSongPage()) {
                    mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                    getSongFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_number_of_songs:
                if (isArtistPage()) {
                    mPreferences
                            .setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_SONGS);
                    getArtistFragment().refresh();
                } else if (isAlbumPage()) {
                    mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                    getAlbumFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_number_of_albums:
                if (isArtistPage()) {
                    mPreferences
                            .setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_ALBUMS);
                    getArtistFragment().refresh();
                }
                return true;
            case R.id.menu_sort_by_filename:
                if(isSongPage()) {
                    mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_FILENAME);
                    getSongFragment().refresh();
                }
                return true;
            case R.id.menu_view_as_simple:
                if (isRecentPage()) {
                    mPreferences.setRecentLayout("simple");
                } else if (isArtistPage()) {
                    mPreferences.setArtistLayout("simple");
                } else if (isAlbumPage()) {
                    mPreferences.setAlbumLayout("simple");
                }
                NavUtils.goHome(getActivity());
                return true;
            case R.id.menu_view_as_detailed:
                if (isRecentPage()) {
                    mPreferences.setRecentLayout("detailed");
                } else if (isArtistPage()) {
                    mPreferences.setArtistLayout("detailed");
                } else if (isAlbumPage()) {
                    mPreferences.setAlbumLayout("detailed");
                }
                NavUtils.goHome(getActivity());
                return true;
            case R.id.menu_view_as_grid:
                if (isRecentPage()) {
                    mPreferences.setRecentLayout("grid");
                } else if (isArtistPage()) {
                    mPreferences.setArtistLayout("grid");
                } else if (isAlbumPage()) {
                    mPreferences.setAlbumLayout("grid");
                }
                NavUtils.goHome(getActivity());
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCenterItemClick(final int position) {
        if (position == 3) {
            getArtistFragment().scrollToCurrentArtist();
        } else if (position == 4) {
            getAlbumFragment().scrollToCurrentAlbum();
        } else if (position == 5) {
            getSongFragment().scrollToCurrentSong();
        }
    }

    public int currentItem(){
        return mViewPager.getCurrentItem();
    }

    public FolderFragment getFolderFragment(){
        return (FolderFragment)mPagerAdapter.getFragment(0);
    }

    private boolean isArtistPage() {
        return mViewPager.getCurrentItem() == 3;
    }

    private ArtistFragment getArtistFragment() {
        return (ArtistFragment)mPagerAdapter.getFragment(3);
    }

    private boolean isAlbumPage() {
        return mViewPager.getCurrentItem() == 4;
    }

    private AlbumFragment getAlbumFragment() {
        return (AlbumFragment)mPagerAdapter.getFragment(4);
    }

    private boolean isSongPage() {
        return mViewPager.getCurrentItem() == 5;
    }

    private SongFragment getSongFragment() {
        return (SongFragment)mPagerAdapter.getFragment(5);
    }

    private boolean isRecentPage() {
        return mViewPager.getCurrentItem() == 2;
    }
}