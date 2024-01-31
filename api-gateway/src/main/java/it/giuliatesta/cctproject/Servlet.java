package it.giuliatesta.cctproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "gateway", description = "Custom API gateway")
public class Servlet extends HttpServlet {

    static final int NUMBER_OF_SERVICES = 2;

    private final ServiceClient client;

    public Servlet(RestTemplate restTemplate) {
        this.client = new ServiceClient(restTemplate);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        client.send(req, resp, HttpMethod.GET, randomlyChooseMicroservice());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        client.send(req, resp, HttpMethod.POST, randomlyChooseMicroservice());
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