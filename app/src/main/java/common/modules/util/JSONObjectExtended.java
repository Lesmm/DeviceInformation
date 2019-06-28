package common.modules.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JSONObjectExtended extends JSONObject {

    public JSONObjectExtended(Map copyFrom) {
        super();

        Map<String, Object> nameValuePairs = (Map<String, Object>) IReflectUtil.getFieldValue(this, "nameValuePairs");

        Map<?, ?> contentsTyped = (Map<?, ?>) copyFrom;
        for (Map.Entry<?, ?> entry : contentsTyped.entrySet()) {
            /*
             * Deviate from the original by checking that keys are non-null and
             * of the proper type. (We still defer validating the values).
             */
            String key = (String) entry.getKey();
            if (key == null) {
                throw new NullPointerException("key == null");
            }
            Object value = wrap(entry.getValue());
            if (value != null) {
                nameValuePairs.put(key, value);
            }
        }
    }

    public static Object wrap(Object o) {
        if (o == null) {
            return NULL;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        if (o.equals(NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JSONArrayEx((Collection) o);
            } else if (o.getClass().isArray()) {
                return new JSONArrayEx(o);
            }
            if (o instanceof Map) {
                return new JSONObjectExtended((Map) o);
            }
            if (o instanceof Boolean ||
                    o instanceof Byte ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    o instanceof String) {
                return o;
            }
            if (o.getClass().isEnum()) {
                return o.toString();
            }
            return objectToJson(o);
        } catch (Exception ignored) {

        }
        return null;
    }


    /* Extend Methods */
    private static JSONObject objectToJson(Object object) {
        Map<?, ?> result = objectFieldNameValues(3, object);

        if (result == null) {
            return null;
        }

        int recursiveDepth = 0;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            StackTraceElement ele = elements[i];
            String traceDescription = ele.toString();
            if (traceDescription.contains("JSONObjectExtended.objectToJson")){
                recursiveDepth++;
            }
        }
        if (recursiveDepth >= 2) {
            return new JSONObject(result);
        } else {
            return new JSONObjectExtended(result);
        }
    }

    private static Map<?, ?> objectFieldNameValues(int superClassDepth, Object obj) {
        Boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();

        if (
                clazz == Bitmap.class || clazz == Color.class ||
                        clazz == BitmapDrawable.class ||  clazz == ColorDrawable.class ||
                        clazz == Drawable.class  || clazz == Icon.class
        ) {
            return null;
        }

        Map<String, Object> result = new HashMap<String, Object>();

        do {

            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                try {
                    Field field = fields[i];
                    field.setAccessible(true);
                    String fieldName = field.getName();

                    if (fieldName.equals("CREATOR")) {
                        continue;
                    }

                    Object fieldValue = field.get(Modifier.isStatic(field.getModifiers()) ? clazz : obj);
                    if (fieldValue != null) {
                        result.put(fieldName, fieldValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // get super class fields
            superClassDepth--;
            clazz = clazz.getSuperclass();

        } while (superClassDepth >= 0 && clazz != null && clazz != java.lang.Object.class); // java.lang.Object has properties: shadow$_monitor_ & shadow$_klass_

        if (result.size() == 0) {
            return null;
        }

        return result;
    }

}
