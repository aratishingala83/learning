# learning
learning


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class AsyncHttpClientExample {
    public static void main(String[] args) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // First API Call
        HttpGet request1 = new HttpGet("https://example.com/api/resource1");
        httpclient.execute(request1, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response1) {
                // Process response1

                // Second API Call (dependent on response1)
                HttpGet request2 = new HttpGet("https://example.com/api/resource2");
                httpclient.execute(request2, new FutureCallback<HttpResponse>() {
                    @Override
                    public void completed(HttpResponse response2) {
                        // Process response2

                        // Third API Call (dependent on response2)
                        HttpGet request3 = new HttpGet("https://example.com/api/resource3");
                        httpclient.execute(request3, new FutureCallback<HttpResponse>() {
                            @Override
                            public void completed(HttpResponse response3) {
                                // Process response3

                                // Fourth API Call (dependent on response3)
                                HttpGet request4 = new HttpGet("https://example.com/api/resource4");
                                httpclient.execute(request4, new FutureCallback<HttpResponse>() {
                                    @Override
                                    public void completed(HttpResponse response4) {
                                        // Process response4

                                        // All API calls completed, you can now handle the final result
                                    }

                                    @Override
                                    public void failed(Exception ex) {
                                        // Handle errors for the fourth API call
                                    }

                                    @Override
                                    public void cancelled() {
                                        // Handle cancellation for the fourth API call
                                    }
                                });
                            }

                            @Override
                            public void failed(Exception ex) {
                                // Handle errors for the third API call
                            }

                            @Override
                            public void cancelled() {
                                // Handle cancellation for the third API call
                            }
                        });
                    }

                    @Override
                    public void failed(Exception ex) {
                        // Handle errors for the second API call
                    }

                    @Override
                    public void cancelled() {
                        // Handle cancellation for the second API call
                    }
                });
            }

            @Override
            public void failed(Exception ex) {
                // Handle errors for the first API call
            }

            @Override
            public void cancelled() {
                // Handle cancellation for the first API call
            }
        });

        // Do other work here while waiting for API calls to complete asynchronously

        // Close the HttpClient when you're done
        httpclient.close();
    }
}

