package common.modules.util;

import android.os.Looper;

public class IHandlerUtil {

    public static void postToMainThread(Runnable runnable) {
        new android.os.Handler(Looper.getMainLooper()).post(runnable);
    }

}
