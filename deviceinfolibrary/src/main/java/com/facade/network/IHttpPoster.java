package com.facade.network;

import android.util.Log;

import com.facade.Manager;

import org.json.JSONObject;

import common.modules.util.IHttpUtil;
import common.modules.util.IPreferenceUtil;

public class IHttpPoster {

    //    public static final String apiIp_domain_1 = "www.nsshw.com";
//    public static final String apiIp_domain_2 = "www.game8111.com";
    public static final String apiIp = "139.9.44.149";      // 华为云
    public static final String apiIp_domain_1 = "139.9.44.149";
    public static final String apiIp_domain_2 = "139.9.44.149";
//    public static final String apiIp = "192.168.3.208";      // 本机

    public static String apiPort = "10099";
    public static String apiProtocol = "https://";

    public static String apiBase = apiProtocol + apiIp + ":" + apiPort;


    public static String download_controller = "/download/file?fileName=";
    public static final String addTemplate_controller = "/phonetemplate/addCN";
    public static final String checkConnection_controller = "/common/check_connection";


    public static void postDeviceInfo(JSONObject deviceInfo) {
        JSONObject postJson = new JSONObject();
        try {
            JSONObject buildInfo = deviceInfo.optJSONObject("Build");
            String manufacturer = buildInfo.optString("MANUFACTURER");
            String model = buildInfo.optString("MODEL");

            JSONObject buildVersionInfo = deviceInfo.optJSONObject("Build.VERSION");
            Integer sdkInt = buildVersionInfo.optInt("SDK_INT");

            JSONObject TelephonyInfo = deviceInfo.optJSONObject("Telephony");
            String device_id = TelephonyInfo.optString("getDeviceId");

            postJson.put("phone_info", deviceInfo.toString());
            postJson.put("manufacturer", manufacturer);
            postJson.put("model", model);
            postJson.put("device_id", device_id);
            postJson.put("sdk_int", sdkInt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String postString = postJson.toString();

        apiBase = apiProtocol + apiIp + ":" + apiPort;
        postWithRetry(addTemplate_controller, postString, 3);
    }

    private static void postWithRetry(String subUrlStr, String postString, int retryCount) {
        String urlStr = apiBase + subUrlStr;
        Log.d("DeviceInfo", "request: " + urlStr);
        Log.d("DeviceInfo", "request retry count: " + retryCount);

        retryCount--;
        if (retryCount < 0) {
            return;
        }

        final String fSubUrlStr = subUrlStr;
        final String fPostString = postString;
        final int fRetryCount = retryCount;

        IHttpUtil.post(false, urlStr, null, postString, new IHttpUtil.JsonCallback() {
            @Override
            public void handle(JSONObject json) {
                Log.d("DeviceInfo", "RESPONSE: " + (json != null ? json.toString() : "response json is null"));

                if (json != null) {
                    Log.d("DeviceInfo", "response: " + json.toString());
                } else {
                    Log.d("DeviceInfo", "response: <null>");
                }

                if (json != null && json.optBoolean("flag")) {
                    // 收集及上传成功
                    Log.d("DeviceInfo", "Post Success");
                    int newVal = IPreferenceUtil.getSharedPreferences().getInt(Manager.__key_count_dev_info_got__, 0);
                    IPreferenceUtil.setSharedPreferences(Manager.__key_count_dev_info_got__, (Integer) (++newVal));
                } else {
                    // 上传失败
                    Log.d("DeviceInfo", "Post Failed");

                    if (json != null && json.optInt("code") == 20001 && json.optString("message").contains("exception")) {
                        Log.d("DeviceInfo", "Internal exception, no need to retry???");
                    }

                    // 重新上传
                    if (fRetryCount == 2) {
                        apiBase = IHttpPoster.apiProtocol + apiIp_domain_1 + ":" + apiPort;
                    } else if (fRetryCount == 1) {
                        apiBase = IHttpPoster.apiProtocol + apiIp_domain_2 + ":" + apiPort;
                    } else if (fRetryCount == 0) {
                        apiBase = IHttpPoster.apiProtocol + apiIp + ":" + apiPort;
                    }
                    postWithRetry(fSubUrlStr, fPostString, fRetryCount);
                }
            }
        });
    }

    public static void checkConnection() {
        String urlStr = apiBase + checkConnection_controller;
        Log.d("DeviceInfo", "request: " + urlStr);
        IHttpUtil.getAsync(urlStr, null, new IHttpUtil.JsonCallback() {
            @Override
            public void handle(JSONObject json) {

                if (json != null && json.optBoolean("flag")) {
                    Log.d("DeviceInfo", "connection is good");
                } else {
                    Log.d("DeviceInfo", "connection is lost");
                }
            }
        });
    }

}
