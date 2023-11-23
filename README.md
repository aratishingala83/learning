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
            
            // Set the new token in the request header
            response.getRequest().getHeaders().setBearerAuth(tokenProvider.getToken());
            
            // Set cookies from the original request to the new request
            response.getRequest().getHeaders().put(HttpHeaders.COOKIE,
                    response.getHeaders().get(HttpHeaders.SET_COOKIE));

            // Retry the request with the new token and cookies
            this.getInterceptors().get(0).intercept(response.getRequest(), response.getBody(),
                    (org.springframework.http.client.ClientHttpRequestExecution) (request, body) -> {
                        return execution.execute(request, body);
                    });
        } else {
            throw new RuntimeException("Unhandled error: " + response.getStatusCode());
        }
    }
}



===
response.getRequest().getHeaders().put(HttpHeaders.COOKIE,
        response.getHeaders().get(HttpHeaders.SET_COOKIE));



