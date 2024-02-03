package it.giuliatesta.cctproject;

import java.io.IOException;

import org.springframework.http.HttpMethod;

import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// load balancer with retry policy
// puts hosts in timeout if they are failing more than MAX_FAILURES
// retries to use the timed out hosts after WAIT number of request intercepted by the servlet.
// retries calls to microservice at most MAX_FAILURES
public abstract class RetryingLoadBalancer {

    // maximum number of request attempts
    private static final int MAX_FAILURES = 5;
    private int consecutiveFailures = 0;

    public void send(HttpServletRequest request, HttpServletResponse response, HttpMethod method)
            throws IOException {
        MicroserviceRequestInfo microserviceRequestInfo = getMicroServiceInfo(request);
        var host = microserviceRequestInfo.usedHost;
        var url = microserviceRequestInfo.url();
        try {
            if (host.isInTimeOut()) {
                System.out.println("[Retrying Load Balancer] Host " + host.host + "not in timeout: can be called.");
                // if can retry
                if (canRetry()) {
                    System.out.println("[Retrying Load Balancer] Attempt: " + consecutiveFailures);
                    // I forward the request and if doesn't fail (throws exception) I reset the
                    // failure's count.
                    call(response, url, method);
                    consecutiveFailures = 0;
                } else {
                    // If the hosts has run out all the available attempts,
                    // I return 503 and put the host in timeout
                    stop(response, host);
                }
            } else {
                System.out.println("[Retrying Load Balancer] Host " + host.host + " in time. Should retry? ");
                // If the host is in timeout
                // and I can retry (meaning 10 requests have gone by)
                if (host.shouldRetry()) {
                    System.out.println("[Retrying Load Balancer] retrying...");
                    // I forward the call and if doesn't fail I reset the failure's count and take
                    // it out of time out.
                    call(response, url, method);
                    consecutiveFailures = 0;
                    host.setTimeOut(false);
                    System.out
                            .println("[Host] Microservice host " + host
                                    + " is now functioning again. Out from time out.");
                } else {
                    System.out.println(
                            "[Retrying Load Balancer] skipping the timed out host since it's too soon to retry.");
                    // If it's too soon to retry, I skip it and choose another host
                    microserviceRequestInfo.changeHost();
                    System.out
                            .println("[Retrying Load Balancer] New host: " + microserviceRequestInfo.usedHost.host);
                    call(response, microserviceRequestInfo.url(), method);
                    consecutiveFailures = 0;
                }
            }
        } catch (Exception e) {
            consecutiveFailures++;
            // stops if it definitely fails
            System.out.println("[Retrying Load Balancer] Request to " + microserviceRequestInfo.route.sourcePath
                    + " has failed: " + e);
            if (!canRetry()) {
                System.out.println("[Retrying Load Balancer] circuit break opening.");
                stop(response, host);
            } else {
                System.out.println("[Retrying Load Balancer] retrying with new host...");
                // retry
                send(request, response, method);
            }
        }
    }

    // the actual method that calls
    protected abstract void call(HttpServletResponse resp, String url, HttpMethod method)
            throws Exception;

    private boolean canRetry() {
        return consecutiveFailures < MAX_FAILURES;
    }

    private void stop(HttpServletResponse response, Host host) throws IOException {
        System.out.println("[Retying Load Balancer] Reached maximum number of attempts (" + MAX_FAILURES
                + "). Cannot retry again.");
        response.getWriter().println("Reached maximum number of attempts");
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        consecutiveFailures = 0;
        host.setTimeOut(true);
        System.out.println("[Host] Microservice host " + host + " is failing. Time out.");
    }

    private MicroserviceRequestInfo getMicroServiceInfo(HttpServletRequest request) {
        PropertyLoader config = PropertyLoader.getInstance();
        var sourcePath = request.getRequestURI();
        Route route = config.getRoute(sourcePath);
        if (route == null) {
            throw new PropertyNotFoundException("[RetryingLoadBalancer] Requested route " + sourcePath + " not found");
        }
        return new MicroserviceRequestInfo(route);
    }

}