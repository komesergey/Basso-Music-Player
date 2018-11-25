package com.basso.basso.ui.fragments.profile;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.adapters.ProfileSongAdapter;
import com.basso.basso.recycler.RecycleHolder;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.widgets.ProfileTabCarousel;
import com.basso.basso.widgets.VerticalScrollListener;

public class InfoAlbumFragment extends Fragment {

    private String albumName;

    private String artistName;

    private ProfileSongAdapter mAdapter;

    private ListView mListView;

    private TextView mInfo;

    private ProfileTabCarousel mProfileTabCarousel;

    public InfoAlbumFragment() {}

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mProfileTabCarousel = (ProfileTabCarousel)activity.findViewById(R.id.acivity_profile_base_tab_carousel);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ProfileSongAdapter(getActivity(), R.layout.list_item_simple, ProfileSongAdapter.DISPLAY_ALBUM_SETTING);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.list_base, null);
        mListView = (ListView)rootView.findViewById(R.id.list_base);
        mInfo = (TextView)rootView.findViewById(R.id.empty);
        mInfo.setPadding(0,((ProfileTabCarousel)getActivity().findViewById(R.id.acivity_profile_base_tab_carousel)).getTabHeight(),0,0);
        mInfo.setText(getString(R.string.loading_info));
        mInfo.setTextColor(Color.DKGRAY);
        mInfo.setTypeface(Typeface.SANS_SERIF);
        mInfo.setLinkTextColor(getResources().getColor(R.color.action_bar_background));
        mInfo.setMovementMethod(LinkMovementMethod.getInstance());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mInfo);
        mListView.setRecyclerListener(new RecycleHolder());
        mListView.setOnScrollListener(new VerticalScrollListener(null, mProfileTabCarousel, 0));
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setFastScrollEnabled(false);
        mListView.setPadding(0, 0, 0, 0);
        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            System.out.println("Artist in fragment " + arguments.getString(Config.ARTIST_NAME));
            this.artistName = arguments.getString(Config.ARTIST_NAME);
            this.albumName = arguments.getString(Config.NAME);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(getArguments() != null ? getArguments() : new Bundle());
    }

    @Override
    public void onStart(){
        super.onStart();
        BassoUtils.setAlbumInfo(getActivity(), artistName, albumName, mInfo);
    }
}