package com.deviceinfo.info;

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

            // MAC
            key = "/proc/net/if_inet6";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/class/net/wlan0/address";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/fb000000.qcom,wcnss-wlan/net/wlan0/address";
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

            // TODO ... Hook 那边 根据下面命令的结果 来处理 getprop 带上具体一个Key的情况
            command = "getprop";
            output = IProcessUtil.execCommands(command);
            commandsInfos.put(command, output);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return commandsInfos;

    }

}
