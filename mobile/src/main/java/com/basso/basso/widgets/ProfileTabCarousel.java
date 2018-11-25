package com.basso.basso.widgets;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.basso.basso.R;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.cache.ImageWorker;
import com.basso.basso.ui.activities.ProfileActivity;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;


@SuppressLint("NewApi")
public class ProfileTabCarousel extends HorizontalScrollView implements OnTouchListener {

    private static final int TAB_COUNT = 3;

    private static final int TAB_INDEX_FIRST = 0;

    private static final int TAB_INDEX_SECOND = 1;

    private static final int TAB_INDEX_THIRD =  2;

    private static final float MAX_ALPHA = 0.6f;

    private static final float[] mYCoordinateArray = new float[TAB_COUNT];

    private final float tabWidthScreenWidthFraction;

    private final float tabHeightScreenWidthFraction;

    private final int mTabDisplayLabelHeight;

    private final int mTabShadowHeight;

    private int tabHeight;

    private final TabClickListener mTabOneTouchInterceptListener = new TabClickListener(TAB_INDEX_FIRST);

    private final TabClickListener mTabTwoTouchInterceptListener = new TabClickListener(TAB_INDEX_SECOND);

    private final TabClickListener mTabThreeTouchInterceptListener = new TabClickListener(TAB_INDEX_THIRD);

    private int mLastScrollPosition = Integer.MIN_VALUE;

    private int mAllowedHorizontalScrollLength = Integer.MIN_VALUE;

    private int mAllowedVerticalScrollLength = Integer.MIN_VALUE;

    private int actionBarHeight;

    private int mCurrentTab = TAB_INDEX_FIRST;

    private ColorDrawable newColor;

    private float mScrollScaleFactor = 1.0f;

    private boolean mScrollToCurrentTab = false;

    private boolean mTabCarouselIsAnimating;

    private boolean mEnableSwipe;

    private ImageFetcher mImageFetcher;

    private ImageView mHeaderPicture;

    private ImageView mHeaderFilter;

    private ImageView mBlueFilter;

    private CarouselTab mFirstTab;

    private CarouselTab mSecondTab;

    private CarouselTab mThirdTab;

    private Listener mListener;

    public ProfileTabCarousel(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        final Resources mResources = context.getResources();
        this.newColor = new ColorDrawable(getResources().getColor(R.color.action_bar_background));
        this.newColor.setAlpha(0);
        this.setBackgroundColor(newColor.getColor());
        mTabDisplayLabelHeight = mResources
                .getDimensionPixelSize(R.dimen.profile_photo_shadow_height);
        mTabShadowHeight = mResources.getDimensionPixelSize(R.dimen.profile_carousel_label_height);
        tabWidthScreenWidthFraction = mResources.getFraction(R.fraction.tab_width_screen_percentage, 1, 1);
        tabHeightScreenWidthFraction = mResources.getFraction(R.fraction.tab_height_screen_percentage, 1, 1);
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        mImageFetcher = BassoUtils.getImageFetcher((Activity)context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderPicture = (ImageView)findViewById(R.id.header_picture);
        mHeaderFilter = (ImageView)findViewById(R.id.header_filter);
        mBlueFilter = (ImageView)findViewById(R.id.blue_filter);
        mHeaderFilter.setBackgroundColor(getResources().getColor(R.color.transparent_glass));
        mFirstTab = (CarouselTab)findViewById(R.id.profile_tab_carousel_tab_one);
        mFirstTab.setOverlayOnClickListener(mTabOneTouchInterceptListener);
        mSecondTab = (CarouselTab)findViewById(R.id.profile_tab_carousel_tab_two);
        mSecondTab.setOverlayOnClickListener(mTabTwoTouchInterceptListener);
        mThirdTab = (CarouselTab)findViewById(R.id.profile_tab_carousel_tab_three);
        mThirdTab.setOverlayOnClickListener(mTabThreeTouchInterceptListener);
    }

    public int getTabHeight(){
        return tabHeight;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int tabWidth = Math.round(tabWidthScreenWidthFraction * screenWidth);

        mAllowedHorizontalScrollLength = tabWidth * TAB_COUNT - screenWidth;
        if (mAllowedHorizontalScrollLength == 0) {
            mScrollScaleFactor = 1.0f;
        } else {
            mScrollScaleFactor = screenWidth / mAllowedHorizontalScrollLength;
        }

        tabHeight = Math.round(screenWidth * tabHeightScreenWidthFraction) + mTabShadowHeight;
        if (getChildCount() > 0) {
            final View child = getChildAt(0);

            final int seperatorPixels = (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()) + 0.5f);

            if (mEnableSwipe) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(TAB_COUNT * tabWidth + (TAB_COUNT - 1)
                                * seperatorPixels, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(tabHeight, MeasureSpec.EXACTLY));
            } else {
                child.measure(MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(tabHeight, MeasureSpec.EXACTLY));
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAllowedVerticalScrollLength = tabHeight - mTabDisplayLabelHeight - mTabShadowHeight - actionBarHeight - getStatusBarHeight();
        }else {
            mAllowedVerticalScrollLength = tabHeight - mTabDisplayLabelHeight - mTabShadowHeight - actionBarHeight;
        }
        setMeasuredDimension(resolveSize(screenWidth, widthMeasureSpec), resolveSize(tabHeight, heightMeasureSpec));
    }
    public int getStatusBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
            final int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mScrollToCurrentTab) {
            return;
        }
        mScrollToCurrentTab = false;
        BassoUtils.doAfterLayout(this, new Runnable() {
            @Override
            public void run() {
                scrollTo(mCurrentTab == TAB_INDEX_FIRST ? 0 : mAllowedHorizontalScrollLength, 0);
            }
        });
    }
    public int getBackgroundAlpha(){
        return newColor.getAlpha();
    }
    public void setBackgroundAlpha(int backgroundAlpha){
        this.newColor.setAlpha(backgroundAlpha);
        this.mBlueFilter.setBackgroundColor(newColor.getColor());
    }

    @Override
    protected void onScrollChanged(final int x, final int y, final int oldX, final int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        if (mLastScrollPosition == x) {
            return;
        }
        final int scaledL = (int)(x * mScrollScaleFactor);
        final int oldScaledL = (int)(oldX * mScrollScaleFactor);
        mListener.onScrollChanged(scaledL, y, oldScaledL, oldY);
        mLastScrollPosition = x;
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mListener.onTouchDown();
                return true;
            case MotionEvent.ACTION_UP:
                mListener.onTouchUp();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        final boolean mInterceptTouch = super.onInterceptTouchEvent(ev);
        if (mInterceptTouch) {
            mListener.onTouchDown();
        }
        return mInterceptTouch;
    }

    public void reset() {
        scrollTo(0, 0);
        setCurrentTab(TAB_INDEX_FIRST);
        moveToYCoordinate(TAB_INDEX_FIRST, 0);
    }

    public void restoreCurrentTab(final int position) {
        setCurrentTab(position);
        mScrollToCurrentTab = true;
    }

    public void restoreYCoordinate(final int duration, final int tabIndex) {
        final float storedYCoordinate = getStoredYCoordinateForTab(tabIndex);
        final ObjectAnimator tabCarouselAnimator = ObjectAnimator.ofFloat(this, "y", storedYCoordinate);
        float ratio = storedYCoordinate/-this.getAllowedVerticalScrollLength();
        final ObjectAnimator alphaTabCarouselAnimator = ObjectAnimator.ofInt(this,"backgroundAlpha",getBackgroundAlpha(),(int)(ratio*255));
        tabCarouselAnimator.addListener(mTabCarouselAnimatorListener);
        alphaTabCarouselAnimator.addListener(mTabCarouselAnimatorListener);
        tabCarouselAnimator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R.anim.accelerate_decelerate_interpolator));
        alphaTabCarouselAnimator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R.anim.accelerate_decelerate_interpolator));
        tabCarouselAnimator.setDuration(duration);
        alphaTabCarouselAnimator.setDuration(duration);
        tabCarouselAnimator.start();
        alphaTabCarouselAnimator.start();
    }

    public void moveToYCoordinate(final int tabIndex, final float y) {
        storeYCoordinate(tabIndex, y);
        restoreYCoordinate(0, tabIndex);
    }

    public void storeYCoordinate(final int tabIndex, final float y) {
        mYCoordinateArray[tabIndex] = y;
    }

    public float getStoredYCoordinateForTab(final int tabIndex) {
        return mYCoordinateArray[tabIndex];
    }

    public int getAllowedHorizontalScrollLength() {
        return mAllowedHorizontalScrollLength;
    }

    public int getAllowedVerticalScrollLength() {
        return mAllowedVerticalScrollLength;
    }

    private void updateAlphaLayers() {
        float alpha = mLastScrollPosition * MAX_ALPHA / mAllowedHorizontalScrollLength;
        alpha = AlphaTouchInterceptorOverlay.clamp(alpha, 0.0f, 1.0f);
        mFirstTab.setAlphaLayerValue(alpha);
        mSecondTab.setAlphaLayerValue(MAX_ALPHA - alpha);
        mThirdTab.setAlphaLayerValue(MAX_ALPHA - alpha);
    }

    public ImageView getPhoto() {
        return mFirstTab.getPhoto();
    }

    public ImageView getAlbumArt() {
        return mFirstTab.getAlbumArt();
    }

    private final AnimatorListener mTabCarouselAnimatorListener = new AnimatorListener() {

        @Override
        public void onAnimationCancel(final Animator animation) {
            mTabCarouselIsAnimating = false;
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            mTabCarouselIsAnimating = false;
        }

        @Override
        public void onAnimationRepeat(final Animator animation) {
            mTabCarouselIsAnimating = true;
        }

        @Override
        public void onAnimationStart(final Animator animation) {
            mTabCarouselIsAnimating = true;
        }
    };

    public boolean isTabCarouselIsAnimating() {
        return mTabCarouselIsAnimating;
    }

    public void setCurrentTab(final int position) {
        final CarouselTab selected, deselectedOne, deselectedTwo;

        switch (position) {
            case TAB_INDEX_FIRST:
                selected = mFirstTab;
                deselectedOne = mSecondTab;
                deselectedTwo = mThirdTab;
                break;
            case TAB_INDEX_SECOND:
                selected = mSecondTab;
                deselectedOne = mFirstTab;
                deselectedTwo = mThirdTab;
                break;
            case TAB_INDEX_THIRD:
                selected = mThirdTab;
                deselectedOne = mFirstTab;
                deselectedTwo = mSecondTab;
                break;
            default:
                throw new IllegalStateException("Invalid tab position " + position);
        }
        selected.setSelected(true);
        selected.showSelectedState();
        selected.setOverlayClickable(false);
        deselectedOne.setSelected(false);
        deselectedOne.showDeselectedState();
        deselectedOne.setOverlayClickable(true);
        deselectedTwo.setSelected(false);
        deselectedTwo.showDeselectedState();
        deselectedTwo.setOverlayClickable(true);
        mCurrentTab = position;
    }

    public void setListener(final Listener listener) {
        mListener = listener;
    }

    public void setArtistProfileHeader(final Activity context, final String artistName) {
        mFirstTab.setLabel(getResources().getString(R.string.information));
        mSecondTab.setLabel(getResources().getString(R.string.page_songs));
        mThirdTab.setLabel(getResources().getString(R.string.page_albums));
        mImageFetcher.loadArtistImage(artistName, mHeaderPicture, ImageWorker.ImageSource.OTHER);
        mEnableSwipe = true;
    }

    public void setAlbumProfileHeader(final Activity context, final String albumName, final String artistName) {
        mFirstTab.setLabel(getResources().getString(R.string.information));
        mSecondTab.setLabel(getResources().getString(R.string.page_songs));
        mImageFetcher.loadAlbumImage(artistName, albumName, MusicUtils.getIdForAlbum(context, albumName, artistName), mHeaderPicture, ImageWorker.ImageSource.OTHER);
        mThirdTab.setVisibility(View.GONE);
        mEnableSwipe = true;
    }

    public void setPlaylistOrGenreProfileHeader(final Activity context, final String profileName) {
        mFirstTab.setDefault(context);
        mFirstTab.setLabel(getResources().getString(R.string.page_songs));
        mFirstTab.setPlaylistOrGenrePhoto(context, profileName);
        mSecondTab.setVisibility(View.GONE);
        mThirdTab.setVisibility(View.GONE);
        mHeaderPicture.setImageDrawable(context.getResources().getDrawable(R.drawable.header_temp));
        mEnableSwipe = false;
    }

    public void fetchAlbumPhoto(final Activity context, final String albumName, final String artistName) {
        mFirstTab.fetchAlbumPhoto(context, albumName, artistName);
    }

    public ImageView getHeaderPhoto() {
        return mFirstTab.getPhoto();
    }

    private final class TabClickListener implements OnClickListener {
        private final int mTab;

        public TabClickListener(final int tab) {
            super();
            mTab = tab;
        }

        @Override
        public void onClick(final View v) {
            mListener.onTabSelected(mTab);
        }
    }

    public interface Listener {
        public void onTouchDown();
        public void onTouchUp();
        public void onScrollChanged(int l, int t, int oldl, int oldt);
        public void onTabSelected(int position);
    }
}
