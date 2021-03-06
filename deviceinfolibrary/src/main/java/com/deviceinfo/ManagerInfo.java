package com.deviceinfo;

import android.content.Context;
import android.util.Log;

import com.deviceinfo.higher.HiInputMethodManager;
import com.deviceinfo.higher.HiLocationManager;
import com.deviceinfo.higher.HiTelephonyManager;
import com.deviceinfo.higher.HiWifiManager;
import com.deviceinfo.info.AndroidInternalResourcesInfo;
import com.deviceinfo.info.BatteryInfo;
import com.deviceinfo.info.BluetoothManagerInfo;
import com.deviceinfo.info.BroadcastInfo;
import com.deviceinfo.info.BuildInfo;
import com.deviceinfo.info.ConnectivityManagerInfo;
import com.deviceinfo.info.DeviceIdentifiersPolicyInfo;
import com.deviceinfo.info.DisplayManagerInfo;
import com.deviceinfo.info.ExtrasInfo;
import com.deviceinfo.info.HardwareInfo;
import com.deviceinfo.info.LocationManagerInfo;
import com.deviceinfo.info.MediaInfo;
import com.deviceinfo.info.PackageManagerInfo;
import com.deviceinfo.info.SensorsInfo;
import com.deviceinfo.info.SettingsInfo;
import com.deviceinfo.info.StatInfo;
import com.deviceinfo.info.SubscriptionManagerInfo;
import com.deviceinfo.info.SystemInfo;
import com.deviceinfo.info.SystemPropertiesInfo;
import com.deviceinfo.info.TelephonyManagerInfo;
import com.deviceinfo.info.WifiManagerInfo;
import com.deviceinfo.info.WindowManagerInfo;
import com.facade.Manager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import common.modules.util.IJSONObjectUtil;
import me.weishu.reflection.Reflection;

public class ManagerInfo {

    public static Boolean _IS_DEBUG_ = false;

    public static JSONObject getInfo(Context mContext) {
        JSONObject result = new JSONObject();

        try {
            // https://github.com/tiann/FreeReflection
            Reflection.unseal(Manager.getApplication().getBaseContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ExtrasInfo.cacheIpAddressAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            JSONObject broadcastInfo = BroadcastInfo.getInfo(mContext);// ??????????????????????????????????????????????????????????????????????????????????????????
            result.put("Broadcast", broadcastInfo);

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

            JSONObject locationInfo = LocationManagerInfo.getInfo(mContext);
            result.put("Location", locationInfo);

            JSONObject deviceIdentifiersInfo = DeviceIdentifiersPolicyInfo.getInfo(mContext);
            result.put("DeviceIdentifiers", deviceIdentifiersInfo);

            JSONObject buildInfo = BuildInfo.getBuildInfo(mContext);
            result.put("Build", buildInfo);

            JSONObject buildVersionInfo = BuildInfo.getBuildVersionInfo(mContext);
            result.put("Build.VERSION", buildVersionInfo);

            JSONObject subscriptionInfo = SubscriptionManagerInfo.getInfo(mContext); // ????????? TelephonyManagerInfo ???????????? TelephonyManagerInfo ????????????iterate*?????????????????????crash
            result.put("Subscription", subscriptionInfo);

            JSONObject telephonyInfo = TelephonyManagerInfo.getInfo(mContext, subscriptionInfo);
            result.put("Telephony", telephonyInfo);

            JSONObject packageInfo = PackageManagerInfo.getInfo(mContext);
            result.put("Package", packageInfo);

            JSONObject connectivityInfo = ConnectivityManagerInfo.getInfo(mContext);
            result.put("Connectivity", connectivityInfo);

            JSONObject androidInternalResourcesInfo = AndroidInternalResourcesInfo.getInfo(mContext);
            result.put("ResourcesValues", androidInternalResourcesInfo);

            JSONObject filesInfos = HardwareInfo.getInfoInFiles(mContext);
            result.put("Files.Contents", filesInfos);

            JSONObject commandsInfos = HardwareInfo.getInfoInCommands(mContext);
            result.put("Commands.Contents", commandsInfos);

            JSONObject propertiesInfo = SystemPropertiesInfo.getInfo(mContext);
            result.put("SystemProperties", propertiesInfo);

            JSONObject systemInfo = SystemInfo.getInfo(mContext);
            result.put("System", systemInfo);

            JSONObject settingsInfo = SettingsInfo.getInfo(mContext);
            result.put("Settings", settingsInfo);

            JSONObject wifiInfo = WifiManagerInfo.getInfo(mContext);    // ???????????????????????????
            result.put("Wifi", wifiInfo);

            JSONObject mediaInfo = MediaInfo.getInfo(mContext);
            result.put("Media", mediaInfo);

            // Extras Info
            JSONObject extrasInfo = ExtrasInfo.getInfo(mContext);
            result.put("Extras", extrasInfo);


            // Put in packageInfo
            StatInfo.fillInPackageSizeStatInfo(mContext, packageInfo);

            // ??????????????????API
            JSONObject hiTelephonyInfo = new HiTelephonyManager().getInfo(mContext);
            IJSONObjectUtil.replaceJsonElementsValues(telephonyInfo, hiTelephonyInfo);

            JSONObject hiWifiInfo = new HiWifiManager().getInfo(mContext);
            IJSONObjectUtil.replaceJsonElementsValues(wifiInfo, hiWifiInfo);

            JSONObject hiLocationExtraInfo = new HiLocationManager().getInExtrasInfo(mContext);
            IJSONObjectUtil.replaceJsonElementsValues(extrasInfo, hiLocationExtraInfo);

            JSONObject inputMethodInfo = new HiInputMethodManager().getInfo(mContext);
            result.put("InputMethods", inputMethodInfo);

            Log.d("DeviceInfo", "_set_debug_here_");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

}
