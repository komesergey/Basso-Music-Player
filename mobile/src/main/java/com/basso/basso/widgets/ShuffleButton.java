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


public class ShuffleButton extends ImageButton implements OnClickListener, OnLongClickListener {

    private static final String SHUFFLE = "btn_playback_shuffle";

    private static final String SHUFFLE_ALL = "btn_playback_shuffle_all";

    private final ThemeUtils mResources;

    @SuppressWarnings("deprecation")
    public ShuffleButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mResources = new ThemeUtils(context);
        setBackgroundDrawable(new HoloSelector(context));
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        MusicUtils.cycleShuffle();
        updateShuffleState();
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

    public void updateShuffleState() {
        switch (MusicUtils.getShuffleMode()) {
            case MusicPlaybackService.SHUFFLE_NORMAL:
                setContentDescription(getResources().getString(R.string.accessibility_shuffle_all));
                setImageDrawable(mResources.getDrawable(SHUFFLE_ALL));
                break;
            case MusicPlaybackService.SHUFFLE_AUTO:
                setContentDescription(getResources().getString(R.string.accessibility_shuffle_all));
                setImageDrawable(mResources.getDrawable(SHUFFLE_ALL));
                break;
            case MusicPlaybackService.SHUFFLE_NONE:
                setContentDescription(getResources().getString(R.string.accessibility_shuffle));
                setImageDrawable(mResources.getDrawable(SHUFFLE));
                break;
            default:
                break;
        }
    }
}
