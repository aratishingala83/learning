import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

public class SSLContextHelper {

    public static SSLContext createSSLContext(String keystorePath, String keystorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream keyStoreInputStream = new FileInputStream(keystorePath)) {
            keyStore.load(keyStoreInputStream, keystorePassword.toCharArray());
        }

        // Create and initialize KeyManagerFactory with the same keystore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

        // Create and initialize TrustManagerFactory with the same keystore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Create SSLContext with the same keystore for both key material and trust material
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }
}




static class ResponseInterceptor implements org.springframework.http.client.ClientHttpResponseInterceptor {
        private final CookieStore cookieStore;

        public ResponseInterceptor(CookieStore cookieStore) {
            this.cookieStore = cookieStore;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
        }

        @Override
        public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            // Intercept the response
            ClientHttpResponse response = execution.execute(request, body);
            // Extract and update cookies from the response
            cookieStore.addCookie(new BasicHttpClientCookie("example_cookie", "example_value")); // Replace with actual cookie extraction logic
            return response;
        }
    }
