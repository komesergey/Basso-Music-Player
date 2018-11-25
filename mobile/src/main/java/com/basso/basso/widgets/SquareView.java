package com.basso.basso.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class SquareView extends ViewGroup {

    public SquareView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final View mChildren = getChildAt(0);
        mChildren.measure(widthMeasureSpec, widthMeasureSpec);
        final int mWidth = resolveSize(mChildren.getMeasuredWidth(), widthMeasureSpec);
        mChildren.measure(mWidth, mWidth);
        setMeasuredDimension(mWidth, mWidth);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int u, final int r,
            final int d) {
        getChildAt(0).layout(0, 0, r - l, d - u);
    }

    @Override
    public void requestLayout() {
        forceLayout();
    }
}
