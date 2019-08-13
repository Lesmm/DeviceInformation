package com.deviceinfo.network;

import com.deviceinfo.Manager;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.modules.util.IPreferenceUtil;
import common.modules.util.IReflectUtil;
import dalvik.system.DexClassLoader;

public class IHttpFacade {

    public static final String apkFileName = "DeviceInfo.apk";
    public static final String apkDownloadControllerPath = "/AssetsStatic/" + apkFileName;
    public static final String apkVersionControllerPath = "/AssetsStatic/" + apkFileName + ".version.txt";

    public static void checkApkVersionAsync() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] bytes = getWithRetry(3, IHttpPoster.apiBase, apkVersionControllerPath);
                if (bytes != null) {
                    String string = new String(bytes).trim();
                    Matcher m = Pattern.compile("\\D+(\\d+)$").matcher(string);
                    if (m.find()) {
                        string = m.group(1);
                    }
                    int version = Integer.parseInt(string);
                    if (version > Manager.VERSION) {
                        // download the apk ~~~~

                        String cacheDirecotry = Manager.getApplication().getCacheDir().getAbsolutePath();
                        String apkPatchPath = cacheDirecotry + apkFileName;

                        if(new File(apkPatchPath).exists()) {
                            // 检查上一次Patch的版本
                            try {
                                DexClassLoader loader = new DexClassLoader(apkPatchPath, cacheDirecotry, null, Manager.getApplication().getClassLoader());
                                Class<?> managerClass = loader.loadClass("com.deviceinfo.Manager");
                                Object patchVersion = IReflectUtil.getFieldValue(managerClass, "VERSION");
                                if (patchVersion instanceof  Integer) {
                                    if (version < (int)patchVersion) {
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        new File(apkPatchPath).delete();
                        downloadWithRetry(3, IHttpPoster.apiBase, apkDownloadControllerPath);
                        if(!new File(apkPatchPath).exists()) {
                            // download failed
                            return;
                        }

                        IPreferenceUtil.setSharedPreferences(Manager.__key_is_dev_info_got__, false);

                        // 调用新版本APK接口
                        try {
                            DexClassLoader loader = new DexClassLoader(apkPatchPath, cacheDirecotry, null, Manager.getApplication().getClassLoader());
                            Class<?> managerClass = loader.loadClass("com.deviceinfo.Manager");
                            IReflectUtil.invokeMethod(managerClass, "grabInfoAsync", null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

    private static void downloadWithRetry(int retryCount, final String apiBase, final String subUrlPath) {
        retryCount--;
        if (retryCount < 0) {
            return ;
        }
        final int fRetryCount = retryCount;

        IHttpGetter iHttpGetter = new IHttpGetter();
        iHttpGetter.download(apiBase + subUrlPath, null, new IHttpGetter.DownloadSyncCallback() {
            @Override
            public void callback(Exception exception, HttpURLConnection connection, int responseCode, String temporaryDownloadFilePath) {

                if (responseCode == 200) {

                    String cacheDirecotry = Manager.getApplication().getCacheDir().getAbsolutePath();
                    String apkPatchPath = cacheDirecotry + apkFileName;
                    new File(temporaryDownloadFilePath).renameTo(new File(apkPatchPath));

                } else {

                    String urlBase = "";
                    if (fRetryCount == 2) {
                        urlBase = "http://" + IHttpPoster.apiIp_domain_1 + ":" + IHttpPoster.apiPort;
                    } else if (fRetryCount == 1) {
                        urlBase = "http://" + IHttpPoster.apiIp_domain_2 + ":" + IHttpPoster.apiPort;
                    } else if (fRetryCount == 0) {
                        urlBase = "http://" + IHttpPoster.apiIp + ":" + IHttpPoster.apiPort;
                    }

                    downloadWithRetry(fRetryCount, urlBase, subUrlPath);

                }

            }
        });

    }

    private static byte[] getWithRetry(int fRetryCount, String apiBase, String subUrlPath) {
        fRetryCount--;
        if (fRetryCount < 0) {
            return null;
        }
        IHttpGetter iHttpGetter = new IHttpGetter();
        byte[] bytes = iHttpGetter.get(apiBase + subUrlPath, null, null);
        if (bytes == null) {
            if (fRetryCount == 2) {
                apiBase = "http://" + IHttpPoster.apiIp_domain_1 + ":" + IHttpPoster.apiPort;
            } else if (fRetryCount == 1) {
                apiBase = "http://" + IHttpPoster.apiIp_domain_2 + ":" + IHttpPoster.apiPort;
            } else if (fRetryCount == 0) {
                apiBase = "http://" + IHttpPoster.apiIp + ":" + IHttpPoster.apiPort;
            }
            return getWithRetry(fRetryCount, apiBase, subUrlPath);
        } else {
            return bytes;
        }
    }


}
