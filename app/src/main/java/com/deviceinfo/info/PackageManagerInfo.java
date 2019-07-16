package com.deviceinfo.info;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.util.Log;

import com.deviceinfo.InvokerOfObject;
import com.deviceinfo.InvokerOfService;
import com.deviceinfo.JSONArrayExtended;
import com.deviceinfo.JSONObjectExtended;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.modules.util.IArrayUtil;
import common.modules.util.IReflectUtil;
import common.modules.util.IReflectUtilWrapper;

public class PackageManagerInfo {

    public static JSONObject getInfo(Context mContext) {
        PackageManager mApplicationPackageManager = mContext.getPackageManager();

        // compare below two jsons
        int flags = 0;
        List<PackageInfo> allPackageInfos = mApplicationPackageManager.getInstalledPackages(flags);
        int mflags = PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES;
        List<PackageInfo> mPackageInfos = mApplicationPackageManager.getInstalledPackages(mflags);
        JSONArray json1 = new JSONArrayExtended(allPackageInfos);
        JSONArray json2 = new JSONArrayExtended(mPackageInfos);


        // permission groups and permission
        try {
            List<PermissionGroupInfo> permissionGroupInfos = mApplicationPackageManager.getAllPermissionGroups(PackageManager.GET_META_DATA);
            PermissionGroupInfo groupInfo = permissionGroupInfos.get(1);
            PermissionGroupInfo permissionGroupInfo = mApplicationPackageManager.getPermissionGroupInfo(groupInfo.name, PackageManager.GET_META_DATA);
            List<PermissionInfo> permissionInfos = mApplicationPackageManager.queryPermissionsByGroup(groupInfo.name, PackageManager.GET_META_DATA);
            Log.d("", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // getSystemSharedLibraryNames
        Object proxy = InvokerOfService.getProxy("android.content.pm.IPackageManager", "package");
        String[] names = (String[]) IReflectUtil.invokeMethod(proxy, "getSystemSharedLibraryNames", new Class[]{}, new Object[]{});


        // get info ......
        JSONObject packageInfo = getIPackageInfo(mContext);

        return packageInfo;
    }

    public static JSONObject getIPackageInfo(final Context mContext) {

        Object proxy = InvokerOfService.getProxy("android.content.pm.IPackageManager", "package");
        final int userId = (Integer) IReflectUtil.invokeMethod(mContext, "getUserId", new Class[]{}, new Object[]{});
        final Object opPackageName = IReflectUtil.invokeMethod(mContext, "getOpPackageName", new Class[]{}, new Object[]{});

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
                    // public android.content.pm.IPackageInstaller getPackageInstaller() throws android.os.RemoteException;
                    // public int getInstallLocation() throws android.os.RemoteException;
                    // public boolean hasSystemUidErrors() throws android.os.RemoteException;
                    // public boolean isSafeMode() throws android.os.RemoteException;
                    if (methodName.equals("getPackageInstaller") || methodName.equals("getInstallLocation") || method.equals("hasSystemUidErrors")
                            || method.equals("isSafeMode")) {
                        return null;
                    }

                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    return value;
                }

                final Object fObj = obj;
                final Method fMethod = method;
                final String fMethodName = methodName;
                final Map<String, Object> fResultMap = resultMap;


                if (parameterTypes.length == 1) {

                    if (parameterTypes[0] == int.class) {

                        // public java.util.List<android.content.pm.PermissionGroupInfo> getAllPermissionGroups(int flags) throws android.os.RemoteException;
                        if (methodName.equals("getAllPermissionGroups")) {
                            Object value = method.invoke(obj, new Object[]{PackageManager.GET_META_DATA});
                            List list = IReflectUtilWrapper.getFieldsValues((List) value, IArrayUtil.arrayToList(new String[]{"packageName", "name"}));
                            return list;
                        }

                        // public java.lang.String[] getPackagesForUid(int uid) throws android.os.RemoteException;
                        // public java.lang.String getNameForUid(int uid) throws android.os.RemoteException;
                        if (methodName.equals("getPackagesForUid") || methodName.equals("getNameForUid")) {
                            // just one uid now: 1000
                            Object value = method.invoke(obj, new Object[]{android.os.Process.SYSTEM_UID});
                            String key = methodName + "_arg0_int_" + android.os.Process.SYSTEM_UID;
                            resultMap.put(key, value);
                            return null;    // already set into resultMap, just return null.
                            // 关于uid，除了这两个还有:
                            // public int getUidForSharedUser(java.lang.String sharedUserName) throws android.os.RemoteException;
                            // public int getFlagsForUid(int uid) throws android.os.RemoteException;
                            // public int getPrivateFlagsForUid(int uid) throws android.os.RemoteException;
                            // public boolean isUidPrivileged(int uid) throws android.os.RemoteException;
                        }
                    }

                    // ### public android.content.pm.FeatureInfo[] getSystemAvailableFeatures() throws android.os.RemoteException;
                    // public boolean hasSystemFeature(java.lang.String name) throws android.os.RemoteException;    // TODO ... Hook 那边根据 getSystemAvailableFeatures() 的来返回值
                    if (parameterTypes[0] == String.class) {

                    }
                }

                // !!! Package List 有两个来源，分别是 getPackageInfo & getInstalledPackages， 一个是json一个是array, 其实前者的values就是后者的array element! 值是一样，分开写是因为定制化更高
                // !!! 同理放在 getApplicationInfo 与 getInstalledApplications 也一样。 另 PackageInfo 包含了 ApplicationInfo。


                if (parameterTypes.length == 2) {

                    // public boolean isPackageAvailable(java.lang.String packageName, int userId) throws android.os.RemoteException;
                    if (parameterTypes[0] == String.class && parameterTypes[1] == int.class) {

                        // TODO... isPackageAvailable 这方法就由 Hook 那边根据 Package List 来处理true, 其余的如系统package交给系统吧,这里只是拿一下看看
                        // TODO... 但如 com.cyanogenmod 及 org.cyanogenmod 得返回 false，要不会被认为是 CM 手机
                        if (methodName.equals("isPackageAvailable")) {
                            final Map availableMap = new HashMap();
                            fResultMap.put(methodName, availableMap);

                            iterateAllPackageInfoList(mContext, new IterateHandler() {
                                @Override
                                public void handle(PackageInfo info) throws Exception {
                                    String packageName = info.packageName;
                                    Object value = fMethod.invoke(fObj, new Object[]{packageName, userId}); // value is Boolean
                                    if (value != null) {
                                        availableMap.put(packageName, value);
                                    }
                                }
                            });
                        }

                    }

                    // public android.content.pm.ParceledListSlice getInstalledPackages(int flags, int userId) throws android.os.RemoteException;
                    // public android.content.pm.ParceledListSlice getInstalledApplications(int flags, int userId) throws android.os.RemoteException;
                    if (parameterTypes[0] == int.class && parameterTypes[1] == int.class) {

                        if (methodName.equals("getInstalledPackages")) {
                            int flags = 0;
                            Object value = method.invoke(obj, new Object[]{flags, userId});
                            List<PackageInfo> packageInfos = (List<PackageInfo>) IReflectUtil.getFieldValue(value, "mList");
                            JSONArray array = translatePackageInfos2JSONArray(packageInfos);
                            return array;
                        }

                        if (methodName.equals("getInstalledApplications")) {
                            int flags = 0;
                            Object value = method.invoke(obj, new Object[]{flags, userId});
                            List<ApplicationInfo> applicationInfos = (List<ApplicationInfo>) IReflectUtil.getFieldValue(value, "mList");
                            JSONArray array = translateApplicationInfos2JSONArray(applicationInfos);
                            return array;
                        }

                    }


                }

                if (parameterTypes.length == 3) {

                    // public android.content.pm.PackageInfo getPackageInfo(java.lang.String packageName, int flags, int userId) throws android.os.RemoteException;
                    // TODO ... getApplicationInfo() Hook 那边根把 Packages list 里的 ApplicationInfo 来处理
                    // public android.content.pm.ApplicationInfo getApplicationInfo(java.lang.String packageName, int flags, int userId) throws android.os.RemoteException;
                    // TODO ... getActivityInfo() 待研究, 若 getPackageInfo 里带上 GET_ACTIVITIES flag 后有 ActivityInfo, 那么 Hook 那边要根据 Packages list 里的 ActivityInfo 来处理
                    // public android.content.pm.ActivityInfo getActivityInfo(android.content.ComponentName className, int flags, int userId) throws android.os.RemoteException;
                    // TODO ... 同上 getActivityInfo() 带上 GET_RECEIVERS flag
                    // public android.content.pm.ActivityInfo getReceiverInfo(android.content.ComponentName className, int flags, int userId) throws android.os.RemoteException;
                    // TODO ... 同上 getActivityInfo() 带上 GET_SERVICES flag
                    // public android.content.pm.ServiceInfo getServiceInfo(android.content.ComponentName className, int flags, int userId) throws android.os.RemoteException;
                    // TODO ... 同上 getActivityInfo() 带上 GET_PROVIDERS flag
                    // public android.content.pm.ProviderInfo getProviderInfo(android.content.ComponentName className, int flags, int userId) throws android.os.RemoteException;

                    if (parameterTypes[0] == String.class && parameterTypes[1] == int.class && parameterTypes[2] == int.class) {

                        if (methodName.equals("getPackageInfo")) {
                            final Map availableMap = new HashMap();
                            fResultMap.put(methodName, availableMap);

                            iterateAllPackageInfoList(mContext, new IterateHandler() {
                                @Override
                                public void handle(PackageInfo info) throws Exception {
                                    String packageName = info.packageName;
                                    int flags = 0;
                                    int mflags = PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES; // 加上flag，获取的包会不全
                                    Object value = fMethod.invoke(fObj, new Object[]{packageName, flags, userId}); // flags is 0 now! value is android.content.pm.PackageInfo
                                    if (value != null) {
                                        JSONObject packageInfoJson = translatePackageInfo2JSONObject((PackageInfo) value);
                                        if (packageInfoJson != null) {
                                            availableMap.put(packageName, packageInfoJson);
                                        }
                                    }
                                }
                            });
                        }


                    }
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
                int mflags = PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES; // 加上flag，获取的包会不全
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


    // ---------------------- 处理 PackageInfo， 提取我们所需要的key-value ----------------------
    public static JSONArray translatePackageInfos2JSONArray(List<PackageInfo> packageInfos) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < packageInfos.size(); i++) {
            PackageInfo packageInfo = packageInfos.get(i);
            JSONObject packageInfoJson = translatePackageInfo2JSONObject(packageInfo);
            if (packageInfoJson != null) {
                array.put(packageInfoJson);
            }
        }
        return array;
    }

    public static JSONArray translateApplicationInfos2JSONArray(List<ApplicationInfo> applicationInfos) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < applicationInfos.size(); i++) {
            ApplicationInfo applicationInfo = applicationInfos.get(i);
            JSONObject applicationInfoJson = translateApplicationInfo2JSONObject(applicationInfo);
            if (applicationInfoJson != null) {
                array.put(applicationInfoJson);
            }
        }
        return array;
    }

    public static JSONObject translateApplicationInfo2JSONObject(ApplicationInfo applicationInfo) {
        Map<?, ?> map = IReflectUtilWrapper.getFieldsValues(applicationInfo, IArrayUtil.arrayToList(new String[]{"sourceDir", "publicSourceDir", "dataDir", "className", "processName", "targetSdkVersion"}));
        return new JSONObjectExtended(map);
    }

    public static JSONObject translatePackageInfo2JSONObject(PackageInfo packageInfo) {
        String packageName = packageInfo.packageName;

        // remove the android official system common app
        // 官方的包: com.android, 高通的包: com.qualcomm & org.codeaurora(org.codeaurora.bluetooth), CM的包: com.cyanogenmod & org.cyanogenmod
        if (packageName.startsWith("com.android") || packageName.startsWith("com.qualcomm")
                || packageName.startsWith("com.cyanogenmod") || packageName.startsWith("org.cyanogenmod")
        ) {
            return null;
        }

        // new packageJsonInfo
        Map<?, ?> map = IReflectUtilWrapper.getFieldsValues(packageInfo, IArrayUtil.arrayToList(new String[]{"firstInstallTime", "lastUpdateTime", "versionCode", "versionName"}));
        JSONObject newPackageInfo = new JSONObjectExtended(map);

        // new applicationInfo
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        JSONObject newApplicationInfo = translateApplicationInfo2JSONObject(applicationInfo);

        try {
            newPackageInfo.put("packageName", packageName);
            newPackageInfo.put("applicationInfo", newApplicationInfo);
            return newPackageInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
