package model;

import java.util.ArrayList;
import java.util.List;

public class TestCase {
    private final String name;
    private final int quantum;
    private final int contextSwitch;
    private final List<Process> processes;

    public TestCase(String name, int quantum, int contextSwitch, List<Process> processes) {
        this.name = name;
        this.quantum = quantum;
        this.contextSwitch = contextSwitch;
        this.processes = processes;
    }

    public String getName() { return name; }
    public int getQuantum() { return quantum; }
    public int getContextSwitch() { return contextSwitch; }

    public List<Process> freshProcesses() {
        List<Process> copies = new ArrayList<>();
        for (Process p : processes) copies.add(p.copy());
        return copies;
    }
}