package lms.ui;

import lms.model.Loan;
import lms.model.Member;
import lms.model.Person;
import lms.service.LibraryService;
import lms.util.Config;
import lms.util.DateTimeUtil;
import lms.util.FineCalculator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

/** Member's "My Loans" page: active loans with due dates and overdue status. */
public class MemberLoansPanel extends JPanel {

    private final Config config;
    private final LibraryService service;
    private final Member member;

    private final JTable table = new JTable();
    private final JLabel summary = new JLabel(" ");

    public MemberLoansPanel(Config config, LibraryService service, Person user) {
        this.config = config;
        this.service = service;
        this.member = (Member) user;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        add(SectionPanel.headerStrip("My Loans", SectionPanel.todayLabel()), BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UITheme.SURFACE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel heading = new JLabel("Books Currently on Loan to You");
        heading.setFont(UITheme.SECTION);
        tableCard.add(heading, BorderLayout.NORTH);

        TableStyle.apply(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        summary.setFont(UITheme.SMALL);
        summary.setForeground(UITheme.MUTED);
        summary.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 0));
        tableCard.add(summary, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
        refresh();
    }

    /** Reloads the member's loans from the service layer. */
    public void refresh() {
        UiWorker.run(() -> service.getMemberLoans(member.getId()), this::render);
    }

    private void render(List<Loan> loans) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Loan", "Book", "Issued", "Due", "Renewed", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        int overdue = 0;
        for (Loan l : loans) {
            if (l.isOverdue()) {
                overdue++;
            }
            model.addRow(new Object[]{
                    "#" + l.getId(),
                    l.getBookTitle() != null ? l.getBookTitle() : ("Book " + l.getBookId()),
                    DateTimeUtil.format(l.getIssueDate()),
                    DateTimeUtil.format(l.getDueDate()),
                    l.getRenewedCount() + "/2",
                    l.isOverdue() ? "OVERDUE" : "ACTIVE"
            });
        }
        table.setModel(model);
        summary.setText(loans.size() + " active loan(s)" + (overdue > 0 ? ", " + overdue + " OVERDUE" : "")
                + ".  Loan period " + config.getLoanPeriodDays()
                + " days; overdue loans accrue " + FineCalculator.money(config.getFinePerDay()) + " per day.");
    }
}
