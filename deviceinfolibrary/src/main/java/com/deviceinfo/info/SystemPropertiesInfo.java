package com.deviceinfo.info;

import android.content.Context;
import android.util.Log;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.ManagerInfo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import common.modules.util.IProcessUtil;
import common.modules.util.IReflectUtil;
import common.modules.util.ISystemPropertiesUtil;

public class SystemPropertiesInfo {

    public static JSONObject getInfo(final Context mContext) {

        JSONObject buildPropertiesInfo = getBuildPropertiesInfo();
        JSONObject defaultPropertiesInfo = getDefaultPropertiesInfo();
        JSONObject commandGetpropPropertiesInfo = getCommandGetpropPropertiesInfo();
        JSONObject telephonyPropertiesInfo = getTelephonyPropertiesInfo();

        if (ManagerInfo._IS_DEBUG_) {
            // Diff对比了一下上面这几个JSON，commandGetpropPropertiesInfo 与 buildPropertiesInfo 确实大部分互有包含。但也有各自都没有的Key-Value
            JSONObject summaryJson = new JSONObject();
            InfoJsonHelper.mergeJSONObject(summaryJson, telephonyPropertiesInfo);
            JSONObject jsonObject1 = InfoJsonHelper.checkJSONObjectDuplicateKeysValues(summaryJson, buildPropertiesInfo);
            InfoJsonHelper.mergeJSONObject(summaryJson, buildPropertiesInfo);
            JSONObject jsonObject2 = InfoJsonHelper.checkJSONObjectDuplicateKeysValues(summaryJson, defaultPropertiesInfo);
            InfoJsonHelper.mergeJSONObject(summaryJson, defaultPropertiesInfo);
            JSONObject jsonObject3 = InfoJsonHelper.checkJSONObjectDuplicateKeysValues(summaryJson, commandGetpropPropertiesInfo);
            InfoJsonHelper.mergeJSONObject(summaryJson, commandGetpropPropertiesInfo);
            Log.d("DeviceInfo", "_set_debug_here_");
        }

        // TODO ... Hook 那边把 所有 Keys-Values 整成 [key]: [value] 格式来返回当APP 执行 getprop 时, 因为它比较大，所以不弄到 Commands.Contents 了
        // TODO ... Hook 那边把 所有 Keys-Values 转成 Properties 文件格式写到 /system/build.prop 去, 因为它比较大，所以不弄到 Files.Contents 了
        // TODO ... Hook 那边, 至于 /default.prop ，我们同样带上一份到 Files.Contents 里去，因为它比较小, 但有些系统的APP是没有读 /default.prop 权限的
        JSONObject info = new JSONObject();
        InfoJsonHelper.mergeJSONObject(info, buildPropertiesInfo);
        InfoJsonHelper.mergeJSONObject(info, defaultPropertiesInfo);
        InfoJsonHelper.mergeJSONObject(info, commandGetpropPropertiesInfo);
        InfoJsonHelper.mergeJSONObject(info, telephonyPropertiesInfo);

        JSONObject weMustToNeedInfo = getTheValueWeMustToNeed(info);
        InfoJsonHelper.mergeJSONObject(info, weMustToNeedInfo);


        return info;
    }


    public static JSONObject getTelephonyPropertiesInfo() {
        JSONObject result = new JSONObject();

        try {
            Class TelephonyProperties = Class.forName("com.android.internal.telephony.TelephonyProperties");
            Map<?, ?> fieldNamesValues = IReflectUtil.objectFieldNameValues(TelephonyProperties);
            JSONObject jsonObject = new JSONObject(fieldNamesValues);

            java.util.Iterator iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                try {
                    String fieldName = (String) iterator.next();
                    Object fieldValue = jsonObject.get(fieldName);
                    if (fieldValue instanceof String) {
                        String key = (String) fieldValue;
                        Object propertyVal = getValue(key);
                        result.put(key, propertyVal);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject getBuildPropertiesInfo() {
        return getPropertiesInfoFromPropertiesFile("/system/build.prop");
    }

    public static JSONObject getDefaultPropertiesInfo() {
        return getPropertiesInfoFromPropertiesFile("/default.prop");
    }

    public static JSONObject getCommandGetpropPropertiesInfo() {
        JSONObject buildPropertiesInfo = new JSONObject();

        String command = "getprop";
        String output = IProcessUtil.execCommands(command);
        try {
            BufferedReader bufferedReader = new BufferedReader(new StringReader(output));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                try {

                    // 注意下面两种情况
                    // [ro.bootimage.build.date]: [2019年 06月 09日 星期日 23:31:09 CST]
                    // [ro.bootimage.build.fingerprint]: [Xiaomi/mk_cancro/cancro:8.1.0/OPM7.181205.001/46f57c6500:userdebug/test-keys]

                    String[] keyValue = line.split("\\]: \\[");
                    if (keyValue.length != 2) {
                        keyValue = line.split("\\]:");
                    }
                    if (keyValue.length != 2) {
                        keyValue = line.split(": ");
                    }
                    if (keyValue.length != 2) {
                        keyValue = line.split(" : ");
                    }
                    if (keyValue.length == 2) {
                        String key = keyValue[0].replace("[", "").replace("]", "").trim();
                        String propertyVal = keyValue[1].replace("[", "").replace("]", "").trim();

                        buildPropertiesInfo.put(key, propertyVal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildPropertiesInfo;
    }

    private static JSONObject getPropertiesInfoFromPropertiesFile(String propertiesFileName) {
        JSONObject propertiesInfo = new JSONObject();
        try {
            // /system/build.prop
            Properties properties = new Properties();
            properties.load(new BufferedReader(new FileReader(propertiesFileName)));

            Enumeration enu = properties.propertyNames();
            while (enu.hasMoreElements()) {
                String key = (String) enu.nextElement();
                Object propertyVal = properties.get(key);
                propertiesInfo.put(key, propertyVal);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return propertiesInfo;
    }

    // 有些系统会没权限读 /system/build.prop & /default.prop， 但一般会有权限执行 getprop 命令
    // 为了防止没有，我们检查及取一遍我们必须要有的 key - value
    public static JSONObject getTheValueWeMustToNeed(JSONObject info) {
        JSONObject must2HaveInfo = new JSONObject();

        String[] arrayKeys = new String[]{
                "persist.sys.timezone", // IMPORTANT!!!!
                "gsm.version.baseband", "gsm.version.ril-impl", "net.bt.name", "net.dns1", "persist.sys.timezone",
                "ro.bootimage.build.date", "ro.bootimage.build.date.utc", "ro.bootimage.build.fingerprint", "ro.build.date", "ro.build.date.utc",
                "ro.build.description", "ro.build.display.id", "ro.build.expect.baseband", "ro.build.fingerprint", "ro.build.flavor", "ro.build.host", "ro.build.id",
                "ro.build.product", "ro.build.version.codename", "ro.build.version.incremental", "ro.build.version.release", "ro.build.version.sdk", "ro.expect.recovery_id",
                "ro.product.board", "ro.product.brand", "ro.product.cpu.abi", "ro.product.cpu.abi2",
                "ro.product.device", "ro.product.manufacturer", "ro.product.model", "ro.product.name", "ro.product.locale",
                "ro.recovery_id",
                "ro.vendor.build.date", "ro.vendor.build.date.utc", "ro.vendor.build.fingerprint", "ro.vendor.product.brand", "ro.vendor.product.device",
                "ro.vendor.product.manufacturer", "ro.vendor.product.model", "ro.vendor.product.name",
                "sys.usb.controller"};

        for (int i = 0; i < arrayKeys.length; i++) {
            try {
                String key = arrayKeys[i];
                if (!info.has(key)) {
                    Object value = getValue(key);
                    if (value != null) {
                        must2HaveInfo.put(key, value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return must2HaveInfo;
    }


    private static Object getValue(String key) {
        // Hook 那边会处理 getXXX 的返回值类型问题的了。这里全返回String也没关系。
        if (key.endsWith("numeric")) {
            return ISystemPropertiesUtil.getInt(key, 0);
        } else {
            return ISystemPropertiesUtil.get(key);
        }
    }

}
