package com.deviceinfo.higher;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.facade.Manager;

import org.json.JSONObject;

import java.util.Map;

public abstract class HiBase {

    protected abstract JSONObject getInfo(Context mContext);

    public void __put_2_map__(Map<String, Object> map, Object value, String highMethodName) {
        JSONObject mappings = keysMappings();
        String realApiName = mappings != null ? mappings.optString(highMethodName, highMethodName) : highMethodName;
        map.put(realApiName, value);
    }

    protected abstract JSONObject keysMappings();


    /**
     * Static Methods
     */
    // i.e. Manifest.permission.ACCESS_COARSE_LOCATION
    public static boolean checkPermission(String permission) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Manager.getApplication().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean checkPermission(String permission, Runnable runnable) {
        if (checkPermission(permission)) {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

}
