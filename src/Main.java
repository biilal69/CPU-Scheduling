import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import metrics.MetricsCalculator;
import model.SimulationResult;
import model.TestCase;
import output.ResultPrinter;
import output.SampleCases;
import output.TimelinePrinter;
import scheduler.SJFScheduler;
import scheduler.Scheduler;
import scheduler.acs.ACSScheduler;
import scheduler.acs.AgingPolicy;

public class Main {

    /** Console entry point: choose a test case and scheduler, then print timeline and metrics. */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<TestCase> cases = SampleCases.all();
        boolean again = true;
        while (again) {
            for (int i = 0; i < cases.size(); i++) {
                TestCase c = cases.get(i);
                System.out.println((i + 1) + ") " + c.getName() + "  (Q=" + c.getQuantum()
                        + ", CS=" + c.getContextSwitch() + ")");
            }
            int idx = readInt(in, "Choose test case: ", 1, 1, cases.size());
            TestCase tc = cases.get(idx - 1);
            int q = readInt(in, "Quantum Q (Enter = " + tc.getQuantum() + "): ", tc.getQuantum(), 1, Integer.MAX_VALUE);
            int cs = readInt(in, "Context switch CS (Enter = " + tc.getContextSwitch() + "): ",
                    tc.getContextSwitch(), 0, Integer.MAX_VALUE);
            int choice = readInt(in, "Scheduler 1 = SJF, 2 = ACS, 3 = both: ", 3, 1, 3);

            if (choice == 1 || choice == 3) runOne(new SJFScheduler(), tc, q, cs);
            if (choice == 2 || choice == 3) {
                runOne(new ACSScheduler(new AgingPolicy(AgingPolicy.DEFAULT_INTERVAL)), tc, q, cs);
            }

            System.out.print("Continue? (y/n): ");
            String answer = in.nextLine().trim().toLowerCase(Locale.US);
            again = answer.startsWith("y");
        }
    }

    /** Runs one scheduler on fresh processes and prints timeline, metrics and utilization. */
    private static void runOne(Scheduler s, TestCase tc, int q, int cs) {
        SimulationResult r = s.run(tc.freshProcesses(), q, cs); // always fresh processes
        TimelinePrinter.print(r);
        ResultPrinter.print(s.getName(), MetricsCalculator.calculate(r.getFinished()),
                MetricsCalculator.averageWaiting(r.getFinished()),
                MetricsCalculator.averageTurnaround(r.getFinished()));
        System.out.println("CPU utilization: " + String.format(Locale.US, "%.2f",
                MetricsCalculator.cpuUtilization(r)) + " %");
    }

    /** Reads an int in [min, max]; an empty line returns the default; asks again on bad input. */
    private static int readInt(Scanner in, String prompt, int def, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = in.nextLine().trim();
            if (line.isEmpty()) return def;
            try {
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException e) {
                // fall through and ask again
            }
            System.out.println("Invalid input, try again.");
        }
    }
}
