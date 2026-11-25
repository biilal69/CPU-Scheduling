package scheduler.acs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Process;
import model.SimulationResult;
import model.TimeStep;
import scheduler.Scheduler;

public class ACSScheduler implements Scheduler {
    private final AgingPolicy aging;
    private RoundRobinTier tier1;
    private PriorityTier tier2;
    private SimulationResult result;
    private List<Process> pending;
    private int contextSwitch;
    private int time;
    private Process lastRan;

    public ACSScheduler(AgingPolicy aging) {
        this.aging = aging;
    }

    public String getName() {
        return "ACS";
    }

    public SimulationResult run(List<Process> processes, int quantum, int contextSwitch) {
        if (quantum < 1 || contextSwitch < 0) {
            throw new IllegalArgumentException("quantum must be >= 1 and contextSwitch must be >= 0");
        }
        this.contextSwitch = contextSwitch;
        result = new SimulationResult("ACS");
        pending = new ArrayList<>(processes);
        pending.sort(Comparator.comparingInt(Process::getArrivalTime).thenComparing(Process::getName));
        tier1 = new RoundRobinTier(quantum);
        tier2 = new PriorityTier();
        time = 0;
        lastRan = null;

        while (result.getFinished().size() < processes.size()) {
            admitArrivals();
            aging.apply(tier2.getAll(), time);
            if (tier1.isEmpty() && tier2.isEmpty()) {
                record(TimeStep.IDLE, "");
                time++;
                lastRan = null;
            } else if (!tier1.isEmpty()) {
                runTier1();
            } else {
                runTier2();
            }
        }
        return result;
    }

    private void admitArrivals() {
        while (!pending.isEmpty() && pending.get(0).getArrivalTime() <= time) {
            tier1.add(pending.remove(0));
        }
    }

    private void runTier1() {
        Process next = tier1.poll();
        switchTo(next);
        int slice = Math.min(tier1.getQuantum(), next.getRemainingTime());
        execute(next, slice);
    }

    private void runTier2() {
        Process next = tier2.pollHighest();
        switchTo(next);
        execute(next, next.getRemainingTime());
    }

    private void switchTo(Process next) {
        if (lastRan == null || lastRan == next || contextSwitch == 0) {
            return;
        }
        for (int i = 0; i < contextSwitch; i++) {
            String event = "";
            if (i == 0) {
                event = "Context switch saves " + lastRan.getName() + " and loads " + next.getName();
            }
            record(TimeStep.CS, event);
            tick();
        }
    }

    private void execute(Process p, int units) {
        for (int i = 0; i < units; i++) {
            if (p.getStartTime() == -1) {
                p.setStartTime(time);
            }
            record(p.getName(), "");
            p.run(1);
            tick();
        }
        if (p.isFinished()) {
            p.setFinishTime(time);
            result.addFinished(p);
        } else {
            tier2.add(p, time);
        }
        lastRan = p;
    }

    private void tick() {
        time++;
        admitArrivals();
        aging.apply(tier2.getAll(), time);
    }

    private void record(String running, String event) {
        List<String> lines = List.of(tier1.snapshot(), tier2.snapshot());
        result.addStep(new TimeStep(time, running, event, lines));
    }
}