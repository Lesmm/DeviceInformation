package com.deviceinfo.info;

import android.content.Context;

import com.deviceinfo.InfoJsonHelper;

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
        Properties systemProperties = (Properties) IReflectUtil.getFieldValue(java.lang.System.class, "systemProperties");
        if (systemProperties == null) {
            systemProperties = (Properties) IReflectUtil.getFieldValue(java.lang.System.class, "props");
        }

        Properties unchangeableSystemProperties = (Properties) IReflectUtil.getFieldValue(java.lang.System.class, "unchangeableSystemProperties");
        if (unchangeableSystemProperties == null) {
            unchangeableSystemProperties = (Properties) IReflectUtil.getFieldValue(java.lang.System.class, "unchangeableProps");
        }

        // OK 把取信息出来

        JSONObject propsInfo = new JSONObject();

        // 像一加的系统，完全不给拿回来，又没有源码
        if (systemProperties == null) {
            systemProperties = p;
        }
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
        InfoJsonHelper.mergeJSONObject(info, propsInfo);
        InfoJsonHelper.mergeJSONObject(info, unchangeablePropsInfo);

        JSONObject weMustToNeedInfo = getTheValueWeMustToNeed(info);
        InfoJsonHelper.mergeJSONObject(info, weMustToNeedInfo);

        return info;
    }

    public static JSONObject getTheValueWeMustToNeed(JSONObject info) {
        JSONObject must2HaveInfo = new JSONObject();

        String[] arrayKeys = new String[]{"java.version ", "java.vendor", "java.vendor.url", "java.home", "java.class.version", "java.class.path",
                "http.agent", "java.runtime.name", "java.runtime.version", "java.vm.name", "java.vm.vendor", "java.vm.vendor.url",
                "java.vm.specification.name", "java.vm.specification.vendor", "java.vm.specification.version",
                "os.name", "os.arch", "os.version",
                "file.separator", "path.separator", "line.separator",
                "user.name", "user.home", "user.dir", "user.language", "user.region", "user.locale"};

        for (int i = 0; i < arrayKeys.length; i++) {
            try {
                String key = arrayKeys[i];
                if (!info.has(key)) {
                    Object value = System.getProperty(key);
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
}
