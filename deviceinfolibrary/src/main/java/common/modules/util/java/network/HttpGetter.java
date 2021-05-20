package common.modules.util.java.network;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class HttpGetter {

    /**
     * Static Convenient Methods
     */

    public static abstract class ConnectionHandler {
        public void beforeConnect(HttpURLConnection connection) {
        }

        public void afterConnect(HttpURLConnection connection) {
        }

        public void onResponsed(HttpURLConnection connection, int responseCode, Exception exception, byte[] responseData) {
        }
    }

    public static void getAsync(final String urlStr, final Map<String, Object> headers, final ConnectionHandler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                get(urlStr, headers, handler);
            }
        }).start();
    }

    public static byte[] get(String urlStr, Map<String, Object> headers, ConnectionHandler handler) {
        return get(urlStr, 60 * 1000, 60 * 1000, headers, handler);
    }

    public static byte[] get(String urlStr, int connect_timeout, int read_timeout, Map<String, Object> headers, ConnectionHandler handler) {
        return get(urlStr, null, 0, null, null, null, connect_timeout, read_timeout, headers, handler);
    }

    public static byte[] get(String urlStr,
                             String proxy_ip,
                             int proxy_port,
                             String referer,
                             String user_agent,
                             String cookie,
                             int connect_timeout,
                             int read_timeout,
                             Map<String, Object> headers,
                             ConnectionHandler handler) {
        HttpURLConnection connection = null;
        byte[] responseData = null;
        Exception exception = null;
        int responseCode = -1;

        try {
            URL urlObj = new URL(urlStr);

            if (proxy_ip != null && !proxy_ip.isEmpty() && proxy_port != 0) {
                connection = (HttpURLConnection) urlObj.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_ip, proxy_port)));
            } else {
                connection = (HttpURLConnection) urlObj.openConnection();
            }

            if (urlStr.startsWith("https")) {
                SSLContext sslcontext = SSLContext.getInstance("SSL");
                sslcontext.init(null, new TrustManager[]{new HttpsWorker.TrustAnyTrustManager()}, new java.security.SecureRandom());
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslcontext.getSocketFactory());
                ((HttpsURLConnection) connection).setHostnameVerifier(new HttpsWorker.TrustAnyHostnameVerifier());
            }

            connection.setRequestMethod("GET");

            if (referer != null && !referer.isEmpty()) {
                connection.setRequestProperty("Referer", referer);
            }
            if (user_agent != null && !user_agent.isEmpty()) {
                connection.setRequestProperty("User-Agent", user_agent);
            }
            if (cookie != null && !cookie.isEmpty()) {
                connection.setRequestProperty("Cookie", cookie);
            }

            connection.setConnectTimeout(connect_timeout);
            connection.setReadTimeout(read_timeout);

            if (headers != null) {
                for (String key : headers.keySet()) {
                    Object value = headers.get(key);
                    if (value != null) {
                        connection.setRequestProperty(key, value.toString());
                    }
                }
            }

            // -------------------- call before connect --------------------
            if (handler != null) {
                try {
                    handler.beforeConnect(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // -------------------- call before connect --------------------

            connection.connect();

            // -------------------- call after connect --------------------
            if (handler != null) {
                try {
                    handler.afterConnect(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // -------------------- call after connect --------------------

            // String set_cookie = connection.getHeaderField("Set-Cookie"); // get cookie

            responseCode = connection.getResponseCode();
            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int len = -1;
            byte[] buffer = new byte[512 * 1024]; // 512 KB buffer
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();

            responseData = outputStream.toByteArray();
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        } finally {

            // -------------------- call on finally --------------------
            if (handler != null) {
                try {
                    handler.onResponsed(connection, responseCode, exception, responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // -------------------- call on finally --------------------

        }
        return responseData;
    }

}
