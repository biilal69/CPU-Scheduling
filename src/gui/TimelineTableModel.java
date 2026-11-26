package gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.TimeStep;

public class TimelineTableModel extends AbstractTableModel {
    private static final String[] COLS = {"Time", "Running", "Event", "Queues"};
    private List<TimeStep> steps = new ArrayList<>();

    public void setSteps(List<TimeStep> steps) {
        this.steps = steps;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return steps.size(); }
    @Override public int getColumnCount() { return COLS.length; }
    @Override public String getColumnName(int c) { return COLS[c]; }

    @Override public Object getValueAt(int r, int c) {
        TimeStep s = steps.get(r);
        switch (c) {
            case 0: return s.getTime();
            case 1: return s.getRunning();
            case 2: return s.getEvent();
            default: return "<html>" + String.join("<br>", s.getQueueLines()) + "</html>";
        }
    }
}