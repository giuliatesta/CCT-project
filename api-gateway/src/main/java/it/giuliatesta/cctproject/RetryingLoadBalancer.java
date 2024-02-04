package it.giuliatesta.cctproject;

import java.io.IOException;

import org.springframework.http.HttpMethod;

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
        MicroserviceRequestInfo microserviceRequestInfo = getMicroserviceInfo(request);
        if (microserviceRequestInfo == null) {
            System.out.println("[Retrying Load Balancer] Request not found.");
            response.getWriter().println("Request not found.");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            var host = microserviceRequestInfo.usedHost;
            var url = microserviceRequestInfo.url();
            try {
                if (!host.isInTimeOut()) {
                    System.out
                            .println("[Retrying Load Balancer] Host " + host.host + " not in timeout: can be called.");
                    // if can retry
                    if (canRetry()) {
                        // I forward the request and if doesn't fail (throws exception) I reset the
                        // failure's count.
                        call(response, url, method);
                        consecutiveFailures = 0;
                    } else {
                        // If the hosts has run out all the available attempts,
                        // I return 503 and put the host in timeout
                        stop(response, host, "Reached maximum number of attempts");
                    }
                } else {
                    System.out.println("[Retrying Load Balancer] Host " + host.host + " in time out. Should retry? ");
                    // If the host is in timeout
                    // and I can retry (meaning 10 requests have gone by)
                    if (host.shouldRetry()) {
                        System.out.println("[Retrying Load Balancer] retrying...");
                        // I forward the call and if doesn't fail I reset the failure's count and take
                        // it out of time out.
                        call(response, url, method);
                        consecutiveFailures = 0;
                        host.putOutTimeOut();
                    } else {
                        System.out.println(
                                "[Retrying Load Balancer] skipping the timed out host since it's too soon to retry.");
                        // If it's too soon to retry, I skip it and choose another host
                        microserviceRequestInfo.changeHost();
                        if (microserviceRequestInfo.usedHost == null) {
                            throw new Exception("[Retrying Load Balancer] no other host available.");
                        }
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
                    stop(response, host, e.getMessage());
                } else {
                    System.out.println("[Retrying Load Balancer] retrying with new host...");
                    // retry
                    send(request, response, method);
                }
            }
        }
    }

    protected abstract void call(HttpServletResponse resp, String url, HttpMethod method)
            throws Exception;

    private boolean canRetry() {
        return consecutiveFailures < MAX_FAILURES;
    }

    private void stop(HttpServletResponse response, Host host, String message) throws IOException {
        System.out.println("[Retying Load Balancer] " + message);
        response.getWriter().println(message);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        consecutiveFailures = 0;
        host.putInTimeOut();
    }

    private MicroserviceRequestInfo getMicroserviceInfo(HttpServletRequest request) {
        PropertyLoader config = PropertyLoader.getInstance();
        var sourcePath = request.getRequestURI();
        Route route = config.getRoute(sourcePath);
        if (route == null) {
            System.out.println("[RetryingLoadBalancer] Requested route " + sourcePath + " not found");
            return null;
        }
        return new MicroserviceRequestInfo(route);
    }

}