package it.giuliatesta.cctproject;

import java.io.PrintWriter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;

public class Service extends RetryingLoadBalancer {

    private final RestTemplate restTemplate;

    public Service(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected void call(HttpServletResponse resp, String url, HttpMethod method) throws Exception {
        PrintWriter out = resp.getWriter();
        System.out.println("[ServiceClient] Calling " + url);
        // makes the call to the microservice
        if (url != null) {
            ResponseEntity<String> response = restTemplate.exchange(url, method,
                    new HttpEntity<String>(
                            new HttpHeaders()),
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                System.out.println("[Service Client] Success!");
                out.println("Response from microservice:\n" + responseBody);
            } else {
                throw new Exception("Error calling microservice: " + response.getStatusCode());
            }
        }
    }
}
