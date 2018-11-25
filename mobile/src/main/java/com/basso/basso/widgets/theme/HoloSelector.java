package com.basso.basso.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import com.basso.basso.utils.ThemeUtils;
import java.lang.ref.WeakReference;

public class HoloSelector extends StateListDrawable {

    private static final String RESOURCE_NAME = "holo_selector";

    private static final int FOCUSED = android.R.attr.state_focused;

    private static final int PRESSED = android.R.attr.state_pressed;

    @SuppressLint("NewApi")
    public HoloSelector(final Context context) {
        final ThemeUtils resources = new ThemeUtils(context);
        final int themeColor = resources.getColor(RESOURCE_NAME);
        addState(new int[] {
            FOCUSED
        }, makeColorDrawable(themeColor));
        addState(new int[] {
            PRESSED
        }, makeColorDrawable(themeColor));
        addState(new int[] {}, makeColorDrawable(Color.TRANSPARENT));
        setExitFadeDuration(400);
    }

    private static final ColorDrawable makeColorDrawable(final int color) {
        return new WeakReference<ColorDrawable>(new ColorDrawable(color)).get();
    }
}
