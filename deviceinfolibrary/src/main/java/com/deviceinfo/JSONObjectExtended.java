package com.deviceinfo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.modules.util.IReflectUtil;

public class JSONObjectExtended extends JSONObject {

    private static String __Class_Name__ = JSONObjectExtended.class.getSimpleName();

    public int superClassDepth = 3; // for element is Object parse by __objectToJson__
    public int recursiveDepth = 2;  // for element is Object parse by __objectToJson__

    public JSONObjectExtended() {
        super();
    }

    public JSONObjectExtended(Map copyFrom) {
        super();
        copyFrom(copyFrom);
    }

    public JSONObjectExtended(Map copyFrom, int superClassDepth, int recursiveDepth) {
        super();
        this.superClassDepth = superClassDepth;
        this.recursiveDepth = recursiveDepth;
        copyFrom(copyFrom);
    }

    public void copyFrom(Map copyFrom) {
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
            Object value = __wrap__(entry.getValue());
            if (value != null) {
                nameValuePairs.put(key, value);
            }
        }
    }

    public Object __wrap__(Object o) {
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
                return new JSONArrayExtended((Collection) o);
            } else if (o.getClass().isArray()) {
                return new JSONArrayExtended(o);
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
            return __objectToJson__(o);
        } catch (Exception ignored) {
            Log.d(__Class_Name__, "ignored -------------->>>");
            ignored.printStackTrace();
            Log.d(__Class_Name__, "ignored --------------<<<");
        }
        return null;
    }


    /* Extend Methods */
    public JSONObject __objectToJson__(Object object) {
        return __method_objectToJson__(object, superClassDepth, recursiveDepth);
    }

    public JSONObject __method_objectToJson__(Object object, int superClassDepth, int recursiveDepth) {
        Map<?, ?> result = objectFieldNameValues(superClassDepth, object);

        if (result == null) {
            return null;
        }

        int recursivelyDeep = 0;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            StackTraceElement ele = elements[i];
            String traceDescription = ele.toString();
            if (traceDescription.contains(__Class_Name__ + "." + "__method_objectToJson__")) {
                recursivelyDeep++;
            }
        }
        if (recursivelyDeep >= recursiveDepth) {
            return new JSONObject(result);
        } else {
            return new JSONObjectExtended(result, superClassDepth, recursiveDepth);
        }
    }

    private static Map<?, ?> objectFieldNameValues(int superClassDepth, Object obj) {
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        // 处理Bundle, 如 Telephony 的 getCellLocation 方法
        if (clazz == Bundle.class /*|| BaseBundle.class.isAssignableFrom(clazz)*/) {
            IReflectUtil.invokeMethod(obj, "unparcel", new Class[]{}, new Object[]{});
            Map<?, ?> mMap = (Map<?, ?>) IReflectUtil.getFieldValue(obj, "mMap");  // ArrayMap<String, Object>
            return mMap;
        }

        //  处理一些JSON无法表达的类
        if (
                clazz == Bitmap.class || clazz == Color.class ||
                        clazz == BitmapDrawable.class ||
                        clazz == ColorDrawable.class || clazz == Drawable.class
        ) {
            return null;
        }

        String clazzName = clazz.getName();
        if (clazzName.equals("android.graphics.drawable.Icon")) {       // API < 23 没有 Icon.class 类
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

                    boolean isStatic = Modifier.isStatic(field.getModifiers());
                    boolean isFinal = Modifier.isFinal(field.getModifiers());

                    // Class<?> type = field.getType();
                    if (isStatic && isFinal) {
                        // TODO ... if fieldName characters are all uppercase ???
                        // TODO ... but Build.class also has static final ..., So Build.class not permitted use JSONObjectExtended
                        continue;
                    }

                    if (fieldName.equals("shadow$_monitor_") || fieldName.equals("shadow$_klass_") || fieldName.equals("CREATOR") || fieldName.equals("mRemote")) {
                        continue;
                    }

                    Object fieldValue = field.get(isStatic ? clazz : obj);

                    // for WifiSsid in getConnectionInfo() and getScanResults() .... 其实这里不用把 ByteArrayOutputStream 转成 String 也行，Hook 那边已经支持了count&buf的解析重构
                    if (fieldValue != null && fieldValue.getClass() == ByteArrayOutputStream.class) {
                        ByteArrayOutputStream byteArray = (ByteArrayOutputStream) fieldValue;
                        if (byteArray.size() <= 512) {
                            fieldValue = byteArray.toString();
                        } else {
                            fieldValue = null;
                        }
                    }

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
