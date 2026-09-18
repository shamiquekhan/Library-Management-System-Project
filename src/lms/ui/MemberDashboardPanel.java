package lms.ui;

import lms.exception.LMSException;
import lms.model.HoldRequest;
import lms.model.Loan;
import lms.model.Member;
import lms.service.DashboardService;
import lms.service.LibraryService;
import lms.util.Config;
import lms.util.FineCalculator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Member-facing dashboard: personal KPIs (loans, overdue, fines) plus the
 * member's own loans and holds. Read-only — circulation actions live on the
 * staff screens and hold placement on My Holds.
 */
public class MemberDashboardPanel extends JPanel {

    private final Config config;
    private final DashboardService dashboard;
    private final LibraryService service;
    private final Member member;

    private StatCard loansCard;
    private StatCard overdueCard;
    private StatCard accruingCard;
    private StatCard unpaidCard;
    private JTable loansTable;
    private JTable holdsTable;
    private JLabel summary;

    public MemberDashboardPanel(Config config, DashboardService dashboard, LibraryService service, Member member) {
        this.config = config;
        this.dashboard = dashboard;
        this.service = service;
        this.member = member;

        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(0, 18));

        add(createStatsRow(), BorderLayout.NORTH);
        add(createTables(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        loansCard = new StatCard("My Loans", "–");
        overdueCard = new StatCard("Overdue", "–");
        accruingCard = new StatCard("Fines Accruing", "–");
        unpaidCard = new StatCard("Unpaid Recorded", "–");
        overdueCard.setValueColor(UITheme.DANGER);
        row.add(loansCard);
        row.add(overdueCard);
        row.add(accruingCard);
        row.add(unpaidCard);
        return row;
    }

    private JPanel createTables() {
        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);

        JPanel loans = sectionShell("My Loans");
        loansTable = new JTable();
        TableStyle.apply(loansTable);
        loansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loans.add(new JScrollPane(loansTable), BorderLayout.CENTER);

        JPanel holds = sectionShell("My Hold Requests");
        holdsTable = new JTable();
        TableStyle.apply(holdsTable);
        holdsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        holds.add(new JScrollPane(holdsTable), BorderLayout.CENTER);

        summary = new JLabel(" ");
        summary.setFont(UITheme.SMALL);
        summary.setForeground(UITheme.MUTED);
        loans.add(summary, BorderLayout.SOUTH);

        center.add(loans);
        center.add(holds);
        return center;
    }

    /** Reloads all member data from the service layer. */
    public void refresh() {
        int memberId = member.getId();
        UiWorker.run(() -> {
            int loanCount = dashboard.getMemberLoanCount(memberId);
            int overdueCount = 0;
            double accruing = 0;
            for (Loan l : dashboard.getActiveLoans()) {
                if (l.getMemberId() == memberId) {
                    if (l.isOverdue()) {
                        overdueCount++;
                        accruing += FineCalculator.calculate(l.getDueDate(), lms.util.DateTimeUtil.today(),
                                dashboard.getFinePerDay());
                    }
                }
            }
            double unpaid = dashboard.getMemberUnpaidFines(memberId);
            var loans = service.getMemberLoans(memberId);
            var holds = service.getMemberHolds(memberId);
            return new Object[]{loanCount, overdueCount, accruing, unpaid, loans, holds};
        }, data -> {
            loansCard.setValue(String.valueOf(data[0]));
            overdueCard.setValue(String.valueOf(data[1]));
            accruingCard.setValue(FineCalculator.money((Double) data[2]));
            unpaidCard.setValue(FineCalculator.money((Double) data[3]));

            @SuppressWarnings("unchecked")
            java.util.List<Loan> loans = (java.util.List<Loan>) data[4];
            fillLoans(loans);

            @SuppressWarnings("unchecked")
            java.util.List<HoldRequest> holds = (java.util.List<HoldRequest>) data[5];
            fillHolds(holds);
        });
    }

    private void fillLoans(java.util.List<Loan> loans) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Loan", "Book", "Issued", "Due", "Renewed", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (Loan l : loans) {
            model.addRow(new Object[]{
                    "#" + l.getId(),
                    l.getBookTitle() != null ? l.getBookTitle() : ("Book " + l.getBookId()),
                    lms.util.DateTimeUtil.format(l.getIssueDate()),
                    lms.util.DateTimeUtil.format(l.getDueDate()),
                    l.getRenewedCount() + "/2",
                    l.isOverdue() ? "OVERDUE" : "ACTIVE"
            });
        }
        loansTable.setModel(model);
        summary.setText(loans.isEmpty()
                ? "You have no books on loan right now."
                : "Loan limit is " + config.getMaxLoansPerMember() + " books; renewals are limited to 2 per loan.");
    }

    private void fillHolds(java.util.List<HoldRequest> holds) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Hold", "Book", "Requested", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (HoldRequest h : holds) {
            model.addRow(new Object[]{
                    "#" + h.getId(),
                    h.getBookTitle() != null ? h.getBookTitle() : ("Book " + h.getBookId()),
                    lms.util.DateTimeUtil.format(h.getRequestDate()),
                    h.isFulfilled() ? "FULFILLED" : "PENDING"
            });
        }
        holdsTable.setModel(model);
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
