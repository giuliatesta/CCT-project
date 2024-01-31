package it.giuliatesta.cctproject;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

// TODO make a static generic getInstance to make a singleton
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String token = request.getAttribute("Authorization").toString();
        System.out.println("DO FILTER");
        if (Auth.validate(token)) {
            System.out.println("VALIDATED");
            // Token is valid, proceed with the chain
            chain.doFilter(request, response);
        } else {
            System.out.println("NOT VALIDATED");
            // Token is invalid, return unauthorized response
            response.getWriter().write("Invalid token");
            // TODO casting works?
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}

class Auth {

    private static String getSecret() {
        try {
            return PropertyLoader.getInstance().get("secrey-key");
        } catch (Exception e) {
            System.out.println("Error while loading secret key: " + e);
            throw e;
        }
    }

    public static boolean validate(String authorizationToken) {
        try {
            // HS256 (HMAC with SHA-256) : same key for generating and validating.
            // TODO think about using asymmetric algorithms (like RS256)
            Algorithm algorithm = Algorithm.HMAC256(getSecret());
            JWT.require(algorithm).build().verify(authorizationToken);
            return true;
        } catch (Exception e) {
            System.out.println("Error while validating jwt: " + e);
            return false;
        }
    }

    public static String generateToken(String microservice) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(getSecret());
            return JWT.create()
                    .withAudience(microservice)
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(getExpirationDate())
                    .withClaim("role", "admin")
                    .withArrayClaim("permissions", new String[] { "read", "write" })
                    .sign(algorithm);
        } catch (Exception e) {
            System.out.println("Error while generating token: " + e);
            return null;
        }
    }

    private static Date getExpirationDate() {
        int secondsToAdd = 86400; // seconds in a day
        // default expiration date = one day
        try {
            secondsToAdd *= (Integer.parseInt(PropertyLoader.getInstance().get("expiration_time_in_days")));
        } catch (Exception e) {
            System.out.println("Error while loading expiration time key: " + e);
            System.out.println("Using default expiration date");
        }
        return Date.from(Instant.now().plusSeconds(secondsToAdd));
    }
}
