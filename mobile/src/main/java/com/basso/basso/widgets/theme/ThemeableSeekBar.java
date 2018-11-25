package com.basso.basso.widgets.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.basso.basso.utils.ThemeUtils;

public class ThemeableSeekBar extends SeekBar {

    public static final String PROGESS = "audio_player_seekbar";

    public ThemeableSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final ThemeUtils resources = new ThemeUtils(context);
        setProgressDrawable(resources.getDrawable(PROGESS));
    }
}
