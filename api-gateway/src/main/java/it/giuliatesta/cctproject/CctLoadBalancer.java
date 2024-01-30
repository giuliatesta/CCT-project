package it.giuliatesta.cctproject;

import java.util.ArrayDeque;

import jakarta.el.PropertyNotFoundException;

public class CctLoadBalancer {

    private ArrayDeque<String> queue = new ArrayDeque<>();

    public String getMicroserviceUrl(String name) {
        CctConfiguration config = CctConfiguration.getInstance();
        String microserviceUrl = config.get(name);

        if (microserviceUrl.contains(",")) {
            prepareRoundRobin(microserviceUrl.split(","));
            microserviceUrl = next();
        }

        if (microserviceUrl == null || microserviceUrl.isEmpty()) {
            throw new PropertyNotFoundException();
        }

        if (!microserviceUrl.endsWith("/")) {
            microserviceUrl += "/";
        }
        return microserviceUrl;
    }

    private String next() {
        if (queue.isEmpty()) {
            throw new IllegalStateException("No elements enqueued");
        }
        String nextElement = queue.poll();
        queue.offer(nextElement);
        return nextElement;
    }

    private void prepareRoundRobin(String[] elementsToBeEnqueued) {
        for (String element : elementsToBeEnqueued) {
            queue.add(element);
        }
    }
}
