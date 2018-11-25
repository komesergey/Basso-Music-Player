package com.basso.basso.widgets.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.basso.basso.R;
import com.basso.basso.utils.ThemeUtils;

@SuppressWarnings("deprecation")
public class BottomActionBar extends RelativeLayout {

    private static final String BOTTOM_ACTION_BAR = "bottom_action_bar";

    public BottomActionBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final ThemeUtils resources = new ThemeUtils(context);
        setBackgroundDrawable(resources.getDrawable(BOTTOM_ACTION_BAR));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final LinearLayout bottomActionBar = (LinearLayout)findViewById(R.id.bottom_action_bar);
        bottomActionBar.setBackgroundDrawable(new HoloSelector(getContext()));
    }
}
