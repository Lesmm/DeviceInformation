package com.deviceinfo.info;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class BluetoothManagerInfo {

    public static JSONObject getInfo(Context mContext) {

        JSONObject info = getIBluetoothManagerInfo(mContext);

        if (info.length() == 0) {
            BluetoothAdapter bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
            try {
                if (bluetoothAdapter.getName() != null) {
                    info.put("getName", bluetoothAdapter.getName());
                }
                if (bluetoothAdapter.getAddress() != null) {
                    info.put("getAddress", bluetoothAdapter.getAddress());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return info;
    }

    public static JSONObject getIBluetoothManagerInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.bluetooth.IBluetoothManager", "bluetooth_manager");

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

                // public android.bluetooth.IBluetoothGatt getBluetoothGatt() throws android.os.RemoteException;
                if (methodName.equals("getBluetoothGatt")) {
                    return null;
                }

                // 只需要下面两个就够了
                // public java.lang.String getAddress() throws android.os.RemoteException;
                // public java.lang.String getName() throws android.os.RemoteException;

                if (!methodName.startsWith("get")) {
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
