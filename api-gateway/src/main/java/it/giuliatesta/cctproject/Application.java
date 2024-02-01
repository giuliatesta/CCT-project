package it.giuliatesta.cctproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServlet;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ServletRegistrationBean<HttpServlet> servlet() {
		System.out.println("[Application] ServletRegistrationBean");
		return new ServletRegistrationBean<HttpServlet>(new Servlet(new RestTemplate()), "/");
	}

	@Bean
	public FilterRegistrationBean<AuthFilter> filter() {
		FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new AuthFilter());
		System.out.println("[Application] FilterRegistrationBean");
		return registrationBean;
	}
}
