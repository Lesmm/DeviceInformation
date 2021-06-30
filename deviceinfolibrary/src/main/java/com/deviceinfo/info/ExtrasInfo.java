package com.deviceinfo.info;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
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
import com.facade.Manager;
import com.meituan.android.walle.WalleChannelReader;

import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.modules.util.IActivityUtil;
import common.modules.util.IBundleUtil;
import common.modules.util.IJSONObjectUtil;
import common.modules.util.IReflectUtil;
import common.modules.util.IThreadUtil;
import common.modules.util.android.IHTTPUtil;

public class ExtrasInfo {

    public static JSONObject getInfo(Context mContext) {
        try {
            return __getInfo__(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    private static JSONObject __getInfo__(Context mContext) {
        JSONObject info = new JSONObject();

        // Build 信息
        try {
            IJSONObjectUtil.putJSONObject(info, "VERSION.SDK_INT", Build.VERSION.SDK_INT);
            IJSONObjectUtil.putJSONObject(info, "VERSION.RELEASE", Build.VERSION.RELEASE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 宿主App的信息，记录一下
        try {
            JSONObject captorJson = new JSONObject();
            IJSONObjectUtil.putJSONObject(info, "Captor", captorJson);
            IJSONObjectUtil.putJSONObject(captorJson, "PackageName", mContext.getPackageName());
            IJSONObjectUtil.putJSONObject(captorJson, "Uid", android.os.Process.myUid());
            IJSONObjectUtil.putJSONObject(captorJson, "Version", Manager.VERSION);
            IJSONObjectUtil.putJSONObject(captorJson, "Date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date()));
            TimeZone timeZone = Calendar.getInstance().getTimeZone();
            String timeZoneName = timeZone.getDisplayName(false, TimeZone.SHORT);
            String timeZoneID = timeZone.getID();
            IJSONObjectUtil.putJSONObject(captorJson, "Zone.name", timeZoneName);
            IJSONObjectUtil.putJSONObject(captorJson, "Zone.id", timeZoneID);
            IJSONObjectUtil.putJSONObject(captorJson, "Channel", WalleChannelReader.getChannel(Manager.getApplication()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissions = new String[]{
                        Manifest.permission.READ_PHONE_STATE,

                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,

                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                };
                for (int i = 0; i < permissions.length; i++) {
                    String p = permissions[i];
                    boolean isGranted = Manager.getApplication().checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED;
                    IJSONObjectUtil.putJSONObject(captorJson, p, isGranted);
                }
            }
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
        int coreCount = new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
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
                Map<String, Object> map = (Map<String, Object>) IReflectUtil.objectFieldNameValues(location);
                map.put("mExtras", IBundleUtil.createJSONFromBundle(location.getExtras()));
                info.put("Location.location", new JSONObject(map));
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // IP 地址
        try {
            if (ipAddressCached != null && !ipAddressCached.isEmpty()) {
                info.put("Location.ip", ipAddressCached);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 6. Wifi 扫描列表信息. 2021.05.20 外边高层API也再扫了一遍，这里就不扫了
        /**
         try {
         WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
         List<ScanResult> scanResults = wifiManager.getScanResults();
         JSONArray scanResultsArray = new JSONArrayExtended(scanResults);
         IJSONObjectUtil.putJSONObject(info, "Wifi.ScanResult", scanResultsArray);
         } catch (Exception e) {
         e.printStackTrace();
         }
         **/

        // 系统信息 uname
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

        // 内存信息
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

        // 看看是否有su
        JSONObject suJson = null;
        String[] DEFAULT_ROOTS = {
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/su/bin/su",
                "/su/xbin/su",
                "/magisk/.core/bin/su"};
        for (int i = 0; i < DEFAULT_ROOTS.length; i++) {
            String su_path = DEFAULT_ROOTS[i];
            boolean isExisted = new File(su_path).exists();
            if (isExisted) {
                if (suJson == null) {
                    suJson = new JSONObject();
                }
                IJSONObjectUtil.putJSONObject(suJson, su_path, isExisted);
            }
        }
        if (suJson != null) {
            IJSONObjectUtil.putJSONObject(info, "su", suJson);
        }

        return info;
    }

    /**
     * Ip address
     */

    public static String ipAddressCached = null;

    public static void cacheIpAddressAsync() {
        if (ipAddressCached != null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = requestIpAddressSync("http://myip.ipip.net");
                if (ip == null || ip.isEmpty()) {
                    ip = requestIpAddressSync("http://ip-api.com/json/");
                }
                if (ip == null || ip.isEmpty()) {
                    ipAddressCached = ip;
                }
            }
        }).start();
    }

    public static String requestIpAddressSync(String ipUrl) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("__connect_timeout__", 30 * 1000);
            headers.put("__read_timeout__", 30 * 1000);
            IHTTPUtil.Results results = IHTTPUtil.get(ipUrl, headers, 0);
            Matcher matcher = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+").matcher(results.getString());
            return matcher.find() ? matcher.group() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Util methods
     */

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
        final String[] results = new String[1];

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // -------------------- the same ------------------
                    String userAgent = new WebView(mContext).getSettings().getUserAgentString();
                    results[0] = userAgent;
                    // -------------------- the same ------------------
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        boolean isInMainThread = Looper.getMainLooper() == Looper.myLooper();
        if (isInMainThread) {
            runnable.run();
        } else {
            new android.os.Handler(Looper.getMainLooper()).post(runnable);
            IThreadUtil.trySleep(8000);     // wait for 8 seconds
        }
        return results[0];
    }

}
