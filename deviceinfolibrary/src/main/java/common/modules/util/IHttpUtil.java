package common.modules.util;

import android.os.Looper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class IHttpUtil {

    public static interface JsonCallback {
        void handle(JSONObject json);
    }

    public static void post(boolean isAsync, String urlStr, Map<String, Object> headers, String bodyString, JsonCallback jsonCallback) {
        try {
            post(isAsync, urlStr, headers, bodyString.getBytes("UTF-8"), jsonCallback);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void post(boolean isAsync, final String urlStr, final Map<String, Object> headers, final byte[] bodyBytes, final JsonCallback jsonCallback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                JSONObject responseJson = null;
                try {
                    byte[] responseData = __post__(urlStr, headers, bodyBytes, null);
                    if (responseData != null) {
                        responseJson = new JSONObject(new String(responseData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (jsonCallback != null) {
                        final JSONObject json = responseJson;
                        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                jsonCallback.handle(json);
                            }
                        });
                    }
                }
            }
        };
        if (isAsync) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    // For get & post
    public static interface RequestCallback {
        void callback(Exception exception, HttpURLConnection connection, int responseCode, byte[] responseData);
    }

    public static byte[] __post__(String urlStr, Map<String, Object> headers, byte[] postBodyBytes, RequestCallback responseHandler) {

        HttpURLConnection conn = null;

        try {

            URL urlObj = new URL(urlStr);
            long postDataLength = postBodyBytes.length;

            conn = (HttpURLConnection) urlObj.openConnection();

            if (urlStr.startsWith("https")) {
                SSLContext sslcontext = SSLContext.getInstance("SSL");
                sslcontext.init(null, new TrustManager[]{new HttpsWorker.TrustAnyTrustManager()}, new SecureRandom());
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslcontext.getSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier(new HttpsWorker.TrustAnyHostnameVerifier());
            }

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(2 * 60 * 1000);
            conn.setReadTimeout(2 * 60 * 1000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataLength));

            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    conn.setRequestProperty(key, headers.get(key).toString());
                }
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.getOutputStream().write(postBodyBytes);

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

    public static void getAsync(final String urlStr, final Map<String, Object> headers, final JsonCallback jsonCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] responseData = get(urlStr, headers, null);
                JSONObject responseJson = null;
                try {
                    if (responseData != null) {
                        responseJson = new JSONObject(new String(responseData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (jsonCallback != null) {
                        final JSONObject json = responseJson;
                        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                jsonCallback.handle(json);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static byte[] get(String urlStr, Map<String, Object> headers, RequestCallback responseHandler) {
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
