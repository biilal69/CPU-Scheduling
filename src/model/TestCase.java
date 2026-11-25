import java.util.List;

public class TestCase {
    public final String name;
    public final List<Process> processes;
    public final int quantum, contextSwitch;

    public TestCase(String name, List<Process> processes, int quantum, int contextSwitch) {
        this.name = name; this.processes = processes;
        this.quantum = quantum; this.contextSwitch = contextSwitch;
    }
}
