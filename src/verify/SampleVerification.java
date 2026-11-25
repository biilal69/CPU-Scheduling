package verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import metrics.MetricsCalculator;
import metrics.ProcessMetrics;
import model.Process;
import model.SimulationResult;
import model.TestCase;
import model.TimeStep;
import output.SampleCases;
import scheduler.SJFScheduler;
import scheduler.Scheduler;
import scheduler.acs.ACSScheduler;
import scheduler.acs.AgingPolicy;
import scheduler.acs.PriorityTier;
       
public class SampleVerification {

    private static final double TOLERANCE= 0.01;         
    private static final String[] NAMES = {"P1" , "P2", "P3"};

    private int totalChecks = 0;
    private int failedChecks = 0;

    public static void main(String[] args) {
        SampleVerification verification = new SampleVerification();
        verification.runAll();
        verification.printSummary();
        if (verification.failedChecks >0) {
            System.exit(1);
        }
    }         
        
    private void runAll() {
        checkTestData();

        verifyScheduler("ACS case 1 (Q=3, CS=0)", newAcs(), SampleCases.case1(),
                3, 0, new int[] {9, 13, 8}, 8.67, 4.33);
        SimulationResult acsCase2 = verifyScheduler("ACS case 2 (Q=3, CS=1)", newAcs(), SampleCases.case2(),
                3, 1, new int[] {18, 15, 20}, 16.00, 11.00);
        verifyScheduler("SJF case 1 (CS=0)", new SJFScheduler(), SampleCases.case1(),
                3, 0, new int[] {4, 13, 6}, 6.33, 2.00);
        verifyScheduler("SJF case 2 (CS=1)", new SJFScheduler(), SampleCases.case2(),
                3, 1, new int[] {5, 17, 10}, 9.00, 4.00);

        if (acsCase2 != null) {
            verifyAcsCase2Trace(acsCase2);
        }
        verifyAgingCheckpoints();
    }

    private ACSScheduler newAcs() {
        return new ACSScheduler(new AgingPolicy(AgingPolicy.DEFAULT_INTERVAL));
    }

    private void checkTestData() {
        System.out.println("--- Sample test data ---");
        checkEquals("case1 quantum", 3, SampleCases.case1().getQuantum());
        checkEquals("case1 context switch", 0, SampleCases.case1().getContextSwitch());
        checkEquals("case2 quantum", 3, SampleCases.case2().getQuantum());
        checkEquals("case2 context switch", 1, SampleCases.case2().getContextSwitch());
    }

    private SimulationResult verifyScheduler(String label, Scheduler scheduler, TestCase testCase,
                                             int quantum, int contextSwitch, int[] expectedFinish,
                                             double expectedTurnaround, double expectedWaiting) {
        System.out.println("--- " + label + " ---");

        SimulationResult result;
        try {
            result = scheduler.run(testCase.freshProcesses(), quantum, contextSwitch);
        } catch (RuntimeException e) {
            report(false, label + ": run() threw " + e);
            return null;
        }

        List<Process> finished = result.getFinished();
        List<ProcessMetrics> metrics = MetricsCalculator.calculate(finished);

        for (int i = 0; i < NAMES.length; i++) {
            checkEquals(label + " finish time of " + NAMES[i], expectedFinish[i], finishTimeOf(metrics, NAMES[i]));
        }
        checkDouble(label + " average turnaround", expectedTurnaround, MetricsCalculator.averageTurnaround(finished));
        checkDouble(label + " average waiting", expectedWaiting, MetricsCalculator.averageWaiting(finished));
        return result;
    }

    private void verifyAcsCase2Trace(SimulationResult result) {
        System.out.println("--- ACS case 2 trace ---");
        checkEquals("total simulation time", 20, result.getTotalTime());
        checkEquals("context switch units at t", Arrays.asList(3, 7, 11, 15, 18), contextSwitchTimes(result));
        checkEquals("execution order", Arrays.asList("P1", "P2", "P3", "P2", "P1", "P3"), executionOrder(result));

        checkSnapshotAt(result, 11, "Tier 2: [P1 (remaining: 2, Priority: 2), P3 (remaining: 1, Priority: 3)]");
        checkSnapshotAt(result, 15, "Tier 2: [P3 (remaining: 1, Priority: 2)]");
    }

    private void verifyAgingCheckpoints() {
        System.out.println("--- Aging checkpoints (case 2 replay) ---");
        List<Process> fresh = SampleCases.case2().freshProcesses();
        Process p1 = findProcess(fresh, "P1");
        Process p2 = findProcess(fresh, "P2");
        Process p3 = findProcess(fresh, "P3");
        if (p1 == null || p2 == null || p3 == null) {
            report(false, "case 2 must contain P1, P2 and P3");
            return;
        }

        PriorityTier tier2 = new PriorityTier();
        AgingPolicy aging = new AgingPolicy(AgingPolicy.DEFAULT_INTERVAL);
        tier2.add(p1, 3);
        tier2.add(p2, 7);
        tier2.add(p3, 11);

        aging.apply(tier2.getAll(), 11);
        checkEquals("t=11 priority of P1", 2, p1.getPriority());
        checkEquals("t=11 priority of P2", 1, p2.getPriority());
        checkEquals("t=11 priority of P3", 3, p3.getPriority());
        checkEquals("t=11 Tier 2 picks", "P2", nameOf(tier2.pollHighest()));

        aging.apply(tier2.getAll(), 15);
        checkEquals("t=15 priority of P1", 1, p1.getPriority());
        checkEquals("t=15 priority of P3", 2, p3.getPriority());
        checkEquals("t=15 Tier 2 picks", "P1", nameOf(tier2.pollHighest()));
    }
            
    private List<Integer> contextSwitchTimes(SimulationResult result) {
        List<Integer> times = new ArrayList<>();
        for (TimeStep step : result.getTimeline()) {
            if (TimeStep.CS.equals(step.getRunning())) {
                times.add(step.getTime());
            }
        }
        return times;
    }
    private List<String> executionOrder(SimulationResult result) {
        List<String> order = new ArrayList<>();
        for (TimeStep step : result.getTimeline()) {
            String running = step.getRunning();
            if (TimeStep.CS.equals(running) || TimeStep.IDLE.equals(running)) {
                continue;
            }
            boolean sameAsLast = !order.isEmpty() && order.get(order.size() - 1).equals(running);
            if (!sameAsLast) {
                order.add(running);
            }
        }
        return order;
    }

    private void checkSnapshotAt(SimulationResult result, int time, String expectedLine) {
        String actual = "<no time step recorded>";
        for (TimeStep step : result.getTimeline()) {
            if (step.getTime() == time) {
                actual = tier2LineOf(step);
            }
        }
        checkEquals("Tier 2 snapshot at t=" + time, expectedLine, actual);
    }
 
    private String tier2LineOf(TimeStep step) {
        for (String line : step.getQueueLines()) {
            if (line.startsWith("Tier 2:")) {
                return line;
            }
        }
        return "<no Tier 2 line>";
    }
    private int finishTimeOf(List<ProcessMetrics> metrics, String name) {
        for (ProcessMetrics m : metrics) {
            if (m.getName().equals(name)) {
                return m.getFinishTime();
            }
        }
        return -1;
    }
    private Process findProcess(List<Process> processes, String name) {
        for (Process p : processes) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
               
    private String nameOf(Process p) {
        return p == null ? "null" : p.getName();
    }
            
    private void checkEquals(String name, Object expected, Object actual) {
        boolean ok = Objects.equals(expected, actual);
        report(ok, ok ? name + " = " + actual
                      : name + ": expected " + expected + " but got " + actual);
    }
    private void checkDouble(String name, double expected, double actual) {
        boolean ok = Math.abs(expected - actual) <= TOLERANCE;
        String expectedText = String.format(Locale.US, "%.2f", expected);
        String actualText = String.format(Locale.US, "%.2f", actual);
        report(ok, ok ? name + " = " + actualText
                      : name + ": expected " + expectedText + " but got " + actualText);
    }
               
    private void report(boolean ok, String message) {
        totalChecks++;
        if (ok) {
            System.out.println("[PASS] " + message);
        } else {
            failedChecks++;
            System.out.println("[FAIL] " + message);
        }
    }
    private void printSummary() {
        System.out.println();
        if (failedChecks == 0) {
            System.out.println("RESULT: all " + totalChecks + " checks passed.");
        } else {
            System.out.println("RESULT: " + failedChecks + " of " + totalChecks + " checks FAILED.");
        }
    }
}
