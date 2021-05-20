package com.deviceinfo.higher;

import android.Manifest;
import android.annotation.SuppressLint;
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
        try {
            Map<String, Object> map = __getInfo__(mContext);
            return new JSONObjectExtended(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public Map<String, Object> __getInfo__(Context mContext) {
        final WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (manager == null) {
            return null;
        }

        final Map<String, Object> map = new HashMap<>();

        HiBase.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, new Runnable() {
            @Override
            public void run() {
                @SuppressLint("MissingPermission")
                List<WifiConfiguration> configuredNetworks = manager.getConfiguredNetworks();
                __put_2_map__(map, configuredNetworks, "getConfiguredNetworks");
            }
        });

        WifiInfo connectionInfo = manager.getConnectionInfo();
        __put_2_map__(map, connectionInfo, "getConnectionInfo");

        DhcpInfo dhcpInfo = manager.getDhcpInfo();
        __put_2_map__(map, dhcpInfo, "getDhcpInfo");

        List<ScanResult> scanResults = manager.getScanResults();
        __put_2_map__(map, scanResults, "getScanResults");

        int wifiState = manager.getWifiState();
        __put_2_map__(map, wifiState, "getWifiState");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("MissingPermission")
            List<PasspointConfiguration> passpointConfigurations = manager.getPasspointConfigurations();
            __put_2_map__(map, passpointConfigurations, "getPasspointConfigurations");
        }
        return map;
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
