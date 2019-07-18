package com.deviceinfo.info;

import com.deviceinfo.ManagerInfoHelper;

import org.json.JSONObject;

import common.modules.util.IFileUtil;
import common.modules.util.IProcessUtil;

public class HardwareInfo {

    public static JSONObject getInfoInFiles() {

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
            key = "/proc/net/if_inet6";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // wlan0
            key = "/sys/class/net/wlan0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/fb000000.qcom,wcnss-wlan/net/wlan0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // p2p0 这个的 address 倒和上面的 MAC 地址是一样的
            key = "/sys/class/net/p2p0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/fb000000.qcom,wcnss-wlan/net/p2p0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // dummy0
            key = "/sys/class/net/dummy0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/virtual/net/dummy0/address";
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


    public static JSONObject getInfoInCommands() {

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
