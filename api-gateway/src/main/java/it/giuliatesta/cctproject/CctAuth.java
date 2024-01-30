package it.giuliatesta.cctproject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;

// TODO make a static generic getInstance to make a singleton
public class CctAuth {

    // TODO validate authorization and maybe auhthentication
    public boolean validate(HttpServletRequest req) {
        return true;
    }

    // TODO prepare entity with required headers for authorization
    public <T> HttpEntity<T> prepare(Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer your_token");
        HttpEntity<T> entity = new HttpEntity<T>(headers);
        return entity;
    }

}
