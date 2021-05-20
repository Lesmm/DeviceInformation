package com.deviceinfo.higher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;

import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.modules.util.IJSONObjectUtil;

public class HiTelephonyManager extends HiBase {

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
        final TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager == null) {
            return null;
        }

        final Map<String, Object> map = new HashMap<>();

        HiBase.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, new Runnable() {
            @Override
            public void run() {
                @SuppressLint("MissingPermission")
                List<CellInfo> allCellInfo = manager.getAllCellInfo();
                __put_2_map__(map, allCellInfo, "getAllCellInfo");
            }
        });

        CellLocation cellLocation = manager.getCellLocation();
        __put_2_map__(map, cellLocation, "getCellLocation");

        return map;
    }

    @Override
    protected JSONObject keysMappings() {
        JSONObject mapping = new JSONObject();

        IJSONObjectUtil.putJSONObject(mapping, "getAllCellInfo", "getAllCellInfo");
        IJSONObjectUtil.putJSONObject(mapping, "getCellLocation", "getCellLocation");

        return mapping;
    }
}
