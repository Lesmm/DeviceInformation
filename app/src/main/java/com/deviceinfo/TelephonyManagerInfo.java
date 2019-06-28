package com.deviceinfo;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import common.modules.util.IReflectUtil;
import common.modules.util.JSONObjectExtended;

public class TelephonyManagerInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject telephonyInfo = getITelephonyInfo(mContext);
        JSONObject phoneSubInfo = getIPhoneSubInfo(mContext);
        JSONObject telecomInfo = getITelecomInfo(mContext);

        JSONObject telephonyResult = new JSONObject();

        ManagerHelper.mergeJSONObject(telephonyResult, telephonyInfo);
        ManagerHelper.mergeJSONObject(telephonyResult, phoneSubInfo);
        ManagerHelper.mergeJSONObject(telephonyResult, telecomInfo);

        return telephonyResult;
    }

    public static JSONObject getITelephonyInfo(Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final Object opPackageName = IReflectUtil.invokeMethod(telephonyManager, "getOpPackageName", new Class[]{}, new Object[] {});
        Object iTelephony = IReflectUtil.invokeMethod(telephonyManager, "getITelephony", new Class[]{}, new Object[] {});

        Map<?, ?> result = InvokerOfObject.invokeObjectMethods(iTelephony, new InvokerOfObject.InvokeHandler() {
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

                // public boolean endCall() throws android.os.RemoteException;
                // public boolean endCallForSubscriber(int subId) throws android.os.RemoteException;
                if (methodName.startsWith("endCall")) { // will block when without sim card
                    return null;
                }
                // public byte[] getAtr() throws android.os.RemoteException;
                // public byte[] getAtrUsingSubId(int subId) throws android.os.RemoteException;
                if (methodName.startsWith("getAtr")) {  // will crash
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
                if ( parameterTypes.length == 1 && parameterTypes[0] == java.lang.String.class
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

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }

    public static JSONObject getIPhoneSubInfo(Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final Object opPackageName = IReflectUtil.invokeMethod(telephonyManager, "getOpPackageName", new Class[]{}, new Object[] {});
        Object iPhoneSubInfo = IReflectUtil.invokeMethod(telephonyManager, "getSubscriberInfo", new Class[]{}, new Object[] {});

        Map<?, ?> result = InvokerOfObject.invokeObjectMethods(iPhoneSubInfo, new InvokerOfObject.InvokeHandler() {
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
                if ( parameterTypes.length == 1 && parameterTypes[0] == java.lang.String.class
                        && ( methodName.equals("getDeviceId") || methodName.equals("getDeviceSvn")
                        || methodName.equals("getSubscriberId") || methodName.equals("getGroupIdLevel1")
                        || methodName.equals("getIccSerialNumber") || methodName.equals("getLine1Number")
                        || methodName.equals("getLine1AlphaTag") || methodName.equals("getMsisdn")
                        || methodName.equals("getVoiceMailNumber") || methodName.equals("getVoiceMailAlphaTag") ) ) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }


    public static JSONObject getITelecomInfo(Context mContext) {
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
