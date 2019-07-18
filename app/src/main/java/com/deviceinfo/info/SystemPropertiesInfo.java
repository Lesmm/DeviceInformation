package com.deviceinfo.info;

import android.content.Context;
import android.util.Log;

import com.deviceinfo.ManagerInfoHelper;

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

        if (ManagerInfoHelper.IS_DEBUG) {
            // Diff对比了一下上面这几个JSON，commandGetpropPropertiesInfo 与 buildPropertiesInfo 确实大部分互有包含。但也有各自都没有的Key-Value
            JSONObject summaryJson = new JSONObject();
            ManagerInfoHelper.mergeJSONObject(summaryJson, telephonyPropertiesInfo);
            JSONObject jsonObject1 = ManagerInfoHelper.checkJSONObjectDuplicateKeysValues(summaryJson, buildPropertiesInfo);
            ManagerInfoHelper.mergeJSONObject(summaryJson, buildPropertiesInfo);
            JSONObject jsonObject2 = ManagerInfoHelper.checkJSONObjectDuplicateKeysValues(summaryJson, defaultPropertiesInfo);
            ManagerInfoHelper.mergeJSONObject(summaryJson, defaultPropertiesInfo);
            JSONObject jsonObject3 = ManagerInfoHelper.checkJSONObjectDuplicateKeysValues(summaryJson, commandGetpropPropertiesInfo);
            ManagerInfoHelper.mergeJSONObject(summaryJson, commandGetpropPropertiesInfo);
            Log.d("DeviceInfo","_set_debug_here_");
        }

        return null;
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
        JSONObject buildPropertiesInfo = new JSONObject();
        try {
            // /system/build.prop
            Properties properties = new Properties();
            properties.load(new BufferedReader(new FileReader("/system/build.prop")));

            Enumeration enu = properties.propertyNames();
            while (enu.hasMoreElements()) {
                String key = (String) enu.nextElement();
                Object propertyVal = properties.get(key);
                buildPropertiesInfo.put(key, propertyVal);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildPropertiesInfo;
    }

    public static JSONObject getDefaultPropertiesInfo() {
        JSONObject buildPropertiesInfo = new JSONObject();
        try {
            // /default.prop
            Properties properties = new Properties();
            properties.load(new BufferedReader(new FileReader("/default.prop")));

            Enumeration enu = properties.propertyNames();
            while (enu.hasMoreElements()) {
                String key = (String) enu.nextElement();
                Object propertyVal = properties.get(key);
                buildPropertiesInfo.put(key, propertyVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildPropertiesInfo;
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


    private static Object get(String key) {
        // Hook 那边会处理 getXXX 的返回值类型问题的了。这里全返回String也没关系。
        if (key.endsWith("numeric")) {
            return ISystemPropertiesUtil.getInt(key, 0);
        } else {
            return ISystemPropertiesUtil.get(key);
        }
    }

}
