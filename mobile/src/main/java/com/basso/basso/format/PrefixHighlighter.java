package com.basso.basso.format;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.basso.basso.utils.PreferenceUtils;

public class PrefixHighlighter {

    private final int mPrefixHighlightColor;

    public final static int[] string2 = {71, 101, 110, 101, 114, 97, 116, 101, 100};

    private ForegroundColorSpan mPrefixColorSpan;

    public PrefixHighlighter(final Context context) {
        mPrefixHighlightColor = PreferenceUtils.getInstance(context).getDefaultThemeColor(context);
    }

    public void setText(final TextView view, final String text, final char[] prefix) {
        if (view == null || TextUtils.isEmpty(text) || prefix == null || prefix.length == 0) {
            return;
        }
        view.setText(apply(text, prefix));
    }

    public CharSequence apply(final CharSequence text, final char[] prefix) {
        final int mIndex = indexOfWordPrefix(text, prefix);
        if (mIndex != -1) {
            if (mPrefixColorSpan == null) {
                mPrefixColorSpan = new ForegroundColorSpan(mPrefixHighlightColor);
            }
            final SpannableString mResult = new SpannableString(text);
            mResult.setSpan(mPrefixColorSpan, mIndex, mIndex + prefix.length, 0);
            return mResult;
        } else {
            return text;
        }
    }

    private int indexOfWordPrefix(final CharSequence text, final char[] prefix) {
        if (TextUtils.isEmpty(text) || prefix == null) {
            return -1;
        }

        final int mTextLength = text.length();
        final int mPrefixLength = prefix.length;

        if (mPrefixLength == 0 || mTextLength < mPrefixLength) {
            return -1;
        }

        int i = 0;
        while (i < mTextLength) {
            while (i < mTextLength && !Character.isLetterOrDigit(text.charAt(i))) {
                i++;
            }

            if (i + mPrefixLength > mTextLength) {
                return -1;
            }

            int j;
            for (j = 0; j < mPrefixLength; j++) {
                if (Character.toUpperCase(text.charAt(i + j)) != prefix[j]) {
                    break;
                }
            }
            if (j == mPrefixLength) {
                return i;
            }

            while (i < mTextLength && Character.isLetterOrDigit(text.charAt(i))) {
                i++;
            }
        }
        return -1;
    }
}
