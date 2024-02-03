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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// TODO make a static generic getInstance to make a singleton
@WebFilter(filterName = "Custom web filter to menage authorization")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var token = ((HttpServletRequest) request).getHeader("Authorization");
        if (Auth.validate(token)) {
            System.out.println("[Auth Filter] Request validated");
            // Token is valid, proceed with the chain
            chain.doFilter(request, response);
        } else {
            System.out.println("[Auth Filter] Request rejected");
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
            var secret = PropertyLoader.getInstance().get("secret-key");
            if (secret == null) {
                throw new Exception("'secret-key' property not found or ill written.");
            }
            return secret;
        } catch (Exception e) {
            System.out.println("[Auth] Error while loading secret key: " + e);
            return null;
        }
    }

    public static boolean validate(Object authorizationToken) {
        try {
            if (authorizationToken == null) {
                System.out.println("[Auth] Authorization header not found. Request rejected");
                return false;
            }

            String strToken = authorizationToken.toString();
            if (!strToken.startsWith("bearer ")) {
                System.out.println("[Auth] Invalid authorization header format. Request rejected");
                return false;
            }
            // remove the "bearer " prefix
            strToken = strToken.substring(7);

            // HS256 (HMAC with SHA-256): same key for generating and validating.
            // TODO think about using asymmetric algorithms (like RS256)
            Algorithm algorithm = Algorithm.HMAC256(getSecret());
            JWT.require(algorithm).build().verify(strToken);
            return true;
        } catch (Exception e) {
            System.out.println("[Auth] Error while validating jwt: " + e);
            return false;
        }
    }

    // usually gateways don't send request to microservices with authorization
    // headers
    // once the request is valided by the gateway there is no need to check it
    // again.

    // TODO add AUDIENCE
    public static String generateToken() {
        try {
            Algorithm algorithm = Algorithm.HMAC256(getSecret());
            var token = JWT.create()
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(getExpirationDate())
                    .withClaim("role", "admin")
                    .withArrayClaim("permissions", new String[] { "read", "write" })
                    .sign(algorithm);
            System.out.println("[Auth]: Authorization header successfully created");
            return token;
        } catch (Exception e) {
            System.out.println("[Auth] Error while generating token: " + e);
            return null;
        }
    }

    private static Date getExpirationDate() {
        int secondsToAdd = 86400; // seconds in a day
        // default expiration date = one day
        try {
            secondsToAdd *= (Integer.parseInt(PropertyLoader.getInstance().get("expiration_time_in_days")));
        } catch (Exception e) {
            System.out.println("[Auth] Error while loading expiration time key: " + e);
            System.out.println("[Auth] Using default expiration date");
        }
        return Date.from(Instant.now().plusSeconds(secondsToAdd));
    }
}
