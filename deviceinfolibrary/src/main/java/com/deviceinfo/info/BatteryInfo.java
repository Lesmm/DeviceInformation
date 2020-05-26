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

        // 电池状态
        try {
            info.put("Broadcast", batteryStatsInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 电池容量
        final Map result0 = new HashMap();
        final Map result1 = new HashMap();

        try {
            final Class PowerProfile = Class.forName("com.android.internal.os.PowerProfile");
            Object powerProfileObj = IReflectUtil.newInstanceOf(PowerProfile, new Class[]{Context.class}, new Object[]{mContext});
            Double batteryCapacity = (Double) IReflectUtil.invokeMethod(powerProfileObj, "getAveragePower",
                    new Class[]{String.class}, new Object[]{"battery.capacity"});

            result0.put("battery.capacity", batteryCapacity);

            IReflectUtil.iterateFields(PowerProfile, new IReflectUtil.IterateFieldHandler() {
                @Override
                public boolean action(Class<?> clazz, Field field, String fieldName) {
                    Class<?> type = field.getType();
                    if (!Map.class.isAssignableFrom(type)) {
                        return false;
                    }
                    try {
                        // static final HashMap<String, Double> sPowerItemMap = new HashMap<>();
                        // static final HashMap<String, Double[]> sPowerArrayMap = new HashMap<>();
                        Map map = (Map) field.get(clazz);
                        if (map != null && map.size() > 0) {
                            Object value = map.get(map.keySet().toArray()[0]);
                            Class valueClazz = value.getClass();
                            if (valueClazz == Double.class) {
                                result0.putAll(map);
                            } else if (valueClazz == Double[].class) {
                                result1.putAll(map);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            info.put("PowerProfile.Item", new JSONObject(result0));
            // info.put("PowerProfile.Array", new JSONObject(result1));
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

                    // 得到了我们要的了，释放掉这个 BroadcastReceiver 实例
                    if (broadcastReceiver != null) {
                        ManagerInfo.getApplication().unregisterReceiver(broadcastReceiver);
                        broadcastReceiver = null;
                    }

                }

            }
        };

        ManagerInfo.getApplication().registerReceiver(broadcastReceiver, intentFilter);

        return batteryInfo;
    }
}
