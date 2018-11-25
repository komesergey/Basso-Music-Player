package com.basso.basso.adapters;

import android.app.FragmentManager;
import android.app.Fragment;
import android.support.wearable.view.FragmentGridPagerAdapter;

public class MainGridViewPagerAdapter extends FragmentGridPagerAdapter {
    private final Fragment[][] mFragment;
    public MainGridViewPagerAdapter(FragmentManager fragmentManager, Fragment[][] fragments){
        super(fragmentManager);
        this.mFragment = fragments;
    }
    @Override
    public int getColumnCount(int rowNum)
    {
        return mFragment[rowNum].length;
    }
    @Override
    public int getRowCount(){
        return mFragment.length;
    }

    @Override
    public Fragment getFragment(int row, int col)
    {
        return mFragment[row][col];
    }
}
