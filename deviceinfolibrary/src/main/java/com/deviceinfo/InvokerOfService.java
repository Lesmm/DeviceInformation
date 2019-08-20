package com.deviceinfo;

import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import common.modules.util.HUtil;
import common.modules.util.IJSONObjectUtil;
import common.modules.util.IReflectUtil;

public class InvokerOfService {

    public static final String CLASS_NAME = InvokerOfService.class.getSimpleName();

    public static Object getProxy(String serviceInterfaceName, String serviceName) {
        return  asInterface(serviceInterfaceName + "$Stub", getService(serviceName));
    }

    public static IBinder getService(String serviceName) {
        // com.android.internal.telephony.ISub iSub = com.android.internal.telephony.ISub.Stub.asInterface(ServiceManager.getService("isub"));
        // android.os.IBinder
        // IBinder mRemote = (IBinder)IReflectUtil.invokeClassMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{"isub"})

        IBinder mRemote = (IBinder) IReflectUtil.invokeClassMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{serviceName});
        return mRemote;
    }

    public static Object asInterface(String stubClassName, IBinder mRemote) {
        // com.android.internal.telephony.ISub$Stub$Proxy
        // Object proxy = IReflectUtil.invokeClassMethod("com.android.internal.telephony.ISub$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{mRemote});

        Object proxy = IReflectUtil.invokeClassMethod(stubClassName, "asInterface", new Class[]{IBinder.class}, new Object[]{mRemote});
        return proxy;
    }

    public static void logMethodsOfObject(Object proxy) {
        Log.d(CLASS_NAME, "------------ start: " + proxy.getClass().getName() + " ------------");
        Method[] methods = proxy.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String methodName = method.getName();
            Log.d(CLASS_NAME, methodName);
        }
        Log.d(CLASS_NAME, "------------ end: " + proxy.getClass().getName() + " ------------");
    }

    public static void logServiceClass(String serviceName, String serviceInterfaceName) {
        Log.d(CLASS_NAME,"\r\n-------------------------------- " + serviceName);

        String serviceClazzName = serviceInterfaceName;
        Log.d(CLASS_NAME,"________________________________ " + serviceClazzName);
        Map<String, List<String>> map = HUtil.getClassStruct(serviceClazzName);
        Log.d(CLASS_NAME, IJSONObjectUtil.formatJsonString(new JSONObject(map).toString()));
        Log.d(CLASS_NAME,"________________________________ ");

        String serviceStubClazzName = serviceClazzName + "$Stub";;
        Log.d(CLASS_NAME,"________________________________ " + serviceStubClazzName);
        Map<String, List<String>> mapStub = HUtil.getClassStruct(serviceStubClazzName);
        Log.d(CLASS_NAME, IJSONObjectUtil.formatJsonString(new JSONObject(mapStub).toString()));
        Log.d(CLASS_NAME,"________________________________ ");


        String stubProxyClazzName = serviceStubClazzName + "$Proxy";
        Log.d(CLASS_NAME,"________________________________ " + stubProxyClazzName);
        Map<String, List<String>> mapProxy = HUtil.getClassStruct(stubProxyClazzName);
        Log.d(CLASS_NAME, IJSONObjectUtil.formatJsonString(new JSONObject(mapProxy).toString()));
        Log.d(CLASS_NAME,"________________________________ ");
    }

}
