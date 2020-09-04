package com.deviceinfo.info;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.ManagerInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import common.modules.util.IReflectUtil;

// done with api diff
public class TelephonyManagerInfo {

    public static JSONObject getInfo(Context mContext, JSONObject subscriptionInfo) {
        JSONObject telephonyResult = new JSONObject();

        // 通过调用高层接口
        try {
            // Android 9.0 以后，不能通过反射来获取一些API的值了!!! 所以这里调用高层API再手动处理一下。
            // https://developer.android.google.cn/distribute/best-practices/develop/restrictions-non-sdk-interfaces?hl=zh_cn
            // https://developer.android.google.cn/preview/behavior-changes-all?hl=zh_cn#preferences
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

            // 1. 调用高层接口
            Map managerMap = InvokerOfObject.invokeObjectMethodsWithGetPrefixZeroArgs(telephonyManager);
            JSONObject managerInfo = new JSONObjectExtended(managerMap);

            // 2. 移除掉一些无用的WIFI_SERVICE
            managerInfo.remove("getITelephony");
            managerInfo.remove("getCarrierConfig"); // TODO ... 里面信息有没有标识设备的信息，有空再看
            managerInfo.remove("getPhoneCount");
            managerInfo.remove("getSimCount");  // 与 getPhoneCount 是相同意义的东西
            managerInfo.remove("getProcCmdLine");   // 与 /proc/cmdline 的值相同, Android 8.0 以上都获取不到的
            // 都是通过 SystemProperties 获取同步过来的的, 查看 TelephonyManager.getTelephonyProperty 方法，根把phoneId作为index, 添加','作为分隔符
            managerInfo.remove("getLteOnCdmaModeStatic"); // TODO ... 此值与 SystemProperties.getInt(TelephonyProperties.PROPERTY_LTE_ON_CDMA_DEVICE, ..) 得同步
            managerInfo.remove("getMultiSimConfiguration"); // TODO ... 此值与 SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG) 得同步
            managerInfo.remove("getNetworkOperator");
            managerInfo.remove("getNetworkOperatorName");
            managerInfo.remove("getSimCountryIso");
            managerInfo.remove("getSimOperator");
            managerInfo.remove("getSimOperatorName");
            managerInfo.remove("getSimOperatorNumeric");

            // 3. 转换，与低级API aidl层对应
            // getCellLocation 的 key 值不变, value 里的key根据 CellLocation.newFromBundle(bundle) 需把前缀m去掉，再把根在m后的第一个字母变小写
            JSONObject getCellLocationInfo = managerInfo.optJSONObject("getCellLocation");
            JSONArray names = getCellLocationInfo != null ? getCellLocationInfo.names() : null;
            for (int i = 0; names != null && i < names.length(); i++) {
                try {
                    String key = names.optString(i);
                    if (key.startsWith("m") && key.length() > 2) {
                        String newKey = key.substring(1);
                        newKey = newKey.substring(0, 1).toLowerCase() + newKey.substring(1, newKey.length() - 1);
                        getCellLocationInfo.put(newKey, getCellLocationInfo.opt(key));
                        getCellLocationInfo.remove(key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Map<String, String> api2LowerApiMapping = new HashMap();
            api2LowerApiMapping.put("getCurrentPhoneType", "getActivePhoneTypeForSlot"); // TODO ... 此外，还通过getPhoneTypeFromProperty方法获取i.e.'ro.telephony.default_network'以后有空再完善
            api2LowerApiMapping.put("getDataEnabled", "isUserDataEnabled");
            api2LowerApiMapping.put("getDataNetworkType", "getDataNetworkTypeForSubscriber");
            api2LowerApiMapping.put("getDeviceSoftwareVersion", "getDeviceSoftwareVersionForSlot");
            api2LowerApiMapping.put("getImei", "getImeiForSlot");
            api2LowerApiMapping.put("getLine1Number", "getLine1NumberForDisplay");
            api2LowerApiMapping.put("getLteOnCdmaMode", "getLteOnCdmaModeForSubscriber");
            api2LowerApiMapping.put("getMeid", "getMeidForSlot");
            api2LowerApiMapping.put("getMsisdn", "getMsisdnForSubscriber");
            api2LowerApiMapping.put("getNai", "getNaiForSubscriber");
            api2LowerApiMapping.put("getNetworkCountryIso", "getNetworkCountryIsoForPhone");
            api2LowerApiMapping.put("getNetworkType", "getNetworkTypeForSubscriber");
            api2LowerApiMapping.put("getPhoneType", "getActivePhoneTypeForSlot");
            api2LowerApiMapping.put("getServiceState", "getServiceStateForSubscriber");
            api2LowerApiMapping.put("getSimCarrierId", "getSubscriptionCarrierId");
            api2LowerApiMapping.put("getSimSerialNumber", "getIccSerialNumberForSubscriber");
            api2LowerApiMapping.put("getSubscriberId", "getSubscriberIdForSubscriber");
            api2LowerApiMapping.put("getVoiceMailAlphaTag", "getVoiceMailAlphaTagForSubscriber");
            api2LowerApiMapping.put("getVoiceMailNumber", "getVoiceMailNumberForSubscriber");
            api2LowerApiMapping.put("getVoiceMessageCount", "getVoiceMessageCountForSubscriber");
            api2LowerApiMapping.put("getVoiceNetworkType", "getVoiceNetworkTypeForSubscriber");
            for (String key : api2LowerApiMapping.keySet()) {
                try {
                    String toKey = api2LowerApiMapping.get(key);
                    Object obj = managerInfo.opt(key);
                    if (obj != null) {
                        managerInfo.put(toKey, obj);
                    }
                    if (toKey.startsWith(key)) {
                        // nothing ...
                    } else {
                        managerInfo.remove(key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            // 4. 需要转到 SubscriptionManager 去的
            // getDefaultSubI
            // getNetworkSpecifier 其实就是 getSubId， getSubId 同步去外层 Subscription -> getDefaultSubId, String值改成int
            Map<String, String> subscriptionMapping = new HashMap<>();
            String getNetworkSpecifier = managerInfo.optString("getNetworkSpecifier");
            if (getNetworkSpecifier != null && !getNetworkSpecifier.isEmpty()) {
                try {
                    int subId = Integer.parseInt(getNetworkSpecifier);
                    managerInfo.put("getSubId", subId);
                    managerInfo.remove("getNetworkSpecifier");
                    // subscriptionMapping.put("getSubId", "getDefaultSubId"); // 不需要加进来了，SubscriptionManager高层API可以拿到
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // getSimStateForSlotIndex
            subscriptionMapping.put("getSimState", "getSimStateForSlotIndex");
            managerInfo.remove("getSimApplicationState");
            managerInfo.remove("getSimCardState");
            // getSlotIndex
            subscriptionMapping.put("getSlotIndex", "getSlotIndex");
            for (String key : subscriptionMapping.keySet()) {
                try {
                    String toKey = subscriptionMapping.get(key);
                    Object obj = managerInfo.opt(key);
                    if (obj != null) {
                        subscriptionInfo.put(toKey, obj);
                    }
                    managerInfo.remove(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            // 5. 放到result中
            InfoJsonHelper.mergeJSONObject(telephonyResult, managerInfo);

            Log.d("DeviceInfo", "_set_debug_here_");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 通过反射来获取
        JSONObject telephonyInfo = getITelephonyInfo(mContext);
        JSONObject phoneSubInfo = getIPhoneSubInfo(mContext);
        JSONObject telecomInfo = getITelecomInfo(mContext);

        InfoJsonHelper.mergeJSONObject(telephonyResult, telephonyInfo);
        InfoJsonHelper.mergeJSONObject(telephonyResult, phoneSubInfo);
        InfoJsonHelper.mergeJSONObject(telephonyResult, telecomInfo);

        return telephonyResult;
    }

    public static JSONObject getITelephonyInfo(final Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        Object packageName = IReflectUtil.invokeMethod(mContext, "getOpPackageName", new Class[]{}, new Object[]{});
        if (packageName == null) {
            packageName = mContext.getPackageName();
        }
        final Object opPackageName = packageName;
        Object iTelephony = IReflectUtil.invokeMethod(telephonyManager, "getITelephony", new Class[]{}, new Object[]{});

        Map<?, ?> result = InvokerOfObject.invokeObjectMethods(iTelephony, new InvokerOfObject.InvokeHandler() {
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
                // all enable or disable
                if (methodName.startsWith("enable") || methodName.startsWith("disable")) {
                    return null;
                }

                // public boolean endCall() throws android.os.RemoteException;
                // public boolean endCallForSubscriber(int subId) throws android.os.RemoteException;
                // will block when without sim card
                if (methodName.startsWith("endCall")) {
                    return null;
                }
                // public byte[] getAtr() throws android.os.RemoteException;
                // public byte[] getAtrUsingSubId(int subId) throws android.os.RemoteException;
                // will crash when without sim card: com.android.phone E/PhoneInterfaceManager: [PhoneIntfMgr] getIccId: ICC ID is null or empty.
                if (methodName.startsWith("getAtr")) {
                    return null;
                }

                // public boolean showCallScreen() throws android.os.RemoteException    // Android 4.4
                // public int getLastError() throws android.os.RemoteException;     // Android 4.4
                // public byte[] getATR() throws android.os.RemoteException;        // Android 4.4
                if (methodName.equals("showCallScreen") || methodName.equals("getLastError") || methodName.equals("getATR")) {
                    return null;
                }

                if (parameterTypes.length == 0) {
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                // public boolean isOffhook(java.lang.String callingPackage) throws android.os.RemoteException;
                // public boolean isRinging(java.lang.String callingPackage) throws android.os.RemoteException;
                // public boolean isIdle(java.lang.String callingPackage) throws android.os.RemoteException;
                // public boolean isRadioOn(java.lang.String callingPackage) throws android.os.RemoteException;
                // public boolean isSimPinEnabled(java.lang.String callingPackage) throws android.os.RemoteException;
                // public int getCdmaEriIconIndex(java.lang.String callingPackage) throws android.os.RemoteException;
                // public int getCdmaEriIconMode(java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getCdmaEriText(java.lang.String callingPackage) throws android.os.RemoteException;
                // public int getDataNetworkType(java.lang.String callingPackage) throws android.os.RemoteException;	// important!!!
                // public int getLteOnCdmaMode(java.lang.String callingPackage) throws android.os.RemoteException;
                // public int getCalculatedPreferredNetworkType(java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String[] getMergedSubscriberIds(java.lang.String callingPackage) throws android.os.RemoteException;
                // public boolean isVideoCallingEnabled(java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getDeviceId(java.lang.String callingPackage) throws android.os.RemoteException;	// important!!!

                // public java.util.List<android.telephony.CellInfo> getAllCellInfo(java.lang.String callingPkg) throws android.os.RemoteException;
                // public java.util.List<android.telephony.NeighboringCellInfo> getNeighboringCellInfo(java.lang.String callingPkg) throws android.os.RemoteException;
                // public android.os.Bundle getCellLocation(java.lang.String callingPkg) throws android.os.RemoteException;

                if (parameterTypes.length == 1 && parameterTypes[0] == String.class
                        && (methodName.equals("getDeviceId") || methodName.equals("getDataNetworkType")
                        || methodName.equals("isOffhook") || methodName.equals("isRinging")
                        || methodName.equals("isIdle") || methodName.equals("isRadioOn")
                        || methodName.equals("isSimPinEnabled") || methodName.equals("getCdmaEriIconIndex")
                        || methodName.equals("getCdmaEriIconMode") || methodName.equals("getCdmaEriText")
                        || methodName.equals("getLteOnCdmaMode") || methodName.equals("getCalculatedPreferredNetworkType")
                        || methodName.equals("getMergedSubscriberIds") || methodName.equals("isVideoCallingEnabled"))
                        || methodName.equals("getAllCellInfo") || methodName.equals("getNeighboringCellInfo")
                        || methodName.equals("getCellLocation")
                ) {

                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }


                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                if (parameterTypes.length == 1 && parameterTypes[0] == int.class) {

                    // public int getCallStateForSubscriber(int subId) throws android.os.RemoteException;
                    // public int getActivePhoneTypeForSubscriber(int subId) throws android.os.RemoteException;
                    // public int getVoiceMessageCountForSubscriber(int subId) throws android.os.RemoteException;
                    // public java.lang.String getIccOperatorNumericForData(int subId) throws android.os.RemoteException;
                    // public int getPreferredNetworkType(int subId) throws android.os.RemoteException;
                    // public com.android.internal.telephony.CellNetworkScanResult getCellNetworkScanResults(int subId) throws android.os.RemoteException;
                    // public boolean getDataEnabled(int subId) throws android.os.RemoteException;
                    // public java.lang.String getCdmaMdn(int subId) throws android.os.RemoteException;
                    // public java.lang.String getCdmaMin(int subId) throws android.os.RemoteException;
                    // public byte[] getAtrUsingSubId(int subId) throws android.os.RemoteException;         // TODO ... Check How to handle byte[] in Json end

                    // public java.lang.String getEsn(int subId) throws android.os.RemoteException;     // Android 8.1
                    // public java.lang.String getCdmaPrlVersion(int subId) throws android.os.RemoteException;  // Android 8.1
                    // public android.telephony.SignalStrength getSignalStrength(int subId) throws android.os.RemoteException; // Android 8.1

                    if (methodName.equals("getCallStateForSubscriber") || methodName.equals("getActivePhoneTypeForSubscriber")
                            || methodName.equals("getVoiceMessageCountForSubscriber") || methodName.equals("getIccOperatorNumericForData")
                            || methodName.equals("getPreferredNetworkType") || methodName.equals("getCellNetworkScanResults")
                            || methodName.equals("getDataEnabled") || methodName.equals("getCdmaMdn")
                            || methodName.equals("getCdmaMin") || methodName.equals("getAtrUsingSubId")
                            || methodName.equals("getEsn") || methodName.equals("getCdmaPrlVersion")
                            || methodName.equals("getSignalStrength")
                    ) {

                        SubscriptionManagerInfo.ID.iterateActiveSubIds(mContext, new SubscriptionManagerInfo.ID.IterateIdsHandler() {
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

                    // public boolean hasIccCardUsingSlotId(int slotId) throws android.os.RemoteException;
                    // public java.util.List<android.service.carrier.CarrierIdentifier> getAllowedCarriers(int slotIndex) throws android.os.RemoteException;  // Android 9.0
                    if (methodName.equals("hasIccCardUsingSlotId") || methodName.equals("getAllowedCarriers")) {
                        SubscriptionManagerInfo.ID.iterateActiveSlotIndexes(mContext, new SubscriptionManagerInfo.ID.IterateIdsHandler() {
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

                if (parameterTypes.length == 2 && parameterTypes[0] == int.class && parameterTypes[1] == String.class) {

                    // public boolean isOffhookForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public boolean isRingingForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public boolean isIdleForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public boolean isRadioOnForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public int getCdmaEriIconIndexForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public int getCdmaEriIconModeForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public java.lang.String getCdmaEriTextForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public int getNetworkTypeForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public int getDataNetworkTypeForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public int getVoiceNetworkTypeForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public int getLteOnCdmaModeForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public java.lang.String getLine1NumberForDisplay(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    // public java.lang.String getLine1AlphaTagForDisplay(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                    if (methodName.equals("isOffhookForSubscriber") || methodName.equals("isRingingForSubscriber")
                            || methodName.equals("isIdleForSubscriber") || methodName.equals("isRadioOnForSubscriber")
                            || methodName.equals("getCdmaEriIconIndexForSubscriber") || methodName.equals("getCdmaEriIconModeForSubscriber")
                            || methodName.equals("getCdmaEriTextForSubscriber") || methodName.equals("getNetworkTypeForSubscriber")
                            || methodName.equals("getDataNetworkTypeForSubscriber") || methodName.equals("getVoiceNetworkTypeForSubscriber")
                            || methodName.equals("getLteOnCdmaModeForSubscriber") || methodName.equals("getLine1NumberForDisplay")
                            || methodName.equals("getLine1AlphaTagForDisplay")
                    ) {

                        SubscriptionManagerInfo.ID.iterateActiveSubIds(mContext, new SubscriptionManagerInfo.ID.IterateIdsHandler() {
                            @Override
                            public void handle(int subId) throws Exception {

                                String methodKey = fMethodName + "_with_args";
                                Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                if (methodArgsMap == null) {
                                    methodArgsMap = new HashMap();
                                    fResultMap.put(methodKey, methodArgsMap);
                                }

                                String key = "_arg0_int_" + subId;
                                Object value = fMethod.invoke(fObj, new Object[]{subId, opPackageName});
                                if (value != null) {
                                    methodArgsMap.put(key, value);
                                }

                            }
                        });

                    }


                    // Note: phoneId can be got by subId in SubscriptionManager.getPhoneId(int subId) or ISub getPhoneId(int subId) interface ...
                    // public int getRadioAccessFamily(int phoneId, java.lang.String callingPackage) throws android.os.RemoteException;
                    if (methodName.equals("getRadioAccessFamily")) {

                        SubscriptionManagerInfo.ID.iterateActivePhoneIds(mContext, new SubscriptionManagerInfo.ID.IterateIdsHandler() {
                            @Override
                            public void handle(int phoneId) throws Exception {

                                String methodKey = fMethodName + "_with_args";
                                Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                if (methodArgsMap == null) {
                                    methodArgsMap = new HashMap();
                                    fResultMap.put(methodKey, methodArgsMap);
                                }

                                String key = "_arg0_int_" + phoneId;
                                Object value = fMethod.invoke(fObj, new Object[]{phoneId, opPackageName});
                                if (value != null) {
                                    methodArgsMap.put(key, value);
                                }

                            }
                        });

                    }

                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }

    public static JSONObject getIPhoneSubInfo(final Context mContext) {
        // iphonesubinfo: [com.android.internal.telephony.IPhoneSubInfo]
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        Object packageName = IReflectUtil.invokeMethod(mContext, "getOpPackageName", new Class[]{}, new Object[]{});
        if (packageName == null) {
            packageName = mContext.getPackageName();
        }
        final Object opPackageName = packageName;
        Object iPhoneSubInfo = IReflectUtil.invokeMethod(telephonyManager, "getSubscriberInfo", new Class[]{}, new Object[]{});

        Map<?, ?> result = InvokerOfObject.invokeObjectMethods(iPhoneSubInfo, new InvokerOfObject.InvokeHandler() {
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
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                // public java.lang.String getDeviceId(java.lang.String callingPackage) throws android.os.RemoteException;	// important !!!
                // public java.lang.String getDeviceSvn(java.lang.String callingPackage) throws android.os.RemoteException;	// important !!!
                // public java.lang.String getSubscriberId(java.lang.String callingPackage) throws android.os.RemoteException;	// important !!!
                // public java.lang.String getGroupIdLevel1(java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getIccSerialNumber(java.lang.String callingPackage) throws android.os.RemoteException;	// important !!!
                // public java.lang.String getLine1Number(java.lang.String callingPackage) throws android.os.RemoteException;	// important !!!
                // public java.lang.String getLine1AlphaTag(java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getMsisdn(java.lang.String callingPackage) throws android.os.RemoteException;	// important !!!
                // public java.lang.String getVoiceMailNumber(java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getVoiceMailAlphaTag(java.lang.String callingPackage) throws android.os.RemoteException;
                if (parameterTypes.length == 1 && parameterTypes[0] == String.class
                        && (methodName.equals("getDeviceId") || methodName.equals("getDeviceSvn")
                        || methodName.equals("getSubscriberId") || methodName.equals("getGroupIdLevel1")
                        || methodName.equals("getIccSerialNumber") || methodName.equals("getLine1Number")
                        || methodName.equals("getLine1AlphaTag") || methodName.equals("getMsisdn")
                        || methodName.equals("getVoiceMailNumber") || methodName.equals("getVoiceMailAlphaTag"))) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }


                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                // public java.lang.String getCompleteVoiceMailNumberForSubscriber(int subId) throws android.os.RemoteException;
                // public java.lang.String getIsimImpi(int subId) throws android.os.RemoteException;    // Android 9
                // public java.lang.String getIsimDomain(int subId) throws android.os.RemoteException;  // Android 9
                // public java.lang.String[] getIsimImpu(int subId) throws android.os.RemoteException;  // Android 9
                // public java.lang.String getIsimIst(int subId) throws android.os.RemoteException;     // Android 9
                // public java.lang.String[] getIsimPcscf(int subId) throws android.os.RemoteException; // Android 9
                if (parameterTypes.length == 1 && parameterTypes[0] == int.class) {
                    if (methodName.equals("getCompleteVoiceMailNumberForSubscriber")
                            || methodName.equals("getIsimImpi") || methodName.equals("getIsimDomain") || methodName.equals("getIsimImpu")
                            || methodName.equals("getIsimIst") || methodName.equals("getIsimPcscf")
                    ) {

                        SubscriptionManagerInfo.ID.iterateActiveSubIds(mContext, new SubscriptionManagerInfo.ID.IterateIdsHandler() {
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

                }

                // public java.lang.String getNaiForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getImeiForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getDeviceSvnUsingSubId(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getSubscriberIdForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getGroupIdLevel1ForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getIccSerialNumberForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getLine1NumberForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getLine1AlphaTagForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getMsisdnForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getVoiceMailNumberForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.lang.String getVoiceMailAlphaTagForSubscriber(int subId, java.lang.String callingPackage) throws android.os.RemoteException;
                if (parameterTypes.length == 2 && parameterTypes[0] == int.class && parameterTypes[1] == String.class) {

                    if (methodName.equals("getNaiForSubscriber") || methodName.equals("getImeiForSubscriber")
                            || methodName.equals("getDeviceSvnUsingSubId") || methodName.equals("getSubscriberIdForSubscriber")
                            || methodName.equals("getGroupIdLevel1ForSubscriber") || methodName.equals("getIccSerialNumberForSubscriber")
                            || methodName.equals("getLine1NumberForSubscriber") || methodName.equals("getLine1AlphaTagForSubscriber")
                            || methodName.equals("getMsisdnForSubscriber") || methodName.equals("getVoiceMailNumberForSubscriber")
                            || methodName.equals("getVoiceMailAlphaTagForSubscriber")
                    ) {

                        SubscriptionManagerInfo.ID.iterateActiveSubIds(mContext, new SubscriptionManagerInfo.ID.IterateIdsHandler() {
                            @Override
                            public void handle(int subId) throws Exception {

                                String methodKey = fMethodName + "_with_args";
                                Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                if (methodArgsMap == null) {
                                    methodArgsMap = new HashMap();
                                    fResultMap.put(methodKey, methodArgsMap);
                                }

                                String key = "_arg0_int_" + subId;
                                Object value = fMethod.invoke(fObj, new Object[]{subId, opPackageName});
                                if (value != null) {
                                    methodArgsMap.put(key, value);
                                }

                            }
                        });

                    }


                    // public java.lang.String getDeviceIdForPhone(int phoneId, java.lang.String callingPackage) throws android.os.RemoteException;
                    if (methodName.equals("getDeviceIdForPhone")) {

                        SubscriptionManagerInfo.ID.iterateActivePhoneIds(mContext, new SubscriptionManagerInfo.ID.IterateIdsHandler() {
                            @Override
                            public void handle(int phoneId) throws Exception {

                                String methodKey = fMethodName + "_with_args";
                                Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                if (methodArgsMap == null) {
                                    methodArgsMap = new HashMap();
                                    fResultMap.put(methodKey, methodArgsMap);
                                }

                                String key = "_arg0_int_" + phoneId;
                                Object value = fMethod.invoke(fObj, new Object[]{phoneId, opPackageName});
                                if (value != null) {
                                    methodArgsMap.put(key, value);
                                }

                            }
                        });

                    }

                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }


    public static JSONObject getITelecomInfo(final Context mContext) {
        Object packageName = IReflectUtil.invokeMethod(mContext, "getOpPackageName", new Class[]{}, new Object[]{});
        if (packageName == null) {
            packageName = mContext.getPackageName();
        }
        final Object opPackageName = packageName;
        Object proxy = InvokerOfService.getProxy("com.android.internal.telecom.ITelecomService", "telecom");

        // Android 5.0 开始才有 telecom Service

        if (ManagerInfo._IS_DEBUG_) {
            // The same ...
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            Object telecomService = IReflectUtil.invokeMethod(telephonyManager, "getTelecomService", new Class[]{}, new Object[]{});
            Object mRemote = IReflectUtil.getFieldValue(telecomService, "mRemote");
            Object mmRemote = IReflectUtil.getFieldValue(proxy, "mRemote");
            Object mmmRemote = InvokerOfService.getService(Context.TELECOM_SERVICE);
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

                // all create methods
                // public android.content.Intent createManageBlockedNumbersIntent() throws android.os.RemoteException;  // Android 8
                if (methodName.startsWith("create")) {
                    return null;
                }

                // public boolean endCall() throws android.os.RemoteException;
                if (methodName.startsWith("endCall")) {
                    return null;
                }

                // public android.telecom.TelecomAnalytics dumpCallAnalytics() throws android.os.RemoteException;   // Android 8
                if (methodName.equals("dumpCallAnalytics")) {
                    return null;
                }

                if (parameterTypes.length == 0) {

                    // public android.content.ComponentName getDefaultPhoneApp() throws android.os.RemoteException; // important !!!
                    // public java.util.List<android.telecom.PhoneAccountHandle> getAllPhoneAccountHandles() throws android.os.RemoteException;

                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }


                // public boolean isTtySupported(java.lang.String callingPackage) throws android.os.RemoteException;
                // public int getCurrentTtyMode(java.lang.String callingPackage) throws android.os.RemoteException;
                // public boolean isInManagedCall(java.lang.String callingPackage) throws android.os.RemoteException;   // Android 8
                if (parameterTypes.length == 1 && parameterTypes[0] == java.lang.String.class
                        && (methodName.equals("isTtySupported") || methodName.equals("getCurrentTtyMode") || methodName.equals("isInManagedCall"))) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }


                // TODO ... Hook 那边根据 getAllPhoneAccounts() 的值来处理
                // public android.telecom.PhoneAccount getPhoneAccount(android.telecom.PhoneAccountHandle account) throws android.os.RemoteException

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }

}
