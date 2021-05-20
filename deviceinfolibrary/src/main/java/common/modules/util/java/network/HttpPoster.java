package common.modules.util.java.network;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class HttpPoster {

    /**
     * Wrapper Methods
     */

    public int getResponseCode() throws Exception {
        return getConnection().getResponseCode();   // get code after write the byte of specific length
    }

    public byte[] post(Map<String, Object> headers, byte[] requestBytes) throws Exception {
        setContentLength(requestBytes.length);
        setContentType("application/json");
        setRequestHeaders(headers);

        getConnection().connect();
        write(requestBytes);

        return read();
    }


    /**
     * Base Properties
     */

    private HttpURLConnection connection = null;

    public int connect_timeout = 60 * 1000;
    public int read_timeout = 60 * 1000;

    public HttpPoster(String urlStr) throws Exception {
        URL urlObj = new URL(urlStr);
        connection = (HttpURLConnection) urlObj.openConnection();

        if (urlStr.startsWith("https")) {
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, new TrustManager[]{new HttpsWorker.TrustAnyTrustManager()}, new java.security.SecureRandom());
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslcontext.getSocketFactory());
            ((HttpsURLConnection) connection).setHostnameVerifier(new HttpsWorker.TrustAnyHostnameVerifier());
        }

        connection.setRequestMethod("POST");
        connection.setConnectTimeout(connect_timeout);
        connection.setReadTimeout(read_timeout);
        connection.setRequestProperty("Content-Type", "application/octet-stream");  // attention default is application/octet-stream

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    /*
     * Request Properties
     */

    public void setContentType(String contentType) {
        connection.setRequestProperty("Content-Type", contentType);
    }

    public void setContentLength(long contentLength) {
        connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
    }

    public void setRequestHeader(String key, String value) {
        connection.setRequestProperty(key, value);
    }

    public void setRequestHeaders(Map<String, Object> headers) {
        if (headers == null) {
            return;
        }
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            Object value = headers.get(key);
            if (value != null) {
                setRequestHeader(key, value.toString());
            }
        }
    }

    /*
     * Write & Read
     */

    public void write(byte[] bytes) throws Exception {
        connection.getOutputStream().write(bytes);
    }

    public void write(byte[] bytes, int off, int len) throws Exception {
        connection.getOutputStream().write(bytes, off, len);
    }

    public void flush() throws Exception {
        connection.getOutputStream().flush();
    }

    public byte[] read() throws Exception {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = connection.getInputStream();
            outputStream = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[64 * 1024]; // 64KB
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw e;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
