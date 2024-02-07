package it.giuliatesta.cctproject;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("INTERCEPTED REQUEST:" + ((HttpServletRequest) request).getRequestURI());
        chain.doFilter(request, response);
        System.out.println("RESPONSE STATUS:" + ((HttpServletResponse) response).getStatus());
    }

}
