public class TimelinePrinter {

    public static void print(SimulationResult result) {
        System.out.printf("%-6s %-8s %s%n", "Time", "Process", "Tier");
        System.out.println("-".repeat(80));
        for (TimelineEntry e : result.timeline) {
            String[] lines = {e.tier1, e.tier2};
            boolean first = true;
            for (String line : lines) {
                if (line == null || line.isEmpty()) continue;
                if (first) {
                    System.out.printf("%-6d %-8s %s%n", e.time, e.running == null ? "" : e.running, line);
                    first = false;
                } else {
                    System.out.printf("%-6s %-8s %s%n", "", "", line);
                }
            }
            if (first) { // both tier strings empty (continuation row)
                System.out.printf("%-6d %-8s%n", e.time, e.running == null ? "" : e.running);
            }
        }
        System.out.println();
    }
}
