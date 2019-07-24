package com.deviceinfo.info;

import android.content.Context;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebView;

import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.Manager;

import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class ExtrasInfo {

    public static JSONObject getInfo(Context mContext) {
        JSONObject info = new JSONObject();

        // 1. All interfaces address in other way
        JSONObject addressInfo = getAllInterfacesAddress();
        try {
            info.put("Network.Address", addressInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. WebKit.UserAgent
        String userAgent = getWebKitUserAgent(mContext);
        try {
            info.put("WebKit.UserAgent", userAgent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 屏幕尺寸
        JSONObject displayMetricsInfo = getDisplayMetricsInfo();
        try {
            info.put("Display.Metrics", displayMetricsInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    private static JSONObject getAllInterfacesAddress() {
        JSONObject addressInfo = new JSONObject();
        try {
            List<NetworkInterface> allInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface netInterface : allInterfaces) {
                try {
                    String ifName = netInterface.getName();
                    byte[] hardwareAddressBytes = netInterface.getHardwareAddress();
                    if (hardwareAddressBytes != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (byte b : hardwareAddressBytes) {
                            stringBuilder.append(String.format("%02X:", b));
                        }
                        if (stringBuilder.length() > 0) {
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        }

                        String address = stringBuilder.toString();
                        addressInfo.put(ifName, address);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addressInfo;
    }

    private static JSONObject getDisplayMetricsInfo() {
        WindowManager windowManager = (WindowManager) Manager.getApplication().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);

        int widthPixels = outMetrics.widthPixels;
        int heightPixels = outMetrics.heightPixels;
        float density = outMetrics.density;
        float densityDpi = outMetrics.densityDpi;
        float xdpi = outMetrics.xdpi;
        float ydpi = outMetrics.ydpi;

        JSONObject displayMetricsInfo = JSONObjectExtended.objectToJson(outMetrics);
        return displayMetricsInfo;
    }

    private static String getWebKitUserAgent(final Context mContext) {
        final String results[] = new String[1];

        boolean isInMainThread = Looper.getMainLooper() == Looper.myLooper();
        if (isInMainThread) {
            // -------------------- the same ------------------
            String userAgent = new WebView(mContext).getSettings().getUserAgentString();
            results[0] = userAgent;
            // -------------------- the same ------------------

        } else {

            new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // -------------------- the same ------------------
                    String userAgent = new WebView(mContext).getSettings().getUserAgentString();
                    results[0] = userAgent;
                    // -------------------- the same ------------------

                    // go on ...
                    synchronized (ExtrasInfo.class) {
                        try {
                            ExtrasInfo.class.notify();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });

            // wait ....
            synchronized (ExtrasInfo.class) {
                try {
                    ExtrasInfo.class.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return results[0];
    }



}