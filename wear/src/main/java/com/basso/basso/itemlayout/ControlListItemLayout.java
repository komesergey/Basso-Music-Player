package com.basso.basso.itemlayout;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.basso.basso.R;

public class ControlListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {
    private final int mNonCenterColor;
    private final int mCenterColor;
    private ImageButton leftButton;
    private ImageButton centerButton;
    private ImageButton rightButton;
    private TextView description;
    private TextView controlName;
    private SeekBar controlSeekBar;
    public ControlListItemLayout(Context context) {
        this(context, null);
    }

    public ControlListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlListItemLayout(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        mNonCenterColor = getResources().getColor(android.R.color.transparent);
        mCenterColor = getResources().getColor(R.color.transparent_black);
    }

    @Override
    protected void onFinishInflate() {
        leftButton = (ImageButton)findViewById(R.id.leftButton);
        centerButton = (ImageButton)findViewById(R.id.centerButton);
        rightButton = (ImageButton)findViewById(R.id.rightButton);
        description = (TextView)findViewById(R.id.description);
        controlSeekBar = (SeekBar)findViewById(R.id.control_seekbar);
        controlName = (TextView)findViewById(R.id.control_name);
        super.onFinishInflate();
    }

    @Override
    public void onCenterPosition(boolean animate) {
        leftButton.setAlpha(1f);
        centerButton.setAlpha(1f);
        rightButton.setAlpha(1f);
        description.setAlpha(1f);
        controlName.setAlpha(1f);
        controlSeekBar.setAlpha(1f);
        this.setBackgroundColor(mCenterColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        leftButton.setAlpha(0.0f);
        centerButton.setAlpha(0.0f);
        rightButton.setAlpha(0.0f);
        description.setAlpha(0.0f);
        controlName.setAlpha(0.0f);
        controlSeekBar.setAlpha(0.0f);
        this.setBackgroundColor(mNonCenterColor);
    }
}
