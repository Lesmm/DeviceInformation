package com.deviceinfo.info;

import android.content.Context;
import android.os.Build;

import org.json.JSONObject;

import common.modules.util.IReflectUtil;

public class BuildInfo {

    public static JSONObject getBuildInfo(Context mContext) {

        JSONObject buildJson = new JSONObject(IReflectUtil.objectFieldNameValues(Build.class));


        // 8.0以上时
        // TODO ... 同时 Hook 那边判断有没有(因为8.0以上才有) IDeviceIdentifiersPolicyService device_identifiers 这个service, 并Hook了所有aidl
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                String serial = Build.getSerial();
                String SERIAL = buildJson.optString("SERIAL");
                if (SERIAL == null || SERIAL.isEmpty() || SERIAL.equals(Build.UNKNOWN)) {
                    buildJson.put("SERIAL", serial);
                }
            } catch ( SecurityException e ) {
                e.printStackTrace();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        return buildJson;
    }

    public static JSONObject getBuildVersionInfo(Context mContext) {

        JSONObject buildVersionJson = new JSONObject(IReflectUtil.objectFieldNameValues(Build.VERSION.class));

        // 这个就不需要了，版本代码都约定好了的
        // JSONObject buildVersionCodesJson = new JSONObject(IReflectUtil.objectFieldNameValues(Build.VERSION_CODES.class));

        return buildVersionJson;
    }


}
