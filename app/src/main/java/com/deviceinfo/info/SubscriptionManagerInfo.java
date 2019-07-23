package com.deviceinfo.info;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.Manager;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.modules.util.IArrayUtil;
import common.modules.util.IReflectUtil;

public class SubscriptionManagerInfo {

    private static int isSupportedFeatureSubscription = -1;

    public static JSONObject getInfo(Context mContext) {
        JSONObject subInfo = getISubInfo(mContext);

        return subInfo;
    }

    // Android 4.4 无此服务 及 SubscriptionManager、SubscriptionInfo 这些 class
    private static boolean isSupportedFeatureSubscription() {
        if (isSupportedFeatureSubscription == -1) {
            if (Build.VERSION.SDK_INT < 22) {
                isSupportedFeatureSubscription = 0;
            } else {
                IBinder mRemote = InvokerOfService.getService("isub");
                if (mRemote == null) {
                    isSupportedFeatureSubscription = 0;
                } else {
                    isSupportedFeatureSubscription = 1;
                }
            }
        }
        return isSupportedFeatureSubscription == 1;
    }

    public static JSONObject getISubInfo(final Context mContext) {
        // Android 4.4 无此服务 及 SubscriptionManager、SubscriptionInfo 这些 class
        if (!isSupportedFeatureSubscription()) {
            return null;
        }

        IBinder mRemote = InvokerOfService.getService("isub");
        Object proxy = InvokerOfService.asInterface("com.android.internal.telephony.ISub$Stub", mRemote);
        final String opPackageName = mContext.getPackageName();
        SubscriptionManager subscriptionManager = (SubscriptionManager) mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        if (Manager.IS_DEBUG) {
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

                List<SubscriptionInfo> allList = (List<SubscriptionInfo>) IReflectUtil.invokeMethod(proxy, "getAllSubInfoList", new Class[]{String.class}, new Object[]{opPackageName});
                int allCount = (Integer) IReflectUtil.invokeMethod(proxy, "getAllSubInfoCount", new Class[]{String.class}, new Object[]{opPackageName});

                Log.d("DeviceInfo", "_set_debug_here_");
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ---------------------------- just for look ----------------------------
        }


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

                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                if (parameterTypes.length == 1) {

                    // public java.util.List<android.telephony.SubscriptionInfo> getAllSubInfoList(java.lang.String callingPackage) throws android.os.RemoteException;
                    // public int getAllSubInfoCount(java.lang.String callingPackage) throws android.os.RemoteException;
                    // public java.util.List<android.telephony.SubscriptionInfo> getActiveSubscriptionInfoList(java.lang.String callingPackage) throws android.os.RemoteException;
                    // public int getActiveSubInfoCount(java.lang.String callingPackage) throws android.os.RemoteException;
                    if (parameterTypes[0] == String.class) {

                        if (methodName.equals("getAllSubInfoList") || methodName.equals("getAllSubInfoCount") ||
                                methodName.equals("getActiveSubscriptionInfoList") || methodName.equals("getActiveSubInfoCount")) {

                            Object value = method.invoke(obj, new Object[]{opPackageName});
                            return value;
                        }

                    }

                    // public int getSlotId(int subId) throws android.os.RemoteException;
                    // public int getPhoneId(int subId) throws android.os.RemoteException;
                    // public int getSubState(int subId) throws android.os.RemoteException;
                    // public boolean isActiveSubId(int subId) throws android.os.RemoteException;

                    // public int[] getSubId(int slotId) throws android.os.RemoteException;
                    // public int getSimStateForSlotIdx(int slotIdx) throws android.os.RemoteException;

                    if (parameterTypes[0] == int.class) {

                        if (methodName.equals("getSlotId") || methodName.equals("getPhoneId") || methodName.equals("getSubState") || methodName.equals("isActiveSubId")) {
                            // subId
                            ID.iterateActiveSubIds(mContext, new ID.IterateIdsHandler() {
                                @Override
                                public void handle(int subId) throws Exception {

                                    String methodKey = fMethodName + "_with_args";
                                    Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                    if (methodArgsMap == null) {
                                        methodArgsMap = new HashMap();
                                        fResultMap.put(methodKey, methodArgsMap);
                                    }

                                    String key = "_arg0_int_" + subId;
                                    Object value = fMethod.invoke(fObj, new Object[]{subId});
                                    if (value != null) {
                                        methodArgsMap.put(key, value);
                                    }

                                }
                            });
                        }


                        if (methodName.equals("getSubId") || methodName.equals("getSimStateForSlotIdx")) {
                            // slotId
                            ID.iterateActiveSlotIndexes(mContext, new ID.IterateIdsHandler() {
                                @Override
                                public void handle(int slotId) throws Exception {

                                    String methodKey = fMethodName + "_with_args";
                                    Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                    if (methodArgsMap == null) {
                                        methodArgsMap = new HashMap();
                                        fResultMap.put(methodKey, methodArgsMap);
                                    }

                                    String key = "_arg0_int_" + slotId;
                                    Object value = fMethod.invoke(fObj, new Object[]{slotId});
                                    if (value != null) {
                                        methodArgsMap.put(key, value);
                                    }

                                }
                            });
                        }

                    }

                }

                // public android.telephony.SubscriptionInfo getActiveSubscriptionInfo(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public android.telephony.SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx, java.lang.String callingPackage) throws android.os.RemoteException;
                if (parameterTypes.length == 2 && parameterTypes[0] == int.class && parameterTypes[1] == String.class
                        && (methodName.equals("getActiveSubscriptionInfo") || methodName.equals("getActiveSubscriptionInfoForSimSlotIndex"))
                ) {
                    // getActiveSubscriptionInfo_arg0_int_1 // subId 从1开始
                    // getActiveSubscriptionInfoForSimSlotIndex_arg0_int_0 // slotIdx 卡槽位置从0开始
                    if (methodName.equals("getActiveSubscriptionInfo")) {
                        INFO.iterateActiveSubscriptionInfoList(mContext, new INFO.IterateInfosHandler() {
                            @Override
                            public void handle(SubscriptionInfo info) throws Exception {

                                String methodKey = fMethodName + "_with_args";
                                Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                if (methodArgsMap == null) {
                                    methodArgsMap = new HashMap();
                                    fResultMap.put(methodKey, methodArgsMap);
                                }

                                int mId = (Integer) IReflectUtil.getFieldValue(info, "mId");
                                String key = "_arg0_int_" + mId;
                                Object value = fMethod.invoke(fObj, new Object[]{mId, opPackageName});
                                if (value != null) {
                                    methodArgsMap.put(key, value);
                                }

                            }
                        });
                    }
                    if (methodName.equals("getActiveSubscriptionInfoForSimSlotIndex")) {
                        INFO.iterateActiveSubscriptionInfoList(mContext, new INFO.IterateInfosHandler() {
                            @Override
                            public void handle(SubscriptionInfo info) throws Exception {

                                String methodKey = fMethodName + "_with_args";
                                Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                if (methodArgsMap == null) {
                                    methodArgsMap = new HashMap();
                                    fResultMap.put(methodKey, methodArgsMap);
                                }

                                int mSimSlotIndex = (Integer) IReflectUtil.getFieldValue(info, "mSimSlotIndex");
                                String key = "_arg0_int_" + mSimSlotIndex;
                                Object value = fMethod.invoke(fObj, new Object[]{mSimSlotIndex, opPackageName});
                                if (value != null) {
                                    methodArgsMap.put(key, value);
                                }

                            }
                        });
                    }
                    return null;
                }

                // public android.telephony.SubscriptionInfo getActiveSubscriptionInfoForIccId(java.lang.String iccId, java.lang.String callingPackage) throws android.os.RemoteException;
                if (parameterTypes.length == 2 && parameterTypes[0] == String.class && parameterTypes[1] == String.class
                        && methodName.equals("getActiveSubscriptionInfoForIccId")
                ) {

                    INFO.iterateActiveSubscriptionInfoList(mContext, new INFO.IterateInfosHandler() {
                        @Override
                        public void handle(SubscriptionInfo info) throws Exception {

                            String methodKey = fMethodName + "_with_args";
                            Map methodArgsMap = (Map) fResultMap.get(methodKey);
                            if (methodArgsMap == null) {
                                methodArgsMap = new HashMap();
                                fResultMap.put(methodKey, methodArgsMap);
                            }

                            // TODO ... 测试插了 171 的卡取出后的 value 为 null
                            String mIccId = (String) IReflectUtil.getFieldValue(info, "mIccId");
                            String key = "_arg0_String_" + mIccId;
                            Object value = fMethod.invoke(fObj, new Object[]{mIccId, opPackageName});
                            if (value != null) {
                                methodArgsMap.put(key, value);
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
    public static class INFO {

        public static interface IterateInfosHandler {
            public void handle(SubscriptionInfo info) throws Exception;
        }

        private static void iterateInfoList(List<SubscriptionInfo> infosList, IterateInfosHandler handler) {
            try {
                for (int i = 0; infosList != null && i < infosList.size(); i++) {
                    try {
                        SubscriptionInfo info = infosList.get(i);
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


        private static List<SubscriptionInfo> activeSubscriptionInfoList = null;

        public static List<SubscriptionInfo> getActiveSubscriptionInfoList(Context mContext) {
            if (activeSubscriptionInfoList == null) {
                List<SubscriptionInfo> activeList = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
                activeSubscriptionInfoList = activeList;
            }
            return activeSubscriptionInfoList;
        }

        public static void iterateActiveSubscriptionInfoList(Context mContext, IterateInfosHandler handler) {
            if (!isSupportedFeatureSubscription()) {
                return;
            }
            iterateInfoList(getActiveSubscriptionInfoList(mContext), handler);
        }

        // ---------------------- 遍历 全部 SubscriptionInfo 工具方法 ----------------------
        private static List<SubscriptionInfo> allSubscriptionInfoList = null;

        public static List<SubscriptionInfo> getAllSubscriptionInfoList(Context mContext) {
            if (allSubscriptionInfoList == null) {
                String packageName = mContext.getPackageName();
                Object proxy = InvokerOfService.getProxy("com.android.internal.telephony.ISub", "isub");
                List<SubscriptionInfo> allList = (List<SubscriptionInfo>) IReflectUtil.invokeMethod(proxy, "getAllSubInfoList", new Class[]{String.class}, new Object[]{packageName});
                allSubscriptionInfoList = allList;
            }
            return allSubscriptionInfoList;
        }

        public static void iterateAllSubscriptionInfoList(Context mContext, IterateInfosHandler handler) {
            if (!isSupportedFeatureSubscription()) {
                return;
            }
            iterateInfoList(getAllSubscriptionInfoList(mContext), handler);
        }

    }


    public static class ID {

        public static interface IterateIdsHandler {
            public void handle(int _id_) throws Exception;
        }

        private static void iterateIds(int[] ids, IterateIdsHandler idHandler) {
            try {
                for (int i = 0; ids != null && i < ids.length; i++) {
                    try {
                        int _id_ = ids[i];
                        idHandler.handle(_id_);
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

        // ---------------------- 遍历 SubscriptionId 工具方法 ----------------------
        private static int[] activeSubIds = null;

        private static int[] getActiveSubIds(Context mContext) {
            if (activeSubIds == null) {
                List<SubscriptionInfo> activeList = INFO.getActiveSubscriptionInfoList(mContext);
                activeSubIds = new int[activeList.size()];
                for (int i = 0; i < activeList.size(); i++) {
                    SubscriptionInfo info = activeList.get(i);
                    int subscriptionId = info.getSubscriptionId();
                    activeSubIds[i] = subscriptionId;
                }
            }
            return activeSubIds;
        }

        public static void iterateActiveSubIds(Context mContext, IterateIdsHandler subIdHandler) {
            if (!isSupportedFeatureSubscription()) {
                return;
            }
            iterateIds(getActiveSubIds(mContext), subIdHandler);
        }


        // ---------------------- 遍历 PhoneId 工具方法 ----------------------
        private static int[] activePhoneIds = null;

        private static int[] getActivePhoneIds(Context mContext) {
            if (activePhoneIds == null) {
                List<SubscriptionInfo> allList = INFO.getAllSubscriptionInfoList(mContext);
                activePhoneIds = new int[allList.size()];
                for (int i = 0; i < allList.size(); i++) {
                    try {
                        SubscriptionInfo subscriptionInfo = allList.get(i);
                        int subscriptionId = subscriptionInfo.getSubscriptionId();
//                int[] activeSubIds = getActiveSubIds(mContext);
//                activePhoneIds = new int[activeSubIds.length];
//                for (int i = 0; i < activeSubIds.length; i++) {
//                    try {
//                        int subscriptionId = activeSubIds[i];
                        Integer phoneId = (Integer) IReflectUtil.invokeMethod(SubscriptionManager.class, "getPhoneId", new Class[]{int.class}, new Object[]{subscriptionId});
                        if (phoneId == null) {
                            Object proxy = InvokerOfService.getProxy("com.android.internal.telephony.ISub", "isub");
                            phoneId = (Integer) IReflectUtil.invokeMethod(proxy, "getPhoneId", new Class[]{int.class}, new Object[]{subscriptionId});
                        }
                        activePhoneIds[i] = phoneId;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                activePhoneIds = IArrayUtil.removeDuplicateValue(activePhoneIds);
            }
            return activePhoneIds;
        }

        public static void iterateActivePhoneIds(Context mContext, IterateIdsHandler phoneIdHandler) {
            if (!isSupportedFeatureSubscription()) {
                return;
            }
            iterateIds(getActivePhoneIds(mContext), phoneIdHandler);
        }


        // ---------------------- 遍历 Slot Index 工具方法 ----------------------
        private static int[] activeSlotIndexes = null;

        private static int[] getActiveSlotIndexes(Context mContext) {
            if (activeSlotIndexes == null) {
                int[] activeSubIds = getActiveSubIds(mContext);
                activeSlotIndexes = new int[activeSubIds.length];

                for (int i = 0; i < activeSubIds.length; i++) {
                    try {
                        int subscriptionId = activeSubIds[i];
                        Integer slotIdex = (Integer) IReflectUtil.invokeMethod(SubscriptionManager.class, "getSlotIndex", new Class[]{int.class}, new Object[]{subscriptionId});
                        if (slotIdex == null) {
                            Object proxy = InvokerOfService.getProxy("com.android.internal.telephony.ISub", "isub");
                            slotIdex = (Integer) IReflectUtil.invokeMethod(proxy, "getSlotIndex", new Class[]{int.class}, new Object[]{subscriptionId});
                        }
                        activeSlotIndexes[i] = slotIdex;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return activeSlotIndexes;
        }

        public static void iterateActiveSlotIndexes(Context mContext, IterateIdsHandler slotIndexHandler) {
            if (!isSupportedFeatureSubscription()) {
                return;
            }
            iterateIds(getActiveSlotIndexes(mContext), slotIndexHandler);
        }

    }


}
