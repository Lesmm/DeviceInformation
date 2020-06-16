package com.deviceinfo.info;

import android.app.ActivityManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.system.Os;
import android.system.StructUtsname;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebView;

import com.deviceinfo.JSONObjectExtended;

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

import common.modules.util.IActivityUtil;
import common.modules.util.IReflectUtil;

public class ExtrasInfo {

    public static JSONObject getInfo(Context mContext) {
        JSONObject info = new JSONObject();

        // 宿主App的信息，记录一下
        try {
            String packageName = mContext.getPackageName();
            String grabDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
            info.put("Captor.PackageName", packageName);
            info.put("Captor.Date", grabDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // DeviceId 从外层封装的再取一遍，因为高版本的手机有多个IMEI号，aidl的getDeviceId与高层的获取的不一样
        try {
            String deviceId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            info.put("Telephony.DeviceId", deviceId);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


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

        // 4. 可用核数
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

        // 5. 地理位置
        try {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                info.put("Location.longitude", location.getLongitude());
                info.put("Location.latitude", location.getLatitude());
                info.put("Location.location", IReflectUtil.objectFieldNameValues(location));
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 6. 系统信息 uname
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            StructUtsname uname = Os.uname();

            JSONObject unameInfo = new JSONObject();
            try {
                info.put("Sys.Uname", unameInfo);
                unameInfo.put("version", uname.version);
                unameInfo.put("release", uname.release);
                unameInfo.put("sysname", uname.sysname);
                unameInfo.put("nodename", uname.nodename);
                unameInfo.put("machine", uname.machine);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 7. 内存信息
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo amMemoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(amMemoryInfo);
        long totalMemory = amMemoryInfo.totalMem;

        Long totalMemVal = (Long) IReflectUtil.invokeClassMethod("android.os.Process", "getTotalMemory", null, null);
        long totalMem = totalMemVal != null ? totalMemVal : 0;

        JSONObject memoryJson = new JSONObject();
        try {
            info.put("Sys.Memory", memoryJson);
            memoryJson.put("MemTotal", totalMemory != 0 ? totalMemory : totalMem);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 文件信息
        JSONObject statfsJson = new JSONObject();

        String path = "";
        StatFs stat = null;
        long blockSize = 0;
        long blockCount = 0;
        try {
            JSONObject json = null;

            path = Environment.getExternalStorageDirectory().getPath();  // "/storage/emulated/0"
            stat = new StatFs(path);
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getBlockCountLong();

            json = new JSONObject();
            json.put("block_size", blockSize);
            json.put("block_count", blockCount);
            statfsJson.put(path, json);

            path = "/sdcard";
            stat = new StatFs(path);
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getBlockCountLong();

            json = new JSONObject();
            json.put("block_size", blockSize);
            json.put("block_count", blockCount);
            statfsJson.put(path, json);

            path = Environment.getDataDirectory().getPath(); // "/data"
            stat = new StatFs(path);
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getBlockCountLong();

            json = new JSONObject();
            json.put("block_size", blockSize);
            json.put("block_count", blockCount);
            statfsJson.put(path, json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (statfsJson.length() != 0) {
                info.put("StatFs.Info", statfsJson);
            }
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
                            stringBuilder.append(String.format("%02X:", b)); // %02X for uppercase, %02x for lowercase
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
        WindowManager windowManager = (WindowManager) IActivityUtil.getApplication().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);

        int widthPixels = outMetrics.widthPixels;
        int heightPixels = outMetrics.heightPixels;
        float density = outMetrics.density;
        float densityDpi = outMetrics.densityDpi;
        float xdpi = outMetrics.xdpi;
        float ydpi = outMetrics.ydpi;

        JSONObject displayMetricsInfo = new JSONObjectExtended().__objectToJson__(outMetrics);
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
