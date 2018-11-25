package com.basso.basso.widgets.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.basso.basso.utils.ThemeUtils;

public class ThemeableFrameLayout extends FrameLayout {

    public static final String BACKGROUND = "pager_background";

    @SuppressWarnings("deprecation")
    public ThemeableFrameLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final ThemeUtils resources = new ThemeUtils(context);
        setBackgroundDrawable(resources.getDrawable(BACKGROUND));
    }
}
