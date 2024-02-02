package it.giuliatesta.cctproject;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, ArrayDeque<Host>> hosts = new HashMap<>(); // pairs of microservices and their available hosts

    PropertyLoader config = PropertyLoader.getInstance();

    public void send(HttpServletRequest request, HttpServletResponse response, HttpMethod method, String microservice)
            throws IOException {
        Host host = getHost(microservice);
        String endpoint = getEndPointFromRequest(request);
        String url = host.compose(endpoint);
        try {
            if (host.isInTimeOut()) {
                System.out.println("[Retrying Load Balancer] Host " + host.host + "not in timeout: can be called.");
                // if can retry
                if (canRetry()) {
                    System.out.println("[Retrying Load Balancer] Attempt: " + consecutiveFailures);
                    // I forward the request and if doesn't fail (throws exception) I reset the
                    // failure's count.
                    call(response, url, method, microservice);
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
                    call(response, url, method, microservice);
                    consecutiveFailures = 0;
                    host.setTimeOut(false);
                    System.out
                            .println("[Host] Microservice host " + host
                                    + " is now functioning again. Out from time out.");
                } else {
                    System.out.println(
                            "[Retrying Load Balancer] skipping the timed out host since it's too soon to retry.");
                    // If it's too soon to retry, I skip it and choose another host
                    Host newHost = getAlternativeHost(microservice, host);
                    System.out.println("[Retrying Load Balancer] New host: " + newHost.host);
                    call(response, newHost.compose(endpoint), method, microservice);
                    consecutiveFailures = 0;
                }
            }
        } catch (Exception e) {
            consecutiveFailures++;
            // stops if it definitely fails
            System.out.println("[Retrying Load Balancer] Request to " + microservice + " has failed: " + e);
            if (!canRetry()) {
                System.out.println("[Retrying Load Balancer] circuit break opening.");
                stop(response, host);
            } else {
                System.out.println("[Retrying Load Balancer] retrying with new host...");
                // retry
                send(request, response, method, microservice);
            }
        }
    }

    // the actual method that calls
    protected abstract void call(HttpServletResponse resp, String url, HttpMethod method,
            String microservice)
            throws Exception;

    private boolean canRetry() {
        return consecutiveFailures < MAX_FAILURES;
    }

    private void stop(HttpServletResponse response, Host host) throws IOException {
        System.out.println("[RetryPolicy] Reached maximum number of attempts (" + MAX_FAILURES
                + "). Cannot retry again.");
        response.getWriter().println("Reached maximum number of attempts");
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        consecutiveFailures = 0;
        host.setTimeOut(true);
        System.out.println("[Host] Microservice host " + host + " is failing. Time out.");
    }

    // TODO validate correct routing httpbin/json/ vs json/
    private String getEndPointFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        var splits = requestURI.split("/");
        return splits[splits.length - 1];
    }

    public Host getHost(String serviceName) {
        ArrayDeque<Host> availableHosts = getHostsForMicroservice(serviceName);
        return next(availableHosts);
    }

    public Host getAlternativeHost(String microservice, Host skippedHost) {
        var newHost = getHost(microservice);
        while (newHost != skippedHost) {
            newHost = getHost(microservice);
        }
        return newHost;
    }

    private ArrayDeque<Host> getHostsForMicroservice(String microservice) {
        if (hosts.containsKey(microservice)) {
            return hosts.get(microservice);
        } else {
            String stringifiedHosts = config.get(microservice);

            if (stringifiedHosts == null || stringifiedHosts.isEmpty()) {
                throw new PropertyNotFoundException();
            }

            var list = enqueue(Arrays.asList(stringifiedHosts.split(",")));
            hosts.put(microservice, list);
            return list;
        }
    }

    private ArrayDeque<Host> enqueue(List<String> list) {
        var queue = new ArrayDeque<Host>();
        for (var host : list) {
            queue.add(new Host(host));
        }
        return queue;
    }

    private Host next(ArrayDeque<Host> queue) {
        if (queue.isEmpty()) {
            throw new IllegalStateException("No elements enqueued");
        }
        var nextHost = queue.poll();
        queue.offer(nextHost);
        return nextHost;
    }
}

class Host implements Comparable<Host> {

    private static final int WAIT = 10;

    public String host;
    private boolean timeout = false;
    private int counter = 0;

    Host(String host) {
        this.host = host;
    }

    public boolean shouldRetry() {
        return counter >= WAIT;
    }

    public String compose(String endpoint) {
        if (!host.endsWith("/")) {
            host += "/";
        }
        return host + endpoint;
    }

    public boolean isInTimeOut() {
        return timeout;
    }

    public void setTimeOut(boolean b) {
        // if host is already in timeout and tries to put it again, a warning is shown.
        if (timeout == b && timeout) {
            System.out.println(
                    "[Load Balancer] WORNING Microservice host " + host
                            + " already in timeout. Still not working.");
        }
        timeout = b;
    }

    @Override
    public int compareTo(Host other) {
        return host.compareTo(other.host);
    }

}
