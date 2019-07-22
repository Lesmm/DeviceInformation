package com.deviceinfo.info;

import android.content.Context;
import android.os.Parcelable;
import android.util.Log;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.Manager;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import common.modules.util.IReflectUtil;

public class DisplayManagerInfo {

    public static JSONObject getInfo(Context mContext) {

        if (Manager.IS_DEBUG) {
            try {
                // class android.hardware.display.DisplayManager
                Object object = mContext.getSystemService(Context.DISPLAY_SERVICE);

                int[] displayIds = getDisplayIds();
                Object[] displayInfos = getDisplayInfos();

                Log.d("DeviceInfo", "_set_debug_here_");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JSONObject info = getIDisplayManagerInfo(mContext);

        return info;
    }


    public static JSONObject getIDisplayManagerInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.hardware.display.IDisplayManager", "display" /*Context.DISPLAY_SERVICE*/);

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

                if (methodName.startsWith("register") || methodName.startsWith("start") || methodName.startsWith("stop") ||
                        methodName.startsWith("pause") || methodName.startsWith("resume") || methodName.startsWith("release")) {
                    return null;
                }

                // WifiDisplayStatus 这个信息我们就不需要了
                // public android.hardware.display.WifiDisplayStatus getWifiDisplayStatus() throws android.os.RemoteException;
                if (methodName.equals("getWifiDisplayStatus")) {
                    return null;
                }

                // 其实 android.hardware.display.DisplayManagerGlobal 都可以拿回来了，这里还是用binder拿吧。

                // public int[] getDisplayIds() throws android.os.RemoteException;
                if (parameterTypes.length == 0) {
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                // public android.view.DisplayInfo getDisplayInfo(int displayId) throws android.os.RemoteException;
                if (parameterTypes.length == 1) {

                    if (parameterTypes[0] == int.class) {

                        if (methodName.equals("getDisplayInfo")) {

                            iterateAllDisplayIds(mContext, new IterateDisplayIdsHandler() {
                                @Override
                                public void handle(int displayId) throws Exception {

                                    String methodKey = fMethodName + "_with_args";
                                    Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                    if (methodArgsMap == null) {
                                        methodArgsMap = new HashMap();
                                        fResultMap.put(methodKey, methodArgsMap);
                                    }

                                    String key = "_arg0_int_" + displayId;
                                    Object value = fMethod.invoke(fObj, new Object[]{displayId});
                                    if (value != null) {
                                        methodArgsMap.put(key, value);
                                    }

                                }
                            });

                        }

                    }

                }

                return null;
            }
        });

        return new JSONObjectExtended(result);

    }


    // ---------------------- 遍历 DisplayIds 工具方法 ----------------------
    public static interface IterateDisplayIdsHandler {
        public void handle(int displayId) throws Exception;
    }

    private static int[] allDisplayIds = null;

    public static void iterateAllDisplayIds(Context mContext, IterateDisplayIdsHandler handler) {
        try {

            if (allDisplayIds == null) {
                allDisplayIds = getDisplayIds();
            }
            for (int i = 0; i < allDisplayIds.length; i++) {
                try {
                    int displayId = allDisplayIds[i];
                    if (handler != null) {
                        handler.handle(displayId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------- 遍历 DisplayInfos 工具方法 ----------------------
    public static interface IterateDisplayInfosHandler {
        public void handle(/*android.view.DisplayInfo*/ Parcelable displayInfo) throws Exception;
    }

    private static Parcelable[] allDisplayInfos = null;

    public static void iterateAllDisplayInfos(Context mContext, IterateDisplayInfosHandler handler) {
        try {

            if (allDisplayInfos == null) {
                allDisplayInfos = getDisplayInfos();
            }
            for (int i = 0; i < allDisplayInfos.length; i++) {
                try {
                    Parcelable displayInfo = allDisplayInfos[i];
                    if (handler != null) {
                        handler.handle(displayInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static int[] getDisplayIds() {
        // android.hardware.display.DisplayManagerGlobal dm = android.hardware.display.DisplayManagerGlobal.getInstance();
        // dm.getDisplayIds();
        Object dm = IReflectUtil.invokeClassMethod("android.hardware.display.DisplayManagerGlobal", "getInstance", new Class[]{}, new Object[]{});
        int[] allDisplayIds = (int[]) IReflectUtil.invokeMethod(dm, "getDisplayIds", new Class[]{}, new Object[]{});
        return allDisplayIds;
    }


    private static Parcelable[] getDisplayInfos() {
        Object dm = IReflectUtil.invokeClassMethod("android.hardware.display.DisplayManagerGlobal", "getInstance", new Class[]{}, new Object[]{});

        int[] allDisplayIds = getDisplayIds();
        Parcelable[] allDisplayInfos = new Parcelable[allDisplayIds.length];
        for (int i = 0; i < allDisplayIds.length; i++) {
            int displayId = allDisplayIds[i];
            Parcelable displayInfo = (Parcelable) IReflectUtil.invokeMethod(dm, "getDisplayInfo", new Class[]{int.class}, new Object[]{displayId});
            allDisplayInfos[i] = displayInfo;
        }
        return allDisplayInfos;
    }

}
