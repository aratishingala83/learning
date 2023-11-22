import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class CustomRetryExample {

    public static void main(String[] args) {
        // Your initial headers with the current token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer YOUR_INITIAL_TOKEN");

        // Your API endpoint URL
        String url = "YOUR_API_ENDPOINT_URL";

        // Create a custom RestTemplate with retry logic
        RestTemplate restTemplate = createCustomRestTemplate(headers, url);

        // Perform a request using the custom RestTemplate
        try {
            String response = restTemplate.execute(
                    url,
                    HttpMethod.POST,
                    getRequestCallback(),
                    getResponseExtractor()
            );
            System.out.println("Response: " + response);
            // Process the response as needed
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            // Handle exceptions (e.g., token expiration)
        }
    }

    private static RestTemplate createCustomRestTemplate(HttpHeaders headers, String url) {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Set the retry policy
        CustomRetryPolicy retryPolicy = new CustomRetryPolicy();
        retryTemplate.setRetryPolicy(retryPolicy);

        RestTemplate restTemplate = new RestTemplate();

        // Add the custom interceptor to manage headers and retries
        restTemplate.getInterceptors().add(new CustomInterceptor(headers, url, retryTemplate));

        return restTemplate;
    }

    private static RequestCallback getRequestCallback() {
        // Implement your request callback logic if needed
        return request -> {
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        };
    }

    private static ResponseExtractor<String> getResponseExtractor() {
        // Implement your response extractor logic if needed
        return response -> {
            // Process the response
            // Here, we are assuming the response is a String; adjust as per your use case
            return "Processed Response: " + response.getBody();
        };
    }

    private static class CustomInterceptor implements ClientHttpRequestInterceptor {
        private final HttpHeaders headers;
        private final String url;
        private final RetryTemplate retryTemplate;

        public CustomInterceptor(HttpHeaders headers, String url, RetryTemplate retryTemplate) {
            this.headers = headers;
            this.url = url;
            this.retryTemplate = retryTemplate;
        }

        @Override
        public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            try {
                return retryTemplate.execute((RetryCallback<ClientHttpResponse, HttpClientErrorException>) context -> {
                    // Update the token in the headers before retrying
                    updateTokenInHeaders();

                    // Perform the actual request with the updated headers
                    return execution.execute(request, body);
                });
            } catch (Exception e) {
                // Handle exceptions if needed
                throw new IOException("Max retries reached", e);
            }
        }

        private void updateTokenInHeaders() {
            // Logic to refresh the token and update it in the headers
            String newToken = "YOUR_LOGIC_TO_OBTAIN_NEW_TOKEN";
            headers.set("Authorization", "Bearer " + newToken);
        }
    }

    private static class CustomRetryPolicy implements org.springframework.retry.RetryPolicy {
        private int maxAttempts = 3;

        @Override
        public boolean canRetry(RetryContext context) {
            // Customize the conditions for retrying, e.g., based on exception type
            return context.getLastThrowable() instanceof TokenExpiredException &&
                    context.getRetryCount() < maxAttempts;
        }

        @Override
        public RetryContext open(RetryContext parent) {
            return new DefaultRetryContext(parent);
        }

        @Override
        public void close(RetryContext context) {
            // Clean-up or additional processing after retries
        }

        @Override
        public void registerThrowable(RetryContext context, Throwable throwable) {
            // Optional: Track or log the thrown exception during retry
        }

        private static class DefaultRetryContext implements RetryContext {
            private final RetryContext parent;

            public DefaultRetryContext(RetryContext parent) {
                this.parent = parent;
            }

            @Override
            public int getRetryCount() {
                return parent != null ? parent.getRetryCount() : 0;
            }

            @Override
            public Throwable getLastThrowable() {
                return parent != null ? parent.getLastThrowable() : null;
            }
        }
    }
}
