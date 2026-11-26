package model;

import java.util.ArrayList;
import java.util.List;

public class SimulationResult {
    private final String schedulerName;
    private final List<TimeStep> timeline = new ArrayList<>();
    private final List<Process> finished = new ArrayList<>();

    public SimulationResult(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getSchedulerName() { return schedulerName; }
    public List<TimeStep> getTimeline() { return timeline; }
    public List<Process> getFinished() { return finished; }

    public void addStep(TimeStep step) { timeline.add(step); }
    public void addFinished(Process p) { finished.add(p); }

    /** Total simulated time: every timeline entry is exactly one time unit. */
    public int getTotalTime() { return timeline.size(); }
}
