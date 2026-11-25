import javax.swing.table.AbstractTableModel;
import java.util.*;

public class TimelineTableModel extends AbstractTableModel {
    private static final String[] COLS = {"Time", "Process", "Tier 1", "Tier 2"};
    private List<TimelineEntry> rows = new ArrayList<>();

    public void setRows(List<TimelineEntry> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLS.length; }
    @Override public String getColumnName(int c) { return COLS[c]; }

    @Override public Object getValueAt(int r, int c) {
        TimelineEntry e = rows.get(r);
        switch (c) {
            case 0: return e.time;
            case 1: return e.running;
            case 2: return e.tier1;
            default: return e.tier2;
        }
    }
}
