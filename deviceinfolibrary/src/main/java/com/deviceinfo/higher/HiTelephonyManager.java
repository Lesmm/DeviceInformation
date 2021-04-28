package com.deviceinfo.higher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellInfo;
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
        JSONObject results = new JSONObject();

        final TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager == null) {
            return results;
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


        return new JSONObjectExtended(map);
    }

    @Override
    protected JSONObject keysMappings() {
        JSONObject mapping = new JSONObject();

        IJSONObjectUtil.putJSONObject(mapping, "getAllCellInfo", "getAllCellInfo");

        return mapping;
    }
}
