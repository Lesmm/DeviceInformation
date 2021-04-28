package com.google.deviceinfo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

public class MainApplication extends Application {

    private static MainApplication instance;
    private static Context appContext;
    private static Activity resumedActivity;

    private int appCount;
    private boolean isRunInBackground;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appContext = instance.getApplicationContext();

        // 未捕捉异常
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                Log.d("DeviceInfo", "-------------- uncaughtException --------------");
                e.printStackTrace();
                Log.d("DeviceInfo", "-------------- uncaughtException --------------");
            }
        });

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                appCount++;
                if (isRunInBackground) {
                    //应用从后台回到前台 需要做的操作
                    backToApp(activity);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                resumedActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
                if (appCount == 0) {
                    //应用进入后台 需要做的操作
                    leaveApp(activity);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public static synchronized MainApplication getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static Activity getTopActivity() {
        return resumedActivity;
    }

    /**
     * 从后台回到前台需要执行的逻辑
     *
     * @param activity
     */
    private void backToApp(Activity activity) {
        isRunInBackground = false;
    }

    /**
     * 离开应用压入后台或者退出应用
     *
     * @param activity
     */
    private void leaveApp(Activity activity) {
        isRunInBackground = true;
    }

}
