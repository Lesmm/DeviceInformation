package com.deviceinfo.info;

import android.content.Context;

import com.deviceinfo.ManagerInfoHelper;

import org.json.JSONObject;

import java.util.Enumeration;
import java.util.Properties;

import common.modules.util.IReflectUtil;

public class SystemInfo {

    public static JSONObject getInfo(Context mContext) {

        // TODO Hook 那边需要做改进。 Android 5.0 后，有个 default (unchangable) 的 Properties 在，不允许随便set了。Hook的方法得用反射来了。
        // Android 5.0 以后 只有这两个可改的了 java.io.tmpdir 和 http.agent

        JSONObject info = new JSONObject();

        Properties p = System.getProperties(); // 调一下此方法，初始化一下

        // Android 4.4 的时候
        // private static Properties systemProperties;

        // Android 5.0 以后，加入了个 unchangeableSystemProperties
        // private static final Properties unchangeableSystemProperties;
        // private static Properties systemProperties;

        // Android 7.0 以后重命名了一下
        // private static Properties props;
        // private static Properties unchangeableProps;


        // 兼容4.4以上所有版本

        Properties systemProperties = (Properties) IReflectUtil.getFieldValue(System.class, "systemProperties");
        if (systemProperties == null) {
            systemProperties = (Properties) IReflectUtil.getFieldValue(System.class, "props");
        }

        Properties unchangeableSystemProperties = (Properties) IReflectUtil.getFieldValue(System.class, "unchangeableSystemProperties");
        if (unchangeableSystemProperties == null) {
            unchangeableSystemProperties = (Properties) IReflectUtil.getFieldValue(System.class, "unchangeableProps");
        }

        // OK 取信息出来

        JSONObject propsInfo = new JSONObject();

        if (systemProperties != null) {
            Enumeration enu = systemProperties.propertyNames();
            while (enu.hasMoreElements()) {
                try {
                    String key = (String) enu.nextElement();
                    Object value = systemProperties.get(key);
                    propsInfo.put(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        JSONObject unchangeablePropsInfo = new JSONObject();

        if (unchangeableSystemProperties != null) {
            Enumeration enu = unchangeableSystemProperties.propertyNames();
            while (enu.hasMoreElements()) {
                try {
                    String key = (String) enu.nextElement();
                    Object value = unchangeableSystemProperties.get(key);
                    unchangeablePropsInfo.put(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 合并

        ManagerInfoHelper.mergeJSONObject(info, propsInfo);
        ManagerInfoHelper.mergeJSONObject(info, unchangeablePropsInfo);

        return info;
    }

}
