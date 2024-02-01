package it.giuliatesta.cctproject;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class RetryPolicy {

    private static final int MAX_FAILURES = 5;
    private int consecutiveFailures = 0;
    private final LoadBalancer balancer = new LoadBalancer();

    private boolean canRetry() {
        return consecutiveFailures < MAX_FAILURES;
    }

    // tries to make the call. If it fails, consecuteFailure is incremented and
    // retries until the consecutiveFailures reach MAX_FAILURES
    // It will fail after MAX_FAILURES tries with 503 status code
    public void send(HttpServletRequest request, HttpServletResponse response, HttpMethod method, String microservice)
            throws IOException {
        String endpoint = getEndPointFromRequest(request);
        String url = composeUrl(endpoint, microservice);
        if (!canRetry()) {
            System.out.println(
                    "[RetryPolicy] Reached maximum number of attempts (" + MAX_FAILURES + "). Cannot retry again.");
            response.getWriter().println("Reached maximum number of attempts");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            consecutiveFailures = 0;
            balancer.putMicroserviceInTimeOut(url);

        } else {
            try {
                call(response, url, method, microservice);
                // if the call was successful, the counter needs to be resetted
                consecutiveFailures = 0;
                if (balancer.isInTimeOut(url)) {
                    balancer.putMicroserviceOutFromTimeOut(url);
                }
            } catch (Exception e) {
                consecutiveFailures++;
                // retry
                send(request, response, method, microservice);
            }
        }
    }

    // the actual method that calls
    protected abstract void call(HttpServletResponse resp, String url, HttpMethod method,
            String microservice)
            throws ServletException, IOException;

    // TODO check @NonNull decorator
    @NonNull
    private String composeUrl(String endpoint, String microservice) {
        var microserviceUrl = balancer.getMicroserviceUrl(microservice);
        return microserviceUrl + endpoint;
    }

    // TODO validate correct routing httpbin/json/ vs json/
    private String getEndPointFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        var splits = requestURI.split("/");
        return splits[splits.length - 1];
    }
}
