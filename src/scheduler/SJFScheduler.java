package scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Process;
import model.SimulationResult;
import model.TimeStep;

public class SJFScheduler implements Scheduler {

    /** Returns the short display name of this scheduler. */
    @Override
    public String getName() {
        return "SJF";
    }

    /** Simulates preemptive SJF (shortest remaining time first) with context-switch cost. */
    @Override
    public SimulationResult run(List<Process> processes, int quantum, int contextSwitch) {
        if (contextSwitch < 0) {
            throw new IllegalArgumentException("contextSwitch must be >= 0");
        }
        SimulationResult result = new SimulationResult("SJF");
        List<Process> pending = new ArrayList<>(processes);
        pending.sort(Comparator.comparingInt(Process::getArrivalTime).thenComparing(Process::getName));
        List<Process> pool = new ArrayList<>(); // arrived and unfinished, in arrival order

        int time = 0;
        int finishedCount = 0;
        Process lastRan = null;

        while (finishedCount < processes.size()) {
            admitArrivals(pending, pool, time);

            if (pool.isEmpty()) {
                record(result, time, TimeStep.IDLE, "", pool, null);
                time++;
                lastRan = null;
                continue;
            }

            Process next = pickShortest(pool, lastRan);

            if (lastRan != null && next != lastRan && contextSwitch > 0) {
                for (int i = 0; i < contextSwitch; i++) {
                    String event = (i == 0)
                            ? "Context switch saves " + lastRan.getName() + " and loads " + next.getName()
                            : "";
                    record(result, time, TimeStep.CS, event, pool, next);
                    time++;
                    admitArrivals(pending, pool, time);
                }
            }

            // exactly one unit of next (also the guaranteed unit after a context switch)
            if (next.getStartTime() == -1) {
                next.setStartTime(time);
            }
            record(result, time, next.getName(), "", pool, next);
            next.run(1);
            time++;

            if (next.isFinished()) {
                next.setFinishTime(time);
                pool.remove(next);
                result.addFinished(next);
                finishedCount++;
            }
            lastRan = next;
        }
        return result;
    }

    /** Picks the process with the smallest remaining time (R6 tie-breaks). */
    private Process pickShortest(List<Process> pool, Process lastRan) {
        Process best = null;
        for (Process p : pool) {
            if (best == null || isBetter(p, best, lastRan)) {
                best = p;
            }
        }
        return best;
    }

    /** True if candidate should run before current best: remaining, last ran, arrival, name. */
    private boolean isBetter(Process cand, Process best, Process lastRan) {
        if (cand.getRemainingTime() != best.getRemainingTime()) {
            return cand.getRemainingTime() < best.getRemainingTime();
        }
        if (cand == lastRan) return true;
        if (best == lastRan) return false;
        if (cand.getArrivalTime() != best.getArrivalTime()) {
            return cand.getArrivalTime() < best.getArrivalTime();
        }
        return cand.getName().compareTo(best.getName()) < 0;
    }

    /** Moves every pending process that has arrived by the given time into the pool. */
    private void admitArrivals(List<Process> pending, List<Process> pool, int time) {
        while (!pending.isEmpty() && pending.get(0).getArrivalTime() <= time) {
            pool.add(pending.remove(0));
        }
    }

    /** Records one time unit with the "Ready: [...]" snapshot (the running/loaded process excluded). */
    private void record(SimulationResult result, int time, String running, String event,
                        List<Process> pool, Process exclude) {
        StringBuilder sb = new StringBuilder();
        for (Process p : pool) {
            if (p == exclude) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.toString());
        }
        String line = "Ready: [" + (sb.length() == 0 ? "Empty" : sb.toString()) + "]";
        List<String> lines = new ArrayList<>();
        lines.add(line);
        result.addStep(new TimeStep(time, running, event, lines));
    }
}

