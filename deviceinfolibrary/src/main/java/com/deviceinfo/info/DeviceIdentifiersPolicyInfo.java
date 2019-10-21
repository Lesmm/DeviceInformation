package com.deviceinfo.info;

import android.content.Context;
import android.os.Build;

import com.deviceinfo.InfoJsonHelper;
import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class DeviceIdentifiersPolicyInfo {

    public static JSONObject getInfo(Context mContext) {
        JSONObject deviceIdentifiersResult = new JSONObject();

        // 通过调用高层接口
        try {
            // Android 9.0 以后，不能通过反射来获取一些API的值了!!! 所以这里调用高层API再手动处理一下。

            // Android 8.0 后, Build.SERIAL 会在程序启动后某个阶段设置了，同时提供了 Build.getSerial() 接口
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String serial = Build.getSerial();
                deviceIdentifiersResult.put("getSerial", serial);   // Android 8.0, 8.1, 9.0
                deviceIdentifiersResult.put("getSerialForPackage", serial); // Android 10.0
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 通过反射来获取
        JSONObject info = getIDeviceIdentifiersPolicyInfo(mContext);

        InfoJsonHelper.mergeJSONObject(deviceIdentifiersResult, info);

        return deviceIdentifiersResult;

    }

    public static JSONObject getIDeviceIdentifiersPolicyInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.os.IDeviceIdentifiersPolicyService", "device_identifiers");

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

                // public java.lang.String getSerial() throws android.os.RemoteException;   // Android 8.0 above
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
