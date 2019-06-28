package com.deviceinfo;

import android.content.Context;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import common.modules.util.IReflectUtil;
import common.modules.util.JSONObjectExtended;

public class SubscriptionManagerInfo {

    public static JSONObject getInfo(Context mContext) {
        JSONObject subInfo = getISubInfo(mContext);

        return subInfo;
    }

    public static JSONObject getISubInfo(final Context mContext) {
        IBinder mRemote = InvokerOfService.getService ("isub");
        Object proxy = InvokerOfService.asInterface("com.android.internal.telephony.ISub$Stub", mRemote);
        final Object opPackageName = mContext.getPackageName();

        SubscriptionManager subscriptionManager = (SubscriptionManager) mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        // ---------------------------- just test for look ----------------------------
        InvokerOfService.logMethodsOfObject(proxy);
        try {
            Integer dataSubId = (Integer) IReflectUtil.invokeMethod(proxy, "getDefaultDataSubId", new Class[]{}, new Object[]{});
            Integer subId = (Integer) IReflectUtil.invokeMethod(proxy, "getDefaultSmsSubId", new Class[]{}, new Object[]{});
            Integer smsSubId = (Integer) IReflectUtil.invokeMethod(proxy, "getDefaultSubId", new Class[]{}, new Object[]{});
            Integer voiceSubId = (Integer) IReflectUtil.invokeMethod(proxy, "getDefaultVoiceSubId", new Class[]{}, new Object[]{});

            int count = SubscriptionManager.from(mContext).getActiveSubscriptionInfoCount();
            int countMax = SubscriptionManager.from(mContext).getActiveSubscriptionInfoCountMax();
            List<SubscriptionInfo> list = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
            SubscriptionInfo info = SubscriptionManager.from(mContext).getActiveSubscriptionInfo(1);   // 从1开始

            Log.d("DeviceInfo","_set_debug_here_");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ---------------------------- just test for look ----------------------------

        Map<?, ?> result = InvokerOfObject.invokeObjectMethods(proxy, new InvokerOfObject.InvokeHandler() {
            @Override
            public Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes, Class<?> returnType) throws Exception {
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

                if (parameterTypes.length == 0) {

                    // public int clearSubInfo() throws android.os.RemoteException;
                    // public void clearDefaultsForInactiveSubIds() throws android.os.RemoteException;
                    if (methodName.startsWith("clear")) {
                        return null;
                    }
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                // public java.util.List<android.telephony.SubscriptionInfo> getAllSubInfoList(java.lang.String callingPackage) throws android.os.RemoteException;
                // public int getAllSubInfoCount(java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.util.List<android.telephony.SubscriptionInfo> getActiveSubscriptionInfoList(java.lang.String callingPackage) throws android.os.RemoteException;
                // public int getActiveSubInfoCount(java.lang.String callingPackage) throws android.os.RemoteException;
                if ( parameterTypes.length == 1 && parameterTypes[0] == java.lang.String.class
                        && ( methodName.equals("getAllSubInfoList") || methodName.equals("getAllSubInfoCount")
                        || methodName.equals("getActiveSubscriptionInfoList") || methodName.equals("getActiveSubInfoCount")
                ) ) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }

}
