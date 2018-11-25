package com.basso.basso.format;

import android.text.TextUtils;

public class Capitalize {

    public Capitalize() {
    }

    public static final String capitalize(String str) {
        return capitalize(str, null);
    }

    public static final String capitalize(String str, char... delimiters) {
        final int delimLen = delimiters == null ? -1 : delimiters.length;
        if (TextUtils.isEmpty(str) || delimLen == 0) {
            return str;
        }
        final char[] buffer = str.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < buffer.length; i++) {
            char ch = buffer[i];
            if (isDelimiter(ch, delimiters)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer[i] = Character.toTitleCase(ch);
                capitalizeNext = false;
            }
        }
        return new String(buffer);
    }


    private static final boolean isDelimiter(char ch, char[] delimiters) {
        if (delimiters == null) {
            return Character.isWhitespace(ch);
        }
        for (char delimiter : delimiters) {
            if (ch == delimiter) {
                return true;
            }
        }
        return false;
    }
}
