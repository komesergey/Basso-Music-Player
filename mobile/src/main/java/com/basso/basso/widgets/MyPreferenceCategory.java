package com.basso.basso.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.basso.basso.R;

public class MyPreferenceCategory extends PreferenceCategory {

    public MyPreferenceCategory(Context context) {
        super(context);
    }
    public MyPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        parent.setPadding(0,0,0,0);
        TextView categoryTitle =  (TextView)super.onCreateView(parent);
        categoryTitle.setTextColor(parent.getResources().getColor(R.color.action_bar_background));
        categoryTitle.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        return categoryTitle;
    }
}