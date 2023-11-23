=============
===============
================


import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

public class TokenRefreshRestTemplate extends RestTemplate {

    private TokenProvider tokenProvider;
    private HttpRequest originalRequest;

    public TokenRefreshRestTemplate(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.setErrorHandler(new TokenRefreshErrorHandler());
        this.setInterceptors(List.of(new TokenInterceptor()));
    }

    public void setOriginalRequest(HttpRequest originalRequest) {
        this.originalRequest = originalRequest;
    }

    private class TokenInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            // Set the Authorization header with the current token
            request.getHeaders().setBearerAuth(tokenProvider.getToken());
            return execution.execute(request, body);
        }
    }

    private class TokenRefreshErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.getStatusCode().value() == 401) {
                // Refresh the token
                tokenProvider.refreshToken();

                // Retry the request with the new token
                retryRequestWithNewToken(response);
            } else {
                throw new RuntimeException("Unhandled error: " + response.getStatusCode());
            }
        }

        private void retryRequestWithNewToken(ClientHttpResponse response) throws IOException {
            // Set the new token in the request header
            originalRequest.getHeaders().setBearerAuth(tokenProvider.getToken());

            // Retry the request with the new token
            execute(originalRequest.getMethod(), originalRequest.getURI(), originalRequestCallback,
                    originalRequest.getResponseExtractor());
        }
    }

    // Rest of your TokenProvider and other methods...
}
