package it.giuliatesta.cctproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.http.HttpServlet;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ServletRegistrationBean<HttpServlet> servlet() {
		System.out.println("[Application] ServletRegistrationBean");
		return new ServletRegistrationBean<HttpServlet>(new Servlet(), "/");
	}

	@Bean
	public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
		FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new RateLimitingFilter());
		registrationBean.setOrder(0);
		System.out.println("[Application] Registration of RateLimitingFilter");
		return registrationBean;
	}

	@Bean
	public FilterRegistrationBean<AuthFilter> authorizationFilter() {
		FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new AuthFilter());
		System.out.println("[Application] Registration of AuthFilter");
		return registrationBean;
	}
}
