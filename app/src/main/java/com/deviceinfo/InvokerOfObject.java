package com.deviceinfo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InvokerOfObject {

    public static interface InvokeHandler {
        Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes, Class<?> returnType) throws Exception;
    }

    public static Map<?, ?> invokeObjectMethods(Object obj, InvokeHandler handler) {
        Map<String, Object> result = new HashMap<String, Object>();

        boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            method.setAccessible(true);
            String methodName = method.getName();

            try {

                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> returnType = method.getReturnType();

                Object value = handler.handle(obj, clazz, method, methodName, parameterTypes, returnType);
                if (value != null) {
                    result.put(methodName, value);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
