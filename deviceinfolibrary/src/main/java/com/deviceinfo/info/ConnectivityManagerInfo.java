package com.deviceinfo.info;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.os.Build;
import android.util.Log;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONArrayExtended;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.util.ArrayList;
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
            public Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes,
                                 Class<?> returnType, Map<String, Object> resultMap) throws Exception {
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

                // TODO ... 4.4 ??? 6.0 ??? API ??????
                // public android.net.LinkQualityInfo getLinkQualityInfo(int networkType) throws android.os.RemoteException;    // 4.4 ???????????????6.0???????????????
                // public android.net.LinkQualityInfo getActiveLinkQualityInfo() throws android.os.RemoteException;             // 4.4 ???????????????6.0???????????????
                // public android.net.LinkQualityInfo[] getAllLinkQualityInfo() throws android.os.RemoteException;              // 4.4 ???????????????6.0???????????????

                // public android.net.NetworkState[] getAllNetworkState() throws android.os.RemoteException;            // 4.4???????????????6.0???????????????
                // public android.net.NetworkQuotaInfo getActiveNetworkQuotaInfo() throws android.os.RemoteException;   // 4.4???????????????6.0???????????????
                // public boolean isActiveNetworkMetered() throws android.os.RemoteException;
                // public java.util.List<android.net.wifi.WifiDevice> getTetherConnectedSta() throws android.os.RemoteException;

                // public android.net.ProxyInfo getGlobalProxy() throws android.os.RemoteException;         // Proxy is null, ??????????????????????????????
                // public void setGlobalProxy(android.net.ProxyInfo p) throws android.os.RemoteException;   // Proxy ??????????????????????????????
                // public android.net.ProxyInfo getProxyForNetwork(android.net.Network nework) throws android.os.RemoteException; // Proxy ??????????????????????????????


                // TODO ... Hook ???????????? VPN ??????????????????????????????VPN??????????????????????????????VPN???????????????APP???????????????VPN???Hook??????????????????????????????
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (value instanceof android.net.LinkProperties) {
                            value = object2Json4LinkProperties((android.net.LinkProperties) value);
                        }
                    }
                    return value;
                }


                if (parameterTypes.length == 1) {

                    // public int getRestoreDefaultNetworkDelay(int networkType) throws android.os.RemoteException;
                    // public android.net.LinkProperties getLinkPropertiesForType(int networkType) throws android.os.RemoteException;

                    // TODO ... ??????Json Hook????????????APP??????????????????????????????Json Key????????????Hook????????????????????????????????????(?????????????????????NetworkInfo??????true)
                    // public boolean isNetworkSupported(int networkType) throws android.os.RemoteException;

                    // TODO ... ?????????????????????????????? getAllNetworkInfo() & getAllNetworks() ?????????
                    // TODO ... ???: NetworkInfo & Network ??????????????? getAllNetworkInfo() & getAllNetworks() ???????????????!
                    // public android.net.NetworkInfo getNetworkInfo(int networkType) throws android.os.RemoteException;
                    // public android.net.Network getNetworkForType(int networkType) throws android.os.RemoteException;

                    if (parameterTypes[0] == int.class) {

                        if (methodName.equals("getLinkPropertiesForType")
                                || methodName.equals("getRestoreDefaultNetworkDelay")
                                || methodName.equals("isNetworkSupported")
                                || methodName.equals("getNetworkInfo")
                                || methodName.equals("getNetworkForType")) {
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

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            if (value instanceof android.net.LinkProperties) {
                                                value = object2Json4LinkProperties((android.net.LinkProperties) value);
                                            }
                                        }

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

                    // API LEVEL 21 ?????? android.net.Network ??????Android 4.4 ??????
                    if (parameterTypes[0].getName().equals("android.net.Network")) {
                        // if ( parameterTypes[0] == android.net.Network.class ) {

                        if (methodName.equals("getLinkProperties")
                                || methodName.equals("getNetworkCapabilities")
                                || methodName.equals("getNetworkInfoForNetwork")) {
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

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            if (value instanceof android.net.LinkProperties) {
                                                value = object2Json4LinkProperties((android.net.LinkProperties) value);
                                            }
                                        }

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

    // ---------------------- ?????? Network (netId ??????) ???????????? ----------------------
    public static interface IterateNetworkHandler {
        public void handle(Network network) throws Exception;
    }

    // API LEVEL 21 ?????? android.net.Network ??????Android 4.4 ??????
    private static Object[] allNetworkList = null;// private static Network[] allNetworkList = null;  // ???????????? ????????? ???????????????Crash, ????????????

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

    // ---------------------- ?????? NetworkInfo (mNetworkType ??????) ???????????? ----------------------
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


    /**
     * Some Object To Json
     */
    public static JSONObject object2Json4LinkProperties(android.net.LinkProperties linkProperties) {
        JSONObject json = new JSONObjectExtended().__objectToJson__(linkProperties);

        // for InetAddress getting holder fields-values json
        JSONArray mLinkAddressesJson = new JSONArray();
        ArrayList<LinkAddress> mLinkAddresses = (ArrayList<LinkAddress>) IReflectUtil.getFieldValue(linkProperties, "mLinkAddresses");
        for (int i = 0; i < mLinkAddresses.size(); i++) {
            LinkAddress linkAddress = mLinkAddresses.get(i);
            JSONObject js = new JSONObjectExtended().__objectToJson__(linkAddress);
            mLinkAddressesJson.put(js);

            InetAddress address = (InetAddress) IReflectUtil.getFieldValue(linkAddress, "address");
            if (address != null) {
                JSONObject inetJson = new JSONObjectExtended().__objectToJson__(address);
                try {
                    js.put("address", inetJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // for InetAddress getting holder fields-values json
        JSONArray mRoutesJson = new JSONArray();
        ArrayList<RouteInfo> mRoutes = (ArrayList<RouteInfo>) IReflectUtil.getFieldValue(linkProperties, "mRoutes");
        for (int i = 0; i < mRoutes.size(); i++) {
            RouteInfo routeInfo = mRoutes.get(i);
            JSONObject js = new JSONObjectExtended().__objectToJson__(routeInfo);
            mRoutesJson.put(js);

            InetAddress mGateway = (InetAddress) IReflectUtil.getFieldValue(routeInfo, "mGateway");
            if (mGateway != null) {
                JSONObject inetJson = new JSONObjectExtended().__objectToJson__(mGateway);
                try {
                    js.put("mGateway", inetJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                IpPrefix mDestination = (IpPrefix) IReflectUtil.getFieldValue(routeInfo, "mDestination");
                if (mDestination != null) {
                    JSONObject elementInfo = new JSONObjectExtended().__objectToJson__(mDestination);
                    try {
                        js.put("mDestination", elementInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) IReflectUtil.getFieldValue(linkProperties, "mDnses");
        JSONArray mDnsesJson = new JSONArrayExtended(mDnses);

        ArrayList<InetAddress> mPcscfs = (ArrayList<InetAddress>) IReflectUtil.getFieldValue(linkProperties, "mPcscfs");
        JSONArray mPcscfsJson = new JSONArrayExtended(mPcscfs);

        ArrayList<InetAddress> mValidatedPrivateDnses = (ArrayList<InetAddress>) IReflectUtil.getFieldValue(linkProperties, "mValidatedPrivateDnses");
        JSONArray mValidatedPrivateDnsesJson = new JSONArrayExtended(mValidatedPrivateDnses);

        try {
            json.put("mLinkAddresses", mLinkAddressesJson);
            json.put("mRoutes", mRoutesJson);
            json.put("mDnses", mDnsesJson);
            json.put("mPcscfsJson", mPcscfsJson);
            json.put("mValidatedPrivateDnses", mValidatedPrivateDnsesJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
