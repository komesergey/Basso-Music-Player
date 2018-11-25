package com.basso.basso;

import android.app.Application;
import android.os.StrictMode;

import com.basso.basso.cache.ImageCache;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BassoApplication extends Application {
    private static final boolean DEBUG = false;

    @Override
    public void onCreate() {
        enableStrictMode();
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    @Override
    public void onLowMemory() {
        ImageCache.getInstance(this).evictAll();
        super.onLowMemory();
    }

    private void enableStrictMode() {
        if (DEBUG) {
            final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyLog();
            final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyLog();

            threadPolicyBuilder.penaltyFlashScreen();
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }
}
