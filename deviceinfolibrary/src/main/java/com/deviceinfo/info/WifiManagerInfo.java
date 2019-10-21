package com.deviceinfo.info;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.modules.util.IArrayUtil;
import common.modules.util.IReflectUtil;
import common.modules.util.IReflectUtilWrapper;

// done with api diff
public class WifiManagerInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject wifiManagerResult = new JSONObject();

        // 通过调用高层接口
        try {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            // 1. 调用高层接口
            Map managerMap = InvokerOfObject.invokeObjectMethodsWithGetPrefixZeroArgs(wifiManager);
            JSONObject managerInfo = new JSONObjectExtended(managerMap);

            // 2. 移除掉一些无用的
            managerInfo.remove("getVerboseLoggingLevel");

            // 3. 转换，与低级API aidl层对应
            Map<String, String> api2LowerApiMapping = new HashMap();
            api2LowerApiMapping.put("getWifiApState", "getWifiApEnabledState");
            api2LowerApiMapping.put("getWifiState", "getWifiEnabledState");
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

            InfoJsonHelper.mergeJSONObject(wifiManagerResult, managerInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 通过反射来获取
        JSONObject info = getIWifiManagerInfo(mContext);

        InfoJsonHelper.mergeJSONObject(wifiManagerResult, info);

        return wifiManagerResult;
    }


    public static JSONObject getIWifiManagerInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.net.wifi.IWifiManager", "wifi");

        final String callingPackage = mContext.getPackageName();

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

                // we don't need all 'is' methods
                if (methodName.startsWith("is")) {
                    return null;
                }

                if (parameterTypes.length == 0) {

                    // public boolean pingSupplicant() throws android.os.RemoteException;
                    // public boolean saveConfiguration() throws android.os.RemoteException;
                    if (methodName.equals("pingSupplicant") || methodName.equals("saveConfiguration")) {
                        return null;
                    }

                    // 下面这些get的信息没什么太大用处，就不需要了
                    // public int getAggressiveHandover() throws android.os.RemoteException;
                    // public int getAllowScansWithTraffic() throws android.os.RemoteException;
                    // public boolean getEnableAutoJoinWhenAssociated() throws android.os.RemoteException;
                    // public int getFrequencyBand() throws android.os.RemoteException;
                    // public int getHalBasedAutojoinOffload() throws android.os.RemoteException;
                    // public int getSupportedFeatures() throws android.os.RemoteException;
                    // public int getVerboseLoggingLevel() throws android.os.RemoteException;
                    // public int getWifiEnabledState() throws android.os.RemoteException;
                    // public int getWifiApEnabledState() throws android.os.RemoteException;

                    // public java.util.List<android.net.wifi.WifiChannel> getChannelList() throws android.os.RemoteException;
                    // public android.os.Messenger getWifiServiceMessenger() throws android.os.RemoteException;
                    // public android.net.wifi.WifiActivityEnergyInfo reportActivityInfo() throws android.os.RemoteException;

                    // public java.lang.String getCurrentNetworkWpsNfcConfigurationToken() throws android.os.RemoteException;      // Android 8.1
                    // public java.util.List<android.net.wifi.hotspot2.PasspointConfiguration> getPasspointConfigurations() throws android.os.RemoteException; // Android 8.1

                    // public android.os.Messenger getWifiStateMachineMessenger() throws android.os.RemoteException;        // Android 4.4
                    // public android.net.wifi.WifiEapSimInfo getSimInfo() throws android.os.RemoteException;               // Android 5.1 特有的

                    if (methodName.equals("getAggressiveHandover") || methodName.equals("getAllowScansWithTraffic") || methodName.equals("getEnableAutoJoinWhenAssociated") ||
                            methodName.equals("getFrequencyBand") || methodName.equals("getHalBasedAutojoinOffload") || methodName.equals("getSupportedFeatures") ||
                            methodName.equals("getVerboseLoggingLevel") || methodName.equals("getWifiEnabledState") || methodName.equals("getWifiApEnabledState") ||
                            methodName.equals("getChannelList") || methodName.equals("getWifiServiceMessenger") || methodName.equals("reportActivityInfo") ||
                            methodName.equals("getCurrentNetworkWpsNfcConfigurationToken") || methodName.equals("getPasspointConfigurations") || methodName.equals("getWifiStateMachineMessenger") ||
                            methodName.equals("getSimInfo")) {
                        return null;
                    }

                    // public android.net.wifi.WifiConfiguration getWifiApConfiguration() throws android.os.RemoteException;
                    if (methodName.equals("getWifiApConfiguration")) {
                        Object value = method.invoke(obj, new Object[]{});
                        Map<?, ?> map = IReflectUtilWrapper.getFieldsValues(value, IArrayUtil.arrayToList(new String[]{"SSID", "preSharedKey", "wepKeys"}));
                        return map;
                    }

                    // public java.util.List<android.net.wifi.WifiConfiguration> getConfiguredNetworks() throws android.os.RemoteException;
                    // public java.util.List<android.net.wifi.WifiConfiguration> getPrivilegedConfiguredNetworks() throws android.os.RemoteException;

                    // public android.content.pm.ParceledListSlice getConfiguredNetworks() throws android.os.RemoteException;   // Android 8.1
                    // public android.content.pm.ParceledListSlice getPrivilegedConfiguredNetworks() throws android.os.RemoteException;   // Android 8.1

                    if (methodName.equals("getConfiguredNetworks") || methodName.equals("getPrivilegedConfiguredNetworks")) {
                        Object value = method.invoke(obj, new Object[]{});

                        if (value == null) {
                            return null;
                        }

                        // 兼容 8.0
                        List onfiguredNetworks = null;
                        if (value.getClass().getName().equals("android.content.pm.ParceledListSlice")) {
                            onfiguredNetworks = (List) IReflectUtil.getFieldValue(value, "mList");
                        } else if (value instanceof List) {
                            onfiguredNetworks = (List) value;
                        }

                        if (onfiguredNetworks == null) {
                            return null;
                        }

                        // 这里我们需要8个就够了
                        int length = onfiguredNetworks.size() > 8 ? 8 : onfiguredNetworks.size();
                        JSONArray array = new JSONArray();
                        for (int i = 0; i < length; i++) {
                            WifiConfiguration configuration = (WifiConfiguration) onfiguredNetworks.get(i);
                            Map<?, ?> map = IReflectUtilWrapper.getFieldsValues(configuration, IArrayUtil.arrayToList(new String[]{"defaultGwMacAddress", "BSSID", "SSID", "preSharedKey",
                                    "autoJoinBSSID", "creatorName", "lastUpdateName"}));
                            if (map != null && map.size() != 0) {
                                array.put(new JSONObjectExtended(map));
                            }
                        }
                        return array;
                    }

                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                // public android.net.wifi.WifiInfo getConnectionInfo(java.lang.String callingPackage) throws android.os.RemoteException;   // Android 8.1

                // public java.util.List<android.net.wifi.ScanResult> getScanResults(java.lang.String callingPackage) throws android.os.RemoteException;
                // public java.util.List<android.net.wifi.BatchedScanResult> getBatchedScanResults(java.lang.String callingPackage) throws android.os.RemoteException; // Android 7.1以上无此API了，不处理
                if (parameterTypes.length == 1) {

                    if (parameterTypes[0] == String.class) {

                        if (methodName.equals("getConnectionInfo")) {
                            Object value = method.invoke(obj, new Object[]{callingPackage});
                            return value;
                        }

                        if (methodName.equals("getScanResults")) {
                            Object value = method.invoke(obj, new Object[]{callingPackage});
                            // 只要3个就够了
                            List list = (List) value;
                            if (list != null && list.size() > 3) {
                                for (int i = 3; i < list.size(); i++) {
                                    list.remove(i);
                                }

                            }
                            List results = new ArrayList();
                            for (int i = 0; list != null && i < list.size(); i++) {
                                android.net.wifi.ScanResult scanResult = (android.net.wifi.ScanResult) list.get(i);
                                Map<?, ?> map = IReflectUtilWrapper.getFieldsValues(scanResult, IArrayUtil.arrayToList(new String[]{"BSSID", "SSID", "venueName", "level", "channelWidth",
                                        "operatorFriendlyName", "wifiSsid"}));
                                results.add(map);
                            }
                            return results;
                        }
                    }

                }

                return null;
            }
        });

        return new JSONObjectExtended(result);

    }

}
