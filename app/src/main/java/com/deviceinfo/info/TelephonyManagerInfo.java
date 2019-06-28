package com.deviceinfo.info;

import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.ManagerInfoHelper;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import common.modules.util.IReflectUtil;

public class TelephonyManagerInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject telephonyInfo = getITelephonyInfo(mContext);
        JSONObject phoneSubInfo = getIPhoneSubInfo(mContext);
        JSONObject telecomInfo = getITelecomInfo(mContext);

        JSONObject telephonyResult = new JSONObject();

        ManagerInfoHelper.mergeJSONObject(telephonyResult, telephonyInfo);
        ManagerInfoHelper.mergeJSONObject(telephonyResult, phoneSubInfo);
        ManagerInfoHelper.mergeJSONObject(telephonyResult, telecomInfo);

        return telephonyResult;
    }

    public static JSONObject getITelephonyInfo(final Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final Object opPackageName = IReflectUtil.invokeMethod(telephonyManager, "getOpPackageName", new Class[]{}, new Object[] {});
        Object iTelephony = IReflectUtil.invokeMethod(telephonyManager, "getITelephony", new Class[]{}, new Object[] {});

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

                // public boolean endCall() throws android.os.RemoteException;
                // public boolean endCallForSubscriber(int subId) throws android.os.RemoteException;
                if (methodName.startsWith("endCall")) { // will block when without sim card
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
                if ( parameterTypes.length == 1 && parameterTypes[0] == String.class
                        && ( methodName.equals("getDeviceId") || methodName.equals("getDataNetworkType")
                        || methodName.equals("isOffhook") || methodName.equals("isRinging")
                        || methodName.equals("isIdle") || methodName.equals("isRadioOn")
                        || methodName.equals("isSimPinEnabled") || methodName.equals("getCdmaEriIconIndex")
                        || methodName.equals("getCdmaEriIconMode") || methodName.equals("getCdmaEriText")
                        || methodName.equals("getLteOnCdmaMode") || methodName.equals("getCalculatedPreferredNetworkType")
                        || methodName.equals("getMergedSubscriberIds") || methodName.equals("isVideoCallingEnabled") ) ) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }


                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                if ( parameterTypes.length == 1 && parameterTypes[0] == int.class ) {

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
                    if ( methodName.equals("getCallStateForSubscriber") || methodName.equals("getActivePhoneTypeForSubscriber")
                            || methodName.equals("getVoiceMessageCountForSubscriber") || methodName.equals("getIccOperatorNumericForData")
                            || methodName.equals("getPreferredNetworkType") || methodName.equals("getCellNetworkScanResults")
                            || methodName.equals("getDataEnabled") || methodName.equals("getCdmaMdn")
                            || methodName.equals("getCdmaMin") || methodName.equals("getAtrUsingSubId")
                    ) {
                        SubscriptionManagerInfo.IterateAllSubscriptionInfoList(mContext, new SubscriptionManagerInfo.IterateHandler() {
                            @Override
                            public void handle(SubscriptionInfo info) throws Exception {
                                int mId = (Integer) IReflectUtil.getFieldValue(info, "mId");
                                String key = fMethodName + "_arg0_int_" + mId;
                                Object value = fMethod.invoke(fObj, new Object[]{mId});
                                if (value != null) {
                                    fResultMap.put(key, value);
                                }
                            }
                        });
                    }

                }

                if ( parameterTypes.length == 2 && parameterTypes[0] == int.class && parameterTypes[1] == String.class ) {

                    // TODO ... phoneId , what is phoneId & how to get phoneId
                    // public int getRadioAccessFamily(int phoneId, java.lang.String callingPackage) throws android.os.RemoteException;

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
                    if ( methodName.equals("isOffhookForSubscriber") || methodName.equals("isRingingForSubscriber")
                            || methodName.equals("isIdleForSubscriber") || methodName.equals("isRadioOnForSubscriber")
                            || methodName.equals("getCdmaEriIconIndexForSubscriber") || methodName.equals("getCdmaEriIconModeForSubscriber")
                            || methodName.equals("getCdmaEriTextForSubscriber") || methodName.equals("getNetworkTypeForSubscriber")
                            || methodName.equals("getDataNetworkTypeForSubscriber") || methodName.equals("getVoiceNetworkTypeForSubscriber")
                            || methodName.equals("getLteOnCdmaModeForSubscriber") || methodName.equals("getLine1NumberForDisplay")
                            || methodName.equals("getLine1AlphaTagForDisplay")
                    ) {

                        SubscriptionManagerInfo.IterateAllSubscriptionInfoList(mContext, new SubscriptionManagerInfo.IterateHandler() {
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

                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }

    public static JSONObject getIPhoneSubInfo(final Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final Object opPackageName = IReflectUtil.invokeMethod(telephonyManager, "getOpPackageName", new Class[]{}, new Object[] {});
        Object iPhoneSubInfo = IReflectUtil.invokeMethod(telephonyManager, "getSubscriberInfo", new Class[]{}, new Object[] {});

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
                if ( parameterTypes.length == 1 && parameterTypes[0] == String.class
                        && ( methodName.equals("getDeviceId") || methodName.equals("getDeviceSvn")
                        || methodName.equals("getSubscriberId") || methodName.equals("getGroupIdLevel1")
                        || methodName.equals("getIccSerialNumber") || methodName.equals("getLine1Number")
                        || methodName.equals("getLine1AlphaTag") || methodName.equals("getMsisdn")
                        || methodName.equals("getVoiceMailNumber") || methodName.equals("getVoiceMailAlphaTag") ) ) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }


                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                // public java.lang.String getCompleteVoiceMailNumberForSubscriber(int subId) throws android.os.RemoteException;
                if ( parameterTypes.length == 1 && parameterTypes[0] == int.class ) {
                    if ( methodName.equals("getCompleteVoiceMailNumberForSubscriber") ) {

                        SubscriptionManagerInfo.IterateAllSubscriptionInfoList(mContext, new SubscriptionManagerInfo.IterateHandler() {
                            @Override
                            public void handle(SubscriptionInfo info) throws Exception {
                                int mId = (Integer) IReflectUtil.getFieldValue(info, "mId");
                                String key = fMethodName + "_arg0_int_" + mId;
                                Object value = fMethod.invoke(fObj, new Object[]{mId});
                                if (value != null) {
                                    fResultMap.put(key, value);
                                }
                            }
                        });

                    }

                }

                // TODO ... phoneId , what is phoneId & how to get phoneId
                // public java.lang.String getDeviceIdForPhone(int phoneId, java.lang.String callingPackage) throws android.os.RemoteException;

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
                if ( parameterTypes.length == 2 && parameterTypes[0] == int.class && parameterTypes[1] == String.class ) {

                    if ( methodName.equals("getNaiForSubscriber") || methodName.equals("getImeiForSubscriber")
                            || methodName.equals("getDeviceSvnUsingSubId") || methodName.equals("getSubscriberIdForSubscriber")
                            || methodName.equals("getGroupIdLevel1ForSubscriber") || methodName.equals("getIccSerialNumberForSubscriber")
                            || methodName.equals("getLine1NumberForSubscriber") || methodName.equals("getLine1AlphaTagForSubscriber")
                            || methodName.equals("getMsisdnForSubscriber") || methodName.equals("getVoiceMailNumberForSubscriber")
                            || methodName.equals("getVoiceMailAlphaTagForSubscriber")
                    ) {

                        SubscriptionManagerInfo.IterateAllSubscriptionInfoList(mContext, new SubscriptionManagerInfo.IterateHandler() {
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

                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }


    public static JSONObject getITelecomInfo(final Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final Object opPackageName = IReflectUtil.invokeMethod(telephonyManager, "getOpPackageName", new Class[]{}, new Object[] {});
        Object telecomService = IReflectUtil.invokeMethod(telephonyManager, "getTelecomService", new Class[]{}, new Object[] {});
        Object proxy = InvokerOfService.getProxy("com.android.internal.telecom.ITelecomService", "telecom");

        // The same ...
        Object mRemote = IReflectUtil.getFieldValue(telecomService, "mRemote");
        Object mmRemote = IReflectUtil.getFieldValue(proxy, "mRemote");
        Object mmmRemote = InvokerOfService.getService(Context.TELECOM_SERVICE);

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
                    // public boolean endCall() throws android.os.RemoteException;
                    if (methodName.startsWith("endCall")) {
                        return null;
                    }

                    // public android.content.ComponentName getDefaultPhoneApp() throws android.os.RemoteException; // important !!!

                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                // public boolean isTtySupported(java.lang.String callingPackage) throws android.os.RemoteException;
                // public int getCurrentTtyMode(java.lang.String callingPackage) throws android.os.RemoteException;
                if ( parameterTypes.length == 1 && parameterTypes[0] == java.lang.String.class
                        && ( methodName.equals("isTtySupported") || methodName.equals("getCurrentTtyMode") ) ) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }

}