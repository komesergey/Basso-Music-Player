package com.basso.basso.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class FrameLayoutWithOverlay extends FrameLayout {

    private final AlphaTouchInterceptorOverlay mOverlay;

    public FrameLayoutWithOverlay(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mOverlay = new AlphaTouchInterceptorOverlay(context);
        addView(mOverlay);
    }

    @Override
    public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        mOverlay.bringToFront();
    }

    protected void setAlphaLayer(final View layer) {
        mOverlay.setAlphaLayer(layer);
    }

    public void setAlphaLayerValue(final float alpha) {
        mOverlay.setAlphaLayerValue(alpha);
    }

    public void setOverlayOnClickListener(final OnClickListener listener) {
        mOverlay.setOverlayOnClickListener(listener);
    }

    public void setOverlayClickable(final boolean clickable) {
        mOverlay.setOverlayClickable(clickable);
    }
}
