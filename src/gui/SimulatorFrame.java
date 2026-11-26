package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import metrics.MetricsCalculator;
import metrics.ProcessMetrics;
import model.SimulationResult;
import model.TestCase;
import output.SampleCases;
import scheduler.SJFScheduler;
import scheduler.Scheduler;
import scheduler.acs.ACSScheduler;
import scheduler.acs.AgingPolicy;

public class SimulatorFrame extends JFrame {

    private final JComboBox<String> caseBox = new JComboBox<>();
    private final JComboBox<String> schedBox = new JComboBox<>(new String[]{"SJF", "ACS"});
    private final JSpinner qSpin = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JSpinner csSpin = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
    private final TimelineTableModel timelineModel = new TimelineTableModel();
    private final DefaultListModel<String> metricsModel = new DefaultListModel<>();

    public SimulatorFrame() {
        super("Cloud-Cluster CPU Scheduler");

        for (TestCase tc : SampleCases.all()) caseBox.addItem(tc.getName());
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
        table.setRowHeight(34);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(80);
        table.getColumnModel().getColumn(2).setMaxWidth(260);
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
        TestCase tc = SampleCases.all().get(caseBox.getSelectedIndex());
        qSpin.setValue(tc.getQuantum());
        csSpin.setValue(tc.getContextSwitch());
    }

    private void runSimulation() {
        TestCase tc = SampleCases.all().get(caseBox.getSelectedIndex());
        int q = (Integer) qSpin.getValue();
        int cs = (Integer) csSpin.getValue();
        Scheduler scheduler = schedBox.getSelectedIndex() == 0
                ? new SJFScheduler()
                : new ACSScheduler(new AgingPolicy(AgingPolicy.DEFAULT_INTERVAL));
        SimulationResult res = scheduler.run(tc.freshProcesses(), q, cs);

        timelineModel.setSteps(res.getTimeline());

        List<ProcessMetrics> ms = MetricsCalculator.calculate(res.getFinished());
        metricsModel.clear();
        metricsModel.addElement(String.format("%-8s %-9s %-8s %-12s %-10s %-10s",
                "Process", "Arrival", "Finish", "Turnaround", "Waiting", "Response"));
        for (ProcessMetrics m : ms)
            metricsModel.addElement(String.format("%-8s %-9d %-8d %-12d %-10d %-10d",
                    m.getName(), m.getArrivalTime(), m.getFinishTime(), m.getTurnaroundTime(), m.getWaitingTime(), m.getResponseTime()));
        metricsModel.addElement(String.format(Locale.US, "Avg Turnaround: %.2f   Avg Waiting: %.2f",
                MetricsCalculator.averageTurnaround(res.getFinished()), MetricsCalculator.averageWaiting(res.getFinished())));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulatorFrame().setVisible(true));
    }
}