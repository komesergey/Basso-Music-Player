package com.basso.basso.lastfm;

import java.util.Map;

public final class MapUtilities {

    private MapUtilities() {
    }

    public static void nullSafePut(final Map<String, String> map, final String key,
            final String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public static void nullSafePut(final Map<String, String> map, final String key,
            final Integer value) {
        if (value != null) {
            map.put(key, value.toString());
        }
    }

    public static void nullSafePut(final Map<String, String> map, final String key, final int value) {
        if (value != -1) {
            map.put(key, Integer.toString(value));
        }
    }

    public static void nullSafePut(final Map<String, String> map, final String key,
            final double value) {
        if (value != -1) {
            map.put(key, Double.toString(value));
        }
    }
}
