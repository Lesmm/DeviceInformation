package com.deviceinfo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.deviceinfo.info.AndroidInternalResourcesInfo;
import com.deviceinfo.info.BatteryInfo;
import com.deviceinfo.info.BluetoothManagerInfo;
import com.deviceinfo.info.BuildInfo;
import com.deviceinfo.info.ConnectivityManagerInfo;
import com.deviceinfo.info.DisplayManagerInfo;
import com.deviceinfo.info.ExtrasInfo;
import com.deviceinfo.info.HardwareInfo;
import com.deviceinfo.info.LocationManagerInfo;
import com.deviceinfo.info.MediaInfo;
import com.deviceinfo.info.PackageManagerInfo;
import com.deviceinfo.info.SensorsInfo;
import com.deviceinfo.info.SettingsInfo;
import com.deviceinfo.info.SubscriptionManagerInfo;
import com.deviceinfo.info.SystemInfo;
import com.deviceinfo.info.SystemPropertiesInfo;
import com.deviceinfo.info.TelephonyManagerInfo;
import com.deviceinfo.info.WifiManagerInfo;
import com.deviceinfo.info.WindowManagerInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

import com.deviceinfo.network.IHttpWrapper;
import common.modules.util.IPreferenceUtil;
import common.modules.util.IReflectUtil;

public class Manager {

    public static Boolean IS_DEBUG = true;

    public static final String __key_is_dev_info_got__ = "__key_is_dev_info_got__";

    public static void grabInfoAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager.grabInfoSync();
            }
        }).start();
    }

    public static void grabInfoSync() {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }

        if(!IS_DEBUG && IPreferenceUtil.getSharedPreferences().getBoolean(__key_is_dev_info_got__, false)) {
            return;
        }

        JSONObject info = getInfo();
        IHttpWrapper.postDeviceInfo(info);
    }


    public static JSONObject getInfo() {
        return getInfo(Manager.getApplication());
    }

    public static JSONObject getInfo(Context mContext) {
        JSONObject result = new JSONObject();

        try {

            JSONObject batteryInfo = BatteryInfo.getInfo(mContext);     // 放最前吧，因为它要等通知回来。获取不到也无所谓的，有就最好。
            result.put("Battery", batteryInfo);

            JSONArray sensorsInfo = SensorsInfo.getInfo(mContext);
            result.put("Sensors", sensorsInfo);

            JSONObject displayInfo = DisplayManagerInfo.getInfo(mContext);
            result.put("Display", displayInfo);

            JSONObject windowInfo = WindowManagerInfo.getInfo(mContext);
            result.put("Window", windowInfo);

            JSONObject bluetoothInfo = BluetoothManagerInfo.getInfo(mContext);
            result.put("Bluetooth", bluetoothInfo);

            JSONObject locationInfo = LocationManagerInfo.getInfo(mContext);
            result.put("Location", locationInfo);

            JSONObject buildInfo = BuildInfo.getBuildInfo(mContext);
            JSONObject buildVersionInfo = BuildInfo.getBuildVersionInfo(mContext);
            result.put("Build", buildInfo);
            result.put("Build.VERSION", buildVersionInfo);

            JSONObject subscriptionInfo = SubscriptionManagerInfo.getInfo(mContext);    // 得放在 TelephonyManagerInfo 前，因为 TelephonyManagerInfo 会调它的iterate*方法，不提前会crash
            result.put("Subscription", subscriptionInfo);

            JSONObject telephonyInfo = TelephonyManagerInfo.getInfo(mContext);
            result.put("Telephony", telephonyInfo);

            JSONObject packageInfo = PackageManagerInfo.getInfo(mContext);
            result.put("Package", packageInfo);

            JSONObject connectivityInfo = ConnectivityManagerInfo.getInfo(mContext);
            result.put("Connectivity", connectivityInfo);

            JSONObject androidInternalResourcesInfo = AndroidInternalResourcesInfo.getInfo(mContext);
            result.put("ResourcesValues", androidInternalResourcesInfo);

            JSONObject filesInfos = HardwareInfo.getInfoInFiles(mContext);
            JSONObject commandsInfos = HardwareInfo.getInfoInCommands(mContext);
            result.put("Files.Contents", filesInfos);
            result.put("Commands.Contents", commandsInfos);

            JSONObject propertiesInfo = SystemPropertiesInfo.getInfo(mContext);
            result.put("SystemProperties", propertiesInfo);

            JSONObject systemInfo = SystemInfo.getInfo(mContext);
            result.put("System", systemInfo);

            JSONObject settingsInfo = SettingsInfo.getInfo(mContext);
            result.put("Settings", settingsInfo);

            JSONObject wifiInfo = WifiManagerInfo.getInfo(mContext);    // 因为扫描，会比较久
            result.put("Wifi", wifiInfo);

            JSONObject mediaInfo = MediaInfo.getInfo(mContext);
            result.put("Media", mediaInfo);

            JSONObject extrasInfo = ExtrasInfo.getInfo(mContext);
            result.put("Extras", extrasInfo);

            Log.d("DeviceInfo", "_set_debug_here_");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static void checkContextLoadedApkResources(Activity activity) {
        if (Manager.IS_DEBUG) {
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


    public static android.app.Application getApplication() {
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

}
