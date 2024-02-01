package it.giuliatesta.cctproject;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServiceClient extends RetryPolicy {

    private final RestTemplate restTemplate;

    public ServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected void call(HttpServletResponse resp, String url, HttpMethod method, String microservice)
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        try {
            System.out.println("[ServiceClient] Calling " + url);
            ResponseEntity<String> response = restTemplate.exchange(url, method,
                    prepare(String.class, microservice),
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                out.println("Response from microservice" + microservice + ":\n" + responseBody);
            } else {
                throw new Exception("Error calling microservice: " + response.getStatusCode());
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
