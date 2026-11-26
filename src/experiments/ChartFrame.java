package experiments;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** Simple Java2D line chart: average waiting and average turnaround against Q or CS. */
public class ChartFrame extends JFrame {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 560;
    private static final int LEFT = 80;
    private static final int RIGHT = 30;
    private static final int TOP = 80;
    private static final int BOTTOM = 70;
    private static final int X_PAD = 24;
    private static final Color WAIT_COLOR = new Color(0x1F77B4);
    private static final Color TURN_COLOR = new Color(0xD95F02);
    private static final Color GRID_COLOR = new Color(0xDDDDDD);

    private final String title;
    private final List<ExperimentRow> rows;
    private final boolean xIsQuantum;

    /** Builds the (not yet visible) window; x axis is Q when xIsQuantum is true, otherwise CS. */
    public ChartFrame(String title, List<ExperimentRow> rows, boolean xIsQuantum) {
        super(title);
        this.title = title;
        this.rows = new ArrayList<>(rows);
        this.xIsQuantum = xIsQuantum;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(new ChartPanel());
        pack();
    }

    /** Saves the chart as a PNG file, creating missing folders. */
    public void savePng(String path) {
        renderPng(title, rows, xIsQuantum, path);
    }

    /** Package-private, window-free PNG rendering so charts also work on machines without a display. */
    static void renderPng(String title, List<ExperimentRow> rows, boolean xIsQuantum, String path) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            draw(g, WIDTH, HEIGHT, title, rows, xIsQuantum);
        } finally {
            g.dispose();
        }
        File file = new File(path);
        try {
            File parent = file.getAbsoluteFile().getParentFile();
            if (parent != null) parent.mkdirs();
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write PNG file: " + path, e);
        }
    }

    /** Panel that paints the chart inside the window. */
    private class ChartPanel extends JPanel {
        ChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw((Graphics2D) g, getWidth(), getHeight(), title, rows, xIsQuantum);
        }
    }

    // ---------------------------------------------------------------- drawing

    private static void draw(Graphics2D g, int w, int h, String title,
                             List<ExperimentRow> rows, boolean xIsQuantum) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        drawTitle(g, w, title);
        drawLegend(g);
        if (rows.isEmpty()) {
            drawCentered(g, "No data", w / 2, h / 2);
            return;
        }
        TreeSet<Integer> xs = xValues(rows, xIsQuantum);
        double step = niceStep(maxValue(rows));
        double yMax = Math.ceil(maxValue(rows) / step) * step;
        int pw = w - LEFT - RIGHT;
        int ph = h - TOP - BOTTOM;
        drawGridAndTicks(g, xs, step, yMax, pw, ph);
        drawAxes(g, w, h, pw, ph, xIsQuantum);
        drawSeries(g, rows, xs, yMax, pw, ph, xIsQuantum, true, WAIT_COLOR);
        drawSeries(g, rows, xs, yMax, pw, ph, xIsQuantum, false, TURN_COLOR);
    }

    private static void drawTitle(Graphics2D g, int w, String title) {
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        drawCentered(g, title, w / 2, 30);
    }

    private static void drawLegend(Graphics2D g) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        int y = 58;
        int x = LEFT;
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(WAIT_COLOR);
        g.drawLine(x, y, x + 28, y);
        g.fillOval(x + 9, y - 5, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Average waiting time", x + 36, y + 5);
        x += 220;
        g.setColor(TURN_COLOR);
        g.drawLine(x, y, x + 28, y);
        g.fillRect(x + 9, y - 5, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Average turnaround time", x + 36, y + 5);
    }

    private static void drawGridAndTicks(Graphics2D g, TreeSet<Integer> xs, double step,
                                         double yMax, int pw, int ph) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setStroke(new BasicStroke(1f));
        String pattern = step >= 1 ? "%.0f" : (step >= 0.1 ? "%.1f" : "%.2f");
        for (double v = 0; v <= yMax + step / 2; v += step) {
            int y = yPixel(v, yMax, ph);
            g.setColor(GRID_COLOR);
            g.drawLine(LEFT, y, LEFT + pw, y);
            g.setColor(Color.BLACK);
            String label = String.format(Locale.US, pattern, v);
            g.drawString(label, LEFT - 8 - g.getFontMetrics().stringWidth(label), y + 4);
        }
        for (int x : xs) {
            int px = xPixel(x, xs, pw);
            g.setColor(GRID_COLOR);
            g.drawLine(px, TOP, px, TOP + ph);
            g.setColor(Color.BLACK);
            drawCentered(g, String.valueOf(x), px, TOP + ph + 20);
        }
    }

    private static void drawAxes(Graphics2D g, int w, int h, int pw, int ph, boolean xIsQuantum) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(LEFT, TOP, LEFT, TOP + ph);
        g.drawLine(LEFT, TOP + ph, LEFT + pw, TOP + ph);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        String xLabel = xIsQuantum ? "Quantum Q (time units)" : "Context switch CS (time units)";
        drawCentered(g, xLabel, LEFT + pw / 2, h - 16);
        AffineTransform saved = g.getTransform();
        g.rotate(-Math.PI / 2, 22, TOP + ph / 2);
        drawCentered(g, "Average time (time units)", 22, TOP + ph / 2);
        g.setTransform(saved);
    }

    private static void drawSeries(Graphics2D g, List<ExperimentRow> rows, TreeSet<Integer> xs,
                                   double yMax, int pw, int ph, boolean xIsQuantum,
                                   boolean waiting, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(2.5f));
        int prevX = 0;
        int prevY = 0;
        boolean first = true;
        for (ExperimentRow row : sortedByX(rows, xIsQuantum)) {
            int x = xPixel(xValue(row, xIsQuantum), xs, pw);
            int y = yPixel(waiting ? row.getAvgWaiting() : row.getAvgTurnaround(), yMax, ph);
            if (!first) g.drawLine(prevX, prevY, x, y);
            drawMarker(g, x, y, waiting);
            prevX = x;
            prevY = y;
            first = false;
        }
    }

    private static void drawMarker(Graphics2D g, int x, int y, boolean circle) {
        if (circle) g.fillOval(x - 5, y - 5, 10, 10);
        else g.fillRect(x - 5, y - 5, 10, 10);
    }

    private static void drawCentered(Graphics2D g, String text, int cx, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baselineY);
    }

    // ------------------------------------------------------------ data helpers

    private static int xValue(ExperimentRow row, boolean xIsQuantum) {
        return xIsQuantum ? row.getQuantum() : row.getContextSwitch();
    }

    private static TreeSet<Integer> xValues(List<ExperimentRow> rows, boolean xIsQuantum) {
        TreeSet<Integer> xs = new TreeSet<>();
        for (ExperimentRow row : rows) xs.add(xValue(row, xIsQuantum));
        return xs;
    }

    private static List<ExperimentRow> sortedByX(List<ExperimentRow> rows, boolean xIsQuantum) {
        List<ExperimentRow> sorted = new ArrayList<>(rows);
        sorted.sort((a, b) -> Integer.compare(xValue(a, xIsQuantum), xValue(b, xIsQuantum)));
        return sorted;
    }

    private static double maxValue(List<ExperimentRow> rows) {
        double max = 0;
        for (ExperimentRow row : rows) {
            max = Math.max(max, Math.max(row.getAvgWaiting(), row.getAvgTurnaround()));
        }
        return max > 0 ? max : 1;
    }

    /** Rounds max / 5 up to 1, 2 or 5 times a power of ten. */
    private static double niceStep(double max) {
        double raw = max / 5;
        double base = Math.pow(10, Math.floor(Math.log10(raw)));
        double f = raw / base;
        double nice = f <= 1 ? 1 : (f <= 2 ? 2 : (f <= 5 ? 5 : 10));
        return nice * base;
    }

    private static int xPixel(int x, TreeSet<Integer> xs, int pw) {
        int min = xs.first();
        int max = xs.last();
        if (min == max) return LEFT + pw / 2;
        double fraction = (double) (x - min) / (max - min);
        return (int) Math.round(LEFT + X_PAD + fraction * (pw - 2 * X_PAD));
    }

    private static int yPixel(double value, double yMax, int ph) {
        return (int) Math.round(TOP + ph - value / yMax * ph);
    }
}
