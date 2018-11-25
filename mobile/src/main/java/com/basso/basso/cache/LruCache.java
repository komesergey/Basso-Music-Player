package com.basso.basso.cache;

import android.annotation.SuppressLint;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<K, V> {

    private final LinkedHashMap<K, V> map;

    private final int maxSize;

    private int size;

    private int putCount;

    private int createCount;

    private int evictionCount;

    private int hitCount;

    private int missCount;

    public LruCache(final int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
    }

    public final V get(final K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V mapValue;
        synchronized (this) {
            mapValue = map.get(key);
            if (mapValue != null) {
                this.hitCount++;
                return mapValue;
            }
            this.missCount++;
        }


        final V createdValue = create(key);
        if (createdValue == null) {
            return null;
        }

        synchronized (this) {
            this.createCount++;
            mapValue = map.put(key, createdValue);

            if (mapValue != null) {
                this.map.put(key, mapValue);
            } else {
                this.size += safeSizeOf(key, createdValue);
            }
        }

        if (mapValue != null) {
            entryRemoved(false, key, createdValue, mapValue);
            return mapValue;
        } else {
            trimToSize(maxSize);
            return createdValue;
        }
    }

    public final V put(final K key, final V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        V previous;
        synchronized (this) {
            this.putCount++;
            this.size += safeSizeOf(key, value);
            previous = this.map.put(key, value);
            if (previous != null) {
                this.size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }

        trimToSize(maxSize);
        return previous;
    }

    public void trimToSize(final int maxSize) {
        while (true) {
            K key;
            V value;
            synchronized (this) {
                if (this.size < 0 || this.map.isEmpty() && size != 0) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }

                if (this.size <= maxSize || this.map.isEmpty()) {
                    break;
                }

                final Map.Entry<K, V> toEvict = this.map.entrySet().iterator().next();
                key = toEvict.getKey();
                value = toEvict.getValue();
                this.map.remove(key);
                this.size -= safeSizeOf(key, value);
                this.evictionCount++;
            }

            entryRemoved(true, key, value, null);
        }
    }

    public final V remove(final K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V previous;
        synchronized (this) {
            previous = this.map.remove(key);
            if (previous != null) {
                this.size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, null);
        }

        return previous;
    }

    protected void entryRemoved(final boolean evicted, final K key, final V oldValue,
            final V newValue) {
    }

    protected V create(final K key) {
        return null;
    }

    private int safeSizeOf(final K key, final V value) {
        final int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }

    protected int sizeOf(final K key, final V value) {
        return 1;
    }

    public final void evictAll() {
        trimToSize(-1);
    }

    public synchronized final int size() {
        return this.size;
    }

    public synchronized final int maxSize() {
        return this.maxSize;
    }

    public synchronized final int hitCount() {
        return this.hitCount;
    }

    public synchronized final int missCount() {
        return this.missCount;
    }

    public synchronized final int createCount() {
        return this.createCount;
    }

    public synchronized final int putCount() {
        return this.putCount;
    }

    public synchronized final int evictionCount() {
        return this.evictionCount;
    }

    public synchronized final Map<K, V> snapshot() {
        return new LinkedHashMap<K, V>(this.map);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public synchronized final String toString() {
        final int accesses = this.hitCount + this.missCount;
        final int hitPercent = accesses != 0 ? 100 * this.hitCount / accesses : 0;
        return String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", this.maxSize,
                this.hitCount, this.missCount, hitPercent);
    }
}
