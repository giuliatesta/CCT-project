package it.giuliatesta.cctproject;

import java.io.PrintWriter;
import java.util.Objects;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;

public class Router extends RetryingLoadBalancer {

    private final RestTemplate restTemplate = new RestTemplate();

    protected void call(HttpServletResponse resp, String url, HttpMethod method) throws Exception {
        Objects.requireNonNull(resp);
        Objects.requireNonNull(url);
        Objects.requireNonNull(method);

        PrintWriter out = resp.getWriter();
        System.out.println("[ServiceClient] Calling " + url);
        // makes the call to the microservice
        ResponseEntity<String> response = restTemplate.exchange(url, method,
                new HttpEntity<String>(
                        new HttpHeaders()),
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            System.out.println("[Service Client] Success!");
            out.println("Called url:" + url + "\nResponse:\n" + responseBody);
        } else {
            throw new Exception("Error calling microservice: " + response.getStatusCode());
        }

    }
}
