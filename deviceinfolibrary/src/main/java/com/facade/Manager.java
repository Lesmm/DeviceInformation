package com.facade;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.deviceinfo.ManagerInfo;
import com.facade.network.IHttpFacade;
import com.facade.network.IHttpPoster;

import org.json.JSONObject;

import java.lang.reflect.Method;

import common.modules.util.IActivityUtil;
import common.modules.util.IFileUtil;
import common.modules.util.IPreferenceUtil;
import common.modules.util.IReflectUtil;

/**
 * cd $ANDROID_SDK && find -name dx
 * PATH=$PATH:$ANDROID_SDK/build-tools/29.0.2/
 * dx --dex --output=DeviceInfo.jar full.jar
 */

public class Manager {

    public static int VERSION = 1;

    public static final String __key_last_patch_version__ = "__key_last_patch_version__";
    public static final String __key_count_dev_info_got__ = "__key_count_dev_info_got__";
    public static final String __sdcard_file_name_info__ = "/sdcard/phoneInfo.json";

    /**
     * Callback
     */

    public static interface GrabInfoAsyncCallback {
        public void done(JSONObject info);
    }

    /**
     * Api
     */

    public static void grabInfoAsync() {
        grabInfoAsync(null);
    }

    public static void grabInfoAsync(final GrabInfoAsyncCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject info = Manager.grabInfoSync();

                if (callback != null) {
                    callback.done(info);
                }
            }
        }).start();
    }

    public static JSONObject grabInfoSync() {
        if (Build.VERSION.SDK_INT < 19) {
            return null;
        }

        int gotCount = IPreferenceUtil.getSharedPreferences().getInt(Manager.__key_count_dev_info_got__, 0);
        if (!ManagerInfo._IS_DEBUG_ && gotCount >= 5) {
            Log.d("DeviceInfo", "forgive grab for limit of got count: " + gotCount);
            return null;
        }

        Manager.sendBroadcast("__grab_progress__", "We're Collecting...");
        JSONObject info = Manager.grabInfoToSdcardSync();
        Manager.sendBroadcast("__grab_progress__", "We're Collected, Posting...");
        IHttpPoster.postDeviceInfo(info);
        Manager.sendBroadcast("__grab_progress__", "We're Done!");
        return info;
    }

    public static void grabInfoToSdcardAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager.grabInfoToSdcardSync();
            }
        }).start();
    }

    public static JSONObject grabInfoToSdcardSync() {
        JSONObject jsonObject = Manager.getInfo();
        IFileUtil.writeTextToFile(jsonObject.toString(), __sdcard_file_name_info__);
        return jsonObject;
    }

    public static JSONObject getInfo() {
        return getInfo(IActivityUtil.getApplication());
    }

    public static JSONObject getInfo(Context mContext) {
        IHttpFacade.checkApkVersionAsync();
        return ManagerInfo.getInfo(mContext);
    }

    /**
     * Context
     */

    public static Application __application__ = null;

    public static void setApplication(Application application) {
        __application__ = application;
    }

    public static Application getApplication() {
        if (__application__ != null) {
            return __application__;
        }
        try {
            Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
            // Object currentActivityThread = activityThreadClazz.getMethod("currentActivityThread").invoke(activityThreadClazz);
            Method currentActivityThreadMethod = activityThreadClazz.getDeclaredMethod("currentActivityThread", new Class[]{});
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(activityThreadClazz, new Object[]{});
            // Application application = (Application)activityThreadClazz.getMethod("getApplication").invoke(currentActivityThread);
            Method getApplicationMethod = activityThreadClazz.getDeclaredMethod("getApplication", new Class[]{});
            getApplicationMethod.setAccessible(true);
            Application application = (Application) getApplicationMethod.invoke(currentActivityThread, new Object[]{});
            __application__ = application;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return __application__;
    }

    public static void sendBroadcast(String action, String message) {
        try {
            Intent intent = new Intent(action).putExtra("message", message);
            Log.d("DeviceInfo", "sendBroadcast " + action + ", " + message /* + intent.toString() */);
            Manager.getApplication().sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test
     */

    public static void checkContextLoadedApkResources(Activity activity) {
        if (ManagerInfo._IS_DEBUG_) {
            Application application = activity.getApplication();
            Context baseContext = activity.getBaseContext();
            Context applicationContext = activity.getApplicationContext();

            Context baseContextApplicationContext = baseContext.getApplicationContext();
            Context applicationContextApplicationContext = applicationContext.getApplicationContext();

            Context applicationBaseContext = application.getBaseContext();
            Context applicationApplicationContext = application.getApplicationContext();

            Resources resources = activity.getResources();
            Resources applicationResources = application.getResources();
            Resources baseContextResources = baseContext.getResources();
            Resources applicationContextResources = applicationContext.getResources();
            Resources applicationBaseContextResources = applicationBaseContext.getResources();
            Resources applicationApplicationContextResources = applicationApplicationContext.getResources();

            Object loadedApk1 = IReflectUtil.getFieldValue(baseContext, "mPackageInfo");
            Object loadedApk2 = IReflectUtil.getFieldValue(applicationBaseContext, "mPackageInfo");

            Object mResources1 = IReflectUtil.getFieldValue(loadedApk1, "mResources");
            Object mResources2 = IReflectUtil.getFieldValue(loadedApk2, "mResources");

            Log.d("DeviceInfo", "_set_debug_here_");
        }
    }

}
