package it.giuliatesta.cctproject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jakarta.el.PropertyNotFoundException;

public class LoadBalancer {

    static final int RETRY_AFTER_REQUEST_NUMBER = 10;

    private Map<String, ArrayDeque<String>> hostQueues = new HashMap<>();
    private ArrayList<String> timeOut = new ArrayList<>();
    private int requestCounter = 0;

    public String getMicroserviceUrl(String microserviceName) {
        // first time loading this microservice and its hosts
        if (!hostQueues.containsKey(microserviceName)) {
            PropertyLoader config = PropertyLoader.getInstance();
            String availableHosts = config.get(microserviceName);
            if (availableHosts == null || availableHosts.isEmpty()) {
                throw new PropertyNotFoundException();
            }
            hostQueues.put(microserviceName, prepareQueue(availableHosts.split(",")));
        }
        requestCounter++;
        String url = next(hostQueues.get(microserviceName));
        // if the extracted url is in timeout, but it's too soon to retry using it, then
        // it skips it. Otherwise, it tries to use it and resets the counter
        if (isInTimeOut(url)) {
            if (!canRetry()) {
                url = next(hostQueues.get(microserviceName));
            } else {
                requestCounter = 0;
            }
        }
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    private String next(ArrayDeque<String> queue) {
        if (queue.isEmpty()) {
            throw new IllegalStateException("No elements enqueued");
        }
        String nextElement = queue.poll();
        queue.offer(nextElement);
        return nextElement;
    }

    private ArrayDeque<String> prepareQueue(String[] elementsToBeEnqueued) {
        ArrayDeque<String> queue = new ArrayDeque<>();
        for (String element : elementsToBeEnqueued) {
            queue.add(element);
        }
        return queue;
    }

    // put aside the microservice in case of its failure
    public void putMicroserviceInTimeOut(String url) {
        if (timeOut.contains(url)) {
            System.out.println(
                    "[Load Balancer] WORNING Microservice host " + url + " already in timeout. Still not working.");
        }
        timeOut.add(url);
        System.out.println("[Load Balancer] Microservice host " + url + " is failing. Time out.");
    }

    // append on
    public void putMicroserviceOutFromTimeOut(String url) {
        if (!timeOut.contains(url)) {
            System.out.println(
                    "[Load Balancer] WORNING Microservice host " + url
                            + " not in timeout: seems to be working just fine.");
        }
        timeOut.remove(url);
        System.out
                .println("[Load Balancer] Microservice host " + url + " is now functioning again. Out from time out. ");
    }

    public boolean isInTimeOut(String url) {
        return timeOut.contains(url);
    }

    private boolean canRetry() {
        return requestCounter >= RETRY_AFTER_REQUEST_NUMBER;
    }
}
