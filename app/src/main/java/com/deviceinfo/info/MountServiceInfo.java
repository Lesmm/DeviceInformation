package com.deviceinfo.info;

import android.content.Context;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

// 看了抓回的信息，没什么作用，好多方法没权限调用??? ...
public class MountServiceInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject info = getIMountServiceInfo(mContext);

        return info;
    }


    public static JSONObject getIMountServiceInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.os.storage.IMountService", "mount");

        Map<?, ?> result = InvokerOfObject.invokeObjectMethods(proxy, new InvokerOfObject.InvokeHandler() {
            @Override
            public Object handle(Object obj, Class<?> clazz, Method method, String methodName, Class<?>[] parameterTypes, Class<?> returnType, Map<String, Object> resultMap) throws Exception {

                if (returnType == void.class) {
                    return null;
                }
                if (methodName.equals("asBinder") || methodName.equals("getInterfaceDescriptor")) {
                    return null;
                }
                // all set methods
                if (methodName.startsWith("set")) {
                    return null;
                }

                if (parameterTypes.length == 0) {
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                return null;
            }
        });

        return new JSONObjectExtended(result);

    }


}
