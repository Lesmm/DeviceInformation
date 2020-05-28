package com.deviceinfo.info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.deviceinfo.ManagerInfo;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import common.modules.util.IBundleUtil;
import common.modules.util.IJSONObjectUtil;
import common.modules.util.IReflectUtil;

public class BroadcastInfo {

    // TODO ... Battery 有三个Service:
    //  1. batterymanager: [android.app.IBatteryService], 2. batterystats: [com.android.internal.app.IBatteryStats], 3. batteryproperties: [android.os.IBatteryPropertiesRegistrar]
    //  其中， batterystats 下面两个方法， Hook 那边处理一下:
    //  public boolean isCharging() throws android.os.RemoteException;
    //  public long computeBatteryTimeRemaining() throws android.os.RemoteException;

    public static JSONObject getInfo(Context mContext) {
        JSONObject info = new JSONObject();

        String action = null;
        JSONObject actionInfo = null;

        // 电池状态
        action = Intent.ACTION_BATTERY_CHANGED;
        actionInfo = getBroadcastInfo(mContext, action);
        try {
            info.put(action, actionInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // usb 状态
        action = "android.hardware.usb.action.USB_STATE";
        actionInfo = getBroadcastInfo(mContext, action);
        try {
            info.put(action, actionInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    private static JSONObject getBroadcastInfo(Context mContext, String action) {

        Context appCt = ManagerInfo.getApplication();
        final Context ct = appCt != null ? appCt : mContext;

        final JSONObject returnValueInfo = new JSONObject();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        intentFilter.setPriority(Integer.MAX_VALUE);

        // ---------------------- debug ----------------------
        /*
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
         */
        // ---------------------- debug ----------------------

        final BroadcastReceiver[] broadcastReceivers = new BroadcastReceiver[]{null};

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle bundle = intent.getExtras();
                if (action == null || bundle == null) {
                    return;
                }

                JSONObject json = IBundleUtil.createJSONFromBundle(bundle);

                IJSONObjectUtil.putAll(returnValueInfo, json);

                // 得到了我们要的了，释放掉这个 BroadcastReceiver 实例
                if (broadcastReceivers[0] != null) {
                    ct.unregisterReceiver(broadcastReceivers[0]);
                    broadcastReceivers[0] = null;
                }


            }
        };

        broadcastReceivers[0] = broadcastReceiver;

        ct.registerReceiver(broadcastReceiver, intentFilter);

        return returnValueInfo;
    }
}
