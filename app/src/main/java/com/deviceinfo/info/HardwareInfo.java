package com.deviceinfo.info;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Debug;
import android.util.Log;

import com.deviceinfo.JSONArrayExtended;
import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.ManagerInfoHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import common.modules.util.IFileUtil;
import common.modules.util.IProcessUtil;

public class HardwareInfo {

    public static JSONObject getInfoInFiles(Context mContext) {

        if (ManagerInfoHelper.IS_DEBUG) {
            try {
                ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);

                // TODO ... Hook 那边 处理一下 activity service 的 getMemoryInfo 的 "totalMem" 值(bytes)，要与 "/proc/meminfo" 的 MemTotal: XXX kB 要一致。注意单位！
                ActivityManager.MemoryInfo amMemoryInfo = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(amMemoryInfo);
                JSONObject amMemoryInfoJson = JSONObjectExtended.objectToJson(amMemoryInfo);

                ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                am.getMyMemoryState(appProcessInfo);
                JSONObject amAppProcessInfoJson = JSONObjectExtended.objectToJson(appProcessInfo);

                ConfigurationInfo configurationInfo = am.getDeviceConfigurationInfo();
                JSONObject amConfigurationInfoJson = JSONObjectExtended.objectToJson(configurationInfo);

                Debug.MemoryInfo[] amDebugMemoryInfos = am.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
                JSONArray amDebugMemoryInfoArray = new JSONArrayExtended(amDebugMemoryInfos);

                Log.d("DeviceInfo","_set_debug_here_");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JSONObject filesInfos  = new JSONObject();

        try {
            String key = "/proc/cpuinfo";
            String info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/proc/meminfo";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/proc/version";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/proc/mounts";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/proc/cmdline";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // --------------- MAC & /sys/class/net ---------------
            key = "/proc/net/if_inet6";         // 组成规则看 NetworkInterface.java 的方法 collectIpv6Addresses
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // wlan0
            key = "/sys/class/net/wlan0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/fb000000.qcom,wcnss-wlan/net/wlan0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/class/net/wlan0/ifindex";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/fb000000.qcom,wcnss-wlan/net/wlan0/ifindex";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // p2p0 这个的 address 倒和上面的 MAC 地址是一样的
            key = "/sys/class/net/p2p0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/fb000000.qcom,wcnss-wlan/net/p2p0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/class/net/p2p0/ifindex";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/fb000000.qcom,wcnss-wlan/net/p2p0/ifindex";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // dummy0
            key = "/sys/class/net/dummy0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/virtual/net/dummy0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/class/net/dummy0/ifindex";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/virtual/net/dummy0/ifindex";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // lo
            key = "/sys/class/net/lo/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/virtual/net/lo/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/class/net/lo/ifindex";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/virtual/net/lo/ifindex";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // TODO ... Hook 那边, /sys/class/net 下的其余接口如 rev_rmnet0 等等的址，跟 ifconfig -a 的输出 HWaddr 值是一致的，可以根据此命令的Json内容来处理

            // TODO ... Hook 那边， /system/build.prop 放在 Hook 那边拿所有的 SystemProperties 来 处理
            // /default.prop 则在这里也带上一份
            key = "/default.prop";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return  filesInfos;
    }


    public static JSONObject getInfoInCommands(Context mContext) {

        // TODO ... Hook 那边处理 dumpsys meminfo 里的字符串，把所CM的东西给替换了。如果加上PID: dumpsys meminfo [pid] 这个倒没什么。

        JSONObject commandsInfos  = new JSONObject();

        try {

            // TODO ... Hook 那边 根据下面命令的结果 来处理 ifconfig 带上别的选项的返回情况
            String command = "ifconfig -a";
            String output = IProcessUtil.execCommands(command);
            commandsInfos.put(command, output);

            // TODO ... Hook 那边 拿所有 SystemPropertiesInfo 的Keys-Values整成[key]: [value]格式来返回当APP 执行 getprop 时， getprop 的内容不在这里带上了。
            // TODO ... Hook 那边 根据下面命令的结果 来处理 getprop 带上具体一个Key的情况
            if(ManagerInfoHelper.IS_DEBUG) {
                command = "getprop";
                output = IProcessUtil.execCommands(command);
                // commandsInfos.put(command, output);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return commandsInfos;
    }

}
