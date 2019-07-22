package com.deviceinfo.info;

import android.content.Context;
import android.util.Log;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.Manager;

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

        JSONObject telephonyPropertiesInfo = getTelephonyPropertiesInfo();
        JSONObject buildPropertiesInfo = getBuildPropertiesInfo();
        JSONObject defaultPropertiesInfo = getDefaultPropertiesInfo();
        JSONObject commandGetpropPropertiesInfo = getCommandGetpropPropertiesInfo();

        if (Manager.IS_DEBUG) {
            // Diff对比了一下上面这几个JSON，commandGetpropPropertiesInfo 与 buildPropertiesInfo 确实大部分互有包含。但也有各自都没有的Key-Value
            JSONObject summaryJson = new JSONObject();
            InfoJsonHelper.mergeJSONObject(summaryJson, telephonyPropertiesInfo);
            JSONObject jsonObject1 = InfoJsonHelper.checkJSONObjectDuplicateKeysValues(summaryJson, buildPropertiesInfo);
            InfoJsonHelper.mergeJSONObject(summaryJson, buildPropertiesInfo);
            JSONObject jsonObject2 = InfoJsonHelper.checkJSONObjectDuplicateKeysValues(summaryJson, defaultPropertiesInfo);
            InfoJsonHelper.mergeJSONObject(summaryJson, defaultPropertiesInfo);
            JSONObject jsonObject3 = InfoJsonHelper.checkJSONObjectDuplicateKeysValues(summaryJson, commandGetpropPropertiesInfo);
            InfoJsonHelper.mergeJSONObject(summaryJson, commandGetpropPropertiesInfo);
            Log.d("DeviceInfo","_set_debug_here_");
        }

        // TODO ... Hook 那边把 所有 Keys-Values 整成 [key]: [value] 格式来返回当APP 执行 getprop 时, 因为它比较大，所以不弄到 Commands.Contents 了
        // TODO ... Hook 那边把 所有 Keys-Values 转成 Properties 文件格式写到 /system/build.prop 去, 因为它比较大，所以不弄到 Files.Contents 了
        // TODO ... Hook 那边, 至于 /default.prop ，我们同样带上一份到 Files.Contents 里去，因为它比较小
        JSONObject info = new JSONObject();
        InfoJsonHelper.mergeJSONObject(info, telephonyPropertiesInfo);
        InfoJsonHelper.mergeJSONObject(info, buildPropertiesInfo);
        InfoJsonHelper.mergeJSONObject(info, defaultPropertiesInfo);
        InfoJsonHelper.mergeJSONObject(info, telephonyPropertiesInfo);

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
                        Object propertyVal = get(key);
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
                String[] keyValue = line.replace("[","").replace("]","").split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String propertyVal = keyValue[1].trim();

                    buildPropertiesInfo.put(key, propertyVal);
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

    private static Object get(String key) {
        // Hook 那边会处理 getXXX 的返回值类型问题的了。这里全返回String也没关系。
        if (key.endsWith("numeric")) {
            return ISystemPropertiesUtil.getInt(key, 0);
        } else {
            return ISystemPropertiesUtil.get(key);
        }
    }

}
