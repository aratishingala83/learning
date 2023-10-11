














import model.LoginRequest;
import model.response.DocumentInstanceCreationResponse;
import model.response.SearchResponse;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class NonSpringBean {

    public static final String X_MSTR_AUTH_TOKEN = "X-MSTR-AuthToken";
    public static final String X_MSTR_PROJECT_ID = "X-MSTR-ProjectID";
    public static final String COOKIE = "Cookie";
    private RestTemplate restTemplate = null;
    private String documentId = null;
    private String documentInstanceId = null;

    private String authToken = null;

    private String jSessionId = null;

    private Map<String, String> cookies = new HashMap<>(); // Define the cookies field here

    NonSpringBean(){
        this.setRestTemplate(this.createRestTemplate());
    }

    public static void main(String[] args) throws InterruptedException {

        try {
            NonSpringBean client = new NonSpringBean();
            client.login();
            client.searchResultForName();
            client.createDocInstance();
            client.exportToExcel();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private void searchResultForName() {

        String url = "https://demo.microstrategy.com/MicroStrategyLibrary/api/searches/results?name=Regional Sales Overview"; // Replace with your REST API URL
        System.out.println("searchResultForName "+getAuthToken());
        setHeadersForAuthAndProjIdWithCookies();
        ResponseEntity<SearchResponse> response = getRestTemplate().exchange(
                url,
                HttpMethod.GET,
                null, // Request entity (request body), which is null in this example
                SearchResponse.class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("searchResultForName have is2xxSuccessful true");
            SearchResponse searchResponse = response.getBody();
            System.out.println("getTotalItems = "+searchResponse.getTotalItems());
            this.setDocumentId(searchResponse.getResult().get(0).getId());
            System.out.println("searchResultForName success....");
        } else {
            System.out.println("searchResultForName Failed.............");
        }
    }

    private void createDocInstance() {
        String url = "https://demo.microstrategy.com/MicroStrategyLibrary/api/documents/%s/instances"; // Replace with your REST API URL
        url = String.format(url, this.getDocumentId());
        /*setHeaders(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        setHeaders(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);*/
        setHeadersForAuthAndProjIdWithCookies();
        HttpEntity<String> entity = new HttpEntity<>(null, getHttpHeaders(getAuthToken()));
        ResponseEntity<DocumentInstanceCreationResponse> response = getRestTemplate().exchange(url, HttpMethod.POST, entity, DocumentInstanceCreationResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("createDocInstance have is2xxSuccessful true");
            DocumentInstanceCreationResponse creationResponse = response.getBody();
            System.out.println("creationResponse getMid= "+creationResponse.getMid());
            this.setDocumentInstanceId(creationResponse.getMid());
            System.out.println("creationResponse success....");
        } else {
            System.out.println("creationResponse Failed.............");
        }
    }

    private void exportToExcel() {
        String url = "https://demo.microstrategy.com/MicroStrategyLibrary/api/documents/%s/instances/%s/excel"; // Replace with your REST API URL
        url = String.format(url, getDocumentId(), getDocumentInstanceId());
        HttpEntity<String> entity = new HttpEntity<>(null, getHttpHeaders(getAuthToken()));
        ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("exportToExcel have is2xxSuccessful true");
            System.out.println(response.getBody());

        }
    }

    private void setHeadersForAuthAndProjIdWithCookies() {

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().addIfAbsent(X_MSTR_AUTH_TOKEN, getAuthToken());
            request.getHeaders().addIfAbsent(X_MSTR_PROJECT_ID, "B7CA92F04B9FAE8D941C3E9B7E0CD754");
            return execution.execute(request, body);
        });
        if (cookies != null && !cookies.isEmpty()) {
            // Add cookies to the request if they exist
            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().addIfAbsent(COOKIE, buildCookieHeader());
                return execution.execute(request, body);
            });
        }

    }

    private void setHeaders(String key, String value) {
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add(key, value);
            return execution.execute(request, body);
        });
    }

    private HttpHeaders getHttpHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_MSTR_AUTH_TOKEN, authToken);
        headers.add(X_MSTR_PROJECT_ID, "B7CA92F04B9FAE8D941C3E9B7E0CD754");
        headers.add(HttpHeaders.ACCEPT, "*/*");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (cookies != null && !cookies.isEmpty()) {
            headers.add(COOKIE, buildCookieHeader());
        }
        System.out.println(headers);
        return headers;
    }



    private String buildCookieHeader() {
        StringBuilder cookieHeader = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            if (cookieHeader.length() > 0) {
                cookieHeader.append("; ");
            }
            cookieHeader.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return cookieHeader.toString();
    }


    public void login() {


        String url = "https://demo.microstrategy.com/MicroStrategyLibrary/api/auth/login"; // Replace with your REST API URL
        LoginRequest loginRequest = getLoginRequest();
        ResponseEntity<String> response = getRestTemplate().postForEntity(url, loginRequest, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {

            Optional<String> jsessionId = response.getHeaders().get("Set-Cookie").stream()
                    .filter(s -> s.contains("JSESSIONID"))
                    .findFirst();
            jsessionId.ifPresent(s -> {
                String[] split = s.split(";");
                String[] jSessionInfo = split[0].split(":");
                String[] jSessionKV = jSessionInfo[0].split("=");
                this.setjSessionId(jSessionKV[1]);
                System.out.println("jSessionId = "+this.getjSessionId());
            });
            this.setAuthToken(response.getHeaders().get("X-MSTR-AuthToken").get(0));
            System.out.println(this.getAuthToken());
            setCookies(response.getHeaders());

        }
    }

    public void setCookies(HttpHeaders responseHeaders) {
        List<String> setCookieHeaders = responseHeaders.get("Set-Cookie");
        if (setCookieHeaders != null) {
            for (String setCookieHeader : setCookieHeaders) {
                // Parse each Set-Cookie header and add the cookies to your map
                String[] cookieParts = setCookieHeader.split(";");
                if (cookieParts.length > 0) {
                    String[] keyValue = cookieParts[0].trim().split("=");
                    if (keyValue.length == 2) {
                        cookies.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }
    }

    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    private LoginRequest getLoginRequest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginMode(8);
        loginRequest.setUsername("guest");
        loginRequest.setPassword("");
        return loginRequest;
    }

    public synchronized RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentInstanceId() {
        return documentInstanceId;
    }

    public void setDocumentInstanceId(String documentInstanceId) {
        this.documentInstanceId = documentInstanceId;
    }

    public synchronized String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getjSessionId() {
        return jSessionId;
    }

    public void setjSessionId(String jSessionId) {
        this.jSessionId = jSessionId;
    }
}

