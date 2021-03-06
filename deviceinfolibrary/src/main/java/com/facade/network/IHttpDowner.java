package com.facade.network;


import com.facade.Manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import common.modules.util.HttpsWorker;

public class IHttpDowner {

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

            if (urlStr.startsWith("https")) {
                SSLContext sslcontext = SSLContext.getInstance("SSL");
                sslcontext.init(null, new TrustManager[]{new HttpsWorker.TrustAnyTrustManager()}, new SecureRandom());
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslcontext.getSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier(new HttpsWorker.TrustAnyHostnameVerifier());
            }

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
            String cacheDirectory = Manager.getApplication().getCacheDir().getAbsolutePath() + "/";
            String downloadTempFilePath = cacheDirectory + UUID.randomUUID().toString();
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


}
