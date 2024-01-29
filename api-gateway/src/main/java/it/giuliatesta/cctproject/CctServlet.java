package it.giuliatesta.cctproject;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "gateway", description = "Custom API gateway", urlPatterns = { "/" })
public class CctServlet extends HttpServlet {

    private final RestTemplate restTemplate;

    public CctServlet(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("doGet: " + req);
        PrintWriter out = resp.getWriter();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer your_token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("http://httpbin:80/robots.txt", HttpMethod.GET,
                entity, String.class);

        // Process the response
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            // Process the response body as needed
            out.println("Response from microservice: " + responseBody);
        } else {
            // Handle error cases
            out.println("Error calling microservice. Status code: " + response.getStatusCode());
        }
    }

}