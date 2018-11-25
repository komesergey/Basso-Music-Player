package com.basso.basso.widgets;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

@SuppressLint("NewApi")
public class VerticalScrollListener implements OnScrollListener {

    private final ScrollableHeader mHeader;

    private final ProfileTabCarousel mTabCarousel;

    private final int mPageIndex;

    public VerticalScrollListener(final ScrollableHeader header, final ProfileTabCarousel carousel, final int pageIndex) {
        mHeader = header;
        mTabCarousel = carousel;
        mPageIndex = pageIndex;
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem,
            final int visibleItemCount, final int totalItemCount) {

        if (mTabCarousel == null || mTabCarousel.isTabCarouselIsAnimating()) {
            return;
        }

        final View top = view.getChildAt(firstVisibleItem);
        if (top == null) {
            return;
        }

        if (firstVisibleItem != 0) {
            mTabCarousel.moveToYCoordinate(mPageIndex, -mTabCarousel.getAllowedVerticalScrollLength());
            return;
        }
        float y = view.getChildAt(firstVisibleItem).getY();
        final float amtToScroll = Math.max(y, -mTabCarousel.getAllowedVerticalScrollLength());
        mTabCarousel.moveToYCoordinate(mPageIndex, amtToScroll);
        float ratio =  amtToScroll/-mTabCarousel.getAllowedVerticalScrollLength();
        mTabCarousel.setBackgroundAlpha((int) (ratio * 255));
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        if (mHeader != null) {
            mHeader.onScrollStateChanged(view, scrollState);
        }
    }

    public interface ScrollableHeader {
        public void onScrollStateChanged(AbsListView view, int scrollState);
    }
}
