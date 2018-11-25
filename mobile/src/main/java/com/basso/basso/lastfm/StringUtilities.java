package com.basso.basso.lastfm;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class StringUtilities {

    private static MessageDigest mDigest;

    private final static Pattern MD5_PATTERN = Pattern.compile("[a-fA-F0-9]{32}");

    static {
        try {
            mDigest = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException ignored) {
        }
    }

    public final static String md5(final String s) {
        try {
            final byte[] mBytes = mDigest.digest(s.getBytes("UTF-8"));
            final StringBuilder mBuilder = new StringBuilder(32);
            for (final byte aByte : mBytes) {
                final String mHex = Integer.toHexString(aByte & 0xFF);
                if (mHex.length() == 1) {
                    mBuilder.append('0');
                }
                mBuilder.append(mHex);
            }
            return mBuilder.toString();
        } catch (final UnsupportedEncodingException ignored) {
        }
        return null;
    }

    public static int[] stringToArray(String s){
        int[] intArray = new int[s.length()];
        for(int i = 0; i < s.length(); i++){
            intArray[i] = (int)(s.charAt(i));
        }
        return intArray;
    }

    public static String fromIntArray(int[] array){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            str.append((char)array[i]);
        }
        return str.toString();
    }

    public static String encode(final String s) {
        if (s == null) {
            return null;
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (final UnsupportedEncodingException ignored) {
        }
        return null;
    }

    public static String decode(final String s) {
        if (s == null) {
            return null;
        }
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (final UnsupportedEncodingException ignored) {
        }
        return null;
    }

    public static Map<String, String> map(final String... strings) {
        if (strings.length % 2 != 0) {
            throw new IllegalArgumentException("strings.length % 2 != 0");
        }
        final Map<String, String> sMap = new HashMap<String, String>();
        for (int i = 0; i < strings.length; i += 2) {
            sMap.put(strings[i], strings[i + 1]);
        }
        return sMap;
    }

    public static String cleanUp(final String s) {
        return s.replaceAll("[*:/\\\\?|<>\"]", "-");
    }

    public static boolean isMD5(final String s) {
        return s.length() == 32 && MD5_PATTERN.matcher(s).matches();
    }

    public static boolean convertToBoolean(final String resultString) {
        return "1".equals(resultString);
    }

    public static String convertFromBoolean(final boolean value) {
        if (value) {
            return "1";
        } else {
            return "0";
        }
    }
}
