package com.deviceinfo.higher;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Build;

import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.modules.util.IJSONObjectUtil;

public class HiWifiManager extends HiBase {

    @Override
    public JSONObject getInfo(Context mContext) {
        JSONObject results = new JSONObject();

        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (manager == null) {
            return results;
        }

        Map<String, Object> map = new HashMap<>();

        List<WifiConfiguration> configuredNetworks = manager.getConfiguredNetworks();
        __put_2_map__(map, configuredNetworks, "getConfiguredNetworks");

        WifiInfo connectionInfo = manager.getConnectionInfo();
        __put_2_map__(map, connectionInfo, "getConnectionInfo");

        DhcpInfo dhcpInfo = manager.getDhcpInfo();
        __put_2_map__(map, dhcpInfo, "getDhcpInfo");

        List<ScanResult> scanResults = manager.getScanResults();
        __put_2_map__(map, scanResults, "getScanResults");

        int wifiState = manager.getWifiState();
        __put_2_map__(map, wifiState, "getWifiState");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<PasspointConfiguration> passpointConfigurations = manager.getPasspointConfigurations();
            __put_2_map__(map, passpointConfigurations, "getPasspointConfigurations");
        }

        return new JSONObjectExtended(map);
    }

    @Override
    protected JSONObject keysMappings() {
        JSONObject mapping = new JSONObject();

        IJSONObjectUtil.putJSONObject(mapping, "getConfiguredNetworks", "getConfiguredNetworks");
        IJSONObjectUtil.putJSONObject(mapping, "getConnectionInfo", "getConnectionInfo");
        IJSONObjectUtil.putJSONObject(mapping, "getDhcpInfo", "getDhcpInfo");
        IJSONObjectUtil.putJSONObject(mapping, "getScanResults", "getScanResults");
        IJSONObjectUtil.putJSONObject(mapping, "getWifiState", "getWifiEnabledState");
        IJSONObjectUtil.putJSONObject(mapping, "getPasspointConfigurations", "getPasspointConfigurations");

        return mapping;
    }
}
