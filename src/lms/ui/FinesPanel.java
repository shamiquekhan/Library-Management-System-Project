package lms.ui;

import lms.model.Loan;
import lms.model.Member;
import lms.model.Person;
import lms.service.DashboardService;
import lms.service.LibraryService;
import lms.util.Config;
import lms.util.DateTimeUtil;
import lms.util.FineCalculator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Fines page. Staff mode lists recorded unpaid fines per member and collects
 * them via {@link LibraryService#collectFine}. Member mode is read-only: the
 * signed-in member's accruing (overdue, still out) and recorded unpaid fines.
 */
public class FinesPanel extends JPanel {

    private final Config config;
    private final LibraryService service;
    private final DashboardService dashboard;
    private final Person user;
    private final boolean memberMode;

    private final JTable table = new JTable();
    private final JLabel summary = new JLabel(" ");
    private final JTextField memberField = new JTextField(8);

    public FinesPanel(Config config, LibraryService service, Person user, boolean memberMode) {
        this.config = config;
        this.service = service;
        this.dashboard = new DashboardService(config, service);
        this.user = user;
        this.memberMode = memberMode;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        add(SectionPanel.headerStrip(memberMode ? "My Fines" : "Fines", SectionPanel.todayLabel()),
                BorderLayout.NORTH);

        JPanel tableCard = buildTableCard();
        if (memberMode) {
            add(tableCard, BorderLayout.CENTER);
        } else {
            JPanel center = new JPanel(new BorderLayout(0, 12));
            center.setOpaque(false);
            center.add(buildCollectCard(), BorderLayout.NORTH);
            center.add(tableCard, BorderLayout.CENTER);
            add(center, BorderLayout.CENTER);
        }

        refresh();
    }

    private JPanel buildCollectCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.add(UITheme.fieldLabel("Member ID"));
        memberField.setFont(UITheme.BODY);
        row.add(memberField);
        JButton collectButton = new JButton("Collect Fine");
        UITheme.stylePrimaryButton(collectButton);
        collectButton.addActionListener(e -> collectFine());
        row.add(collectButton);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UITheme.SURFACE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel heading = new JLabel(memberMode ? "Overdue Loans Accruing Fines" : "Recorded Unpaid Fines by Member");
        heading.setFont(UITheme.SECTION);
        tableCard.add(heading, BorderLayout.NORTH);

        TableStyle.apply(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        summary.setFont(UITheme.SMALL);
        summary.setForeground(UITheme.MUTED);
        summary.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 0));
        tableCard.add(summary, BorderLayout.SOUTH);
        return tableCard;
    }

    private void collectFine() {
        int memberId;
        try {
            memberId = Integer.parseInt(memberField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Member ID must be a number.", "Collect Fine",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        UiWorker.run(() -> service.collectFine(memberId), total -> {
            JOptionPane.showMessageDialog(this,
                    "Collected " + FineCalculator.money(total) + " from member " + memberId + ".",
                    "Collect Fine", JOptionPane.INFORMATION_MESSAGE);
            memberField.setText("");
            refresh();
        });
    }

    /** Reloads the fines data from the service layer. */
    public void refresh() {
        if (memberMode) {
            UiWorker.run(() -> {
                Member m = (Member) user;
                List<Loan> loans = service.getMemberLoans(m.getId());
                double unpaid = service.getOutstandingFines(m.getId());
                double accruing = 0;
                int overdue = 0;
                for (Loan l : loans) {
                    if (l.isOverdue()) {
                        overdue++;
                        accruing += FineCalculator.calculate(l.getDueDate(), DateTimeUtil.today(),
                                config.getFinePerDay());
                    }
                }
                return new Object[]{loans, unpaid, accruing, overdue};
            }, data -> {
                @SuppressWarnings("unchecked")
                List<Loan> loans = (List<Loan>) data[0];
                double unpaid = (Double) data[1];
                double accruing = (Double) data[2];
                int overdue = (Integer) data[3];

                DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"Loan", "Book", "Due", "Days Late", "Accruing"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int col) {
                        return false;
                    }
                };
                for (Loan l : loans) {
                    if (!l.isOverdue()) {
                        continue;
                    }
                    long late = Math.max(0, DateTimeUtil.daysBetween(l.getDueDate(), DateTimeUtil.today()));
                    model.addRow(new Object[]{
                            "#" + l.getId(),
                            l.getBookTitle() != null ? l.getBookTitle() : ("Book " + l.getBookId()),
                            DateTimeUtil.format(l.getDueDate()),
                            late + "d",
                            FineCalculator.money(FineCalculator.calculate(l.getDueDate(), DateTimeUtil.today(),
                                    config.getFinePerDay()))
                    });
                }
                table.setModel(model);
                summary.setText("Accruing right now: " + FineCalculator.money(accruing)
                        + " (" + overdue + " overdue loan(s))  ·  Recorded unpaid: " + FineCalculator.money(unpaid)
                        + ".  Fines are payable at the circulation desk.");
            });
        } else {
            UiWorker.run(() -> {
                List<Person> members = service.getAllMembers();
                int n = members.size();
                int[] ids = new int[n];
                String[] names = new String[n];
                double[] unpaid = new double[n];
                double total = 0;
                for (int i = 0; i < n; i++) {
                    Person p = members.get(i);
                    ids[i] = p.getId();
                    names[i] = p.getName();
                    unpaid[i] = service.getOutstandingFines(p.getId());
                    total += unpaid[i];
                }
                return new Object[]{ids, names, unpaid, total};
            }, data -> {
                int[] ids = (int[]) data[0];
                String[] names = (String[]) data[1];
                double[] unpaid = (double[]) data[2];
                double total = (Double) data[3];

                DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"Member ID", "Member", "Unpaid Fine"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int col) {
                        return false;
                    }
                };
                for (int i = 0; i < ids.length; i++) {
                    if (unpaid[i] > 0) {
                        model.addRow(new Object[]{ids[i], names[i], FineCalculator.money(unpaid[i])});
                    }
                }
                table.setModel(model);
                summary.setText(table.getRowCount() + " member(s) with recorded unpaid fines, totalling "
                        + FineCalculator.money(total) + ". Accruing fines on overdue loans update automatically.");
            });
        }
    }
}
