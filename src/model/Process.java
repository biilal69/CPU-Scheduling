package model;

public class Process {
    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private final int initialPriority;
    private int priority;
    private int remainingTime;
    private int startTime = -1;
    private int finishTime = -1;
    private int enteredTier2At = -1;

    public Process(String name, int arrivalTime, int burstTime, int initialPriority) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.initialPriority = initialPriority;
        this.priority = initialPriority;
        this.remainingTime = burstTime;
    }

    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getInitialPriority() { return initialPriority; }
    public int getPriority() { return priority; }
    public int getRemainingTime() { return remainingTime; }
    public int getStartTime() { return startTime; }
    public int getFinishTime() { return finishTime; }
    public int getEnteredTier2At() { return enteredTier2At; }

    public void setPriority(int priority) { this.priority = priority; }
    public void setStartTime(int startTime) { this.startTime = startTime; }
    public void setFinishTime(int finishTime) { this.finishTime = finishTime; }
    public void setEnteredTier2At(int time) { this.enteredTier2At = time; }

    public void run(int units) { remainingTime = Math.max(0, remainingTime - units); }
    public boolean isFinished() { return remainingTime == 0; }

    public Process copy() { return new Process(name, arrivalTime, burstTime, initialPriority); }

    @Override
    public String toString() {
        return name + " (remaining: " + remainingTime + ", Priority: " + priority + ")";
    }
}