package experiments;

/** One experiment result: the averages obtained for one (quantum, context switch) pair. */
public class ExperimentRow {
    private final int quantum;
    private final int contextSwitch;
    private final double avgWaiting;
    private final double avgTurnaround;

    /** Creates a row for the given quantum, context-switch cost and measured averages. */
    public ExperimentRow(int quantum, int contextSwitch, double avgWaiting, double avgTurnaround) {
        this.quantum = quantum;
        this.contextSwitch = contextSwitch;
        this.avgWaiting = avgWaiting;
        this.avgTurnaround = avgTurnaround;
    }

    /** Returns the Round Robin quantum used in this run. */
    public int getQuantum() { return quantum; }

    /** Returns the context-switch cost used in this run. */
    public int getContextSwitch() { return contextSwitch; }

    /** Returns the average waiting time of this run. */
    public double getAvgWaiting() { return avgWaiting; }

    /** Returns the average turnaround time of this run. */
    public double getAvgTurnaround() { return avgTurnaround; }
}
