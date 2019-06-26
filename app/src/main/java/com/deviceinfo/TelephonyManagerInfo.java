package com.deviceinfo;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import common.modules.util.IReflectUtil;

public class TelephonyManagerInfo {

    public static JSONObject getInfo(Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        Map<?, ?> telephonyManagerResult = IReflectUtil.invokeObjectAllNonVoidZeroArgsMethods(telephonyManager);

        return null;
    }

    public static JSONObject getTelephonyInfo(Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        Object iTelephony = IReflectUtil.invokeMethod(telephonyManager, "getITelephony", new Class[]{}, new Object[] {});
        final Object opPackageName = IReflectUtil.invokeMethod(telephonyManager, "getOpPackageName", new Class[]{}, new Object[] {});

        Map<?, ?> result = ObjectInvoker.invokeObjectMethods(iTelephony, new ObjectInvoker.InvokeHandler() {
            @Override
            public Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes, Class<?> returnType) throws Exception {
                if (returnType == void.class) {
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
                if ( methodName.equals("getDeviceId") || methodName.equals("getDataNetworkType")
                || methodName.equals("isOffhook") || methodName.equals("isRinging")
                || methodName.equals("isIdle") || methodName.equals("isRadioOn")
                || methodName.equals("isSimPinEnabled") || methodName.equals("getCdmaEriIconIndex")
                || methodName.equals("getCdmaEriIconMode") || methodName.equals("getCdmaEriText")
                || methodName.equals("getLteOnCdmaMode") || methodName.equals("getCalculatedPreferredNetworkType")
                || methodName.equals("getMergedSubscriberIds") || methodName.equals("isVideoCallingEnabled") ) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }

                return null;
            }
        });

        return new JSONObject(result);
    }

    public static JSONObject getIPhoneSubInfo(Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        Object iPhoneSubInfo = IReflectUtil.invokeMethod(telephonyManager, "getSubscriberInfo", new Class[]{}, new Object[] {});
        final Object opPackageName = IReflectUtil.invokeMethod(telephonyManager, "getOpPackageName", new Class[]{}, new Object[] {});

        Map<?, ?> result = ObjectInvoker.invokeObjectMethods(iPhoneSubInfo, new ObjectInvoker.InvokeHandler() {
            @Override
            public Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes, Class<?> returnType) throws Exception {
                if (returnType == void.class) {
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

                if ( methodName.equals("getDeviceId") || methodName.equals("getDataNetworkType") ) {
                    Object value = method.invoke(obj, new Object[]{opPackageName});
                    return value;
                }

                return null;
            }
        });

        return new JSONObject(result);
    }


}
