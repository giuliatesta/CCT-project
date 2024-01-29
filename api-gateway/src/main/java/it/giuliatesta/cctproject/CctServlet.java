package it.giuliatesta.cctproject;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException.NotImplemented;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "gateway", description = "Custom API gateway")
public class CctServlet extends HttpServlet {

    private final RestTemplate restTemplate;

    public CctServlet(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String endpoint = getEndPointFromRequest(req);
        switch (endpoint) {
            case "json":
                call(0, resp);
                break;
            case "html":
                call(1, resp);
            default:
                // not handled
                resp.setStatus(404);
        }

    }

    private void call(int microservice, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        // TODO headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer your_token");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(getUrl(microservice), HttpMethod.GET,
                    entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                out.println("Response from microservice" + microservice + ": " + responseBody);
            } else {
                throw new Exception("error calling microservice: " + response.getStatusCode());
            }
        } catch (Exception e) {
            resp.setStatus(400);
            out.println("Error: " + e.getMessage());
        }
    }

    // TODO validate correct routing httpbin/json/ vs json/
    private String getEndPointFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        var splits = requestURI.split("/");
        return splits[splits.length - 1];
    }

    private String getUrl(int microservice) {
        switch (microservice) {
            case 0:
                return "http://httpbin1:80/json";
            case 1:
                return "http://httpbin2:80/html";
            default:
                throw new IllegalArgumentException();
        }
    }
}