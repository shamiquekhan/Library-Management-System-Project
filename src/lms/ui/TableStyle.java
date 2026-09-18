package lms.ui;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/** Applies the shared light theme to {@link JTable} instances. */
public final class TableStyle {

    private TableStyle() {
    }

    public static void apply(JTable table) {
        table.setRowHeight(34);
        table.setFont(UITheme.BODY);
        table.setBackground(UITheme.SURFACE);
        table.setForeground(UITheme.TEXT);
        table.setGridColor(UITheme.BORDER);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.LABEL);
        header.setBackground(UITheme.SURFACE_ALT);
        header.setForeground(UITheme.TEXT);
        header.setReorderingAllowed(false);
    }
}
