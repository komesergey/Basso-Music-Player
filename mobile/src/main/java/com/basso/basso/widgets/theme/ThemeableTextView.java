package com.basso.basso.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.basso.basso.R;
import com.basso.basso.utils.ThemeUtils;

import java.util.WeakHashMap;

public class ThemeableTextView extends TextView {

    public ThemeableTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final ThemeUtils resources = new ThemeUtils(context);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemeableTextView, 0, 0);
        final String resourceName = typedArray.getString(R.styleable.ThemeableTextView_themeResource);
        if (!TextUtils.isEmpty(resourceName)) {
            setTextColor(resources.getColor(resourceName));
        }
        typedArray.recycle();
    }

    public static final class TypefaceCache {

        private static final WeakHashMap<String, Typeface> MAP = new WeakHashMap<String, Typeface>();

        private static TypefaceCache sInstance;

        public TypefaceCache() {
        }

        public static final TypefaceCache getInstance() {
            if (sInstance == null) {
                sInstance = new TypefaceCache();
            }
            return sInstance;
        }

        public Typeface getTypeface(final String file, final Context context) {
            Typeface result = MAP.get(file);
            if (result == null) {
                result = Typeface.createFromAsset(context.getAssets(), file);
                MAP.put(file, result);
            }
            return result;
        }
    }
}
