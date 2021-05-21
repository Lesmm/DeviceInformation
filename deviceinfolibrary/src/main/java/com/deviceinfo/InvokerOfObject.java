package com.deviceinfo;

import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class InvokerOfObject {

    public static interface InvokeHandler {
        Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes, Class<?> returnType, Map<String, Object> resultMap) throws Exception;
    }

    public static Map<?, ?> invokeObjectMethods(Object obj, InvokeHandler handler) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (obj == null) {
            return result;
        }

        boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {

            Method method = methods[i];
            String methodName = method.getName();

            try {

                method.setAccessible(true);

                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> returnType = method.getReturnType();

                Object value = handler.handle(obj, clazz, method, methodName, parameterTypes, returnType, result);
                if (value != null) {
                    result.put(methodName, value);
                }

            } catch (Exception e) {
                // 若无权限调用的方法，我们不会把 key or null 放到 result 中
                e.printStackTrace();
                Log.d("DeviceInfo", "调service方法失败: " + methodName + " -> " + e.toString());
            }
        }
        return result;
    }


    public static Map<?, ?> invokeObjectMethodsWithGetPrefixZeroArgs(Object obj) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (obj == null) {
            return result;
        }

        boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {

            Method method = methods[i];
            String methodName = method.getName();

            try {

                method.setAccessible(true);

                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> returnType = method.getReturnType();

                if (returnType != void.class && parameterTypes.length == 0 && methodName.startsWith("get")) {
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj);
                    if (value != null) {
                        result.put(methodName, value);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("DeviceInfo", "调Get方法失败: " + methodName + " -> " + e.toString());
            }
        }
        return result;
    }
}
