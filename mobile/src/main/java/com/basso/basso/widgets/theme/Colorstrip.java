package com.basso.basso.widgets.theme;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.basso.basso.utils.ThemeUtils;

public class Colorstrip extends View {

    private static final String COLORSTRIP = "colorstrip";

    public Colorstrip(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final ThemeUtils resources = new ThemeUtils(context);
        setBackgroundColor(Color.WHITE);
    }
}
