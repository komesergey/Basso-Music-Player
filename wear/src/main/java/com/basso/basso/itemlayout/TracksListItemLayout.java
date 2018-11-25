package com.basso.basso.itemlayout;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.basso.basso.R;


public class TracksListItemLayout extends LinearLayout  implements WearableListView.OnCenterProximityListener {
    private final int mNonCenterColor;
    private final int mCenterColor;
    private TextView trackTitle;
    private TextView trackArtist;
    private ImageView cover;
    public TracksListItemLayout(Context context) {
        this(context, null);
    }

    public TracksListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TracksListItemLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
        mNonCenterColor = getResources().getColor(android.R.color.white);
        mCenterColor = getResources().getColor(R.color.list_row_hover_end_color);
    }

    @Override
    protected void onFinishInflate() {
        trackTitle = (TextView)findViewById(R.id.track_title);
        trackArtist = (TextView)findViewById(R.id.track_artist);
        cover = (ImageView)findViewById(R.id.track_list_circle);
        super.onFinishInflate();
    }

    @Override
    public void onCenterPosition(boolean animate) {
        trackTitle.setAlpha(1f);
        trackArtist.setAlpha(1f);
        cover.setAlpha(1f);
        this.setBackgroundColor(mCenterColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        trackTitle.setAlpha(0.6f);
        trackArtist.setAlpha(0.6f);
        cover.setAlpha(0.6f);
        this.setBackgroundColor(mNonCenterColor);
    }
}
