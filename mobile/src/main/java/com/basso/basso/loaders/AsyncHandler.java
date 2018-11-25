package com.basso.basso.loaders;

import android.os.Handler;
import android.os.HandlerThread;

public final class AsyncHandler {

    private static final HandlerThread sHandlerThread = new HandlerThread("AsyncHandler");

    private static final Handler sHandler;

    static {
        sHandlerThread.start();
        sHandler = new Handler(sHandlerThread.getLooper());
    }

    private AsyncHandler() {
    }

    public static void post(final Runnable r) {
        sHandler.post(r);
    }
}
