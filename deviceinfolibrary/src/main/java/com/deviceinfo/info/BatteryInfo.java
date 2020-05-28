package com.deviceinfo.info;

import android.content.Context;

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

        // 电池容量
        final Map result0 = new HashMap();
        final Map result1 = new HashMap();

        try {
            // 要预先获取一遍
            final Class PowerProfile = Class.forName("com.android.internal.os.PowerProfile");
            Object powerProfileObj = IReflectUtil.newInstanceOf(PowerProfile, new Class[]{Context.class}, new Object[]{mContext});
            Double batteryCapacity = (Double) IReflectUtil.invokeMethod(powerProfileObj, "getAveragePower",
                    new Class[]{String.class}, new Object[]{"battery.capacity"});

            // TODO ..... 其实还不够准确， getAveragePower 方法会从第二个 Map 来获取，当第一个 Map 没有这 Key 值的时候
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


}
