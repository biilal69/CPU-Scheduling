package model;

import java.util.List;

public class TimeStep {
    public static final String CS = "CS";
    public static final String IDLE = "IDLE";

    private final int time;
    private final String running;
    private final String event;
    private final List<String> queueLines;

    public TimeStep(int time, String running, String event, List<String> queueLines) {
        this.time = time;
        this.running = running;
        this.event = event;
        this.queueLines = queueLines;
    }

    public int getTime() { return time; }
    public String getRunning() { return running; }
    public String getEvent() { return event; }
    public List<String> getQueueLines() { return queueLines; }
}
