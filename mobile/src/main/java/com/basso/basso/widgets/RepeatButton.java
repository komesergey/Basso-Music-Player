package com.basso.basso.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;

import com.basso.basso.MusicPlaybackService;
import com.basso.basso.R;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.ThemeUtils;
import com.basso.basso.widgets.theme.HoloSelector;

public class RepeatButton extends ImageButton implements OnClickListener, OnLongClickListener {

    private static final String REPEAT_ALL = "btn_playback_repeat_all";

    private static final String REPEAT_CURRENT = "btn_playback_repeat_one";

    private static final String REPEAT_NONE = "btn_playback_repeat";

    private final ThemeUtils mResources;

    @SuppressWarnings("deprecation")
    public RepeatButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mResources = new ThemeUtils(context);
        setBackgroundDrawable(new HoloSelector(context));
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        MusicUtils.cycleRepeat();
        updateRepeatState();
    }

    @Override
    public boolean onLongClick(final View view) {
        if (TextUtils.isEmpty(view.getContentDescription())) {
            return false;
        } else {
            BassoUtils.showCheatSheet(view);
            return true;
        }
    }

    public void updateRepeatState() {
        switch (MusicUtils.getRepeatMode()) {
            case MusicPlaybackService.REPEAT_ALL:
                setContentDescription(getResources().getString(R.string.accessibility_repeat_all));
                setImageDrawable(mResources.getDrawable(REPEAT_ALL));
                break;
            case MusicPlaybackService.REPEAT_CURRENT:
                setContentDescription(getResources().getString(R.string.accessibility_repeat_one));
                setImageDrawable(mResources.getDrawable(REPEAT_CURRENT));
                break;
            case MusicPlaybackService.REPEAT_NONE:
                setContentDescription(getResources().getString(R.string.accessibility_repeat));
                setImageDrawable(mResources.getDrawable(REPEAT_NONE));
                break;
            default:
                break;
        }
    }
}
