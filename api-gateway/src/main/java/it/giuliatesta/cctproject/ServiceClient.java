package it.giuliatesta.cctproject;

import java.io.PrintWriter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;

public class ServiceClient extends RetryingLoadBalancer {

    private final RestTemplate restTemplate;

    public ServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected void call(HttpServletResponse resp, String url, HttpMethod method, String microservice)
            throws Exception {
        PrintWriter out = resp.getWriter();
        System.out.println("[ServiceClient] Calling " + url);
        // makes the call to the microservice
        ResponseEntity<String> response = restTemplate.exchange(url, method,
                prepare(String.class, microservice),
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            System.out.println("Success!");
            out.println("Response from microservice " + microservice + ":\n" + responseBody);
        } else {
            throw new Exception("Error calling microservice: " + response.getStatusCode());
        }
    }

    // TODO prepare entity with required headers for authorization
    public <T> HttpEntity<T> prepare(Class<T> type, String microservice) {
        HttpHeaders headers = new HttpHeaders();
        String token = Auth.generateToken(microservice);
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<T> entity = new HttpEntity<T>(headers);
        return entity;
    }

}
