package com.deviceinfo.info;

import android.content.ContentResolver;
import android.content.Context;
import android.os.UserHandle;

import org.json.JSONObject;

import java.lang.reflect.Method;

import common.modules.util.IJSONObjectUtil;
import common.modules.util.IReflectUtil;

public class SettingsInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject info = new JSONObject();

        // Settings
        JSONObject settingsSystemInfo = getSettings(mContext, android.provider.Settings.System.class);
        JSONObject settingsGlobalInfo = getSettings(mContext, android.provider.Settings.Global.class);
        JSONObject settingsSecureInfo = getSettings(mContext, android.provider.Settings.Secure.class);

        try {
            info.put("System", settingsSystemInfo);
            info.put("Global", settingsGlobalInfo);
            info.put("Secure", settingsSecureInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;

    }


    public static JSONObject getSettings(Context mContext, final Class subSettingClazz) {
        final JSONObject subSettingsInfoJson = new JSONObject();

        try {
            // parameter 1
            final ContentResolver resolver = mContext.getContentResolver();

            // parameter 3
            // int myUserId = UserHandle.myUserId();
            Method myUserIdMethod = UserHandle.class.getDeclaredMethod("myUserId", new Class[]{});
            myUserIdMethod.setAccessible(true);
            final Integer myUserId = (Integer) myUserIdMethod.invoke(UserHandle.class, new Object[]{});

            final Method getStringForUserMethod = subSettingClazz.getDeclaredMethod("getStringForUser", new Class[]{ContentResolver.class, String.class, int.class});
            getStringForUserMethod.setAccessible(true);

            JSONObject fieldNamesValues = new JSONObject(IReflectUtil.objectFieldNameValues(subSettingClazz));
            IJSONObjectUtil.iterateJSONObject(fieldNamesValues, new IJSONObjectUtil.IterateHandler() {
                @Override
                public void iterateAction(String key, Object value) {
                    if (value instanceof String) {
                        // parameter 2
                        final String settingKey = (String) value;
                        try {
                            String settingValue = (String) getStringForUserMethod.invoke(subSettingClazz, new Object[]{resolver, settingKey, myUserId});
                            subSettingsInfoJson.put(settingKey, settingValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subSettingsInfoJson;
    }

}
