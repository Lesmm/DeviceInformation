package com.deviceinfo.network;

import com.deviceinfo.Manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IHttpGetter {

    /*
     * Get 请求
     */

    public interface DownloadSyncCallback {
        void callback(Exception exception, HttpURLConnection connection, int responseCode, String temporaryDownloadFilePath);
    }

    public interface DownloadPhaseHandler {
        void onStart(String temporaryFileName);
        void onOver(String temporaryFileName);
    }

    public DownloadPhaseHandler downloadPhaseHandler;

    public void download(String urlStr, Map<String, Object> headers, DownloadSyncCallback callback) {
        // 不要直接用get方法，因为download的东西有可能很大，get放在byte[]里手机内存或许会不够
        // return get(urlStr, headers, responseHandler);

        String needDeleteDownloadCacheFile = null;

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setConnectTimeout(60 * 1000);
            conn.setReadTimeout(60 * 1000);

            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    conn.setRequestProperty(key, headers.get(key).toString());
                }
            }

            conn.connect();

            int responseCode = conn.getResponseCode();
            InputStream inputStream = conn.getInputStream();

            // 避免内存不足，写文件
            String downloadCacheDirecotry = Manager.getApplication().getCacheDir().getAbsolutePath();
            String downloadTempFilePath = downloadCacheDirecotry + "/" + UUID.randomUUID().toString();
            needDeleteDownloadCacheFile = downloadTempFilePath;
            FileOutputStream outputStream = new FileOutputStream(downloadTempFilePath);

            if (downloadPhaseHandler != null) {
                downloadPhaseHandler.onStart(needDeleteDownloadCacheFile);
            }

            int len = -1;
            byte[] buffer = new byte[1 * 1024 * 1024]; // 1MB buffer
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            if (callback != null) {
                callback.callback(null, conn, responseCode, downloadTempFilePath);
            }

            new File(needDeleteDownloadCacheFile).delete();
            needDeleteDownloadCacheFile = null;

        } catch (Exception e) {
            e.printStackTrace();

            if (callback != null) {
                callback.callback(e, conn, 0, null);
            }

        } finally {
            if (downloadPhaseHandler != null) {
                downloadPhaseHandler.onOver(needDeleteDownloadCacheFile);
            }
            if (needDeleteDownloadCacheFile != null) {
                new File(needDeleteDownloadCacheFile).delete();
            }
        }

    }

    public interface RequestCallback {
        void callback(Exception exception, HttpURLConnection connection, int responseCode, byte[] responseData);
    }

    public byte[] get(String urlStr, Map<String, Object> headers, RequestCallback responseHandler) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setConnectTimeout(60 * 1000);
            conn.setReadTimeout(60 * 1000);

            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    conn.setRequestProperty(key, headers.get(key).toString());
                }
            }

            conn.connect();

            int responseCode = conn.getResponseCode();
            InputStream inputStream = conn.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int len = -1;
            byte[] buffer = new byte[512 * 1024]; // 512 KB buffer
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();

            byte[] responseData = outputStream.toByteArray();
            outputStream.close();
            inputStream.close();

            if (responseHandler != null) {
                responseHandler.callback(null, conn, responseCode, responseData);
            }

            return responseData;

        } catch (Exception e) {
            e.printStackTrace();

            if (responseHandler != null) {
                responseHandler.callback(e, conn, 0, null);
            }
        }

        return null;
    }

}
