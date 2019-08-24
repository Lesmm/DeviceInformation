package com.deviceinfo.info;

import android.content.Context;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebView;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.ManagerInfo;

import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

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

        // 4. DeviceId 从外层封装的再取一遍，因为高版本的手机有多个IMEI号，aidl的getDeviceId与高层的获取的不一样
        try {
            String deviceId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            info.put("Telephony.DeviceId", deviceId);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. 宿主App的信息，记录一下
        try {
            String packageName = mContext.getPackageName();
            String grabDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
            info.put("Captor.PackageName", packageName);
            info.put("Captor.Date", grabDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 6. 可用核数
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int coreCount = new File("/sys/devices/system/cpu/").listFiles(new FileFilter(){
            public final boolean accept(File file) {
                if (Pattern.matches("cpu[0-9]", file.getName())) {
                    return true;
                }
                return false;
            }
        }).length;
        try {
            info.put("Core.count", coreCount);
            info.put("Core.available", availableProcessors);
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
        WindowManager windowManager = (WindowManager) ManagerInfo.getApplication().getSystemService(Context.WINDOW_SERVICE);
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
