package it.giuliatesta.cctproject;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "gateway", description = "Custom API gateway")
public class Servlet extends HttpServlet {

    static final int NUMBER_OF_SERVICES = 6;

    private final Service service;

    public Servlet(RestTemplate restTemplate) {
        this.service = new Service(restTemplate);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service.send(req, resp, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service.send(req, resp, HttpMethod.POST);
    }
}