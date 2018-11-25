package com.basso.basso.widgets;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

public class AlphaTouchInterceptorOverlay extends FrameLayout {

    private final View mInterceptorLayer;

    private float mAlpha = 0.0f;

    private View mAlphaLayer;

    public AlphaTouchInterceptorOverlay(final Context context) {
        super(context);
        mInterceptorLayer = new View(context);
        mInterceptorLayer.setBackgroundColor(0);
        addView(mInterceptorLayer);
        mAlphaLayer = this;
    }

    public void setAlphaLayer(final View alphaLayer) {
        if (mAlphaLayer == alphaLayer) {
            return;
        }

        if (mAlphaLayer == this) {
            setAlphaOnViewBackground(this, 0.0f);
        }

        mAlphaLayer = alphaLayer == null ? this : alphaLayer;
        setAlphaLayerValue(mAlpha);
    }

    public void setAlphaLayerValue(final float alpha) {
        mAlpha = alpha;
        if (mAlphaLayer != null) {
            setAlphaOnViewBackground(mAlphaLayer, mAlpha);
        }
    }

    public void setOverlayOnClickListener(final OnClickListener listener) {
        mInterceptorLayer.setOnClickListener(listener);
    }

    public void setOverlayClickable(final boolean clickable) {
        mInterceptorLayer.setClickable(clickable);
    }

    public static void setAlphaOnViewBackground(final View view, final float alpha) {
        if (view != null) {
            view.setBackgroundColor((int)(clamp(alpha, 0.0f, 1.0f) * 255) << 24);
        }
    }

    public static float clamp(final float input, final float lowerBound, final float upperBound) {
        if (input < lowerBound) {
            return lowerBound;
        } else if (input > upperBound) {
            return upperBound;
        }
        return input;
    }

}
