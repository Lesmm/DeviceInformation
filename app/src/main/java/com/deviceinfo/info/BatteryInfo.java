package com.deviceinfo.info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.deviceinfo.Manager;

import org.json.JSONObject;

import java.util.Map;

import common.modules.util.IReflectUtil;

public class BatteryInfo {

    // TODO ... Battery 有三个Service:
    //  1. batterymanager: [android.app.IBatteryService], 2. batterystats: [com.android.internal.app.IBatteryStats], 3. batteryproperties: [android.os.IBatteryPropertiesRegistrar]
    //  其中， batterystats 下面两个方法， Hook 那边处理一下:
    //  public boolean isCharging() throws android.os.RemoteException;
    //  public long computeBatteryTimeRemaining() throws android.os.RemoteException;

    public static JSONObject getInfo(Context mContext) {

        JSONObject info = new JSONObject();

        JSONObject batteryStatsInfo = getBroadcastBatteryInfo(mContext);

        try {
            info.put("Broadcast", batteryStatsInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;

    }

    private static BroadcastReceiver broadcastReceiver = null;


    private static JSONObject getBroadcastBatteryInfo(Context mContext) {

        final JSONObject batteryInfo = new JSONObject();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    return;
                }

                IReflectUtil.invokeMethod(bundle, "unparcel", new Class[]{}, new Object[]{});
                Map<String, Object> mMap = (Map<String, Object>) IReflectUtil.getFieldValue(bundle, "mMap");
                JSONObject bundleInfos = new JSONObject(mMap);
                Log.d("DeviceInfo", "_set_debug_here_: battery");

                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

                    try {
                        batteryInfo.put("ACTION_BATTERY_CHANGED", bundleInfos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 得到了我们要的了，释放掉这个 BroadcastReceiver
                    if (broadcastReceiver != null) {
                        Manager.getApplication().unregisterReceiver(broadcastReceiver);
                        broadcastReceiver = null;
                    }
                }

            }
        };

        Manager.getApplication().registerReceiver(broadcastReceiver, intentFilter);

        return batteryInfo;
    }
}
