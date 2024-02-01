package it.giuliatesta.cctproject;

import java.io.IOException;

import org.springframework.http.HttpMethod;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class RetryPolicy {

    private static final int MAX_FAILURES = 5;
    private int consecutiveFailures = 0;

    private boolean canRetry() {
        return consecutiveFailures >= MAX_FAILURES;
    }

    // tries to make the call. If it fails, consecuteFailure is incremented and
    // retries until the consecutiveFailures reach MAX_FAILURES
    // It will fail after MAX_FAILURES tries with 503 status code
    public void send(HttpServletRequest request, HttpServletResponse response, HttpMethod method, String microservice)
            throws IOException {

        if (canRetry()) {
            System.out.println(
                    "[RetryPolicy] Reached maximum number of attempts (" + MAX_FAILURES + "). Cannot retry again.");
            response.getWriter().println("Reached maximum number of attempts");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            consecutiveFailures = 0;
        } else {
            try {
                call(request, response, method, microservice);
                // if the call was successful, the counter needs to be resetted
                consecutiveFailures = 0;
            } catch (Exception e) {
                consecutiveFailures++;
                // retry
                send(request, response, method, microservice);
            }
        }
    }

    // the actual method that calls
    protected abstract void call(HttpServletRequest req, HttpServletResponse resp, HttpMethod method,
            String microservice)
            throws ServletException, IOException;

}
