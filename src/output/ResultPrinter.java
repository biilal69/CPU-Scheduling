import java.util.List;

public class ResultPrinter {

    public static void print(String name, List<ProcessMetrics> metrics, double avgW, double avgT) {
        System.out.println("=== " + name + " ===");
        System.out.printf("%-8s %-12s %-10s %-10s%n", "Process", "Turnaround", "Waiting", "Response");
        for (ProcessMetrics m : metrics) {
            System.out.printf("%-8s %-12d %-10d %-10d%n", m.name, m.turnaround, m.waiting, m.response);
        }
        System.out.printf("Average Turnaround Time: %.2f time units%n", avgT);
        System.out.printf("Average Waiting Time:    %.2f time units%n%n", avgW);
    }
}
