package common.modules.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class HttpsWorker {

    public static class TrustAnyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    public static class TrustAnyHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }


}
