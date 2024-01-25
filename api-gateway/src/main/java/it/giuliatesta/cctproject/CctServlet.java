package it.giuliatesta.cctproject;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.web.client.RestTemplate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "gateway", description = "Custom API gateway", urlPatterns = { "/" })
public class CctServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("doGet: " + req);
        try {
            String url = "https://microservice:9080/json";
            PrintWriter out = resp.getWriter();
            RestTemplate restTemplate = new RestTemplate();
            var result = restTemplate.getForEntity(url, String.class);
            out.println(result.toString());
        } catch (Exception e) {
            e.toString();
        }
    }

}