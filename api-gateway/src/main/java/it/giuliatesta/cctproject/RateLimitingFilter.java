package it.giuliatesta.cctproject;

import java.io.IOException;
import java.util.HashMap;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

// implements the token bucket rate limiting algorithm
@WebFilter(filterName = "Custom filter to menage rate limiting")
public class RateLimitingFilter implements Filter {

    private static final int BUCKET_CAPACITY = 10;
    private static final long REFILL_RATE = 1000;

    private static final HashMap<String, Long> refillTimes = new HashMap<>();
    private static final HashMap<String, Integer> bucket = new HashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String address = request.getRemoteAddr();
        refill(address, System.currentTimeMillis());

        // checks if a token is available
        if (tryConsume(address)) {
            // continues with request evaluation
            System.out.println("[Rate Limiting Filter] request accepted.");
            chain.doFilter(request, response);
        } else {
            // stops and return 429 (too many requests)
            response.getWriter().write("Too many requests: rate limit exceeded.\n");
            ((HttpServletResponse) response).setStatus(429);
        }
    }

    // refills the bucket if enough time has passed
    private void refill(String address, long currentTime) {
        if (!refillTimes.containsKey(address)) {
            refillTimes.put(address, currentTime);
            bucket.put(address, BUCKET_CAPACITY);
        } else {
            long lastRefillTime = refillTimes.get(address);
            long timeElapsed = currentTime - lastRefillTime;

            // Refill tokens based on time elapsed
            int tokensToAdd = (int) (timeElapsed / REFILL_RATE);
            int currentTokens = bucket.get(address);
            int newTokens = Math.min(currentTokens + tokensToAdd, BUCKET_CAPACITY);

            refillTimes.put(address, currentTime);
            bucket.put(address, newTokens);
        }
    }

    private boolean tryConsume(String address) {
        if (!bucket.containsKey(address)) {
            bucket.put(address, BUCKET_CAPACITY);
        }
        int tokens = bucket.get(address);
        if (tokens > 0) {
            bucket.put(address, tokens - 1);
            return true;
        } else {
            return false;
        }
    }
}