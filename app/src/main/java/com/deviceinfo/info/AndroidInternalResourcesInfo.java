package com.deviceinfo.info;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import common.modules.util.IReflectUtil;

// TODO ... Hook 那边 hook 的话把 LoadedApk 的 mResources field 改成我们的 继承Reources的类 的实例，重写get方法
public class AndroidInternalResourcesInfo {

    public static JSONObject getInfo(Context mContext) {

        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

            // getMmsUserAgent() -> mContext.getResources().getString(com.android.internal.R.string.config_mms_user_agent);
            String mmsUserAgent = telephonyManager.getMmsUserAgent();
            int config_mms_user_agent = (Integer) IReflectUtil.getFieldValue(Class.forName("com.android.internal.R$string"), "config_mms_user_agent");
            String mmsUserAgentInResource = mContext.getResources().getString(config_mms_user_agent);

            // getMmsUAProfUrl() -> mContext.getResources().getString(com.android.internal.R.string.config_mms_user_agent_profile_url);
            String mmsUAProfUrl = telephonyManager.getMmsUAProfUrl();
            int config_mms_user_agent_profile_url = (Integer) IReflectUtil.getFieldValue(Class.forName("com.android.internal.R$string"), "config_mms_user_agent_profile_url");
            String mmsUAProfUrlInResource = mContext.getResources().getString(config_mms_user_agent_profile_url);

            Log.d("DeviceInfo", "_set_debug_here_");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject info = new JSONObject();

        // --------------------------------- String  ---------------------------------
        JSONObject stringResultsJson = getResourcesKeysValues(mContext, "com.android.internal.R$string");
        // --------------------------------- Boolean  ---------------------------------
        JSONObject boolResultsJson = getResourcesKeysValues(mContext, "com.android.internal.R$bool");
        // --------------------------------- Integer  ---------------------------------
        JSONObject integerResultsJson = getResourcesKeysValues(mContext, "com.android.internal.R$integer");

        Log.d("DeviceInfo", "_set_debug_here_");

        // 过滤出我们需要的, just one now ...
        try {

            // string
            info.put("config_mms_user_agent", stringResultsJson.optString("config_mms_user_agent"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    private static JSONObject getResourcesKeysValues(Context mContext, String clazzName) {
        Map<Object, Object> results = new HashMap<>();
        try {
            Class clazz = Class.forName(clazzName);
            Map<?, ?> namesValues = IReflectUtil.objectFieldNameValues(clazz);
            for (Object name : namesValues.keySet()) {
                try {
                    Integer fieldValue = (Integer) namesValues.get(name);
                    Object value = null;
                    if (clazzName.contains("string")) {
                        value = mContext.getResources().getString(fieldValue);
                    } else if (clazzName.contains("bool")) {
                        value = mContext.getResources().getBoolean(fieldValue);
                    } else if (clazzName.contains("integer")) {
                        value = mContext.getResources().getInteger(fieldValue);
                    }
                    if (value != null) {
                        results.put((Object) name, (Object) value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject stringResultsJson = new JSONObject(results);
        return stringResultsJson;
    }


}
