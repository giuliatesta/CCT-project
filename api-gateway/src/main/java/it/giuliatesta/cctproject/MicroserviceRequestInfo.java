package it.giuliatesta.cctproject;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.el.PropertyNotFoundException;

public class MicroserviceRequestInfo {
    public Route route;
    public Host usedHost;

    private static Map<String, ArrayDeque<Host>> hosts = new HashMap<>(); // pairs of route.destinationHost and its
    // corresponding exposed hosts
    PropertyLoader config = PropertyLoader.getInstance();

    public MicroserviceRequestInfo(Route route) {
        ArrayDeque<Host> availableHosts = getAvailableHosts(route.destinationHost);
        this.route = route;
        this.usedHost = next(availableHosts);
    }

    public String url() {
        if (!usedHost.host.endsWith("/")) {
            usedHost.host += "/";
        }
        return usedHost.host + route.destinationPath;
    }

    public void changeHost() {
        ArrayDeque<Host> availableHosts = getAvailableHosts(route.destinationHost);
        if (availableHosts.size() == 1) {
            usedHost = null;
        }
        var newHost = next(availableHosts);
        while (newHost != usedHost) {
            newHost = next(availableHosts);
        }
        usedHost = newHost;
    }

    private ArrayDeque<Host> getAvailableHosts(String destinationHost) {
        ArrayDeque<Host> availableHosts;
        if (hosts.containsKey(destinationHost)) {
            availableHosts = hosts.get(destinationHost);
        } else {
            String stringifiedHosts = config.get(destinationHost);

            if (stringifiedHosts == null || stringifiedHosts.isEmpty()) {
                throw new PropertyNotFoundException("[RetryingLoadBalancer] Requested hosts not found");
            }

            var list = enqueue(Arrays.asList(stringifiedHosts.split(",")));
            hosts.put(destinationHost, list);
            availableHosts = list;
        }
        return availableHosts;
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

class Route {

    public String sourcePath;
    public String destinationPath;
    public String destinationHost; // just the pointer to the host property with all available hosts

    public Route(String sourcePath, String destinationPath, String destinationHost) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.destinationHost = destinationHost;
    }

    @Override
    public String toString() {
        return "Route{sourcePath:" + sourcePath + ", destinationPath:" + destinationPath + ", destinationHost:"
                + destinationHost + "}";
    }
}
