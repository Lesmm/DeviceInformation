package com.deviceinfo.info;

import android.content.Context;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import common.modules.util.IReflectUtil;

public class SubscriptionManagerInfo {

    public static JSONObject getInfo(Context mContext) {
        JSONObject subInfo = getISubInfo(mContext);

        return subInfo;
    }

    public static JSONObject getISubInfo(final Context mContext) {
        IBinder mRemote = InvokerOfService.getService ("isub");
        Object proxy = InvokerOfService.asInterface("com.android.internal.telephony.ISub$Stub", mRemote);
        final String opPackageName = mContext.getPackageName();

        SubscriptionManager subscriptionManager = (SubscriptionManager) mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        // ---------------------------- just for look ----------------------------
        try {
            Integer dataSubId = (Integer) IReflectUtil.invokeMethod(proxy, "getDefaultDataSubId", new Class[]{}, new Object[]{});
            Integer subId = (Integer) IReflectUtil.invokeMethod(proxy, "getDefaultSmsSubId", new Class[]{}, new Object[]{});
            Integer smsSubId = (Integer) IReflectUtil.invokeMethod(proxy, "getDefaultSubId", new Class[]{}, new Object[]{});
            Integer voiceSubId = (Integer) IReflectUtil.invokeMethod(proxy, "getDefaultVoiceSubId", new Class[]{}, new Object[]{});

            int activeCount = SubscriptionManager.from(mContext).getActiveSubscriptionInfoCount();  // 当前Active的个数
            int activeCountMax = SubscriptionManager.from(mContext).getActiveSubscriptionInfoCountMax();    // 最大允许Active的个数
            List<SubscriptionInfo> activeList = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
            SubscriptionInfo activeInfo = SubscriptionManager.from(mContext).getActiveSubscriptionInfo(1);   // 从1开始

            List<SubscriptionInfo> allList = (List<SubscriptionInfo>)IReflectUtil.invokeMethod(proxy, "getAllSubInfoList", new Class[]{String.class}, new Object[]{opPackageName});
            int allCount = (Integer)IReflectUtil.invokeMethod(proxy, "getAllSubInfoCount", new Class[]{String.class}, new Object[]{opPackageName});

            Log.d("DeviceInfo","_set_debug_here_");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ---------------------------- just for look ----------------------------

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
                if ( parameterTypes.length == 1 && parameterTypes[0] == String.class
                        && ( methodName.equals("getAllSubInfoList") || methodName.equals("getAllSubInfoCount")
                        || methodName.equals("getActiveSubscriptionInfoList") || methodName.equals("getActiveSubInfoCount")
                ) ) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }

                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                // public android.telephony.SubscriptionInfo getActiveSubscriptionInfo(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public android.telephony.SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx, java.lang.String callingPackage) throws android.os.RemoteException;
                if ( parameterTypes.length == 2 && parameterTypes[0] == int.class && parameterTypes[1] == String.class
                        && ( methodName.equals("getActiveSubscriptionInfo") || methodName.equals("getActiveSubscriptionInfoForSimSlotIndex") )
                ) {
                    // getActiveSubscriptionInfo_arg0_int_1 // subId 从1开始
                    // getActiveSubscriptionInfoForSimSlotIndex_arg0_int_0 // slotIdx 卡槽位置从0开始
                    if (methodName.equals("getActiveSubscriptionInfo")) {
                        IterateActiveSubscriptionInfoList(mContext, new IterateHandler() {
                            @Override
                            public void handle(SubscriptionInfo info) throws Exception {
                                int mId = (Integer) IReflectUtil.getFieldValue(info, "mId");
                                String key = fMethodName + "_arg0_int_" + mId;
                                Object value = fMethod.invoke(fObj, new Object[]{mId, opPackageName});
                                if (value != null) {
                                    fResultMap.put(key, value);
                                }
                            }
                        });
                    }
                    if (methodName.equals("getActiveSubscriptionInfoForSimSlotIndex")) {
                        IterateActiveSubscriptionInfoList(mContext, new IterateHandler() {
                            @Override
                            public void handle(SubscriptionInfo info) throws Exception {
                                int mSimSlotIndex = (Integer) IReflectUtil.getFieldValue(info, "mSimSlotIndex");
                                String key = fMethodName + "_arg0_int_" + mSimSlotIndex;
                                Object value = fMethod.invoke(fObj, new Object[]{mSimSlotIndex, opPackageName});
                                if (value != null) {
                                    fResultMap.put(key, value);
                                }
                            }
                        });
                    }
                    return null;
                }

                // public android.telephony.SubscriptionInfo getActiveSubscriptionInfoForIccId(java.lang.String iccId, java.lang.String callingPackage) throws android.os.RemoteException;
                if ( parameterTypes.length == 2 && parameterTypes[0] == String.class && parameterTypes[1] == String.class
                 && methodName.equals("getActiveSubscriptionInfoForIccId")
                ) {

                    IterateActiveSubscriptionInfoList(mContext, new IterateHandler() {
                        @Override
                        public void handle(SubscriptionInfo info) throws Exception {
                            // TODO ... 测试插了 171 的卡取出的 value 为 null
                            String mIccId = (String) IReflectUtil.getFieldValue(info, "mIccId");
                            String key = fMethodName + "_arg0_String_" + mIccId;
                            Object value = fMethod.invoke(fObj, new Object[]{mIccId, opPackageName});
                            if (value != null) {
                                fResultMap.put(key, value);
                            }
                        }
                    });

                    return null;
                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }


    // ---------------------- 遍历 SubscriptionInfo 工具方法 ----------------------

    public static interface IterateHandler {
        public void handle(SubscriptionInfo info) throws Exception;
    }

    private static List<SubscriptionInfo> activeSubscriptionInfoList = null;
    public static void IterateActiveSubscriptionInfoList(Context mContext, IterateHandler handler) {
        try {

            if (activeSubscriptionInfoList == null) {
                List<SubscriptionInfo> activeList = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
                activeSubscriptionInfoList = activeList;
            }
            for (int i = 0; i < activeSubscriptionInfoList.size(); i++) {
                try {
                    SubscriptionInfo info = activeSubscriptionInfoList.get(i);
                    handler.handle(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static List<SubscriptionInfo> allSubscriptionInfoList = null;
    public static void IterateAllSubscriptionInfoList(Context mContext, IterateHandler handler) {
        try {

            if (allSubscriptionInfoList == null) {
                String packageName = mContext.getPackageName();
                Object proxy = InvokerOfService.getProxy ("com.android.internal.telephony.ISub","isub");
                List<SubscriptionInfo> allList = (List<SubscriptionInfo>)IReflectUtil.invokeMethod(proxy, "getAllSubInfoList", new Class[]{String.class}, new Object[]{packageName});
                allSubscriptionInfoList = allList;
            }
            for (int i = 0; i < allSubscriptionInfoList.size(); i++) {
                try {
                    SubscriptionInfo info = allSubscriptionInfoList.get(i);
                    handler.handle(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
