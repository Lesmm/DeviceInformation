package com.google.deviceinfo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import java.lang.reflect.Method;

public class ApplicationImpl extends Application {

    private static ApplicationImpl instance;
    private static Context appContext;
    private static Activity resumedActivity;

    private int appCount;
    private boolean isRunInBackground;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appContext = instance.getApplicationContext();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activity.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    public static synchronized ApplicationImpl getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static Activity getTopActivity() {
        return resumedActivity;
    }

    public static android.app.Application getApplication() {
        try {

            Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
            // Object currentActivityThread = activityThreadClazz.getMethod("currentActivityThread").invoke(activityThreadClazz);
            Method currentActivityThreadMethod = activityThreadClazz.getDeclaredMethod("currentActivityThread", new Class[] {});
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(activityThreadClazz, new Object[] {});
            // Application application = (Application)activityThreadClazz.getMethod("getApplication").invoke(currentActivityThread);
            Method getApplicationMethod = activityThreadClazz.getDeclaredMethod("getApplication", new Class[] {});
            getApplicationMethod.setAccessible(true);
            Application application = (Application)getApplicationMethod.invoke(currentActivityThread, new Object[] {});
            return application;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 从后台回到前台需要执行的逻辑
     * @param activity
     */
    private void backToApp(Activity activity) {
        isRunInBackground = false;
    }

    /**
     * 离开应用压入后台或者退出应用
     * @param activity
     */
    private void leaveApp(Activity activity) {
        isRunInBackground = true;
    }

}
