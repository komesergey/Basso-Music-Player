package com.basso.basso.widgets;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.basso.basso.R;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.ThemeUtils;
import com.basso.basso.widgets.theme.HoloSelector;

public class RepeatingImageButton extends ImageButton implements OnClickListener {

    private static final String NEXT = "btn_playback_next";

    private static final String PREVIOUS = "btn_playback_previous";

    private static final long sInterval = 400;

    private final ThemeUtils mResources;

    private long mStartTime;

    private int mRepeatCount;

    private RepeatListener mListener;

    @SuppressWarnings("deprecation")
    public RepeatingImageButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mResources = new ThemeUtils(context);
        setBackgroundDrawable(new HoloSelector(context));
        setFocusable(true);
        setLongClickable(true);
        setOnClickListener(this);
        updateState();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.action_button_previous_panel:
                MusicUtils.previous(getContext());
                break;
            case R.id.action_button_previous:
                MusicUtils.previous(getContext());
                break;
            case R.id.action_button_next_panel:
                MusicUtils.next();
                break;
            case R.id.action_button_next:
                MusicUtils.next();
            default:
                break;
        }
    }

    public void setRepeatListener(final RepeatListener l) {
        mListener = l;
    }

    @Override
    public boolean performLongClick() {
        if (mListener == null) {
            BassoUtils.showCheatSheet(this);
        }
        mStartTime = SystemClock.elapsedRealtime();
        mRepeatCount = 0;
        post(mRepeater);
        return true;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            removeCallbacks(mRepeater);
            if (mStartTime != 0) {
                doRepeat(true);
                mStartTime = 0;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                super.onKeyDown(keyCode, event);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                removeCallbacks(mRepeater);
                if (mStartTime != 0) {
                    doRepeat(true);
                    mStartTime = 0;
                }
        }
        return super.onKeyUp(keyCode, event);
    }

    private final Runnable mRepeater = new Runnable() {
        @Override
        public void run() {
            doRepeat(false);
            if (isPressed()) {
                postDelayed(this, sInterval);
            }
        }
    };

    private void doRepeat(final boolean shouldRepeat) {
        final long now = SystemClock.elapsedRealtime();
        if (mListener != null) {
            mListener.onRepeat(this, now - mStartTime, shouldRepeat ? -1 : mRepeatCount++);
        }
    }

    public void updateState() {
        switch (getId()) {
            case R.id.action_button_next:
                setImageDrawable(mResources.getDrawable(NEXT));
                break;
            case R.id.action_button_previous:
                setImageDrawable(mResources.getDrawable(PREVIOUS));
                break;
            default:
                break;
        }
    }

    public interface RepeatListener {
        void onRepeat(View v, long duration, int repeatcount);
    }
}
