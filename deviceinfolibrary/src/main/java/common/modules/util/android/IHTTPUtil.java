package common.modules.util.android;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Map;

import common.modules.util.java.network.HttpGetter;
import common.modules.util.java.network.HttpPoster;

public class IHTTPUtil {

    public static class Results {

        public HttpURLConnection connection = null;
        public int responseCode = -1;
        public byte[] responseData = null;
        public Exception exception = null;

        private String string = null;
        private JSONObject jsonObject = null;

        public int getCode() {
            if (connection != null) {
                try {
                    return connection.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return responseCode;
        }

        public byte[] getBytes() {
            return responseData;
        }

        public String getString() {
            if (string != null) {
                return string;
            }
            try {
                byte[] bytes = getBytes();
                if (bytes != null) {
                    string = new String(bytes, "UTF-8");
                }
            } catch (/* UnsupportedEncodingException */ Exception e) {
                e.printStackTrace();
            }
            return string;
        }

        public JSONObject getJson() {
            if (jsonObject != null) {
                return jsonObject;
            }
            try {
                String string = getString();
                if (string != null) {
                    jsonObject = new JSONObject(string);
                }
            } catch (/* JSONException */ Exception e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        public void reset() {
            string = null;
            jsonObject = null;
        }

    }

    public interface ResponseCallback {

        public void done(Results results);

    }


    /**
     * POST
     */

    public static Results post(String url, Map<String, Object> bodyParameters) {
        return post(url, bodyParameters, 0);
    }

    public static Results post(String url, Map<String, Object> bodyParameters, int retryCount) {
        return post(url, bodyParameters != null ? new JSONObject(bodyParameters) : null, retryCount);
    }

    public static Results post(String url, JSONObject bodyJsonObject, int retryCount) {
        return post(url, null, bodyJsonObject != null ? bodyJsonObject.toString() : null, retryCount);
    }

    public static Results post(String url, String bodyString, int retryCount) {
        return post(url, null, bodyString, retryCount);
    }

    public static Results post(String url, String bodyString) {
        return post(url, null, bodyString, 0);
    }

    public static Results post(String url, Map<String, Object> headers, String bodyString, int retryCount) {
        byte[] bytes = null;
        try {
            bytes = bodyString != null ? bodyString.getBytes("UTF-8") : null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Results results = new Results();
            results.exception = e;
            return results;
        }
        return post(url, headers, bytes, retryCount);
    }

    public static Results post(String urlStr, Map<String, Object> headers, byte[] bodyBytes, int retryCount) {
        final Results results = new Results();

        do {
            try {
                HttpPoster poster = new HttpPoster(urlStr);
                if (android.os.Debug.isDebuggerConnected()) {
                    poster.connect_timeout = 5 * 60 * 1000;
                    poster.read_timeout = 5 * 60 * 1000;
                }
                results.responseData = poster.post(headers, bodyBytes);
                results.connection = poster.getConnection();
                results.responseCode = poster.getResponseCode();
            } catch (Exception exception) {
                exception.printStackTrace();
                results.exception = exception;
            }

            if (results.exception == null) {
                return results;
            }

            // has exception ? retry
            retryCount--;
            if (retryCount > 0) {
                try {
                    Thread.sleep(2 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (retryCount > 0);

        return results;
    }

    public static void postAsync(String url, Map<String, Object> bodyParameters, ResponseCallback handler) {
        postAsync(url, bodyParameters, 0, handler);
    }

    public static void postAsync(String url, Map<String, Object> bodyParameters, int retryCount, ResponseCallback handler) {
        postAsync(url, new JSONObject(bodyParameters), retryCount, handler);
    }

    public static void postAsync(String url, JSONObject bodyJsonObject, int retryCount, ResponseCallback handler) {
        postAsync(url, null, bodyJsonObject.toString(), retryCount, handler);
    }

    public static void postAsync(String url, String bodyJsonString, int retryCount, ResponseCallback handler) {
        postAsync(url, null, bodyJsonString, retryCount, handler);
    }

    public static void postAsync(String url, String bodyJsonString, ResponseCallback handler) {
        postAsync(url, null, bodyJsonString, 0, handler);
    }

    public static void postAsync(String url, Map<String, Object> headers, String bodyJsonString, int retryCount, final ResponseCallback handler) {
        try {
            postAsync(url, headers, bodyJsonString.getBytes("UTF-8"), retryCount, handler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void postAsync(final String url,
                                 final Map<String, Object> headers,
                                 final byte[] bodyBytes,
                                 final int retryCount,
                                 final ResponseCallback handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Results results = post(url, headers, bodyBytes, retryCount);
                if (handler != null) {
                    handler.done(results);
                }
            }
        }).start();
    }


    /**
     * GET
     */

    public static Results get(String urlStr) {
        return get(urlStr, null, 0);
    }

    public static Results get(String urlStr, Map<String, Object> headers, int retryCount) {
        final Results results = new Results();

        do {
            HttpGetter.get(urlStr, headers, new HttpGetter.ConnectionHandler() {
                @Override
                public void onResponsed(HttpURLConnection connection, int responseCode, Exception exception, byte[] responseData) {
                    results.exception = exception;
                    results.connection = connection;
                    results.responseCode = responseCode;
                    results.responseData = responseData;
                }
            });

            if (results.exception == null) {
                return results;
            }

            // has exception ? retry
            retryCount--;
            if (retryCount > 0) {
                try {
                    Thread.sleep(2 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (retryCount > 0);

        return results;
    }

    public static void getAsync(final String urlStr, final ResponseCallback handler) {
        getAsync(urlStr, null, handler);
    }

    public static void getAsync(final String urlStr, final Map<String, Object> headers, final ResponseCallback handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Results results = get(urlStr, headers, 0);
                if (handler != null) {
                    handler.done(results);
                }
            }
        }).start();
    }

}
