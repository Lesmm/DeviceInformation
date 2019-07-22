package com.deviceinfo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.deviceinfo.info.AndroidInternalResourcesInfo;
import com.deviceinfo.info.BatteryInfo;
import com.deviceinfo.info.BluetoothManagerInfo;
import com.deviceinfo.info.BuildInfo;
import com.deviceinfo.info.ConnectivityManagerInfo;
import com.deviceinfo.info.DisplayManagerInfo;
import com.deviceinfo.info.ExtrasInfo;
import com.deviceinfo.info.HardwareInfo;
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

import common.modules.util.IReflectUtil;

public class Manager {

    public static final Boolean IS_DEBUG = true;

    public static void getInfo(Context mContext) {
        try {
            JSONObject result = new JSONObject();

            JSONObject batteryInfo = BatteryInfo.getInfo(mContext);
            result.put("Battery", batteryInfo);

            JSONArray sensorsInfo = SensorsInfo.getInfo(mContext);
            result.put("Sensors", sensorsInfo);

            JSONObject displayInfo = DisplayManagerInfo.getInfo(mContext);
            result.put("Display", displayInfo);

            JSONObject windowInfo = WindowManagerInfo.getInfo(mContext);
            result.put("Window", windowInfo);

            JSONObject bluetoothInfo = BluetoothManagerInfo.getInfo(mContext);
            result.put("Bluetooth", bluetoothInfo);

            JSONObject buildInfo = BuildInfo.getBuildInfo(mContext);
            JSONObject buildVersionInfo = BuildInfo.getBuildVersionInfo(mContext);
            result.put("Build", buildInfo);
            result.put("Build.VERSION", buildVersionInfo);

            JSONObject subscriptionInfo = SubscriptionManagerInfo.getInfo(mContext);    // 得放在 TelephonyManagerInfo 前，因为 TelephonyManagerInfo 会调iterate*方法，不提前会crash
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

            Log.d("DeviceInfo","_set_debug_here_");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static void checkContextLoadedApkResources(Activity activity) {
        if (Manager.IS_DEBUG) {
            Application application = activity.getApplication();
            Context baseContext = activity.getBaseContext();
            Context applicationContext = activity.getApplicationContext();

            Context baseContextApplicationContext =  baseContext.getApplicationContext();
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

            Log.d("DeviceInfo","_set_debug_here_");
        }
    }

}
