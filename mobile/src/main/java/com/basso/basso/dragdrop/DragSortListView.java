package com.basso.basso.dragdrop;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.basso.basso.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DragSortListView extends ListView {

    private View mFloatView;

    private final Point mFloatLoc = new Point();

    private int mFloatViewMid;

    private int mFloatViewLeft;

    private int mFloatViewTop;

    private final DataSetObserver mObserver;

    private final float mFloatAlpha = 1.0f;

    private float mCurrFloatAlpha = 1.0f;

    private int mFloatPos;

    private int mScrollY = 0;

    private int mFirstExpPos;

    private int mSecondExpPos;

    private boolean mAnimate = false;

    private int mSrcPos;

    private int mDragDeltaX;

    private int mDragDeltaY;

    private DragListener mDragListener;

    private DropListener mDropListener;

    private RemoveListener mRemoveListener;

    private boolean mDragEnabled = true;

    private final static int IDLE = 0;

    private final static int STOPPED = 1;

    private final static int DRAGGING = 2;

    private int mDragState = IDLE;

    private int mItemHeightCollapsed = 1;

    private int mFloatViewHeight;

    private int mFloatViewHeightHalf;

    private int mWidthMeasureSpec = 0;

    private View[] mSampleViewTypes = new View[1];

    private final DragScroller mDragScroller;

    private float mDragUpScrollStartFrac = 1.0f / 3.0f;

    private float mDragDownScrollStartFrac = 1.0f / 3.0f;

    private int mUpScrollStartY;

    private int mDownScrollStartY;

    private float mDownScrollStartYF;

    private float mUpScrollStartYF;

    private float mDragUpScrollHeight;

    private float mDragDownScrollHeight;

    private float mMaxScrollSpeed = 0.3f;

    private DragScrollProfile mScrollProfile = new DragScrollProfile() {

        /**
         * {@inheritDoc}
         */
        @Override
        public float getSpeed(final float w, final long t) {
            return mMaxScrollSpeed * w;
        }
    };

    private int mX;

    private int mY;

    private int mLastY;

    public final static int DRAG_POS_X = 0x1;

    public final static int DRAG_NEG_X = 0x2;

    public final static int DRAG_POS_Y = 0x4;

    public final static int DRAG_NEG_Y = 0x8;

    private int mDragFlags = 0;

    private boolean mLastCallWasIntercept = false;

    private boolean mInTouchEvent = false;

    private FloatViewManager mFloatViewManager = null;

    private final MotionEvent mCancelEvent;

    private static final int NO_CANCEL = 0;

    private static final int ON_TOUCH_EVENT = 1;

    private static final int ON_INTERCEPT_TOUCH_EVENT = 2;

    private int mCancelMethod = NO_CANCEL;

    private float mSlideRegionFrac = 0.25f;

    private float mSlideFrac = 0.0f;

    private AdapterWrapper mAdapterWrapper;

    private final boolean mTrackDragSort = false;

    private DragSortTracker mDragSortTracker;

    private boolean mBlockLayoutRequests = false;

    private final DragSortController mController;

    public DragSortListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mItemHeightCollapsed = 1;

        mCurrFloatAlpha = mFloatAlpha;

        mSlideRegionFrac = 0.75f;

        mAnimate = mSlideRegionFrac > 0.0f;

        setDragScrollStart(mDragUpScrollStartFrac);

        mController = new DragSortController(this, R.id.edit_track_list_item_handle,
                DragSortController.ON_DOWN, DragSortController.FLING_RIGHT_REMOVE);
        mController.setRemoveEnabled(false);
        mController.setSortEnabled(true);
        mController.setBackgroundColor(getResources().getColor(R.color.holo_blue_light_transparent));

        mFloatViewManager = mController;
        setOnTouchListener(mController);

        mDragScroller = new DragScroller();
        setOnScrollListener(mDragScroller);

        mCancelEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0f, 0f, 0, 0f,
                0f, 0, 0);

        mObserver = new DataSetObserver() {
            private void cancel() {
                if (mDragState == DRAGGING) {
                    stopDrag(false);
                }
            }

            @Override
            public void onChanged() {
                cancel();
            }

            @Override
            public void onInvalidated() {
                cancel();
            }
        };
    }

    public void setFloatAlpha(final float alpha) {
        mCurrFloatAlpha = alpha;
    }

    public float getFloatAlpha() {
        return mCurrFloatAlpha;
    }

    public void setMaxScrollSpeed(final float max) {
        mMaxScrollSpeed = max;
    }

    @Override
    public void setAdapter(final ListAdapter adapter) {
        mAdapterWrapper = new AdapterWrapper(adapter);
        adapter.registerDataSetObserver(mObserver);
        super.setAdapter(mAdapterWrapper);
    }

    public ListAdapter getInputAdapter() {
        if (mAdapterWrapper == null) {
            return null;
        } else {
            return mAdapterWrapper.getAdapter();
        }
    }

    private class AdapterWrapper extends HeaderViewListAdapter {
        private final ListAdapter mAdapter;

        public AdapterWrapper(final ListAdapter adapter) {
            super(null, null, adapter);
            mAdapter = adapter;
        }

        public ListAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            RelativeLayout v;
            View child;
            if (convertView != null) {
                v = (RelativeLayout)convertView;
                final View oldChild = v.getChildAt(0);
                try {
                    child = mAdapter.getView(position, oldChild, v);
                    if (child != oldChild) {
                        v.removeViewAt(0);
                        v.addView(child);
                    }
                } catch (final Exception nullz) {}
            } else {
                final AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                v = new RelativeLayout(getContext());
                v.setLayoutParams(params);
                try {
                    child = mAdapter.getView(position, null, v);
                    v.addView(child);
                } catch (final Exception todo) {}
            }
            adjustItem(position + getHeaderViewsCount(), v, true);
            return v;
        }
    }

    private void drawDivider(final int expPosition, final Canvas canvas) {
        final Drawable divider = getDivider();
        final int dividerHeight = getDividerHeight();
        if (divider != null && dividerHeight != 0) {
            final ViewGroup expItem = (ViewGroup)getChildAt(expPosition - getFirstVisiblePosition());
            if (expItem != null) {
                final int l = getPaddingLeft();
                final int r = getWidth() - getPaddingRight();
                final int t;
                final int b;
                final int childHeight = expItem.getChildAt(0).getHeight();
                if (expPosition > mSrcPos) {
                    t = expItem.getTop() + childHeight;
                    b = t + dividerHeight;
                } else {
                    b = expItem.getBottom() - childHeight;
                    t = b - dividerHeight;
                }
                divider.setBounds(l, t, r, b);
                divider.draw(canvas);
            }
        }
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mFloatView != null) {
            if (mFirstExpPos != mSrcPos) {
                drawDivider(mFirstExpPos, canvas);
            }
            if (mSecondExpPos != mFirstExpPos && mSecondExpPos != mSrcPos) {
                drawDivider(mSecondExpPos, canvas);
            }
            final int w = mFloatView.getWidth();
            final int h = mFloatView.getHeight();
            final int alpha = (int)(255f * mCurrFloatAlpha);
            canvas.save();
            canvas.translate(mFloatViewLeft, mFloatViewTop);
            canvas.clipRect(0, 0, w, h);
            canvas.saveLayerAlpha(0, 0, w, h, alpha, Canvas.ALL_SAVE_FLAG);
            mFloatView.draw(canvas);
            canvas.restore();
            canvas.restore();
        }
    }

    private class ItemHeights {
        int item;
        int child;
    }

    private void measureItemAndGetHeights(final int position, final View item, final ItemHeights heights) {
        ViewGroup.LayoutParams lp = item.getLayoutParams();
        final boolean isHeadFoot = position < getHeaderViewsCount() || position >= getCount() - getFooterViewsCount();
        int height = lp == null ? 0 : lp.height;
        if (height > 0) {
            heights.item = height;
            if (isHeadFoot) {
                heights.child = heights.item;
            } else if (position == mSrcPos) {
                heights.child = 0;
            } else {
                final View child = ((ViewGroup)item).getChildAt(0);
                lp = child.getLayoutParams();
                height = lp == null ? 0 : lp.height;
                if (height > 0) {
                    heights.child = height;
                } else {
                    final int hspec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    final int wspec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
                            getListPaddingLeft() + getListPaddingRight(), lp.width);
                    child.measure(wspec, hspec);
                    heights.child = child.getMeasuredHeight();
                }
            }
        } else {
            final int hspec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            final int wspec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, getListPaddingLeft()
                    + getListPaddingRight(), lp == null ? ViewGroup.LayoutParams.MATCH_PARENT
                    : lp.width);
            item.measure(wspec, hspec);

            heights.item = item.getMeasuredHeight();
            if (isHeadFoot) {
                heights.child = heights.item;
            } else if (position == mSrcPos) {
                heights.child = 0;
            } else {
                heights.child = ((ViewGroup)item).getChildAt(0).getMeasuredHeight();
            }
        }
    }

    private void getItemHeights(final int position, final View item, final ItemHeights heights) {
        final boolean isHeadFoot = position < getHeaderViewsCount() || position >= getCount() - getFooterViewsCount();
        heights.item = item.getHeight();
        if (isHeadFoot) {
            heights.child = heights.item;
        } else if (position == mSrcPos) {
            heights.child = 0;
        } else {
            heights.child = ((ViewGroup)item).getChildAt(0).getHeight();
        }
    }

    private void getItemHeights(final int position, final ItemHeights heights) {

        final int first = getFirstVisiblePosition();
        final int last = getLastVisiblePosition();

        if (position >= first && position <= last) {
            getItemHeights(position, getChildAt(position - first), heights);
        } else {
            final ListAdapter adapter = getAdapter();
            final int type = adapter.getItemViewType(position);
            final int typeCount = adapter.getViewTypeCount();
            if (typeCount != mSampleViewTypes.length) {
                mSampleViewTypes = new View[typeCount];
            }

            View v;
            if (type >= 0) {
                if (mSampleViewTypes[type] == null) {
                    v = adapter.getView(position, null, this);
                    mSampleViewTypes[type] = v;
                } else {
                    v = adapter.getView(position, mSampleViewTypes[type], this);
                }
            } else {
                v = adapter.getView(position, null, this);
            }

            measureItemAndGetHeights(position, v, heights);
        }

    }

    private int getShuffleEdge(final int position, final int top) {
        return getShuffleEdge(position, top, null);
    }

    private int getShuffleEdge(final int position, final int top, ItemHeights heights) {
        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();
        if (position <= numHeaders || position >= getCount() - numFooters) {
            return top;
        }

        final int divHeight = getDividerHeight();

        int edge;

        final int maxBlankHeight = mFloatViewHeight - mItemHeightCollapsed;

        if (heights == null) {
            heights = new ItemHeights();
            getItemHeights(position, heights);
        }
        int otop = top;
        if (mSecondExpPos <= mSrcPos) {
            if (position == mSecondExpPos && mFirstExpPos != mSecondExpPos) {
                if (position == mSrcPos) {
                    otop = top + heights.item - mFloatViewHeight;
                } else {
                    final int blankHeight = heights.item - heights.child;
                    otop = top + blankHeight - maxBlankHeight;
                }
            } else if (position > mSecondExpPos && position <= mSrcPos) {
                otop = top - maxBlankHeight;
            }

        } else {
            if (position > mSrcPos && position <= mFirstExpPos) {
                otop = top + maxBlankHeight;
            } else if (position == mSecondExpPos && mFirstExpPos != mSecondExpPos) {
                final int blankHeight = heights.item - heights.child;
                otop = top + blankHeight;
            }
        }

        if (position <= mSrcPos) {
            final ItemHeights tmpHeights = new ItemHeights();
            getItemHeights(position - 1, tmpHeights);
            edge = otop + (mFloatViewHeight - divHeight - tmpHeights.child) / 2;
        } else {
            edge = otop + (heights.child - divHeight - mFloatViewHeight) / 2;
        }

        return edge;
    }

    private boolean updatePositions() {

        final int first = getFirstVisiblePosition();
        int startPos = mFirstExpPos;
        View startView = getChildAt(startPos - first);

        if (startView == null) {
            startPos = first + getChildCount() / 2;
            startView = getChildAt(startPos - first);
        }
        final int startTop = startView.getTop() + mScrollY;

        final ItemHeights itemHeights = new ItemHeights();
        getItemHeights(startPos, startView, itemHeights);

        int edge = getShuffleEdge(startPos, startTop, itemHeights);
        int lastEdge = edge;

        final int divHeight = getDividerHeight();
        int itemPos = startPos;
        int itemTop = startTop;
        if (mFloatViewMid < edge) {
            while (itemPos >= 0) {
                itemPos--;
                getItemHeights(itemPos, itemHeights);
                if (itemPos == 0) {
                    edge = itemTop - divHeight - itemHeights.item;
                    break;
                }

                itemTop -= itemHeights.item + divHeight;
                edge = getShuffleEdge(itemPos, itemTop, itemHeights);
                if (mFloatViewMid >= edge) {
                    break;
                }

                lastEdge = edge;
            }
        } else {
            final int count = getCount();
            while (itemPos < count) {
                if (itemPos == count - 1) {
                    edge = itemTop + divHeight + itemHeights.item;
                    break;
                }

                itemTop += divHeight + itemHeights.item;
                getItemHeights(itemPos + 1, itemHeights);
                edge = getShuffleEdge(itemPos + 1, itemTop, itemHeights);
                if (mFloatViewMid < edge) {
                    break;
                }

                lastEdge = edge;
                itemPos++;
            }
        }

        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();

        boolean updated = false;

        final int oldFirstExpPos = mFirstExpPos;
        final int oldSecondExpPos = mSecondExpPos;
        final float oldSlideFrac = mSlideFrac;

        if (mAnimate) {
            final int edgeToEdge = Math.abs(edge - lastEdge);

            int edgeTop, edgeBottom;
            if (mFloatViewMid < edge) {
                edgeBottom = edge;
                edgeTop = lastEdge;
            } else {
                edgeTop = edge;
                edgeBottom = lastEdge;
            }

            final int slideRgnHeight = (int)(0.5f * mSlideRegionFrac * edgeToEdge);
            final float slideRgnHeightF = slideRgnHeight;
            final int slideEdgeTop = edgeTop + slideRgnHeight;
            final int slideEdgeBottom = edgeBottom - slideRgnHeight;

            if (mFloatViewMid < slideEdgeTop) {
                mFirstExpPos = itemPos - 1;
                mSecondExpPos = itemPos;
                mSlideFrac = 0.5f * (slideEdgeTop - mFloatViewMid) / slideRgnHeightF;
            } else if (mFloatViewMid < slideEdgeBottom) {
                mFirstExpPos = itemPos;
                mSecondExpPos = itemPos;
            } else {
                mFirstExpPos = itemPos;
                mSecondExpPos = itemPos + 1;
                mSlideFrac = 0.5f * (1.0f + (edgeBottom - mFloatViewMid) / slideRgnHeightF);
            }

        } else {
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        }

        if (mFirstExpPos < numHeaders) {
            itemPos = numHeaders;
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        } else if (mSecondExpPos >= getCount() - numFooters) {
            itemPos = getCount() - numFooters - 1;
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        }

        if (mFirstExpPos != oldFirstExpPos || mSecondExpPos != oldSecondExpPos
                || mSlideFrac != oldSlideFrac) {
            updated = true;
        }

        if (itemPos != mFloatPos) {
            if (mDragListener != null) {
                mDragListener.drag(mFloatPos - numHeaders, itemPos - numHeaders);
            }

            mFloatPos = itemPos;
            updated = true;
        }

        return updated;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (mTrackDragSort) {
            mDragSortTracker.appendState();
        }
    }

    public boolean stopDrag(final boolean remove) {
        if (mFloatView != null) {
            mDragState = STOPPED;

            dropFloatView(remove);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {

        if (!mDragEnabled) {
            return super.onTouchEvent(ev);
        }

        boolean more = false;

        final boolean lastCallWasIntercept = mLastCallWasIntercept;
        mLastCallWasIntercept = false;

        if (!lastCallWasIntercept) {
            saveTouchCoords(ev);
        }

        if (mFloatView != null) {
            onDragTouchEvent(ev);
            more = true;
        } else {

            if (mDragState != STOPPED) {
                if (super.onTouchEvent(ev)) {
                    more = true;
                }
            }

            final int action = ev.getAction() & MotionEvent.ACTION_MASK;
            switch (action) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    doActionUpOrCancel();
                    break;
                default:
                    if (more) {
                        mCancelMethod = ON_TOUCH_EVENT;
                    }
            }
        }

        return more;

    }

    private void doActionUpOrCancel() {
        mCancelMethod = NO_CANCEL;
        mInTouchEvent = false;
        mDragState = IDLE;
        mCurrFloatAlpha = mFloatAlpha;
    }

    private void saveTouchCoords(final MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action != MotionEvent.ACTION_DOWN) {
            mLastY = mY;
        }
        mX = (int)ev.getX();
        mY = (int)ev.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            mLastY = mY;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        if (!mDragEnabled) {
            return super.onInterceptTouchEvent(ev);
        }

        saveTouchCoords(ev);
        mLastCallWasIntercept = true;

        boolean intercept = false;

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            mInTouchEvent = true;
        }

        if (mFloatView != null) {
            intercept = true;
        } else {
            if (super.onInterceptTouchEvent(ev)) {
                intercept = true;
            }

            switch (action) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    doActionUpOrCancel();
                    break;
                default:
                    if (intercept) {
                        mCancelMethod = ON_TOUCH_EVENT;
                    } else {
                        mCancelMethod = ON_INTERCEPT_TOUCH_EVENT;
                    }
            }
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mInTouchEvent = false;
        }

        return intercept;
    }

    public void setDragScrollStart(final float heightFraction) {
        setDragScrollStarts(heightFraction, heightFraction);
    }

    public void setDragScrollStarts(final float upperFrac, final float lowerFrac) {
        if (lowerFrac > 0.5f) {
            mDragDownScrollStartFrac = 0.5f;
        } else {
            mDragDownScrollStartFrac = lowerFrac;
        }

        if (upperFrac > 0.5f) {
            mDragUpScrollStartFrac = 0.5f;
        } else {
            mDragUpScrollStartFrac = upperFrac;
        }

        if (getHeight() != 0) {
            updateScrollStarts();
        }
    }

    private void continueDrag(final int x, final int y) {
        dragView(x, y);
        requestLayout();
        final int minY = Math.min(y, mFloatViewMid + mFloatViewHeightHalf);
        final int maxY = Math.max(y, mFloatViewMid - mFloatViewHeightHalf);

        final int currentScrollDir = mDragScroller.getScrollDir();
        if (minY > mLastY && minY > mDownScrollStartY && currentScrollDir != DragScroller.DOWN) {
            if (currentScrollDir != DragScroller.STOP) {
                mDragScroller.stopScrolling(true);
            }
            mDragScroller.startScrolling(DragScroller.DOWN);
        } else if (maxY < mLastY && maxY < mUpScrollStartY && currentScrollDir != DragScroller.UP) {
            if (currentScrollDir != DragScroller.STOP) {
                mDragScroller.stopScrolling(true);
            }
            mDragScroller.startScrolling(DragScroller.UP);
        } else if (maxY >= mUpScrollStartY && minY <= mDownScrollStartY && mDragScroller.isScrolling()) {
            mDragScroller.stopScrolling(true);
        }
    }

    private void updateScrollStarts() {
        final int padTop = getPaddingTop();
        final int listHeight = getHeight() - padTop - getPaddingBottom();
        final float heightF = listHeight;
        mUpScrollStartYF = padTop + mDragUpScrollStartFrac * heightF;
        mDownScrollStartYF = padTop + (1.0f - mDragDownScrollStartFrac) * heightF;
        mUpScrollStartY = (int)mUpScrollStartYF;
        mDownScrollStartY = (int)mDownScrollStartYF;
        mDragUpScrollHeight = mUpScrollStartYF - padTop;
        mDragDownScrollHeight = padTop + listHeight - mDownScrollStartYF;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateScrollStarts();
    }

    private void dropFloatView(final boolean removeSrcItem) {

        mDragScroller.stopScrolling(true);

        if (removeSrcItem) {
            if (mRemoveListener != null) {
                mRemoveListener.remove(mSrcPos - getHeaderViewsCount());
            }
        } else {
            if (mDropListener != null && mFloatPos >= 0 && mFloatPos < getCount()) {
                final int numHeaders = getHeaderViewsCount();
                mDropListener.drop(mSrcPos - numHeaders, mFloatPos - numHeaders);
            }

            final int firstPos = getFirstVisiblePosition();
            if (mSrcPos < firstPos) {
                final View v = getChildAt(0);
                int top = 0;
                if (v != null) {
                    top = v.getTop();
                }
                setSelectionFromTop(firstPos - 1, top - getPaddingTop());
            }
        }

        mSrcPos = -1;
        mFirstExpPos = -1;
        mSecondExpPos = -1;
        mFloatPos = -1;
        removeFloatView();
        if (mTrackDragSort) {
            mDragSortTracker.stopTracking();
        }
    }

    private void adjustAllItems() {
        final int first = getFirstVisiblePosition();
        final int last = getLastVisiblePosition();

        final int begin = Math.max(0, getHeaderViewsCount() - first);
        final int end = Math.min(last - first, getCount() - 1 - getFooterViewsCount() - first);

        for (int i = begin; i <= end; ++i) {
            final View v = getChildAt(i);
            if (v != null) {
                adjustItem(first + i, v, false);
            }
        }
    }

    private void adjustItem(final int position, final View v, final boolean needsMeasure) {

        final ViewGroup.LayoutParams lp = v.getLayoutParams();
        final int oldHeight = lp.height;
        int height = oldHeight;

        getDividerHeight();

        final boolean isSliding = mAnimate && mFirstExpPos != mSecondExpPos;
        final int maxNonSrcBlankHeight = mFloatViewHeight - mItemHeightCollapsed;
        final int slideHeight = (int)(mSlideFrac * maxNonSrcBlankHeight);

        if (position == mSrcPos) {
            if (mSrcPos == mFirstExpPos) {
                if (isSliding) {
                    height = slideHeight + mItemHeightCollapsed;
                } else {
                    height = mFloatViewHeight;
                }
            } else if (mSrcPos == mSecondExpPos) {
                height = mFloatViewHeight - slideHeight;
            } else {
                height = mItemHeightCollapsed;
            }
        } else if (position == mFirstExpPos || position == mSecondExpPos) {

            final ItemHeights itemHeights = new ItemHeights();
            if (needsMeasure) {
                measureItemAndGetHeights(position, v, itemHeights);
            } else {
                getItemHeights(position, v, itemHeights);
            }

            if (position == mFirstExpPos) {
                if (isSliding) {
                    height = itemHeights.child + slideHeight;
                } else {
                    height = itemHeights.child + maxNonSrcBlankHeight;
                }
            } else {
                height = itemHeights.child + maxNonSrcBlankHeight - slideHeight;
            }
        } else {
            height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        if (height != oldHeight) {
            lp.height = height;

            v.setLayoutParams(lp);
        }

        if (position == mFirstExpPos || position == mSecondExpPos) {
            if (position < mSrcPos) {
                ((RelativeLayout)v).setGravity(Gravity.BOTTOM);
            } else if (position > mSrcPos) {
                ((RelativeLayout)v).setGravity(Gravity.TOP);
            }
        }

        final int oldVis = v.getVisibility();
        int vis = View.VISIBLE;

        if (position == mSrcPos && mFloatView != null) {
            vis = View.INVISIBLE;
        }

        if (vis != oldVis) {
            v.setVisibility(vis);
        }
    }

    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests) {
            super.requestLayout();
        }
    }

    private void doDragScroll(final int oldFirstExpPos, final int oldSecondExpPos) {
        if (mScrollY == 0) {
            return;
        }

        final int padTop = getPaddingTop();
        final int listHeight = getHeight() - padTop - getPaddingBottom();
        final int first = getFirstVisiblePosition();
        final int last = getLastVisiblePosition();

        int movePos;

        if (mScrollY >= 0) {
            mScrollY = Math.min(listHeight, mScrollY);
            movePos = first;
        } else {
            mScrollY = Math.max(-listHeight, mScrollY);
            movePos = last;
        }

        final View moveItem = getChildAt(movePos - first);
        int top = moveItem.getTop() + mScrollY;

        if (movePos == 0 && top > padTop) {
            top = padTop;
        }

        final ItemHeights itemHeightsBefore = new ItemHeights();
        getItemHeights(movePos, moveItem, itemHeightsBefore);
        final int moveHeightBefore = itemHeightsBefore.item;
        final int moveBlankBefore = moveHeightBefore - itemHeightsBefore.child;

        final ItemHeights itemHeightsAfter = new ItemHeights();
        measureItemAndGetHeights(movePos, moveItem, itemHeightsAfter);
        final int moveHeightAfter = itemHeightsAfter.item;
        final int moveBlankAfter = moveHeightAfter - itemHeightsAfter.child;

        if (movePos <= oldFirstExpPos) {
            if (movePos > mFirstExpPos) {
                top += mFloatViewHeight - moveBlankAfter;
            }
        } else if (movePos == oldSecondExpPos) {
            if (movePos <= mFirstExpPos) {
                top += moveBlankBefore - mFloatViewHeight;
            } else if (movePos == mSecondExpPos) {
                top += moveHeightBefore - moveHeightAfter;
            } else {
                top += moveBlankBefore;
            }
        } else {
            if (movePos <= mFirstExpPos) {
                top -= mFloatViewHeight;
            } else if (movePos == mSecondExpPos) {
                top -= moveBlankAfter;
            }
        }

        setSelectionFromTop(movePos, top - padTop);

        mScrollY = 0;
    }

    private void measureFloatView() {
        if (mFloatView != null) {
            ViewGroup.LayoutParams lp = mFloatView.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            final int wspec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, getListPaddingLeft()
                    + getListPaddingRight(), lp.width);
            int hspec;
            if (lp.height > 0) {
                hspec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            } else {
                hspec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }
            mFloatView.measure(wspec, hspec);
            mFloatViewHeight = mFloatView.getMeasuredHeight();
            mFloatViewHeightHalf = mFloatViewHeight / 2;
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mFloatView != null) {
            if (mFloatView.isLayoutRequested()) {
                measureFloatView();
            }
        }
        mWidthMeasureSpec = widthMeasureSpec;
    }

    @Override
    protected void layoutChildren() {

        if (mFloatView != null) {
            mFloatView.layout(0, 0, mFloatView.getMeasuredWidth(), mFloatView.getMeasuredHeight());

            final int oldFirstExpPos = mFirstExpPos;
            final int oldSecondExpPos = mSecondExpPos;

            mBlockLayoutRequests = true;

            if (updatePositions()) {
                adjustAllItems();
            }

            if (mScrollY != 0) {
                doDragScroll(oldFirstExpPos, oldSecondExpPos);
            }

            mBlockLayoutRequests = false;
        }

        super.layoutChildren();
    }

    protected boolean onDragTouchEvent(final MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                stopDrag(false);
                doActionUpOrCancel();
                break;
            case MotionEvent.ACTION_MOVE:
                continueDrag((int)ev.getX(), (int)ev.getY());
                break;
        }

        return true;
    }

    public boolean startDrag(final int position, final int dragFlags, final int deltaX,
            final int deltaY) {
        if (!mInTouchEvent || mFloatViewManager == null) {
            return false;
        }

        final View v = mFloatViewManager.onCreateFloatView(position);

        if (v == null) {
            return false;
        } else {
            return startDrag(position, v, dragFlags, deltaX, deltaY);
        }

    }

    public boolean startDrag(final int position, final View floatView, final int dragFlags,
            final int deltaX, final int deltaY) {
        if (!mInTouchEvent || mFloatView != null || floatView == null) {
            return false;
        }

        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        final int pos = position + getHeaderViewsCount();
        mFirstExpPos = pos;
        mSecondExpPos = pos;
        mSrcPos = pos;
        mFloatPos = pos;
        mDragState = DRAGGING;
        mDragFlags = 0;
        mDragFlags |= dragFlags;

        mFloatView = floatView;
        measureFloatView();

        mDragDeltaX = deltaX;
        mDragDeltaY = deltaY;
        updateFloatView(mX - mDragDeltaX, mY - mDragDeltaY);

        final View srcItem = getChildAt(mSrcPos - getFirstVisiblePosition());
        if (srcItem != null) {
            srcItem.setVisibility(View.INVISIBLE);
        }

        if (mTrackDragSort) {
            mDragSortTracker.startTracking();
        }

        switch (mCancelMethod) {
            case ON_TOUCH_EVENT:
                super.onTouchEvent(mCancelEvent);
                break;
            case ON_INTERCEPT_TOUCH_EVENT:
                super.onInterceptTouchEvent(mCancelEvent);
                break;
        }

        requestLayout();

        return true;
    }

    private void updateFloatView(final int floatX, final int floatY) {

        final int padLeft = getPaddingLeft();
        if ((mDragFlags & DRAG_POS_X) == 0 && floatX > padLeft) {
            mFloatViewLeft = padLeft;
        } else if ((mDragFlags & DRAG_NEG_X) == 0 && floatX < padLeft) {
            mFloatViewLeft = padLeft;
        } else {
            mFloatViewLeft = floatX;
        }

        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();
        final int firstPos = getFirstVisiblePosition();
        final int lastPos = getLastVisiblePosition();

        int topLimit = getPaddingTop();
        if (firstPos < numHeaders) {
            topLimit = getChildAt(numHeaders - firstPos - 1).getBottom();
        }
        if ((mDragFlags & DRAG_NEG_Y) == 0) {
            if (firstPos <= mSrcPos) {
                topLimit = Math.max(getChildAt(mSrcPos - firstPos).getTop(), topLimit);
            }
        }
        int bottomLimit = getHeight() - getPaddingBottom();
        if (lastPos >= getCount() - numFooters - 1) {
            bottomLimit = getChildAt(getCount() - numFooters - 1 - firstPos).getBottom();
        }
        if ((mDragFlags & DRAG_POS_Y) == 0) {
            if (lastPos >= mSrcPos) {
                bottomLimit = Math.min(getChildAt(mSrcPos - firstPos).getBottom(), bottomLimit);
            }
        }

        if (floatY < topLimit) {
            mFloatViewTop = topLimit;
        } else if (floatY + mFloatViewHeight > bottomLimit) {
            mFloatViewTop = bottomLimit - mFloatViewHeight;
        } else {
            mFloatViewTop = floatY;
        }

        mFloatViewMid = mFloatViewTop + mFloatViewHeightHalf;
    }

    private void dragView(final int x, final int y) {
        mFloatLoc.x = x - mDragDeltaX;
        mFloatLoc.y = y - mDragDeltaY;
        final Point touch = new Point(x, y);
        if (mFloatViewManager != null) {
            mFloatViewManager.onDragFloatView(mFloatView, mFloatLoc, touch);
        }
        updateFloatView(mFloatLoc.x, mFloatLoc.y);
    }

    private void removeFloatView() {
        if (mFloatView != null) {
            mFloatView.setVisibility(GONE);
            if (mFloatViewManager != null) {
                mFloatViewManager.onDestroyFloatView(mFloatView);
            }
            mFloatView = null;
        }
    }

    public interface FloatViewManager {

        public View onCreateFloatView(int position);

        public void onDragFloatView(View floatView, Point location, Point touch);

        public void onDestroyFloatView(View floatView);
    }

    public void setFloatViewManager(final FloatViewManager manager) {
        mFloatViewManager = manager;
    }

    public void setDragListener(final DragListener l) {
        mDragListener = l;
    }

    public void setDragEnabled(final boolean enabled) {
        mDragEnabled = enabled;
    }

    public boolean isDragEnabled() {
        return mDragEnabled;
    }

    public void setDropListener(final DropListener l) {
        mDropListener = l;
    }

    public void setRemoveListener(final RemoveListener l) {
        if (mController != null && l == null) {
            mController.setRemoveEnabled(false);
        }
        mRemoveListener = l;
    }

    public interface DragListener {
        public void drag(int from, int to);
    }

    public interface DropListener {
        public void drop(int from, int to);
    }

    public interface RemoveListener {
        public void remove(int which);
    }

    public interface DragSortListener extends DropListener, DragListener, RemoveListener {
    }

    public void setDragSortListener(final DragSortListener l) {
        setDropListener(l);
        setDragListener(l);
        setRemoveListener(l);
    }

    public void setDragScrollProfile(final DragScrollProfile ssp) {
        if (ssp != null) {
            mScrollProfile = ssp;
        }
    }

    public interface DragScrollProfile {
        float getSpeed(float w, long t);
    }

    private class DragScroller implements Runnable, AbsListView.OnScrollListener {

        private boolean mAbort;

        private long mPrevTime;

        private int dy;

        private float dt;

        private long tStart;

        private int scrollDir;

        public final static int STOP = -1;

        public final static int UP = 0;

        public final static int DOWN = 1;

        private float mScrollSpeed;

        private boolean mScrolling = false;

        public boolean isScrolling() {
            return mScrolling;
        }

        public int getScrollDir() {
            return mScrolling ? scrollDir : STOP;
        }

        public DragScroller() {
        }

        public void startScrolling(final int dir) {
            if (!mScrolling) {
                mAbort = false;
                mScrolling = true;
                tStart = SystemClock.uptimeMillis();
                mPrevTime = tStart;
                scrollDir = dir;
                post(this);
            }
        }

        public void stopScrolling(final boolean now) {
            if (now) {
                removeCallbacks(this);
                mScrolling = false;
            } else {
                mAbort = true;
            }

        }

        @Override
        public void run() {
            if (mAbort) {
                mScrolling = false;
                return;
            }

            final int first = getFirstVisiblePosition();
            final int last = getLastVisiblePosition();
            final int count = getCount();
            final int padTop = getPaddingTop();
            final int listHeight = getHeight() - padTop - getPaddingBottom();

            final int minY = Math.min(mY, mFloatViewMid + mFloatViewHeightHalf);
            final int maxY = Math.max(mY, mFloatViewMid - mFloatViewHeightHalf);

            if (scrollDir == UP) {
                final View v = getChildAt(0);
                if (v == null) {
                    mScrolling = false;
                    return;
                } else {
                    if (first == 0 && v.getTop() == padTop) {
                        mScrolling = false;
                        return;
                    }
                }
                mScrollSpeed = mScrollProfile.getSpeed((mUpScrollStartYF - maxY)
                        / mDragUpScrollHeight, mPrevTime);
            } else {
                final View v = getChildAt(last - first);
                if (v == null) {
                    mScrolling = false;
                    return;
                } else {
                    if (last == count - 1 && v.getBottom() <= listHeight + padTop) {
                        mScrolling = false;
                        return;
                    }
                }
                mScrollSpeed = -mScrollProfile.getSpeed((minY - mDownScrollStartYF)
                        / mDragDownScrollHeight, mPrevTime);
            }

            dt = SystemClock.uptimeMillis() - mPrevTime;
            dy = Math.round(mScrollSpeed * dt);
            mScrollY += dy;

            requestLayout();

            mPrevTime += dt;

            post(this);
        }

        @Override
        public void onScroll(final AbsListView view, final int firstVisibleItem,
                final int visibleItemCount, final int totalItemCount) {
            if (mScrolling && visibleItemCount != 0) {
                dragView(mX, mY);
            }
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        }

    }

    private class DragSortTracker {
        StringBuilder mBuilder = new StringBuilder();

        File mFile;

        private int mNumInBuffer = 0;

        private int mNumFlushes = 0;

        private boolean mTracking = false;

        public void startTracking() {
            mBuilder.append("<DSLVStates>\n");
            mNumFlushes = 0;
            mTracking = true;
        }

        public void appendState() {
            if (!mTracking) {
                return;
            }

            mBuilder.append("<DSLVState>\n");
            final int children = getChildCount();
            final int first = getFirstVisiblePosition();
            final ItemHeights itemHeights = new ItemHeights();
            mBuilder.append("    <Positions>");
            for (int i = 0; i < children; ++i) {
                mBuilder.append(first + i).append(",");
            }
            mBuilder.append("</Positions>\n");

            mBuilder.append("    <Tops>");
            for (int i = 0; i < children; ++i) {
                mBuilder.append(getChildAt(i).getTop()).append(",");
            }
            mBuilder.append("</Tops>\n");
            mBuilder.append("    <Bottoms>");
            for (int i = 0; i < children; ++i) {
                mBuilder.append(getChildAt(i).getBottom()).append(",");
            }
            mBuilder.append("</Bottoms>\n");

            mBuilder.append("    <FirstExpPos>").append(mFirstExpPos).append("</FirstExpPos>\n");
            getItemHeights(mFirstExpPos, itemHeights);
            mBuilder.append("    <FirstExpBlankHeight>")
                    .append(itemHeights.item - itemHeights.child)
                    .append("</FirstExpBlankHeight>\n");
            mBuilder.append("    <SecondExpPos>").append(mSecondExpPos).append("</SecondExpPos>\n");
            getItemHeights(mSecondExpPos, itemHeights);
            mBuilder.append("    <SecondExpBlankHeight>")
                    .append(itemHeights.item - itemHeights.child)
                    .append("</SecondExpBlankHeight>\n");
            mBuilder.append("    <SrcPos>").append(mSrcPos).append("</SrcPos>\n");
            mBuilder.append("    <SrcHeight>").append(mFloatViewHeight + getDividerHeight())
                    .append("</SrcHeight>\n");
            mBuilder.append("    <ViewHeight>").append(getHeight()).append("</ViewHeight>\n");
            mBuilder.append("    <LastY>").append(mLastY).append("</LastY>\n");
            mBuilder.append("    <FloatY>").append(mFloatViewMid).append("</FloatY>\n");
            mBuilder.append("    <ShuffleEdges>");
            for (int i = 0; i < children; ++i) {
                mBuilder.append(getShuffleEdge(first + i, getChildAt(i).getTop())).append(",");
            }
            mBuilder.append("</ShuffleEdges>\n");

            mBuilder.append("</DSLVState>\n");
            mNumInBuffer++;

            if (mNumInBuffer > 1000) {
                flush();
                mNumInBuffer = 0;
            }
        }

        public void flush() {
            if (!mTracking) {
                return;
            }

            try {
                boolean append = true;
                if (mNumFlushes == 0) {
                    append = false;
                }
                final FileWriter writer = new FileWriter(mFile, append);

                writer.write(mBuilder.toString());
                mBuilder.delete(0, mBuilder.length());

                writer.flush();
                writer.close();

                mNumFlushes++;
            } catch (final IOException e) {
            }
        }

        public void stopTracking() {
            if (mTracking) {
                mBuilder.append("</DSLVStates>\n");
                flush();
                mTracking = false;
            }
        }

    }

}
