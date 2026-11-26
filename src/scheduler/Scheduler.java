package scheduler;

import java.util.List;
import model.Process;
import model.SimulationResult;

public interface Scheduler {
    String getName();

    SimulationResult run(List<Process> processes, int quantum, int contextSwitch);
}
