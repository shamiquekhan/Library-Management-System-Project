package lms.ui;

import lms.exception.LMSException;
import lms.model.Loan;
import lms.service.DashboardService;
import lms.util.FineCalculator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * Staff overview: live KPI cards, subject chart, recent activity from the
 * activity log, and the "loans needing attention" table. All numbers come
 * from {@link DashboardService} (SQLite) — nothing is hard-coded.
 */
public class DashboardPanel extends JPanel {

    private final DashboardService dashboard;
    private final String role;

    private StatCard booksCard;
    private StatCard membersCard;
    private StatCard loansCard;
    private StatCard overdueCard;
    private SubjectChartPanel chartPanel;
    private JPanel activityList;
    private JTable attentionTable;
    private JLabel attentionSummary;

    public DashboardPanel(DashboardService dashboard, String role) {
        this.dashboard = dashboard;
        this.role = role;

        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(0, 18));

        add(createStatsRow(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        booksCard = new StatCard("Total Books", "–");
        membersCard = new StatCard("Members", "–");
        loansCard = new StatCard("Active Loans", "–");
        overdueCard = new StatCard("Overdue", "–");
        overdueCard.setValueColor(UITheme.DANGER);
        row.add(booksCard);
        row.add(membersCard);
        row.add(loansCard);
        row.add(overdueCard);
        return row;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 18, 0));
        top.setOpaque(false);

        chartPanel = new SubjectChartPanel();

        JPanel activity = sectionShell("Recent Activity");
        activityList = new JPanel();
        activityList.setOpaque(false);
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activity.add(new JScrollPane(activityList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        top.add(chartPanel);
        top.add(activity);

        JPanel attention = sectionShell("Loans Needing Attention");
        attentionTable = new JTable();
        TableStyle.apply(attentionTable);
        attentionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attentionSummary = new JLabel(" ");
        attentionSummary.setFont(UITheme.SMALL);
        attentionSummary.setForeground(UITheme.MUTED);
        attention.add(attentionSummary, BorderLayout.SOUTH);
        attention.add(new JScrollPane(attentionTable), BorderLayout.CENTER);

        center.add(top, BorderLayout.NORTH);
        center.add(attention, BorderLayout.CENTER);
        return center;
    }

    /** Reloads every number from the service layer. */
    public void refresh() {
        UiWorker.run(() -> {
            int books = dashboard.getBookCount();
            int members = dashboard.getMemberCount();
            int active = dashboard.getActiveLoansCount();
            int overdue = dashboard.getOverdueLoansCount();
            var subjects = dashboard.getSubjectStats();
            var overdueLoans = dashboard.getOverdueLoans();
            var activity = lms.util.ActivityLog.recent();
            return new Object[]{books, members, active, overdue, subjects, overdueLoans, activity};
        }, data -> {
            booksCard.setValue(String.valueOf(data[0]));
            membersCard.setValue(String.valueOf(data[1]));
            loansCard.setValue(String.valueOf(data[2]));
            overdueCard.setValue(String.valueOf(data[3]));

            @SuppressWarnings("unchecked")
            java.util.Map<String, Integer> subjects = (java.util.Map<String, Integer>) data[4];
            chartPanel.setData(subjects);

            @SuppressWarnings("unchecked")
            java.util.List<String> logLines = (java.util.List<String>) data[6];
            fillActivity(logLines);

            @SuppressWarnings("unchecked")
            java.util.List<Loan> overdueLoans = (java.util.List<Loan>) data[5];
            fillAttention(overdueLoans);
        });
    }

    private void fillActivity(java.util.List<String> lines) {
        activityList.removeAll();
        if (lines.isEmpty()) {
            activityList.add(mutedLine("No recent activity."));
        }
        // Newest first, limited to keep the panel tidy.
        for (int i = lines.size() - 1; i >= 0 && i > lines.size() - 8; i--) {
            String line = lines.get(i);
            int close = line.indexOf(']');
            String when = close > 0 ? line.substring(1, close) : "";
            String what = close > 0 ? line.substring(close + 1).trim() : line;
            JLabel label = new JLabel("<html><b>" + escape(what) + "</b><br><span style='color:#6E6A60'>"
                    + escape(when) + "</span></html>");
            label.setFont(UITheme.BODY);
            label.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
            activityList.add(label);
        }
        activityList.revalidate();
        activityList.repaint();
    }

    private void fillAttention(java.util.List<Loan> overdueLoans) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Loan", "Book", "Member", "Due", "Days Late", "Accruing"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        double accruing = 0;
        for (Loan l : overdueLoans) {
            long late = Math.max(0, lms.util.DateTimeUtil.daysBetween(l.getDueDate(), lms.util.DateTimeUtil.today()));
            double fine = FineCalculator.calculate(l.getDueDate(), lms.util.DateTimeUtil.today(), dashboard.getFinePerDay());
            accruing += fine;
            model.addRow(new Object[]{
                    "#" + l.getId(),
                    l.getBookTitle() != null ? l.getBookTitle() : ("Book " + l.getBookId()),
                    l.getMemberName() != null ? l.getMemberName() : ("Member " + l.getMemberId()),
                    lms.util.DateTimeUtil.format(l.getDueDate()),
                    late + "d",
                    FineCalculator.money(fine)
            });
        }
        attentionTable.setModel(model);
        attentionSummary.setText(overdueLoans.isEmpty()
                ? "No overdue loans. All loans are on schedule."
                : overdueLoans.size() + " overdue loan(s) — accruing " + FineCalculator.money(accruing)
                + " at " + FineCalculator.money(dashboard.getFinePerDay()) + "/day.");
    }

    private JLabel mutedLine(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.SMALL);
        l.setForeground(UITheme.MUTED);
        return l;
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JPanel sectionShell(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        JLabel heading = new JLabel(title);
        heading.setFont(UITheme.SECTION);
        heading.setForeground(UITheme.TEXT);
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }
}
