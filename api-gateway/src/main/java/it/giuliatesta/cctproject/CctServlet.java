package it.giuliatesta.cctproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "gateway", description = "Custom API gateway")
public class CctServlet extends HttpServlet {

    static final int NUMBER_OF_SERVICES = 2;

    private final RestTemplate restTemplate;
    private final CctAuth authService = new CctAuth();
    private final CctLoadBalancer balancer = new CctLoadBalancer();

    public CctServlet(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (authService.validate(req)) {
            String endpoint = getEndPointFromRequest(req);
            call(endpoint, randomlyChooseMicroservice(), resp);
        }
    }

    private void call(String endpoint, String microservice, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        // TODO headers

        try {
            String url = composeUrl(endpoint, microservice);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                    authService.prepare(String.class), String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                out.println("Response from microservice" + microservice + ": " + responseBody);
            } else {
                throw new Exception("Error calling microservice: " + response.getStatusCode());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // TODO validate correct routing httpbin/json/ vs json/
    private String getEndPointFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        var splits = requestURI.split("/");
        return splits[splits.length - 1];
    }

    private String composeUrl(String endpoint, String microservice) {
        var microserviceUrl = balancer.getMicroserviceUrl(microservice);
        return microserviceUrl + endpoint;
    }

    private String randomlyChooseMicroservice() {
        var random = new Random();
        if (random.nextInt(NUMBER_OF_SERVICES - 1) == 1) {
            return "httpbin1";
        } else {
            return "httpbin2";
        }
    }
}