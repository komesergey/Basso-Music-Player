package com.basso.basso.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;

import com.basso.basso.R;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.ThemeUtils;
import com.basso.basso.widgets.theme.HoloSelector;

public class PlayPauseButton extends ImageButton implements OnClickListener, OnLongClickListener {

    private static final String PLAY = "btn_playback_play";

    private static final String PAUSE = "btn_playback_pause";

    private final ThemeUtils mResources;

    @SuppressWarnings("deprecation")
    public PlayPauseButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mResources = new ThemeUtils(context);
        setBackgroundDrawable(new HoloSelector(context));
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        MusicUtils.playOrPause();
        updateState();
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

    public void updateState() {
        if (MusicUtils.isPlaying()) {
            setContentDescription(getResources().getString(R.string.accessibility_pause));
            setImageDrawable(mResources.getDrawable(PAUSE));
        } else {
            setContentDescription(getResources().getString(R.string.accessibility_play));
            setImageDrawable(mResources.getDrawable(PLAY));
        }
    }
}