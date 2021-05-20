package common.modules.util.java.network;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class HttpFormer {

    public static interface FormCallback {
        public void callback(Exception exception, int responseCode, byte[] responseData);
    }

    public static interface FormProgressHandler {
        public void progress(int progress);
    }

    public static void formUploadAsync(final String urlStr, final Map<String, Object> headers, final Map<String, Object> parametersMap, final Map<String, Object> filesMap,
                                       final FormCallback responseHandler, final FormProgressHandler progressHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    formUpload(urlStr, headers, parametersMap, filesMap, responseHandler, progressHandler);
                } catch (Exception e) {
                    // handle by responseHandler
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static byte[] formUploadSync(String urlStr, Map<String, Object> headers, Map<String, Object> parametersMap, Map<String, Object> filesMap) throws Exception {
        return formUpload(urlStr, headers, parametersMap, filesMap, null, null);
    }

    /**
     * 每个post参数之间的需要有个 boundary 分隔。boundary 随意设定，只要不会和其他的字符串重复即可。
     */
    public static byte[] formUpload(String urlStr, Map<String, Object> headers, Map<String, Object> parametersMap, Map<String, Object> filesMap,
                                    FormCallback responseHandler, FormProgressHandler progressHandler) throws Exception {

        try {

            // 1 随机生成一个boundary, 如:
            // --------------------------151461423562855491510638
            long time = new java.util.Date().getTime(); // len = 13
            long random = Math.abs(time * new Random().nextInt());
            String randomString = time + "" + random + "" + time;
            String boundary = "--------------------------" + randomString.substring(0, 24);

            // 2 向服务器发送post请求
            URL urlObj = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

            if (urlStr.startsWith("https")) {
                SSLContext sslcontext = SSLContext.getInstance("SSL");
                sslcontext.init(null, new TrustManager[] { new HttpsWorker.TrustAnyTrustManager() }, new java.security.SecureRandom());
                ((HttpsURLConnection)connection).setSSLSocketFactory(sslcontext.getSocketFactory());
                ((HttpsURLConnection)connection).setHostnameVerifier(new HttpsWorker.TrustAnyHostnameVerifier());
            }

            // 2.1 发送POST请求必须设置如下两行
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(60 * 1000);
            connection.setReadTimeout(60 * 1000);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    connection.setRequestProperty(key, headers.get(key).toString());
                }
            }

            OutputStream out = connection.getOutputStream();

            // 3 写入内容
            // 3.1 处理普通表单域(即形如key = value对)的POST请求
            if (parametersMap != null) {

                StringBuffer parametersBody = new StringBuffer();
                for (String key : parametersMap.keySet()) {
                    parametersBody.append("\r\n").append("--" + boundary).append("\r\n")
                            .append("Content-Disposition: form-data; name=\"").append(key + "\"")
                            .append("\r\n").append("\r\n").append(parametersMap.get(key));
                }
                out.write(parametersBody.toString().getBytes("UTF-8"));

            }


            if (progressHandler != null) {
                progressHandler.progress(10);
            }

            // 3.2 处理文件上传
            if (filesMap != null) {

                Map<String, String> spec = (Map<String, String>)filesMap.get("__spec_file_name__");

                for (String key : filesMap.keySet()) {
                    StringBuffer fileContentBody = new StringBuffer();

                    Object object = filesMap.get(key);
                    if ( !(object instanceof String) ) {
                        continue;
                    }

                    String filePath = (String)object;
                    String fileName = (spec != null  && spec.containsKey(key)) ? spec.get(key) : filePath;
                    fileContentBody.append("\r\n").append("--" + boundary).append("\r\n")
                            .append("Content-Disposition:form-data; name=\"").append(key + "\"; ")
                            // form中field的名称
                            .append("filename=\"").append(fileName + "\"")
                            // 上传文件的文件名，包括目录
                            .append("\r\n")
                            .append("Content-Type:multipart/form-data")
                            .append("\r\n\r\n");
                    out.write(fileContentBody.toString().getBytes("utf-8"));

                    if (progressHandler != null) {
                        progressHandler.progress(20);
                    }

                    // 3.3 写文件入流中
                    File file = new File(filePath);
                    long fileLength = file.length();

                    DataInputStream dis = new DataInputStream(new FileInputStream(file));

                    // 不推荐一次性读完, 占内存，作者真是的不考虑文件大内存小的设备
                    // int bytes = 0;
                    // byte[] bufferOut = new byte[(int) file.length()];
                    // bytes = dis.read(bufferOut);
                    // out.write(bufferOut, 0, bytes);

                    long sendLen = 0;

                    int len = -1;
                    byte[] buffer = new byte[512 * 1024]; // 512KB
                    while ((len = dis.read(buffer)) != -1) {
                        out.write(buffer, 0, len);

                        if (progressHandler != null) {
                            sendLen += len;
                            progressHandler.progress(20 + (int)(75 * ((float)sendLen / (float)fileLength)) );
                        }
                    }

                    dis.close();
                }

            }


            // 4. 写boundary结尾
            String endBoundary = "\r\n--" + boundary + "--\r\n";
            out.write(endBoundary.getBytes("utf-8"));
            out.flush();
            out.close();

            // 5. 获得服务器的响应结果和状态码
            int responseCode = connection.getResponseCode();
            InputStream inputStream = connection.getInputStream();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[1 * 1024 * 1024]; // 1M
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            byte[] responseData = outputStream.toByteArray();

            // pls handler in responseHandler
            //if (progressHandler != null) {
            //    progressHandler.progress(100);
            //}

            if (responseHandler != null) {
                responseHandler.callback(null, responseCode, responseData);
            }

            return responseData;

        } catch (Exception e) {
            e.printStackTrace();

            if (responseHandler != null) {
                responseHandler.callback(e, 0, null);
            }

            throw e;
        }
    }


}
