package it.giuliatesta.cctproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServlet;

@SpringBootApplication
public class CctProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(CctProjectApplication.class, args);
	}

	@Bean
	public ServletRegistrationBean<HttpServlet> servletRegistrationBean() {
		return new ServletRegistrationBean<HttpServlet>(new CctServlet(new RestTemplate()), "/");
	}

}
