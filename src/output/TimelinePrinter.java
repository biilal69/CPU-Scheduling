package output;

import java.util.List;
import model.SimulationResult;
import model.TimeStep;

public class TimelinePrinter {

    public static void print(SimulationResult result) {
        System.out.println("=== " + result.getSchedulerName() + " timeline ===");
        System.out.println(String.format("%-5s | %-7s | %-36s | %s", "Time", "Running", "Event", "Queues"));
        System.out.println("-".repeat(80));
        List<String> previous = null;
        for (TimeStep step : result.getTimeline()) {
            List<String> lines = step.getQueueLines();
            boolean changed = !lines.equals(previous);
            String first = (changed && !lines.isEmpty()) ? lines.get(0) : "";
            System.out.println(String.format("%-5s | %-7s | %-36s | %s", step.getTime(), step.getRunning(), step.getEvent(), first));
            if (changed) {
                for (int i = 1; i < lines.size(); i++) {
                    System.out.println(String.format("%-5s | %-7s | %-36s | %s", "", "", "", lines.get(i)));
                }
            }
            previous = lines;
        }
        System.out.println("Total time: " + result.getTotalTime());
        System.out.println();
    }
}