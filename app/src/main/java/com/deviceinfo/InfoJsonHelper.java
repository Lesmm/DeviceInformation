package com.deviceinfo;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class InfoJsonHelper {

    public static final String CLASS_NAME = InfoJsonHelper.class.getSimpleName();

    public static void mergeJSONObject(JSONObject destination, JSONObject source) {
        if (destination == null || source == null) {
            return;
        }
        Iterator<?> iteratorSource = source.keys();
        while (iteratorSource.hasNext()) {
            try {
                String name = (String) iteratorSource.next();
                Object value = source.opt(name);
                if (destination.has(name)) {
                    Object v = destination.opt(name);
                    Log.d(CLASS_NAME, "name has duplicated, check it out: " + name + ", " + v + " -> " + value);
                }
                destination.put(name, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONArray getJSONObjectDuplicateKeys(JSONObject destination, JSONObject source) {
        if (destination == null || source == null) {
            return null;
        }
        JSONArray array = new JSONArray();

        Iterator<?> iteratorSource = source.keys();
        while (iteratorSource.hasNext()) {
            try {
                String name = (String) iteratorSource.next();
                if (destination.has(name)) {
                    array.put(name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    public static JSONObject checkJSONObjectDuplicateKeysValues(JSONObject destination, JSONObject source) {
        if (destination == null || source == null) {
            return null;
        }
        JSONObject json = new JSONObject();

        Iterator<?> iteratorSource = source.keys();
        while (iteratorSource.hasNext()) {
            try {
                String name = (String) iteratorSource.next();
                Object value = source.opt(name);
                if (destination.has(name)) {
                    Object v = destination.opt(name);
                    Boolean isEqual = value.equals(v);
                    if (!isEqual) {
                        Log.d(CLASS_NAME, "name has duplicated, and values are not the same: " + name + ", " + v + " -> " + value);
                    }
                    json.put(name, isEqual);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return json;
    }

}
