package it.giuliatesta.cctproject;

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
