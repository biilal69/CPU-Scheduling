import java.util.*;

public class SampleCases {

    // Each call builds NEW Process objects: schedulers mutate remaining/start/finish.

    public static TestCase case1() {
        return new TestCase("Case 1 (PDF)", new ArrayList<>(List.of(
                new Process("P1", 0, 4, 2),
                new Process("P2", 1, 7, 3),
                new Process("P3", 3, 2, 1))), 3, 0);
    }

    public static TestCase case2() {
        return new TestCase("Case 2 (PDF)", new ArrayList<>(List.of(
                new Process("P1", 0, 5, 4),
                new Process("P2", 1, 6, 2),
                new Process("P3", 4, 4, 3))), 3, 1);
    }

    // Interactive-heavy: every burst <= Q, so nothing is demoted.
    public static TestCase case3() {
        return new TestCase("Case 3 (interactive-heavy)", new ArrayList<>(List.of(
                new Process("P1", 0, 2, 3),
                new Process("P2", 1, 1, 2),
                new Process("P3", 2, 3, 1),
                new Process("P4", 3, 2, 4),
                new Process("P5", 4, 1, 2))), 4, 1);
    }

    // Batch-heavy: long bursts, all demoted; Tier 2 priorities and aging matter.
    public static TestCase case4() {
        return new TestCase("Case 4 (batch-heavy)", new ArrayList<>(List.of(
                new Process("P1", 0, 10, 3),
                new Process("P2", 1, 12, 1),
                new Process("P3", 2, 8, 2),
                new Process("P4", 3, 15, 4))), 3, 1);
    }

    // Idle gap: CPU goes idle after t=5 until P3 arrives at t=15.
    public static TestCase case5() {
        return new TestCase("Case 5 (idle gap)", new ArrayList<>(List.of(
                new Process("P1", 0, 3, 2),
                new Process("P2", 1, 2, 1),
                new Process("P3", 15, 4, 3),
                new Process("P4", 16, 6, 2))), 3, 0);
    }

    public static List<TestCase> all() {
        return List.of(case1(), case2(), case3(), case4(), case5());
    }

    /** Fresh copy by index (0-based), so repeated runs never share state. */
    public static TestCase byIndex(int i) {
        switch (i) {
            case 0: return case1();
            case 1: return case2();
            case 2: return case3();
            case 3: return case4();
            case 4: return case5();
            default: throw new IllegalArgumentException("No test case " + (i + 1));
        }
    }

    public static int count() { return 5; }
}
