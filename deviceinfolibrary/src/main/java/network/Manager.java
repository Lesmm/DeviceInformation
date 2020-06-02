package network;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.deviceinfo.ManagerInfo;

import org.json.JSONObject;

import java.lang.reflect.Method;

import common.modules.util.IActivityUtil;
import common.modules.util.IFileUtil;
import common.modules.util.IPreferenceUtil;
import common.modules.util.IReflectUtil;

public class Manager {

    public static int VERSION = 1;

    public static final String __key_is_dev_info_got__ = "__key_is_dev_info_got__";
    public static final String __sdcard_file_name_info__ = "/sdcard/phoneInfo.json";

    public static interface GrabInfoAsyncCallback {
        public void done();
    }

    public static void grabInfoAsync() {
        grabInfoAsync(null);
    }

    public static void grabInfoAsync(final GrabInfoAsyncCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager.grabInfoSync();

                if (callback != null) {
                    callback.done();
                }
            }
        }).start();

        IHttpFacade.checkApkVersionAsync();
    }

    public static void grabInfoSync() {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }

        if(!ManagerInfo._IS_DEBUG_ && IPreferenceUtil.getSharedPreferences().getBoolean(__key_is_dev_info_got__, false)) {
            return;
        }

        JSONObject info = getInfo();
        IHttpPoster.postDeviceInfo(info);
    }

    public static void grabInfoToSdcardAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager.grabInfoToSdcardSync();
            }
        }).start();
    }

    public static void grabInfoToSdcardSync() {
        JSONObject jsonObject = Manager.getInfo();
        IFileUtil.writeTextToFile(jsonObject.toString(), __sdcard_file_name_info__);
    }

    public static JSONObject getInfo() {
        return getInfo(IActivityUtil.getApplication());
    }

    public static JSONObject getInfo(Context mContext) {
        return ManagerInfo.getInfo(mContext);
    }

    public static Application getApplication() {
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
            return application;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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
