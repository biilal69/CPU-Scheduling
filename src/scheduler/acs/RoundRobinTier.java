package scheduler.acs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import model.Process;

public class RoundRobinTier {
    private final Queue<Process> queue = new ArrayDeque<>();
    private final int quantum;

    public RoundRobinTier(int quantum) {
        this.quantum = quantum;
    }

    public void add(Process p) {
        queue.add(p);
    }

    public Process poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int getQuantum() {
        return quantum;
    }

    public String snapshot() {
        if (queue.isEmpty()) {
            return "Tier 1: [Empty]";
        }
        List<String> items = new ArrayList<>();
        for (Process p : queue) {
            items.add(p.toString());
        }
        return "Tier 1: [" + String.join(", ", items) + "]";
    }
}