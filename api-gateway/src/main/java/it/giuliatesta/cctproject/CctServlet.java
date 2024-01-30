package it.giuliatesta.cctproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "gateway", description = "Custom API gateway")
public class CctServlet extends HttpServlet {

    static final int NUMBER_OF_SERVICES = 2;

    private final RestTemplate restTemplate;

    public CctServlet(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String endpoint = getEndPointFromRequest(req);
        if ("".equals(endpoint) || "/".equals(endpoint)) {
            resp.getWriter().println("Invalid request");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        call(endpoint, randomlyChooseMicroservice(), resp);
    }

    private void call(String endpoint, String microservice, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        // TODO headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer your_token");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            String url = composeUrl(endpoint, microservice);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                    entity, String.class);

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
        CctConfiguration config = CctConfiguration.getInstance();
        String microserviceUrl = config.get(microservice);
        // there are multiple urls available. Randomly choosing one
        // TODO round robin to choose which one to use
        if (microserviceUrl.contains(",")) {
            var splits = microserviceUrl.split(",");
            var random = new Random();
            microserviceUrl = splits[random.nextInt(splits.length - 1)];
        }
        // if the microservice requested is not supported
        if (microserviceUrl == null || microserviceUrl.isEmpty()) {
            throw new PropertyNotFoundException();
        }

        if (!microserviceUrl.endsWith("/")) {
            microserviceUrl += "/";
        }

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