package com.deviceinfo;

import android.util.Log;

import org.json.JSONObject;

import java.util.Iterator;

public class ManagerHelper {

    public static final String CLASS_NAME = ManagerHelper.class.getName().substring(ManagerHelper.class.getName().lastIndexOf('.') + 1);

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

}
