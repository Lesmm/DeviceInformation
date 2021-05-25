package com.google.applicationjar.util;

import android.os.Looper;

public class IHandlerUtil {

    public static void postToMainThread(Runnable runnable) {
        new android.os.Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void postToMainThread(Runnable runnable, long delayMillis) {
        new android.os.Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis);
    }


}
