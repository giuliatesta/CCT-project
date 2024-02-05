package it.giuliatesta.cctproject;

class Host implements Comparable<Host> {

    // needs to wait at least 30 seconds
    private static final int WAIT = 30;

    public String host;
    private boolean timeout = false;
    private long timestamp = 0;

    Host(String host) {
        this.host = host;
        timestamp = System.currentTimeMillis();
    }

    public boolean shouldRetry() {
        var now = System.currentTimeMillis();
        return now - timestamp >= WAIT;
    }

    public boolean isInTimeOut() {
        return timeout;
    }

    public void putInTimeOut() {
        if (timeout) {
            System.out.println(
                    "[Load Balancer] WORNING Microservice host " + host
                            + " already in timeout. Still not working.");
        } else {
            System.out
                    .println("[Host] Microservice host " + host
                            + " seems to be failing. Time out.");
            timeout = true;
            timestamp = System.currentTimeMillis();
        }

    }

    public void putOutTimeOut() {
        if (!timeout) {
            System.out.println(
                    "[Host] WORNING Microservice host " + host
                            + " currently not in time out.");
        } else {
            System.out
                    .println("[Host] Microservice host " + host
                            + " is now functioning again. Out from time out.");
            timeout = false;
            timestamp = System.currentTimeMillis();
        }
    }

    @Override
    public int compareTo(Host other) {
        return host.compareTo(other.host);
    }

    @Override
    public String toString() {
        return "Host{host:" + host + ", timeout:" + timeout + ", timestamp:" + timestamp + "}";
    }

}
