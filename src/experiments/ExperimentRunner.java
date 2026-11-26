package experiments;

import java.util.ArrayList;
import java.util.List;
import metrics.MetricsCalculator;
import model.SimulationResult;
import model.TestCase;
import output.SampleCases;
import scheduler.Scheduler;
import scheduler.SJFScheduler;
import scheduler.acs.ACSScheduler;
import scheduler.acs.AgingPolicy;

/** Runs a scheduler over a grid of quantum and context-switch values (Requirement 3 experiments). */
public class ExperimentRunner {
    private static final int[] DEFAULT_QUANTUMS = {1, 2, 3, 4, 5, 6, 8};
    private static final int[] DEFAULT_CONTEXT_SWITCHES = {0, 1, 2, 3};
    private static final String RESULTS_FOLDER = "results";
    private static final int CHART_CS = 1;

    /** Outer loop over quantums, inner loop over context switches; every run uses fresh processes. */
    public List<ExperimentRow> run(TestCase testCase, Scheduler scheduler,
                                   int[] quantums, int[] contextSwitches) {
        List<ExperimentRow> rows = new ArrayList<>();
        for (int quantum : quantums) {
            for (int contextSwitch : contextSwitches) {
                rows.add(runOnce(testCase, scheduler, quantum, contextSwitch));
            }
        }
        return rows;
    }

    private ExperimentRow runOnce(TestCase testCase, Scheduler scheduler, int quantum, int contextSwitch) {
        SimulationResult result = scheduler.run(testCase.freshProcesses(), quantum, contextSwitch);
        return new ExperimentRow(quantum, contextSwitch,
                MetricsCalculator.averageWaiting(result.getFinished()),
                MetricsCalculator.averageTurnaround(result.getFinished()));
    }

    /** Runs every sample case with ACS and SJF, writing CSV files and PNG charts into results/. */
    public static void main(String[] args) {
        ExperimentRunner runner = new ExperimentRunner();
        List<TestCase> cases = SampleCases.all();
        for (int i = 0; i < cases.size(); i++) {
            runner.runCase(cases.get(i), "case" + (i + 1));
        }
        System.out.println("Done. Files are in the '" + RESULTS_FOLDER + "' folder.");
    }

    private void runCase(TestCase testCase, String id) {
        Scheduler acs = new ACSScheduler(new AgingPolicy(AgingPolicy.DEFAULT_INTERVAL));
        Scheduler sjf = new SJFScheduler();
        int[] ownQuantum = { testCase.getQuantum() };

        List<ExperimentRow> acsGrid = run(testCase, acs, DEFAULT_QUANTUMS, DEFAULT_CONTEXT_SWITCHES);
        List<ExperimentRow> acsOwnQ = run(testCase, acs, ownQuantum, DEFAULT_CONTEXT_SWITCHES);
        List<ExperimentRow> sjfRows = run(testCase, sjf, ownQuantum, DEFAULT_CONTEXT_SWITCHES);

        writeCsv(id + "_ACS.csv", acsGrid);
        writeCsv(id + "_SJF.csv", sjfRows);

        String name = testCase.getName();
        writeChart(id + "_ACS_vs_Q_CS" + CHART_CS + ".png",
                name + " - ACS: averages vs Q (CS = " + CHART_CS + ")",
                rowsWithContextSwitch(acsGrid, CHART_CS), true);
        writeChart(id + "_ACS_vs_CS_Q" + testCase.getQuantum() + ".png",
                name + " - ACS: averages vs CS (Q = " + testCase.getQuantum() + ")", acsOwnQ, false);
        writeChart(id + "_SJF_vs_CS.png", name + " - SJF: averages vs CS", sjfRows, false);
    }

    private static List<ExperimentRow> rowsWithContextSwitch(List<ExperimentRow> rows, int contextSwitch) {
        List<ExperimentRow> selected = new ArrayList<>();
        for (ExperimentRow row : rows) {
            if (row.getContextSwitch() == contextSwitch) selected.add(row);
        }
        return selected;
    }

    private static void writeCsv(String fileName, List<ExperimentRow> rows) {
        String path = RESULTS_FOLDER + "/" + fileName;
        CsvWriter.write(path, rows);
        System.out.println("Wrote " + path);
    }

    private static void writeChart(String fileName, String title, List<ExperimentRow> rows, boolean xIsQuantum) {
        String path = RESULTS_FOLDER + "/" + fileName;
        ChartFrame.renderPng(title, rows, xIsQuantum, path);
        System.out.println("Wrote " + path);
    }
}
