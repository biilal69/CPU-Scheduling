import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SimulatorFrame extends JFrame {

    /** Main supplies this so the GUI never duplicates scheduling logic. */
    public interface Runner {
        SimulationResult run(TestCase tc, String scheduler, int quantum, int cs);
    }

    private final JComboBox<String> caseBox = new JComboBox<>();
    private final JComboBox<String> schedBox = new JComboBox<>(new String[]{"Preemptive SJF", "ACS"});
    private final JSpinner qSpin = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JSpinner csSpin = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
    private final TimelineTableModel timelineModel = new TimelineTableModel();
    private final DefaultListModel<String> metricsModel = new DefaultListModel<>();
    private final Runner runner;

    public SimulatorFrame(Runner runner) {
        super("Cloud-Cluster CPU Scheduler");
        this.runner = runner;

        for (int i = 0; i < SampleCases.count(); i++) caseBox.addItem(SampleCases.byIndex(i).name);
        caseBox.addActionListener(e -> loadDefaults());
        loadDefaults();

        JButton run = new JButton("Run");
        run.addActionListener(e -> runSimulation());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Test case:")); top.add(caseBox);
        top.add(new JLabel("Scheduler:")); top.add(schedBox);
        top.add(new JLabel("Q:")); top.add(qSpin);
        top.add(new JLabel("CS:")); top.add(csSpin);
        top.add(run);

        JTable table = new JTable(timelineModel);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(80);
        JList<String> metricsList = new JList<>(metricsModel);
        metricsList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane metricsScroll = new JScrollPane(metricsList);
        metricsScroll.setPreferredSize(new Dimension(0, 150));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(metricsScroll, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);
    }

    private void loadDefaults() {
        TestCase tc = SampleCases.byIndex(caseBox.getSelectedIndex());
        qSpin.setValue(tc.quantum);
        csSpin.setValue(tc.contextSwitch);
    }

    private void runSimulation() {
        TestCase tc = SampleCases.byIndex(caseBox.getSelectedIndex()); // fresh copy
        int q = (Integer) qSpin.getValue();
        int cs = (Integer) csSpin.getValue();
        SimulationResult res = runner.run(tc, (String) schedBox.getSelectedItem(), q, cs);

        timelineModel.setRows(res.timeline);

        List<ProcessMetrics> ms = MetricsCalculator.calculate(res.processes);
        metricsModel.clear();
        metricsModel.addElement(String.format("%-8s %-12s %-10s %-10s", "Process", "Turnaround", "Waiting", "Response"));
        for (ProcessMetrics m : ms)
            metricsModel.addElement(String.format("%-8s %-12d %-10d %-10d", m.name, m.turnaround, m.waiting, m.response));
        metricsModel.addElement(String.format("Avg Turnaround: %.2f   Avg Waiting: %.2f",
                MetricsCalculator.averageTurnaround(ms), MetricsCalculator.averageWaiting(ms)));
    }
}
