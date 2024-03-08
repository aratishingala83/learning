import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class Main {

    public static void main(String[] args) throws Exception {
        SSLContext sslContext = createSSLContext("path/to/your/keystore.jks", "your_keystore_password");

        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory(sslContext));

        // Use the RestTemplate to make HTTPS calls
        String url = "https://example.com/api/endpoint";
        String response = restTemplate.getForObject(url, String.class);
        System.out.println("Response: " + response);
    }

    private static SSLContext createSSLContext(String keystorePath, String keystorePassword) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreInputStream = new FileInputStream(keystorePath)) {
            keyStore.load(keyStoreInputStream, keystorePassword.toCharArray());
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new SecureRandom());
        return sslContext;
    }

    private static ClientHttpRequestFactory getClientHttpRequestFactory(SSLContext sslContext) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setSslContext(sslContext);
        return factory;
    }
}
