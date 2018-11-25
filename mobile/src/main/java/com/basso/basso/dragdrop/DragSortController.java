package com.basso.basso.dragdrop;

import android.graphics.Point;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;

public class DragSortController extends SimpleFloatViewManager implements View.OnTouchListener,
        GestureDetector.OnGestureListener {

    public final static int ON_DOWN = 0;

    public final static int ON_DRAG = 1;

    public final static int ON_LONG_PRESS = 2;

    public final static int FLING_RIGHT_REMOVE = 0;

    public final static int FLING_LEFT_REMOVE = 1;

    public final static int SLIDE_RIGHT_REMOVE = 2;

    public final static int SLIDE_LEFT_REMOVE = 3;

    public final static int MISS = -1;

    private final GestureDetector mDetector;

    private final GestureDetector mFlingRemoveDetector;

    private final int mTouchSlop;

    private final int[] mTempLoc = new int[2];

    private final float mFlingSpeed = 500f;

    private final DragSortListView mDslv;

    private boolean mSortEnabled = true;

    private boolean mRemoveEnabled = false;

    private boolean mDragging = false;

    private int mDragInitMode = ON_DOWN;

    private int mRemoveMode;

    private int mHitPos = MISS;

    private int mItemX;

    private int mItemY;

    private int mCurrX;

    private int mCurrY;

    private int mDragHandleId;

    private float mOrigFloatAlpha = 1.0f;

    public DragSortController(DragSortListView dslv) {
        this(dslv, 0, ON_DOWN, FLING_RIGHT_REMOVE);
    }

    public DragSortController(DragSortListView dslv, int dragHandleId, int dragInitMode,
            int removeMode) {
        super(dslv);
        mDslv = dslv;
        mDetector = new GestureDetector(dslv.getContext(), this);
        mFlingRemoveDetector = new GestureDetector(dslv.getContext(), mFlingRemoveListener);
        mFlingRemoveDetector.setIsLongpressEnabled(false);
        mTouchSlop = ViewConfiguration.get(dslv.getContext()).getScaledTouchSlop();
        mDragHandleId = dragHandleId;
        setRemoveMode(removeMode);
        setDragInitMode(dragInitMode);
        mOrigFloatAlpha = dslv.getFloatAlpha();
    }

    public int getDragInitMode() {
        return mDragInitMode;
    }

    public void setDragInitMode(int mode) {
        mDragInitMode = mode;
    }

    public void setSortEnabled(boolean enabled) {
        mSortEnabled = enabled;
    }

    public boolean isSortEnabled() {
        return mSortEnabled;
    }

    public void setRemoveMode(int mode) {
        mRemoveMode = mode;
    }

    public int getRemoveMode() {
        return mRemoveMode;
    }

    public void setRemoveEnabled(boolean enabled) {
        mRemoveEnabled = enabled;
    }

    public boolean isRemoveEnabled() {
        return mRemoveEnabled;
    }

    public void setDragHandleId(int id) {
        mDragHandleId = id;
    }

    public boolean startDrag(int position, int deltaX, int deltaY) {

        int mDragFlags = 0;
        if (mSortEnabled) {
            mDragFlags |= DragSortListView.DRAG_POS_Y | DragSortListView.DRAG_NEG_Y;
        }

        if (mRemoveEnabled) {
            if (mRemoveMode == FLING_RIGHT_REMOVE) {
                mDragFlags |= DragSortListView.DRAG_POS_X;
            } else if (mRemoveMode == FLING_LEFT_REMOVE) {
                mDragFlags |= DragSortListView.DRAG_NEG_X;
            }
        }

        mDragging = mDslv.startDrag(position - mDslv.getHeaderViewsCount(), mDragFlags, deltaX,
                deltaY);
        return mDragging;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        if (mRemoveEnabled && mDragging
                && (mRemoveMode == FLING_RIGHT_REMOVE || mRemoveMode == FLING_LEFT_REMOVE)) {
            mFlingRemoveDetector.onTouchEvent(ev);
        }

        final int mAction = ev.getAction() & MotionEvent.ACTION_MASK;

        switch (mAction) {
            case MotionEvent.ACTION_DOWN:
                mCurrX = (int)ev.getX();
                mCurrY = (int)ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (mRemoveEnabled) {
                    final int x = (int)ev.getX();
                    int thirdW = mDslv.getWidth() / 3;
                    int twoThirdW = mDslv.getWidth() - thirdW;
                    if ((mRemoveMode == SLIDE_RIGHT_REMOVE && x > twoThirdW)
                            || (mRemoveMode == SLIDE_LEFT_REMOVE && x < thirdW)) {
                        mDslv.stopDrag(true);
                    }
                }
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                break;
        }
        return false;
    }

    @Override
    public void onDragFloatView(View floatView, Point position, Point touch) {

        if (mRemoveEnabled) {
            int x = touch.x;

            if (mRemoveMode == SLIDE_RIGHT_REMOVE) {
                int width = mDslv.getWidth();
                int thirdWidth = width / 3;

                float alpha;
                if (x < thirdWidth) {
                    alpha = 1.0f;
                } else if (x < width - thirdWidth) {
                    alpha = ((float)(width - thirdWidth - x)) / ((float)thirdWidth);
                } else {
                    alpha = 0.0f;
                }
                mDslv.setFloatAlpha(mOrigFloatAlpha * alpha);
            } else if (mRemoveMode == SLIDE_LEFT_REMOVE) {
                int width = mDslv.getWidth();
                int thirdWidth = width / 3;

                float alpha;
                if (x < thirdWidth) {
                    alpha = 0.0f;
                } else if (x < width - thirdWidth) {
                    alpha = ((float)(x - thirdWidth)) / ((float)thirdWidth);
                } else {
                    alpha = 1.0f;
                }
                mDslv.setFloatAlpha(mOrigFloatAlpha * alpha);
            }
        }
    }

    public int startDragPosition(MotionEvent ev) {
        return dragHandleHitPosition(ev);
    }

    public int dragHandleHitPosition(MotionEvent ev) {
        final int x = (int)ev.getX();
        final int y = (int)ev.getY();

        int touchPos = mDslv.pointToPosition(x, y);

        final int numHeaders = mDslv.getHeaderViewsCount();
        final int numFooters = mDslv.getFooterViewsCount();
        final int count = mDslv.getCount();

        if (touchPos != AdapterView.INVALID_POSITION && touchPos >= numHeaders
                && touchPos < (count - numFooters)) {
            final View item = mDslv.getChildAt(touchPos - mDslv.getFirstVisiblePosition());
            final int rawX = (int)ev.getRawX();
            final int rawY = (int)ev.getRawY();

            View dragBox = item.findViewById(mDragHandleId);
            if (dragBox != null) {
                dragBox.getLocationOnScreen(mTempLoc);

                if (rawX > mTempLoc[0] && rawY > mTempLoc[1]
                        && rawX < mTempLoc[0] + dragBox.getWidth()
                        && rawY < mTempLoc[1] + dragBox.getHeight()) {

                    mItemX = item.getLeft();
                    mItemY = item.getTop();

                    return touchPos;
                }
            }
        }
        return MISS;
    }

    @Override
    public boolean onDown(MotionEvent ev) {
        mHitPos = startDragPosition(ev);

        if (mHitPos != MISS && mDragInitMode == ON_DOWN) {
            startDrag(mHitPos, (int)ev.getX() - mItemX, (int)ev.getY() - mItemY);
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mHitPos != MISS && mDragInitMode == ON_DRAG && !mDragging) {
            final int x1 = (int)e1.getX();
            final int y1 = (int)e1.getY();
            final int x2 = (int)e2.getX();
            final int y2 = (int)e2.getY();

            boolean start = false;
            if (mRemoveEnabled && mSortEnabled) {
                start = true;
            } else if (mRemoveEnabled) {
                start = Math.abs(x2 - x1) > mTouchSlop;
            } else if (mSortEnabled) {
                start = Math.abs(y2 - y1) > mTouchSlop;
            }

            if (start) {
                startDrag(mHitPos, x2 - mItemX, y2 - mItemY);
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mHitPos != MISS && mDragInitMode == ON_LONG_PRESS) {
            mDslv.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            startDrag(mHitPos, mCurrX - mItemX, mCurrY - mItemY);
        }
    }

    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent ev) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent ev) {
    }

    private final GestureDetector.OnGestureListener mFlingRemoveListener = new GestureDetector.SimpleOnGestureListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            if (mRemoveEnabled) {
                switch (mRemoveMode) {
                    case FLING_RIGHT_REMOVE:
                        if (velocityX > mFlingSpeed) {
                            mDslv.stopDrag(true);
                        }
                        break;
                    case FLING_LEFT_REMOVE:
                        if (velocityX < -mFlingSpeed) {
                            mDslv.stopDrag(true);
                        }
                        break;
                }
            }
            return false;
        }
    };
}