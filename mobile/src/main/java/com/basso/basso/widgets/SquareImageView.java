package com.basso.basso.widgets;

import android.content.Context;
import android.util.AttributeSet;

public class SquareImageView extends LayoutSuppressingImageView {

    public SquareImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(final int widthSpec, final int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        final int mSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(mSize, mSize);
    }
}
