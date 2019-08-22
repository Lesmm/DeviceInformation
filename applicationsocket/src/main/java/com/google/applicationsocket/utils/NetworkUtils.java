package com.google.applicationsocket.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import common.modules.util.IReflectUtil;

/**
 * 网络相关工具类
 * Created by tsy on 16/7/21.
 */
public class NetworkUtils {

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {
        try {

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return intToIp(i);
        } catch (Exception ex) {
            return " 获取IP出错!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
    }

    public static String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 24) & 0xFF);
    }

    public static Boolean checkNetworkConnect(Context mContext) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public static int getNetworkClass(int networkType) {
        Object v = IReflectUtil.invokeMethod(TelephonyManager.class, "getNetworkClass", new Class[]{int.class}, new Object[]{networkType});
        if (v instanceof Integer) {
            return (Integer) v;
        }
        return 0;
    }

    private static SparseArray<String> __network_class_names__ = null;

    public static String getNetworkClassName(int networkType) {
        int classType = getNetworkClass(networkType);

        if (__network_class_names__ == null) {
            __network_class_names__ = new SparseArray();

            IReflectUtil.iterateFields(TelephonyManager.class, new IReflectUtil.IterateFieldHandler() {
                @Override
                public boolean action(Class<?> clazz, Field field, String fieldName) {
                    if (Modifier.isStatic(field.getModifiers()) && fieldName.startsWith("NETWORK_CLASS")) {
                        try {
                            Object v = field.get(TelephonyManager.class);
                            if (v instanceof Integer) {
                                __network_class_names__.put((Integer) v, fieldName);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            });
        }

        String name = __network_class_names__.get(classType);
        if (name != null) {
            name = name.replace("NETWORK_CLASS", "").replace("_", "");
        }
        return name;
    }

    public static String getNetworkTypeName(Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return (String) IReflectUtil.invokeMethod(telephonyManager, "getNetworkTypeName", new Class[]{}, new Object[]{});
    }

    public static String getNetworkTypeName(int type) {
        Object v = IReflectUtil.invokeMethod(TelephonyManager.class, "getNetworkTypeName", new Class[]{int.class}, new Object[]{type});
        if (v instanceof String) {
            return (String) v;
        }
        return null;
    }

    private static Application getApplication() {
        try {
            Class<?> ActivityThread = String.class.getClassLoader().loadClass("android.app.ActivityThread");
            Method currentActivityThreadMethod = ActivityThread.getDeclaredMethod("currentActivityThread", new Class[]{});
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(ActivityThread, new Object[]{});
            Method getApplicationMethod = ActivityThread.getDeclaredMethod("getApplication", new Class[]{});
            getApplicationMethod.setAccessible(true);
            Application application = (Application) getApplicationMethod.invoke(currentActivityThread, new Object[]{});
            return application;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
