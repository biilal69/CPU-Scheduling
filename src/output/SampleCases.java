package output;

import java.util.ArrayList;
import java.util.List;
import model.Process;
import model.TestCase;

public class SampleCases {

    public static TestCase case1() {
        return new TestCase("Test Case 1", 3, 0, new ArrayList<>(List.of(
                new Process("P1", 0, 4, 2),
                new Process("P2", 1, 7, 3),
                new Process("P3", 3, 2, 1))));
    }

    public static TestCase case2() {
        return new TestCase("Test Case 2", 3, 1, new ArrayList<>(List.of(
                new Process("P1", 0, 5, 4),
                new Process("P2", 1, 6, 2),
                new Process("P3", 4, 4, 3))));
    }

    public static TestCase case3() {
        return new TestCase("Test Case 3 (interactive-heavy)", 4, 1, new ArrayList<>(List.of(
                new Process("P1", 0, 2, 3),
                new Process("P2", 1, 1, 2),
                new Process("P3", 2, 3, 1),
                new Process("P4", 3, 2, 4),
                new Process("P5", 4, 1, 2))));
    }

    public static TestCase case4() {
        return new TestCase("Test Case 4 (batch-heavy)", 3, 1, new ArrayList<>(List.of(
                new Process("P1", 0, 10, 3),
                new Process("P2", 1, 12, 1),
                new Process("P3", 2, 8, 2),
                new Process("P4", 3, 15, 4))));
    }

    public static TestCase case5() {
        return new TestCase("Test Case 5 (idle gap)", 3, 0, new ArrayList<>(List.of(
                new Process("P1", 0, 3, 2),
                new Process("P2", 1, 2, 1),
                new Process("P3", 15, 4, 3),
                new Process("P4", 16, 6, 2))));
    }

    public static List<TestCase> all() {
        return List.of(case1(), case2(), case3(), case4(), case5());
    }
}