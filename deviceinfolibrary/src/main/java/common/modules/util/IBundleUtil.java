package common.modules.util;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class IBundleUtil {

    public static Bundle createBundleFromJSON(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return createBundleFromJSON(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bundle createBundleFromJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        @SuppressWarnings("unchecked")
        Map<String, Object> mMap = (Map<String, Object>) IReflectUtil.getFieldValue(bundle, "mMap");

        Iterator<?> iterator = jsonObject.keys();

        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            Object value = jsonObject.opt(key);
//			intent.putExtra(key, obj);	// have to cast the Type ..., so we use the map ...
            mMap.put(key, value);
        }

        return bundle;
    }

    public static JSONObject createJSONFromBundle(Bundle bundle) {
        JSONObject json = new JSONObject();
        try {
            Map<String, Object> mMap = IBundleUtil.getMapFromBundle(bundle);
            // mMap maybe null
            if (mMap != null) {
                json = new JSONObject(mMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static Map getMapFromBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> mMap = (Map<String, Object>) IReflectUtil.getFieldValue(bundle, "mMap");
        if (mMap == null) {
            IReflectUtil.invokeMethod(bundle, "unparcel", null, null);
            mMap = (Map<String, Object>) IReflectUtil.getFieldValue(bundle, "mMap");
        }
        return mMap;
    }

    public static JSONObject createJSONFromIntent(Intent intent) {
        JSONObject json = new JSONObject();
        try {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();

            json = IBundleUtil.createJSONFromBundle(bundle);
            json.put("__ACTION__", action);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static void logIntentAsJsonString(Intent intent) {
        Log.d("HLog-Intent", IBundleUtil.createJSONFromIntent(intent).toString());
    }

    public static void logBundleAsJsonString(Bundle bundle) {
        Log.d("HLog-Bundle", IBundleUtil.createJSONFromBundle(bundle).toString());
    }

}
