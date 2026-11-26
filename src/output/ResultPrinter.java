package output;

import java.util.List;
import java.util.Locale;
import metrics.ProcessMetrics;

public class ResultPrinter {

    public static void print(String name, List<ProcessMetrics> metrics, double avgW, double avgT) {
        System.out.println("=== " + name + " results ===");
        System.out.printf("%-7s | %-7s | %-6s | %-10s | %-7s | %s%n", "Process", "Arrival", "Finish", "Turnaround", "Waiting", "Response");
        for (ProcessMetrics m : metrics) {
            System.out.printf("%-7s | %-7d | %-6d | %-10d | %-7d | %d%n", m.getName(), m.getArrivalTime(), m.getFinishTime(),
                    m.getTurnaroundTime(), m.getWaitingTime(), m.getResponseTime());
        }
        System.out.printf(Locale.US, "Average Turnaround Time: %.2f time units%n", avgT);
        System.out.printf(Locale.US, "Average Waiting Time: %.2f time units%n%n", avgW);
    }
}