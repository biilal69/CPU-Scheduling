package experiments;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/** Writes experiment rows to a CSV file (static utility, no state). */
public class CsvWriter {
    private static final String HEADER = "quantum,contextSwitch,avgWaiting,avgTurnaround";

    /** Writes the header and one line per row to path, creating missing folders; decimals use Locale.US. */
    public static void write(String path, List<ExperimentRow> rows) {
        Path file = Paths.get(path);
        try {
            createParentFolder(file);
            try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                out.write(HEADER);
                out.newLine();
                for (ExperimentRow row : rows) {
                    out.write(toLine(row));
                    out.newLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write CSV file: " + path, e);
        }
    }

    private static void createParentFolder(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);
    }

    private static String toLine(ExperimentRow row) {
        return String.format(Locale.US, "%d,%d,%.2f,%.2f",
                row.getQuantum(), row.getContextSwitch(), row.getAvgWaiting(), row.getAvgTurnaround());
    }
}
