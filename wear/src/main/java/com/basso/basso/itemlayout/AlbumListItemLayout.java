package com.basso.basso.itemlayout;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.basso.basso.R;


public class AlbumListItemLayout extends LinearLayout  implements WearableListView.OnCenterProximityListener {
    private final int mNonCenterColor;
    private final int mCenterColor;
    private TextView albumName;
    private TextView artistName;
    private ImageView cover;
    public AlbumListItemLayout(Context context) {
        this(context, null);
    }

    public AlbumListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumListItemLayout(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        mNonCenterColor = getResources().getColor(android.R.color.white);
        mCenterColor = getResources().getColor(R.color.list_row_hover_end_color);
    }

    @Override
    protected void onFinishInflate() {
        albumName = (TextView) findViewById(R.id.album_title);
        artistName = (TextView) findViewById(R.id.album_artist);
        cover = (ImageView) findViewById(R.id.album_list_circle);
        super.onFinishInflate();
    }

    @Override
    public void onCenterPosition(boolean animate) {
        albumName.setAlpha(1f);
        artistName.setAlpha(1f);
        cover.setAlpha(1f);
        this.setBackgroundColor(mCenterColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        albumName.setAlpha(0.6f);
        artistName.setAlpha(0.6f);
        cover.setAlpha(0.6f);
        this.setBackgroundColor(mNonCenterColor);
    }
}
