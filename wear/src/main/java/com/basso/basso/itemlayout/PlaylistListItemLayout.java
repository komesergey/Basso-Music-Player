package com.basso.basso.itemlayout;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.basso.basso.R;

public class PlaylistListItemLayout extends LinearLayout  implements WearableListView.OnCenterProximityListener {

    private final int mNonCenterColor;

    private final int mCenterColor;

    private TextView playlistTitle;

    public PlaylistListItemLayout(Context context) {
        this(context, null);
    }

    public PlaylistListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaylistListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mNonCenterColor = getResources().getColor(android.R.color.white);
        mCenterColor = getResources().getColor(R.color.list_row_hover_end_color);
    }

    @Override
    protected void onFinishInflate() {
        playlistTitle = (TextView)findViewById(R.id.playlist_title);
        super.onFinishInflate();
    }

    @Override
    public void onCenterPosition(boolean animate) {
        playlistTitle.setAlpha(1f);
        this.setBackgroundColor(mCenterColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        playlistTitle.setAlpha(0.6f);
        this.setBackgroundColor(mNonCenterColor);
    }
}
