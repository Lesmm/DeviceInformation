package com.deviceinfo.info;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.Manager;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import common.modules.util.IReflectUtil;

// done with api diff
public class TelephonyManagerInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject telephonyInfo = getITelephonyInfo(mContext);
        JSONObject phoneSubInfo = getIPhoneSubInfo(mContext);
        JSONObject telecomInfo = getITelecomInfo(mContext);

        JSONObject telephonyResult = new JSONObject();

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
                if (parameterTypes.length == 1 && parameterTypes[0] == String.class
                        && (methodName.equals("getDeviceId") || methodName.equals("getDataNetworkType")
                        || methodName.equals("isOffhook") || methodName.equals("isRinging")
                        || methodName.equals("isIdle") || methodName.equals("isRadioOn")
                        || methodName.equals("isSimPinEnabled") || methodName.equals("getCdmaEriIconIndex")
                        || methodName.equals("getCdmaEriIconMode") || methodName.equals("getCdmaEriText")
                        || methodName.equals("getLteOnCdmaMode") || methodName.equals("getCalculatedPreferredNetworkType")
                        || methodName.equals("getMergedSubscriberIds") || methodName.equals("isVideoCallingEnabled"))) {
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
                    // public android.telephony.SignalStrength getSignalStrength(int subId) throws android.os.RemoteException;

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
                    if ( methodName.equals("hasIccCardUsingSlotId") || methodName.equals("getAllowedCarriers") ) {
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

        if (Manager.IS_DEBUG) {
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
