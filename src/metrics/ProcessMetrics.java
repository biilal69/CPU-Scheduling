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

    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getFinishTime() { return finishTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getResponseTime() { return responseTime; }
}