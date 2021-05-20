package common.modules.util.java.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class HttpDownloader {

    public static interface DownloadAsyncCallback {
        void callback(boolean isSuccess);
    }

    public void downloadAsync(final String urlStr, final String fileName, final DownloadAsyncCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // 先写入缓存目录，再替换，还是给调用操作这逻辑吧，封装的不做了
//                String cacheDir = getTemporaryCacheDir();
//                String need2DeleteDownloadCacheFile = cacheDir + File.separator + "ToBeDelete_" + UUID.randomUUID().toString().replace("-", "");

                download(urlStr, fileName, new ConnectionHandler() {
                    @Override
                    public void onDownloaded(HttpURLConnection connection, int responseCode, Exception exception, String downloadedFileName) {
                        long length = 0;
                        try {
                            String contentLength = connection.getHeaderField("Content-Length");
                            if (contentLength != null) {
                                length = Long.parseLong(contentLength);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        File file = new File(fileName);
                        boolean isDownloadSuccess = exception == null && responseCode >= 200 && responseCode < 300 && file.exists() && file.length() != 0;
                        if (length != 0) {
                            isDownloadSuccess = isDownloadSuccess && length == file.length();
                        }

//                        if (isDownloadSuccess && !downloadedFileName.equals(fileName)) {
//                            new File(downloadedFileName).renameTo(file);
//                        }

                        if (callback != null) {
                            callback.callback(isDownloadSuccess);
                        }
                    }
                });
            }
        }).start();

    }

    public static String getTemporaryCacheDir() {
        String dir = null;

        // for Android platform
        boolean isAndroidPlatform = false;
        try {
            String vmName = System.getProperty("java.vm.name");
            String rtName = System.getProperty("java.runtime.name");
            if (vmName != null && vmName.contains("Dalvik") || rtName != null && rtName.contains("Android")) {
                isAndroidPlatform = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isAndroidPlatform) {
            try {
                Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
                Method currentActivityThreadMethod = ActivityThread.getDeclaredMethod("currentActivityThread");
                currentActivityThreadMethod.setAccessible(true);
                Object currentActivityThread = currentActivityThreadMethod.invoke(ActivityThread);
                Method getApplicationMethod = ActivityThread.getDeclaredMethod("getApplication");
                getApplicationMethod.setAccessible(true);
                Object application = getApplicationMethod.invoke(currentActivityThread, new Object[]{});

                Class<?> ContextWrapper = Class.forName("android.content.ContextWrapper");
                Method getCacheDirMethod = ContextWrapper.getDeclaredMethod("getCacheDir");
                getCacheDirMethod.setAccessible(true);
                File cacheDir = (File) getCacheDirMethod.invoke(application, new Object[]{});
                dir = cacheDir.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (dir != null && !dir.isEmpty()) {
            return dir;
        }

        // for other Java platform
        dir = System.getProperty("java.io.tmpdir");

        if (dir != null && !dir.isEmpty()) {
            return dir;
        }

        return isAndroidPlatform ? "/sdcard/" : "/tmp/";
    }


    /**
     * Static Convenient Methods
     */

    public static abstract class ConnectionHandler {
        public void beforeConnect(HttpURLConnection connection) {
        }

        public void afterConnect(HttpURLConnection connection) {
        }

        public void onDownloaded(HttpURLConnection connection, int responseCode, Exception exception, String downloadedFileName) {
        }
    }

    public static int download(String urlStr, String fileName, ConnectionHandler connectionHandler) {
        if (!new File(fileName).getParentFile().exists()) {
            new File(fileName).getParentFile().mkdirs();
        }
        new File(fileName).delete();

        int responseCode = -1;
        Exception exception = null;
        HttpURLConnection connection = null;
        FileOutputStream outputStream = null;

        try {
            URL urlObj = new URL(urlStr);

            connection = (HttpURLConnection) urlObj.openConnection();

            if (urlStr.startsWith("https")) {
                SSLContext sslcontext = SSLContext.getInstance("SSL");
                sslcontext.init(null, new TrustManager[]{new HttpsWorker.TrustAnyTrustManager()}, new java.security.SecureRandom());
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslcontext.getSocketFactory());
                ((HttpsURLConnection) connection).setHostnameVerifier(new HttpsWorker.TrustAnyHostnameVerifier());
            }

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2 * 60 * 1000);
            connection.setReadTimeout(60 * 1000);

            // -------------------- call before connect --------------------
            if (connectionHandler != null) {
                try {
                    connectionHandler.beforeConnect(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // -------------------- call before connect --------------------

            connection.connect();

            // -------------------- call after connect --------------------
            if (connectionHandler != null) {
                try {
                    connectionHandler.afterConnect(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // -------------------- call after connect --------------------

            responseCode = connection.getResponseCode();
            InputStream inputStream = connection.getInputStream();

            // 避免内存不足，写文件
            outputStream = new FileOutputStream(fileName);

            int len = -1;
            byte[] buffer = new byte[512 * 1024]; // 512 KB buffer
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // -------------------- call on finally --------------------
            if (connectionHandler != null) {
                try {
                    connectionHandler.onDownloaded(connection, responseCode, exception, fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // -------------------- call on finally --------------------
        }
        return responseCode;
    }

}
