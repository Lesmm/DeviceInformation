package com.deviceinfo.info;

import android.content.Context;
import android.location.LocationManager;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationManagerInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject info = getILocationManagerInfo(mContext);

        return info;
    }

    public static JSONObject getILocationManagerInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.location.ILocationManager", "location");
        final Object packageName = mContext.getPackageName();

        Map<?, ?> result = InvokerOfObject.invokeObjectMethods(proxy, new InvokerOfObject.InvokeHandler() {
            @Override
            public Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes, Class<?> returnType, Map<String, Object> resultMap) throws Exception {
                if (returnType == void.class) {
                    return null;
                }
                if (methodName.equals("asBinder") || methodName.equals("getInterfaceDescriptor")) {
                    return null;
                }
                // all set methods
                if (methodName.startsWith("set")) {
                    return null;
                }

                if (methodName.startsWith("add") || methodName.startsWith("send")) {
                    return null;
                }

                // public java.util.List<java.lang.String> getAllProviders() throws android.os.RemoteException;
                // public java.lang.String getNetworkProviderPackage() throws android.os.RemoteException;
                if (parameterTypes.length == 0) {
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }


                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;


                if (parameterTypes.length == 1) {

                    // public boolean isProviderEnabled(java.lang.String provider) throws android.os.RemoteException;
                    // public com.android.internal.location.ProviderProperties getProviderProperties(java.lang.String provider) throws android.os.RemoteException;
                    if (parameterTypes[0] == String.class) {
                        if (methodName.equals("getProviderProperties") || methodName.equals("isProviderEnabled")) {

                            String methodKey = fMethodName + "_with_args";
                            Map methodArgsMap = (Map) fResultMap.get(methodKey);
                            if (methodArgsMap == null) {
                                methodArgsMap = new HashMap();
                                fResultMap.put(methodKey, methodArgsMap);
                            }

                            List<String> allProviders = getAllProviders(mContext);
                            for (int i = 0; i < allProviders.size(); i++) {
                                String provider = allProviders.get(i);

                                String key = "_arg0_String_" + provider;
                                Object value = method.invoke(obj, new Object[]{provider});
                                if (value != null) {
                                    methodArgsMap.put(key, value);
                                }
                            }
                        }


                    }

                }

                if (parameterTypes.length == 2) {

                    // public android.location.Location getLastLocation(android.location.LocationRequest request, java.lang.String packageName) throws android.os.RemoteException;
                    if (methodName.equals("getLastLocation")) {
                        Object value = method.invoke(obj, new Object[]{null, packageName});
                        return value;
                    }

                }


                return null;
            }
        });

        return new JSONObjectExtended(result);

    }


    // ---------------------- 遍历 All Providers 工具方法 ----------------------
    private static List<String> allProviders = null;

    public static List<String> getAllProviders(Context mContext) {
        if (allProviders == null) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            allProviders = locationManager.getAllProviders();

            if (allProviders == null) {
                allProviders = new ArrayList<>();
            }
        }
        return allProviders;
    }
}
