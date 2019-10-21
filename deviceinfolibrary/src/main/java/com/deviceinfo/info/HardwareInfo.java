package com.deviceinfo.info;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.JSONArrayExtended;
import com.deviceinfo.JSONObjectExtended;
import com.deviceinfo.ManagerInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.modules.util.IFileUtil;
import common.modules.util.IProcessUtil;

public class HardwareInfo {

    public static JSONObject getInfoInFiles(Context mContext) {

        if (ManagerInfo._IS_DEBUG_) {
            try {
                ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

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

                Log.d("DeviceInfo", "_set_debug_here_");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JSONObject filesInfos = new JSONObject();

        try {

            // --------------- cpu, meminfo ... ---------------
            String key = "/proc/cpuinfo";                   // 这个输出比较特别，用 cat 与 BufferedReader 读出的同容不一样。 cmake or ndk 加上 NEON 支持。
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

            key = "/proc/tty/drivers";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/fs/selinux/enforce";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/class/android_usb/android0/state";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/selinux_version";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);


            // CPU 核数，注意，要逆向这会不会影响到被 Hooked 的 APP 的运行。
            key = "/sys/devices/system/cpu/possible";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            key = "/sys/devices/system/cpu/present";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);


            // --------------- MAC & /sys/class/net ---------------
            key = "/proc/net/if_inet6";             // 组成规则看 NetworkInterface.java 的方法 collectIpv6Addresses
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

            // wlan0
            String defaultLinkDirPath = "/sys/devices/fb000000.qcom,wcnss-wlan/net";
            readInterfaceWrapper(filesInfos, "wlan0", defaultLinkDirPath);

            // p2p0
            readInterfaceWrapper(filesInfos, "p2p0", defaultLinkDirPath);  // 大多 p2p0 的 address 和 wlan0 的 address 是一样的

            // dummy0
            defaultLinkDirPath = "/sys/devices/virtual/net/";
            readInterfaceWrapper(filesInfos, "dummy0", defaultLinkDirPath);

            // lo
            readInterfaceWrapper(filesInfos, "lo", defaultLinkDirPath);

            // others
            List<String> ifNames = getAllInterfacesName();
            for (int i = 0; ifNames != null && i < ifNames.size(); i++) {
                String name = ifNames.get(i);
                if (name.equals("wlan0") || name.equals("p2p0") || name.equals("dummy0") || name.equals("lo")) {
                    continue;
                }
                readInterfaceWrapper(filesInfos, name, null);
            }


            // --------------- SDCard cid & csd ---------------
            // https://www.cameramemoryspeed.com/sd-memory-card-faq/reading-sd-card-cid-serial-psn-internal-numbers/
            // https://www.kernel.org/doc/Documentation/mmc/mmc-dev-attrs.txt
            // https://www.bunniestudios.com/blog/?page_id=1022

            // https://richard.burtons.org/2016/07/01/changing-the-cid-on-an-sd-card/
            // https://richard.burtons.org/2016/07/31/cid-change-on-sd-card-update-evoplus_cid/
            // https://github.com/beaups/SamsungCID & https://github.com/raburton/evoplus_cid

            // 11 0100 303136474532 00 e4210943 4200    // 就是 serial 不同了
            // 11 0100 303136474532 00 601b2935 4200
            // mid + oemid + name(016GE2)'s ASCII + 00(PRV) + serial + 4200

            readFileWithSoftLink(filesInfos, "/sys/block/mmcblk0", "/sys/devices/msm_sdcc.1/mmc_host/mmc0/mmc0:0001/block/mmcblk0", "", "/device/cid");
            readFileWithSoftLink(filesInfos, "/sys/class/mmc_host/mmc0", "devices/msm_sdcc.1/mmc_host/mmc0", "", "/mmc0:0001/cid");

            readFileWithSoftLink(filesInfos, "/sys/block/mmcblk0", "/sys/devices/msm_sdcc.1/mmc_host/mmc0/mmc0:0001/block/mmcblk0", "", "/device/type");
            readFileWithSoftLink(filesInfos, "/sys/class/mmc_host/mmc0", "devices/msm_sdcc.1/mmc_host/mmc0", "", "/mmc0:0001/type");

            readFileWithSoftLink(filesInfos, "/sys/block/mmcblk0", "/sys/devices/msm_sdcc.1/mmc_host/mmc0/mmc0:0001/block/mmcblk0", "", "/device/name");
            readFileWithSoftLink(filesInfos, "/sys/class/mmc_host/mmc0", "devices/msm_sdcc.1/mmc_host/mmc0", "", "/mmc0:0001/name");

            readFileWithSoftLink(filesInfos, "/sys/block/mmcblk0", "/sys/devices/msm_sdcc.1/mmc_host/mmc0/mmc0:0001/block/mmcblk0", "", "/device/csd");
            readFileWithSoftLink(filesInfos, "/sys/class/mmc_host/mmc0", "devices/msm_sdcc.1/mmc_host/mmc0", "", "/mmc0:0001/csd");

            // TODO ... Hook 那边, /sys/class/net 下的其余接口如 rev_rmnet0 等等的址，跟 ifconfig -a 的输出 HWaddr 值是一致的，可以根据此命令的Json内容来处理

            // --------------- default.prop ---------------
            // TODO ... Hook 那边， /system/build.prop 放在 Hook 那边拿所有的 SystemProperties 来 处理
            // /default.prop 则在这里也带上一份
            key = "/default.prop";
            info = IFileUtil.readFileToText(key);
            filesInfos.put(key, info);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return filesInfos;
    }


    public static JSONObject getInfoInCommands(Context mContext) {

        // TODO ... Hook 那边处理 dumpsys meminfo 里的字符串，把所CM的东西给替换了。如果加上PID: dumpsys meminfo [pid] 这个倒没什么。

        JSONObject commandsInfos = new JSONObject();

        try {

            // ifconfig -a
            // TODO ... Hook 那边 根据下面命令的结果 来处理 ifconfig 带上别的选项的返回情况
            String command = "ifconfig -a";
            String output = IProcessUtil.execCommands(command);
            if (output.contains("No such device")) {
                command = "ifconfig";
                output = IProcessUtil.execCommands(command);
            }
            commandsInfos.put(command, output);


            // uname -a
            // TODO ... Hook 那边 uname -a 的值里有跟 java.lang.System.java 的 "os.arch", "os.name", "os.version" 对应
            command = "uname -a";
            output = IProcessUtil.execCommands(command);
            commandsInfos.put(command, output);

            // env 命令， 相当于 java.lang.System.getenv();
            command = "env";
            output = IProcessUtil.execCommands(command);
            commandsInfos.put(command, output);


            // TODO ... Hook 那边 拿所有 SystemPropertiesInfo 的Keys-Values整成[key]: [value]格式来返回当APP 执行 getprop 时， getprop 的内容不在这里带上了。
            // TODO ... Hook 那边 根据下面命令的结果 来处理 getprop 带上具体一个Key的情况
            if (ManagerInfo._IS_DEBUG_) {
                command = "getprop";
                output = IProcessUtil.execCommands(command);
                // commandsInfos.put(command, output);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return commandsInfos;
    }

    private static void readInterfaceWrapper(JSONObject filesInfos, String ifName, String defaultLinkDirPath) {
        boolean isAdded = readFileWithSoftLink(filesInfos, "/sys/class/net/", defaultLinkDirPath, ifName, "/address");
        if (!isAdded) {
            return;
        }
        readFileWithSoftLink(filesInfos, "/sys/class/net/", defaultLinkDirPath, ifName, "/ifindex");
    }

    private static boolean readFileWithSoftLink(JSONObject filesInfos, String dirPath, String defaultLinkDirPath, String ifName, String fileName) {
        try {

            // 处理一下主要的
            String mainDirPath = removeRedundantSlash(dirPath + ifName);
            String mainFullPath = removeRedundantSlash(mainDirPath + "/" + fileName);

            String info = IFileUtil.readFileToText(mainFullPath);
            if (info == null || info.trim().isEmpty()) {
                return false;
            }
            filesInfos.put(mainFullPath, info);

            // 处理一下Link
            String dirlink = readLink(mainDirPath);
            // 如果是两都是空，就处理Link了
            if ((defaultLinkDirPath != null && !defaultLinkDirPath.isEmpty()) || (dirlink != null && !dirlink.isEmpty())) {
                String defaultLinkKey = removeRedundantSlash(defaultLinkDirPath + "/" + ifName);

                String link = (dirlink == null || dirlink.isEmpty()) ? defaultLinkKey : dirlink;
                String linkFullPath = removeRedundantSlash(link + fileName);

                String linkInfo = IFileUtil.readFileToText(linkFullPath);
                if (linkInfo != null && !linkInfo.trim().isEmpty()) {
                    filesInfos.put(linkFullPath, linkInfo);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private static String removeRedundantSlash(String path) {
        String purePath = new File(path).getAbsolutePath();
        return purePath;
    }

    private static String readLink(String path) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                String readlink = android.system.Os.readlink(path);
                if (readlink.startsWith("/")) {
                    return readlink;
                } else if (readlink.startsWith("../")) {
                    String replacedPath = new File(path).getParent();
                    String replaceReadLink = readlink;
                    while (replaceReadLink.startsWith("../")) {
                        replacedPath = new File(replacedPath).getParent();
                        replaceReadLink = replaceReadLink.substring(3);
                    }
                    return replacedPath + "/" + replaceReadLink;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private static List<String> getAllInterfacesName() {
        List<NetworkInterface> allInterfaces = null;
        try {
            allInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> names = new ArrayList<>();
        for (int i = 0; allInterfaces != null && i < allInterfaces.size(); i++) {
            NetworkInterface netInterface = allInterfaces.get(i);
            String ifName = netInterface.getName();
            names.add(ifName);
        }

        return names;
    }

}
