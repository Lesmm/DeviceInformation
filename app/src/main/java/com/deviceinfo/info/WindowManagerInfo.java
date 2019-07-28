package com.deviceinfo.info;

import android.content.Context;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class WindowManagerInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject info = getIWindowManagerInfo(mContext);

        return info;
    }


    public static JSONObject getIWindowManagerInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.view.IWindowManager", Context.WINDOW_SERVICE);

        Map<?, ?> result = InvokerOfObject.invokeObjectMethods(proxy, new InvokerOfObject.InvokeHandler() {
            @Override
            public Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes, Class<?> returnType, Map<String, Object> resultMap) throws Exception {

                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                // 因为返回值是void， 在这里提前处理了
                // public void getInitialDisplaySize(int displayId, android.graphics.Point size) throws android.os.RemoteException;
                // public void getBaseDisplaySize(int displayId, android.graphics.Point size) throws android.os.RemoteException;
                if (returnType == void.class && parameterTypes.length == 2) {

                    if ( parameterTypes[0] == int.class && parameterTypes[1] == android.graphics.Point.class ) {

                        if (methodName.contains("DisplaySize")) {

                            DisplayManagerInfo.iterateAllDisplayIds(mContext, new DisplayManagerInfo.IterateDisplayIdsHandler() {
                                @Override
                                public void handle(int displayId) throws Exception {

                                    String methodKey = fMethodName + "_with_args";
                                    Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                    if (methodArgsMap == null) {
                                        methodArgsMap = new HashMap();
                                        fResultMap.put(methodKey, methodArgsMap);
                                    }

                                    android.graphics.Point outPoint = new android.graphics.Point();

                                    String key = "_arg0_int_" + displayId;
                                    fMethod.invoke(fObj, new Object[]{displayId, outPoint});
                                    methodArgsMap.put(key, outPoint);

                                }
                            });

                        }

                    }

                }

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

                // 去掉一些APP无权限调用的方法 以及 返回值没有什么用处的方法
                // public boolean startViewServer(int port) throws android.os.RemoteException;
                // public boolean stopViewServer() throws android.os.RemoteException;
                // public boolean isViewServerRunning() throws android.os.RemoteException;
                // public int getPendingAppTransition() throws android.os.RemoteException;
                // public boolean isKeyguardLocked() throws android.os.RemoteException;
                // public boolean isKeyguardSecure() throws android.os.RemoteException;
                // public boolean inKeyguardRestrictedInputMode() throws android.os.RemoteException;

                // public boolean isRotationFrozen() throws android.os.RemoteException;
                // public boolean hasNavigationBar() throws android.os.RemoteException;
                // public boolean hasPermanentMenuKey() throws android.os.RemoteException;
                // public boolean needsNavigationBar() throws android.os.RemoteException;
                // public boolean isSafeModeEnabled() throws android.os.RemoteException;

                // public float getAnimationScale(int which) throws android.os.RemoteException;
                // public float[] getAnimationScales() throws android.os.RemoteException;
                // public float getCurrentAnimatorScale() throws android.os.RemoteException;
                // public int getRotation() throws android.os.RemoteException;
                // public int getPreferredOptionsPanelGravity() throws android.os.RemoteException;

                if ( methodName.startsWith("start") || methodName.startsWith("stop") ||  methodName.startsWith("getPending")  || methodName.startsWith("inKeyguard") ||
                        methodName.startsWith("is") || methodName.startsWith("has") || methodName.startsWith("needs") || methodName.startsWith("getAnimation") ||
                        methodName.startsWith("getCurrent") || methodName.equals("getRotation") || methodName.equals("getPreferredOptionsPanelGravity") ) {
                    return null;
                }

                if (parameterTypes.length == 0) {
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                // public int getInitialDisplayDensity(int displayId) throws android.os.RemoteException;
                // public int getBaseDisplayDensity(int displayId) throws android.os.RemoteException;
                if (parameterTypes.length == 1) {

                    if (parameterTypes[0] == int.class) {

                        if (methodName.contains("Density")) {

                            DisplayManagerInfo.iterateAllDisplayIds(mContext, new DisplayManagerInfo.IterateDisplayIdsHandler() {
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


}
