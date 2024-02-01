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

    private final LoadBalancer balancer = new LoadBalancer();
    private final RestTemplate restTemplate;

    public ServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected void call(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String microservice)
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        try {
            String endpoint = getEndPointFromRequest(req);
            String url = composeUrl(endpoint, microservice);
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

    // TODO check @NonNull decorator
    @NonNull
    private String composeUrl(String endpoint, String microservice) {
        var microserviceUrl = balancer.getMicroserviceUrl(microservice);
        return microserviceUrl + endpoint;
    }

    // TODO validate correct routing httpbin/json/ vs json/
    private String getEndPointFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        var splits = requestURI.split("/");
        return splits[splits.length - 1];
    }
}
