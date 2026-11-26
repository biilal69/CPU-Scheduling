package metrics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Process;
import model.SimulationResult;
import model.TimeStep;

public class MetricsCalculator {

    /** Builds per-process metrics sorted by arrival time then name; does not modify the input. */
    public static List<ProcessMetrics> calculate(List<Process> finished) {
        List<Process> sorted = new ArrayList<>(finished);
        sorted.sort(Comparator.comparingInt(Process::getArrivalTime).thenComparing(Process::getName));
        List<ProcessMetrics> list = new ArrayList<>();
        for (Process p : sorted) {
            list.add(new ProcessMetrics(p.getName(), p.getArrivalTime(), p.getFinishTime(),
                    waiting(p), turnaround(p), response(p)));
        }
        return list;
    }

    /** Average waiting time (0.0 for an empty list). */
    public static double averageWaiting(List<Process> finished) {
        if (finished.isEmpty()) return 0.0;
        double sum = 0;
        for (Process p : finished) sum += waiting(p);
        return sum / finished.size();
    }

    /** Average turnaround time (0.0 for an empty list). */
    public static double averageTurnaround(List<Process> finished) {
        if (finished.isEmpty()) return 0.0;
        double sum = 0;
        for (Process p : finished) sum += turnaround(p);
        return sum / finished.size();
    }

    /** Average response time (0.0 for an empty list). */
    public static double averageResponse(List<Process> finished) {
        if (finished.isEmpty()) return 0.0;
        double sum = 0;
        for (Process p : finished) sum += response(p);
        return sum / finished.size();
    }

    /** CPU utilization in percent: units running a process / total time * 100. */
    public static double cpuUtilization(SimulationResult result) {
        int total = result.getTotalTime();
        if (total == 0) return 0.0;
        int busy = 0;
        for (TimeStep s : result.getTimeline()) {
            if (!TimeStep.CS.equals(s.getRunning()) && !TimeStep.IDLE.equals(s.getRunning())) busy++;
        }
        return busy * 100.0 / total;
    }

    private static int turnaround(Process p) {
        return p.getFinishTime() - p.getArrivalTime();
    }

    private static int waiting(Process p) {
        return turnaround(p) - p.getBurstTime();
    }

    private static int response(Process p) {
        return p.getStartTime() - p.getArrivalTime();
    }
}

