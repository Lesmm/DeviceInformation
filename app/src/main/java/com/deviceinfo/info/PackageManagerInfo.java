package com.deviceinfo.info;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONArrayExtended;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import common.modules.util.IReflectUtil;

public class PackageManagerInfo {

    public static JSONObject getInfo(Context mContext) {
        PackageManager mApplicationPackageManager = mContext.getPackageManager();

        int flags = 0;
        List<PackageInfo> allPackageInfos = mApplicationPackageManager.getInstalledPackages(flags);
        int mflags = PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES ;
        List<PackageInfo> mPackageInfos = mApplicationPackageManager.getInstalledPackages(mflags);
        JSONArray json1 = new JSONArrayExtended(allPackageInfos);
        JSONArray json2 = new JSONArrayExtended(mPackageInfos);

        Object proxy = InvokerOfService.getProxy("android.content.pm.IPackageManager", "package");
        String[] names = (String[])IReflectUtil.invokeMethod(proxy, "getSystemSharedLibraryNames", new Class[]{}, new Object[]{});

        // get info ......
        JSONObject packageInfo = getPackageInfo(mContext);

        return packageInfo;
    }

    public static JSONObject getPackageInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.content.pm.IPackageManager", "package");
        final int userId = (Integer) IReflectUtil.invokeMethod(mContext, "getUserId", new Class[]{}, new Object[] {});
        final Object opPackageName = IReflectUtil.invokeMethod(mContext, "getOpPackageName", new Class[]{}, new Object[] {});

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

                if (parameterTypes.length == 1) {

                }

                // public boolean isPackageAvailable(java.lang.String packageName, int userId) throws android.os.RemoteException;
                if (parameterTypes.length == 2) {
                    if (methodName.equals("isPackageAvailable")) {
                        iterateAllPackageInfoList(mContext, new IterateHandler() {
                            @Override
                            public void handle(PackageInfo info) throws Exception {
                                String packageName = info.packageName;
                            }
                        });
                    }
                }

                if (parameterTypes.length == 3) {

                }

                return null;
            }
        });

        return new JSONObjectExtended(result);
    }



    // ---------------------- 遍历 Packages 工具方法 ----------------------
    public static interface IterateHandler {
        public void handle(PackageInfo info) throws Exception;
    }

    private static List<PackageInfo> allPackageInfoList = null;
    public static void iterateAllPackageInfoList(Context mContext, IterateHandler handler) {
        try {

            if (allPackageInfoList == null) {
                PackageManager mApplicationPackageManager = mContext.getPackageManager();

                int flags = 0;
                List<PackageInfo> allPackageInfos = mApplicationPackageManager.getInstalledPackages(flags);
                allPackageInfoList = allPackageInfos;
            }
            for (int i = 0; i < allPackageInfoList.size(); i++) {
                try {
                    PackageInfo info = allPackageInfoList.get(i);
                    handler.handle(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<PackageInfo> activityPackageInfoList = null;
    public static void iterateActivityPackageInfoList(Context mContext, IterateHandler handler) {
        try {

            if (activityPackageInfoList == null) {
                PackageManager mApplicationPackageManager = mContext.getPackageManager();

                int flags = 0;
                int mflags = PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES ;
                List<PackageInfo> mPackageInfos = mApplicationPackageManager.getInstalledPackages(mflags);
                activityPackageInfoList = mPackageInfos;
            }
            for (int i = 0; i < activityPackageInfoList.size(); i++) {
                try {
                    PackageInfo info = activityPackageInfoList.get(i);
                    handler.handle(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
