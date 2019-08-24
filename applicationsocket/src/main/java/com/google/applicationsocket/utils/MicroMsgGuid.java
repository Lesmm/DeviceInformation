package com.google.applicationsocket.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Random;

// 微信Guid源码逻辑相关
public class MicroMsgGuid {

    private static final String TAG = "xxxooo";

    // 从本机拿出guid
    public static String get_guid(Context context) {
        String guid;
        StringBuilder stringBuilder = new StringBuilder();

        String android_id = Settings.Secure.getString(context.getContentResolver(), "android_id");
        Log.d(TAG, "android_id: " + android_id);
        stringBuilder.append(android_id);

        String device_id = get_A_imei_16(context);
        Log.d(TAG, "A_imei_16: " + device_id);
        stringBuilder.append(device_id);
        stringBuilder.append(getHardWareId());
        guid = "A" + byteTomd5(stringBuilder.toString().getBytes()).substring(0, 15);
        return guid;
    }

    /*
     *   辅助方法
     */
    public static final String byteTomd5(byte[] bArr) {
        char[] cArr = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(bArr);
            byte[] digest = instance.digest();
            int length = digest.length;
            char[] cArr2 = new char[(length * 2)];
            int i = 0;
            int i2 = 0;
            while (i < length) {
                byte b = digest[i];
                int i3 = i2 + 1;
                cArr2[i2] = cArr[(b >>> 4) & 15];
                int i4 = i3 + 1;
                cArr2[i3] = cArr[b & 15];
                i++;
                i2 = i4;
            }
            return new String(cArr2);
        } catch (Exception e) {
            return null;
        }
    }

    private static String get_A_imei_16(Context context) {
        String obj;
        int i = 0;
        String imei = getDeviceId(context);
        Log.d(TAG, "imei: " + imei);
        if (imei == null || imei.length() <= 0) {
            Random random = new Random();
            random.setSeed(System.currentTimeMillis());
            obj = "A";
            while (i < 15) {
                obj = obj + ((char) (random.nextInt(25) + 65));
                i++;
            }
        } else {
            obj = ("A" + imei + "123456789ABCDEF").substring(0, 15);
        }
        return obj;
    }

    private static String get_A_imei_16_json(String imei) {
        String obj;
        int i = 0;
        obj = ("A" + imei + "123456789ABCDEF").substring(0, 15);
        return obj;
    }

    public static String getDeviceId(Context context) {

        try {
            @SuppressLint("WrongConstant") TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (telephonyManager == null) {
                return null;
            }
            @SuppressLint("MissingPermission") String deviceId = telephonyManager.getDeviceId();
            if (deviceId == null) {
                return null;
            }
            deviceId = deviceId.trim();
            return deviceId;
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }


    private static String getHardWareId() {
        String cpuInfo = getCpuInfo();
        Log.d(TAG, "cpu_info: " + cpuInfo);
        String str = Build.MANUFACTURER + Build.MODEL + cpuInfo;
        return str;
    }

    private static String getHardWareId_json(String manufacturer, String model, String cpuinfo) {
        return manufacturer + model + cpuinfo;
    }

    private static String getCpuInfo_json(String cpuinfo_origin) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(cpuinfo_origin));
        HashMap hashMap = new HashMap();
        try {
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] split = readLine.split(":", 2);
                    if (split != null && split.length >= 2) {
                        String trim = split[0].trim();
                        readLine = split[1].trim();
                        if (hashMap.get(trim) == null) {
                            hashMap.put(trim, readLine);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            bufferedReader.close();
        } catch (Exception e3) {
            e3.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(": ");
        stringBuilder.append(hashMap.get("Features"));
        stringBuilder.append(": ");
        stringBuilder.append(hashMap.get("Processor"));
        stringBuilder.append(": ");
        stringBuilder.append(hashMap.get("CPU architecture"));
        stringBuilder.append(": ");
        stringBuilder.append(hashMap.get("Hardware"));
        stringBuilder.append(": ");
        stringBuilder.append(hashMap.get("Serial"));
        String stringBuilder2 = stringBuilder.toString();
        return stringBuilder2;

    }

    public static String getCpuInfo() {
        HashMap<String, String> cpuinfo = catCpuinfo();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(": ");
        stringBuilder.append(cpuinfo.get("Features"));
        stringBuilder.append(": ");
        stringBuilder.append(cpuinfo.get("Processor"));
        stringBuilder.append(": ");
        stringBuilder.append(cpuinfo.get("CPU architecture"));
        stringBuilder.append(": ");
        stringBuilder.append(cpuinfo.get("Hardware"));
        stringBuilder.append(": ");
        stringBuilder.append(cpuinfo.get("Serial"));
        String stringBuilder2 = stringBuilder.toString();
        return stringBuilder2;
    }


    public static HashMap<String, String> catCpuinfo() {
        HashMap hashMap = new HashMap();
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/cpuinfo"), "UTF-8"));
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] split = readLine.split(":", 2);
                    if (split != null && split.length >= 2) {
                        String trim = split[0].trim();
                        readLine = split[1].trim();
                        if (hashMap.get(trim) == null) {
                            hashMap.put(trim, readLine);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            bufferedReader.close();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return hashMap;
    }
}
