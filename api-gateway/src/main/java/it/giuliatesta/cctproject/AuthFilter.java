package it.giuliatesta.cctproject;

import java.io.IOException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(filterName = "Custom web filter to menage authorization")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;
        var validation = Auth.validate(httpRequest.getHeader("Authorization"), httpRequest.getMethod());
        switch (validation) {
            case INVALID_TOKEN:
                // Token is invalid, return unauthorized response
                System.out.println("[Auth Filter] Request rejected due to invalid token");
                httpResponse.getWriter().write("Invalid token");
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                break;
            case INVALID_PERMISSIONS:
                // Token doesn't have the correct permissions for this type of request.
                System.out.println("[Auth Filter] Request rejected due to insufficient permissions");
                httpResponse.getWriter().write("Insufficient permissions");
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            case AUTHORIZED:
                // Token is valid, proceed with the chain
                System.out.println("[Auth Filter] Request validated");
                chain.doFilter(request, response);
        }
    }
}

enum AuthResponse {
    AUTHORIZED, INVALID_TOKEN, INVALID_PERMISSIONS
}

class Auth {
    public static AuthResponse validate(Object authorizationToken, String method) {
        try {
            if (authorizationToken == null) {
                System.out.println("[Auth] Authorization header not found. Request rejected");
                return AuthResponse.INVALID_TOKEN;
            }

            String strToken = authorizationToken.toString();
            if (!strToken.startsWith("bearer ")) {
                System.out.println("[Auth] Invalid authorization header format. Request rejected");
                return AuthResponse.INVALID_TOKEN;
            }
            // remove the "bearer " prefix
            strToken = strToken.substring(7);

            // HS256 (HMAC with SHA-256): same key for generating and validating.
            Algorithm algorithm = Algorithm.HMAC256(getSecret());
            var decoded = JWT.require(algorithm).build().verify(strToken);
            var role = decoded.getClaim("role").asString();

            // admin can do anything
            // everybody else can only GET, HEAD and OPTIONS
            if (!"admin".equals(role) && !allowedMethod(method)) {
                return AuthResponse.INVALID_PERMISSIONS;
            }
            return AuthResponse.AUTHORIZED;
        } catch (Exception e) {
            System.out.println("[Auth] Error while validating jwt: " + e);
            return AuthResponse.INVALID_TOKEN;
        }
    }

    private static String getSecret() {
        try {
            var secret = PropertyLoader.getInstance().get("secret-key");
            if (secret == null) {
                throw new PropertyNotFoundException("'secret-key' property not found or ill written.");
            }
            return secret;
        } catch (Exception e) {
            System.out.println("[Auth] Error while loading secret key: " + e);
            return null;
        }
    }

    private static boolean allowedMethod(String method) {
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }
}
