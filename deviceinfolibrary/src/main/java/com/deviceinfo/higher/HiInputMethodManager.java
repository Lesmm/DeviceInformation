package com.deviceinfo.higher;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.deviceinfo.JSONObjectExtended;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.modules.util.IJSONObjectUtil;

public class HiInputMethodManager extends HiBase {

    @Override
    public JSONObject getInfo(Context mContext) {
        try {
            Map<String, Object> map = __getInfo__(mContext);
            return new JSONObjectExtended(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public Map<String, Object> __getInfo__(Context mContext) {
        InputMethodManager manager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager == null) {
            return null;
        }
        PackageManager pm = mContext.getPackageManager();

        List<InputMethodInfo> methodList = manager.getInputMethodList();
        JSONArray array = new JSONArray();
        for (int i = 0; methodList != null && i < methodList.size(); i++) {
            JSONObject json = new JSONObject();
            array.put(json);

            InputMethodInfo im = methodList.get(i);

            if (pm != null) {
                CharSequence charSequence = im.loadLabel(pm);
                IJSONObjectUtil.putJSONObject(json, "appName", charSequence);
            }

            IJSONObjectUtil.putJSONObject(json, "mId", im.getId());
            IJSONObjectUtil.putJSONObject(json, "mSettingsActivityName", im.getSettingsActivity());

            ServiceInfo serviceInfo = im.getServiceInfo();
            JSONObject serviceInfoJson = new JSONObjectExtended().__objectToJson__(serviceInfo);
            String[] keysWeNeed = new String[]{"processName", "name", "packageName", "applicationInfo"};
            JSONObject jsonWeNeed = IJSONObjectUtil.getJSONWithKeyContains(serviceInfoJson, keysWeNeed);

            JSONObject mServiceJson = new JSONObject();
            IJSONObjectUtil.putJSONObject(json, "mService", mServiceJson);
            IJSONObjectUtil.putJSONObject(mServiceJson, "serviceInfo", jsonWeNeed);
        }

        final Map<String, Object> map = new HashMap<>();
        __put_2_map__(map, array, "getInputMethodList");

        return map;
    }

    @Override
    protected JSONObject keysMappings() {
        return null;
    }

}
