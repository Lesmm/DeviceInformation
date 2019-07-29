package com.deviceinfo.info;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import common.modules.util.IReflectUtil;

public class ConnectivityManagerInfo {

    public static JSONObject getInfo(final Context mContext) {

        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        Object proxy = InvokerOfService.getProxy("android.net.IConnectivityManager", "connectivity"/*Context.CONNECTIVITY_SERVICE*/);
        final int userId = (Integer) IReflectUtil.invokeMethod(mContext, "getUserId", new Class[]{}, new Object[]{});

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

                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;

                if (methodName.startsWith("update") || methodName.equals("start") || methodName.equals("stop") || methodName.equals("factoryReset")) {
                    return null;
                }

                // TODO ... 4.4 与 6.0 的 API 差异
                // public android.net.LinkQualityInfo getLinkQualityInfo(int networkType) throws android.os.RemoteException;    // 4.4 有此接口，6.0已经没有了
                // public android.net.LinkQualityInfo getActiveLinkQualityInfo() throws android.os.RemoteException;             // 4.4 有此接口，6.0已经没有了
                // public android.net.LinkQualityInfo[] getAllLinkQualityInfo() throws android.os.RemoteException;              // 4.4 有此接口，6.0已经没有了

                // public android.net.NetworkState[] getAllNetworkState() throws android.os.RemoteException;            // 4.4能获取到，6.0的获取不到
                // public android.net.NetworkQuotaInfo getActiveNetworkQuotaInfo() throws android.os.RemoteException;   // 4.4能获取到，6.0的获取不到
                // public boolean isActiveNetworkMetered() throws android.os.RemoteException;
                // public java.util.List<android.net.wifi.WifiDevice> getTetherConnectedSta() throws android.os.RemoteException;

                // public android.net.ProxyInfo getGlobalProxy() throws android.os.RemoteException;         // Proxy is null, 没太大作用，不处理了
                // public void setGlobalProxy(android.net.ProxyInfo p) throws android.os.RemoteException;   // Proxy 没太大作用，不处理了
                // public android.net.ProxyInfo getProxyForNetwork(android.net.Network nework) throws android.os.RemoteException; // Proxy 没太大作用，不处理了


                // TODO ... Hook 那边处理 VPN 的东西了。这里不获取VPN的信息。当我们需要用VPN时，又不想APP知道我们用VPN，Hook那边再处理这情况吧。
                /*
                public void setVpnPackageAuthorization(java.lang.String packageName, int userId, boolean authorized) throws android.os.RemoteException;
                public android.os.ParcelFileDescriptor establishVpn(com.android.internal.net.VpnConfig config) throws android.os.RemoteException;
                public com.android.internal.net.VpnConfig getVpnConfig(int userId) throws android.os.RemoteException;
                public void startLegacyVpn(com.android.internal.net.VpnProfile profile) throws android.os.RemoteException;
                public com.android.internal.net.LegacyVpnInfo getLegacyVpnInfo(int userId) throws android.os.RemoteException;
                public com.android.internal.net.VpnInfo[] getAllVpnInfo() throws android.os.RemoteException;
                public boolean updateLockdownVpn() throws android.os.RemoteException;
                */

                // public android.net.LinkProperties getActiveLinkProperties() throws android.os.RemoteException;
                // public android.net.NetworkInfo[] getAllNetworkInfo() throws android.os.RemoteException;
                // public android.net.Network[] getAllNetworks() throws android.os.RemoteException;

                if (parameterTypes.length == 0) {
                    if (methodName.equals("getGlobalProxy")) {
                        Log.d("DeviceInfo", "Debug method: " + methodName);
                    }
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }


                if (parameterTypes.length == 1) {

                    // public int getRestoreDefaultNetworkDelay(int networkType) throws android.os.RemoteException;
                    // public android.net.LinkProperties getLinkPropertiesForType(int networkType) throws android.os.RemoteException;

                    // TODO ... 这个Json Hook了会不会APP有问题，看情况把这个Json Key去掉即不Hook，现在先取这些信息回来先(发现所有支持的NetworkInfo都是true)
                    // public boolean isNetworkSupported(int networkType) throws android.os.RemoteException;

                    // TODO ... 这两个方法看能不能从 getAllNetworkInfo() & getAllNetworks() 里简化即: NetworkInfo & Network 信息的根据 getAllNetworkInfo() & getAllNetworks() 的值来返回!
                    // public android.net.NetworkInfo getNetworkInfo(int networkType) throws android.os.RemoteException;
                    // public android.net.Network getNetworkForType(int networkType) throws android.os.RemoteException;

                    if (parameterTypes[0] == int.class) {

                        if (methodName.equals("getLinkPropertiesForType") || methodName.equals("getRestoreDefaultNetworkDelay") || methodName.equals("isNetworkSupported")
                                || methodName.equals("getNetworkInfo") || methodName.equals("getNetworkForType")) {
                            iterateAllNetworkInfoList(mContext, new IterateNetworkInfoHandler() {
                                @Override
                                public void handle(NetworkInfo info) throws Exception {

                                    String methodKey = fMethodName + "_with_args";
                                    Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                    if (methodArgsMap == null) {
                                        methodArgsMap = new HashMap();
                                        fResultMap.put(methodKey, methodArgsMap);
                                    }

                                    int mNetworkType = info.getType();
                                    String key = "_arg0_int_" + mNetworkType;
                                    Object value = fMethod.invoke(fObj, new Object[]{mNetworkType});
                                    if (value != null) {
                                        methodArgsMap.put(key, value);
                                    }

                                }
                            });
                        }

                        // public android.net.NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) throws android.os.RemoteException;

                        if (methodName.equals("getDefaultNetworkCapabilitiesForUser")) {
                            Object value = method.invoke(obj, new Object[]{userId});
                            return value;
                        }

                    }


                    // public android.net.LinkProperties getLinkProperties(android.net.Network network) throws android.os.RemoteException;
                    // public android.net.NetworkCapabilities getNetworkCapabilities(android.net.Network network) throws android.os.RemoteException;
                    // public android.net.NetworkInfo getNetworkInfoForNetwork(android.net.Network network) throws android.os.RemoteException;

                    // API LEVEL 21 才有 android.net.Network 类，Android 4.4 没有
                    if (parameterTypes[0].getName().equals("android.net.Network")) {
                        // if ( parameterTypes[0] == android.net.Network.class ) {

                        if (methodName.equals("getLinkProperties") || methodName.equals("getNetworkCapabilities") || methodName.equals("getNetworkInfoForNetwork")) {
                            iterateAllNetworkList(mContext, new IterateNetworkHandler() {
                                @Override
                                public void handle(Network network) throws Exception {

                                    String methodKey = fMethodName + "_with_args";
                                    Map methodArgsMap = (Map) fResultMap.get(methodKey);
                                    if (methodArgsMap == null) {
                                        methodArgsMap = new HashMap();
                                        fResultMap.put(methodKey, methodArgsMap);
                                    }

                                    int netId = (Integer) IReflectUtil.getFieldValue(network, "netId");
                                    String key = "_arg0_Network_" + netId;
                                    Object value = fMethod.invoke(fObj, new Object[]{network});
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

    // ---------------------- 遍历 Network (netId 标识) 工具方法 ----------------------
    public static interface IterateNetworkHandler {
        public void handle(Network network) throws Exception;
    }

    // API LEVEL 21 才有 android.net.Network 类，Android 4.4 没有
    private static Object[] allNetworkList = null;// private static Network[] allNetworkList = null;  // 这会导致 运行时 一加载类就Crash, 注释掉。

    public static void iterateAllNetworkList(Context mContext, IterateNetworkHandler handler) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        if (allNetworkList == null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            allNetworkList = connectivityManager.getAllNetworks();
        }

        for (int i = 0; allNetworkList != null && i < allNetworkList.length; i++) {
            try {
                Network network = (Network) allNetworkList[i];
                handler.handle(network);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------------- 遍历 NetworkInfo (mNetworkType 标识) 工具方法 ----------------------
    public static interface IterateNetworkInfoHandler {
        public void handle(NetworkInfo info) throws Exception;
    }

    private static NetworkInfo[] allNetworkInfoList = null;

    public static void iterateAllNetworkInfoList(Context mContext, IterateNetworkInfoHandler handler) {
        if (allNetworkInfoList == null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            allNetworkInfoList = connectivityManager.getAllNetworkInfo();
        }

        for (int i = 0; allNetworkInfoList != null && i < allNetworkInfoList.length; i++) {
            try {
                NetworkInfo networkInfo = allNetworkInfoList[i];
                handler.handle(networkInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
