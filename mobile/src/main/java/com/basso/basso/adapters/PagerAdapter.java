package com.basso.basso.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.basso.basso.R;
import com.basso.basso.ui.fragments.AlbumFragment;
import com.basso.basso.ui.fragments.ArtistFragment;
import com.basso.basso.ui.fragments.FolderFragment;
import com.basso.basso.ui.fragments.GenreFragment;
import com.basso.basso.ui.fragments.PlaylistFragment;
import com.basso.basso.ui.fragments.RecentFragment;
import com.basso.basso.ui.fragments.SongFragment;
import com.basso.basso.utils.Lists;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;


public class PagerAdapter extends FragmentPagerAdapter {

    private final SparseArray<WeakReference<Fragment>> mFragmentArray = new SparseArray<WeakReference<Fragment>>();

    private final List<Holder> mHolderList = Lists.newArrayList();

    private final FragmentActivity mFragmentActivity;

    private int mCurrentPage;

   public PagerAdapter(final FragmentActivity fragmentActivity) {
        super(fragmentActivity.getSupportFragmentManager());
        mFragmentActivity = fragmentActivity;
    }

    @SuppressWarnings("synthetic-access")
    public void add(final Class<? extends Fragment> className, final Bundle params) {
        final Holder mHolder = new Holder();
        mHolder.mClassName = className.getName();
        mHolder.mParams = params;
        final int mPosition = mHolderList.size();
        mHolderList.add(mPosition, mHolder);
        notifyDataSetChanged();
    }

    public Fragment getFragment(final int position) {
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null && mWeakFragment.get() != null) {
            return mWeakFragment.get();
        }
        return getItem(position);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final Fragment mFragment = (Fragment)super.instantiateItem(container, position);
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
        mFragmentArray.put(position, new WeakReference<Fragment>(mFragment));
        return mFragment;
    }

    @Override
    public Fragment getItem(final int position) {
        final Holder mCurrentHolder = mHolderList.get(position);
        final Fragment mFragment = Fragment.instantiate(mFragmentActivity,
                mCurrentHolder.mClassName, mCurrentHolder.mParams);
        return mFragment;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        super.destroyItem(container, position, object);
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
    }

    @Override
    public int getCount() {
        return mHolderList.size();
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return mFragmentActivity.getResources().getStringArray(R.array.page_titles)[position].toUpperCase(Locale.getDefault());
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    protected void setCurrentPage(final int currentPage) {
        mCurrentPage = currentPage;
    }

    public enum MusicFragments {
        FOLDER(FolderFragment.class),
        PLAYLIST(PlaylistFragment.class),
        RECENT(RecentFragment.class),
        ARTIST(ArtistFragment.class),
        ALBUM(AlbumFragment.class),
        SONG(SongFragment.class),
        GENRE(GenreFragment.class);

        private Class<? extends Fragment> mFragmentClass;

        private MusicFragments(final Class<? extends Fragment> fragmentClass) {
            mFragmentClass = fragmentClass;
        }

        public Class<? extends Fragment> getFragmentClass() {
            return mFragmentClass;
        }
    }

    private final static class Holder {
        String mClassName;
        Bundle mParams;
    }
}
