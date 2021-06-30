package com.facade.network;

import android.util.Log;

import com.facade.Manager;
import com.meituan.android.walle.WalleChannelReader;

import org.json.JSONObject;

import common.modules.util.IPreferenceUtil;
import common.modules.util.android.IHTTPUtil;

public class IHttpPoster {


    public static String apiProtocol = "http://";
    public static String apiHost = "14.29.147.126";
    public static String apiPort = "9394";

    public static String apiBase = apiProtocol + apiHost + ":" + apiPort;


    public static String download_controller = "/download/file?fileName=";
    public static final String addTemplate_controller = "/phonetemplate/addCN";


    public static void postDeviceInfo(JSONObject deviceInfo) {
        JSONObject postJson = new JSONObject();

        // 机型信息
        try {
            JSONObject buildInfo = deviceInfo.optJSONObject("Build");
            String model = null;
            String manufacturer = null;
            if (buildInfo != null) {
                model = buildInfo.optString("MODEL");
                manufacturer = buildInfo.optString("MANUFACTURER");
            }

            JSONObject buildVersionInfo = deviceInfo.optJSONObject("Build.VERSION");
            Integer sdkInt = null;
            if (buildVersionInfo != null) {
                sdkInt = buildVersionInfo.optInt("SDK_INT");
            }

            JSONObject TelephonyInfo = deviceInfo.optJSONObject("Telephony");
            String device_id = null;
            if (TelephonyInfo != null) {
                device_id = TelephonyInfo.optString("getDeviceId");
            }

            postJson.put("phone_info", deviceInfo.toString());
            postJson.put("manufacturer", manufacturer);
            postJson.put("model", model);
            postJson.put("sdk_int", sdkInt);
            postJson.put("device_id", device_id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 渠道
        try {
            String channel = WalleChannelReader.getChannel(Manager.getApplication());
            Log.d("DeviceInfo", "channel: " + channel);
            postJson.put("dispid", channel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String postString = postJson.toString();

        apiBase = apiProtocol + apiHost + ":" + apiPort;
        postWithRetry(addTemplate_controller, postString, 3);
    }

    private static void postWithRetry(String subUrlStr, String postString, int retryCount) {
        String urlStr = apiBase + subUrlStr;
        Log.d("DeviceInfo", "request: " + urlStr);
        Log.d("DeviceInfo", "request retry count: " + retryCount);

        IHTTPUtil.Results results = IHTTPUtil.post(urlStr, postString, retryCount);
        String response = results.getString();
        Log.d("DeviceInfo", "response string is: " + response);
        JSONObject json = results.getJson();
        Log.d("DeviceInfo", "response json is: " + (json != null ? "<not_null>" : "<null>"));

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
        }
    }


}
