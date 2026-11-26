package metrics;

public class ProcessMetrics {
    private final String name;
    private final int arrivalTime;
    private final int finishTime;
    private final int waitingTime;
    private final int turnaroundTime;
    private final int responseTime;

    public ProcessMetrics(String name, int arrivalTime, int finishTime,
                          int waitingTime, int turnaroundTime, int responseTime) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.finishTime = finishTime;
        this.waitingTime = waitingTime;
        this.turnaroundTime = turnaroundTime;
        this.responseTime = responseTime;
    }

    /** Process name. */
    public String getName() { return name; }

    /** Arrival time. */
    public int getArrivalTime() { return arrivalTime; }

    /** Finish time. */
    public int getFinishTime() { return finishTime; }

    /** Waiting time = turnaround - burst. */
    public int getWaitingTime() { return waitingTime; }

    /** Turnaround time = finish - arrival. */
    public int getTurnaroundTime() { return turnaroundTime; }

    /** Response time = start - arrival. */
    public int getResponseTime() { return responseTime; }
}
