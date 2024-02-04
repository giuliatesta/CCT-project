package it.giuliatesta.cctproject;

class Host implements Comparable<Host> {

    // needs to wait at least 2 minutes to retry
    private static final int WAIT = 120;

    public String host;
    private boolean timeout = false;
    private long timestamp = 0;

    Host(String host) {
        this.host = host;
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
        }
        System.out
                .println("[Host] Microservice host " + host
                        + " seems to be failing. Time out.");
        timeout = true;
        timestamp = System.currentTimeMillis();

    }

    public void putOutTimeOut() {
        if (!timeout) {
            System.out.println(
                    "[Load Balancer] WORNING Microservice host " + host
                            + " currently not in time out.");
        }
        System.out
                .println("[Host] Microservice host " + host
                        + " is now functioning again. Out from time out.");
        timeout = false;
        timestamp = 0;
    }

    @Override
    public int compareTo(Host other) {
        return host.compareTo(other.host);
    }

}
