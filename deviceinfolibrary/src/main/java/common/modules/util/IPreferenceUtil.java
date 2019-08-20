package common.modules.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.ManagerInfo;

public class IPreferenceUtil {

    private static Context mContext = null;

    public static void setContext(Context context) {
        mContext = context;
    }

    public static Context getContext() {
        if (mContext == null) {
            mContext = ManagerInfo.getApplication();
        }
        return mContext;
    }

    public static void setSharedPreferences(String key, Object value) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }
        editor.commit();
    }

    public static SharedPreferences getSharedPreferences() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences;
    }

}
