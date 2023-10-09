import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class AsyncHttpClientExample {
    public static void main(String[] args) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // First API Call
        HttpGet request1 = new HttpGet("https://example.com/api/resource1");
        httpclient.execute(request1, new FutureCallback<CloseableHttpResponse>() {
            @Override
            public void completed(CloseableHttpResponse response1) {
                try {
                    // Process response1

                    // Close response1 when you're done with it
                    response1.close();

                    // Second API Call (dependent on response1)
                    HttpGet request2 = new HttpGet("https://example.com/api/resource2");
                    httpclient.execute(request2, new FutureCallback<CloseableHttpResponse>() {
                        @Override
                        public void completed(CloseableHttpResponse response2) {
                            try {
                                // Process response2

                                // Close response2 when you're done with it
                                response2.close();

                                // Third API Call (dependent on response2)
                                HttpGet request3 = new HttpGet("https://example.com/api/resource3");
                                httpclient.execute(request3, new FutureCallback<CloseableHttpResponse>() {
                                    @Override
                                    public void completed(CloseableHttpResponse response3) {
                                        try {
                                            // Process response3

                                            // Close response3 when you're done with it
                                            response3.close();

                                            // Fourth API Call (dependent on response3)
                                            HttpGet request4 = new HttpGet("https://example.com/api/resource4");
                                            httpclient.execute(request4, new FutureCallback<CloseableHttpResponse>() {
                                                @Override
                                                public void completed(CloseableHttpResponse response4) {
                                                    try {
                                                        // Process response4

                                                        // Close response4 when you're done with it
                                                        response4.close();

                                                        // All API calls completed, you can now handle the final result
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
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
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void failed(Exception ex) {
                                        // Handle errors for the third API call
                                    }

                                    @Override
                                    public void cancelled() {
                                       
