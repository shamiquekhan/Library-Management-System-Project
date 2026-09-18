package lms.ui;

import lms.model.Loan;
import lms.model.Person;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Circulation desk (clerks and librarians): issue, return and renew through
 * {@link LibraryService}, with the live active-loans table underneath.
 */
public class CirculationPanel extends JPanel {

    private final Config config;
    private final LibraryService service;
    private final Person user;

    private final JTextField memberField = new JTextField(8);
    private final JTextField bookField = new JTextField(8);
    private final JTextField returnField = new JTextField(8);
    private final JTextField renewField = new JTextField(8);
    private final JTable table = new JTable();
    private final JLabel summary = new JLabel(" ");

    public CirculationPanel(Config config, LibraryService service, Person user) {
        this.config = config;
        this.service = service;
        this.user = user;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        add(SectionPanel.headerStrip("Circulation", SectionPanel.todayLabel()), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(buildActions(), BorderLayout.NORTH);
        center.add(buildLoansTable(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new GridLayout(1, 2, 14, 0));
        actions.setOpaque(false);
        actions.add(issueCard());
        actions.add(returnRenewCard());
        return actions;
    }

    private JPanel issueCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Issue a Book");
        heading.setFont(UITheme.SECTION);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        card.add(heading, gc);

        gc.gridwidth = 1;
        gc.gridy = 1;
        card.add(UITheme.fieldLabel("Member ID"), gc);
        gc.gridx = 1;
        card.add(memberField, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        card.add(UITheme.fieldLabel("Book ID"), gc);
        gc.gridx = 1;
        card.add(bookField, gc);

        JButton issueButton = new JButton("Issue Book");
        UITheme.stylePrimaryButton(issueButton);
        issueButton.addActionListener(e -> issueBook());
        gc.gridx = 1;
        gc.gridy = 3;
        card.add(issueButton, gc);
        return card;
    }

    private JPanel returnRenewCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Return / Renew");
        heading.setFont(UITheme.SECTION);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        card.add(heading, gc);

        gc.gridwidth = 1;
        gc.gridy = 1;
        card.add(UITheme.fieldLabel("Book ID (return)"), gc);
        gc.gridx = 1;
        card.add(returnField, gc);

        JButton returnButton = new JButton("Return Book");
        UITheme.styleButton(returnButton);
        returnButton.addActionListener(e -> returnBook());
        gc.gridx = 1;
        gc.gridy = 2;
        card.add(returnButton, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        card.add(UITheme.fieldLabel("Book ID (renew)"), gc);
        gc.gridx = 1;
        card.add(renewField, gc);

        JButton renewButton = new JButton("Renew Loan");
        UITheme.styleButton(renewButton);
        renewButton.addActionListener(e -> renewLoan());
        gc.gridx = 1;
        gc.gridy = 4;
        card.add(renewButton, gc);
        return card;
    }

    private JPanel buildLoansTable() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel heading = new JLabel("Active Loans");
        heading.setFont(UITheme.SECTION);
        card.add(heading, BorderLayout.NORTH);

        TableStyle.apply(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        summary.setFont(UITheme.SMALL);
        summary.setForeground(UITheme.MUTED);
        summary.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 0));
        card.add(summary, BorderLayout.SOUTH);
        return card;
    }

    private void issueBook() {
        int memberId;
        int bookId;
        try {
            memberId = Integer.parseInt(memberField.getText().trim());
            bookId = Integer.parseInt(bookField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Member ID and Book ID must be numbers.", "Issue Book",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        UiWorker.run(() -> service.issueBook(bookId, memberId), loan -> {
            JOptionPane.showMessageDialog(this,
                    "Issued \"" + loan.getBookTitle() + "\". Due on " + DateTimeUtil.format(loan.getDueDate()) + ".",
                    "Issue Book", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        });
    }

    private void returnBook() {
        int bookId;
        try {
            bookId = Integer.parseInt(returnField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Book ID must be a number.", "Return Book",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        UiWorker.run(() -> service.returnBook(bookId), fine -> {
            String message = fine > 0
                    ? "Returned with fine " + FineCalculator.money(fine) + " (collect at Fines)."
                    : "Returned on time, no fine.";
            JOptionPane.showMessageDialog(this, message, "Return Book", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        });
    }

    private void renewLoan() {
        int bookId;
        try {
            bookId = Integer.parseInt(renewField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Book ID must be a number.", "Renew Loan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        UiWorker.run(() -> service.renewLoan(bookId), due -> {
            JOptionPane.showMessageDialog(this, "Renewed. New due date " + DateTimeUtil.format(due) + ".",
                    "Renew Loan", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        });
    }

    /** Reloads the active-loans table from the service layer. */
    public void refresh() {
        UiWorker.run(() -> {
            List<Loan> loans = service.getActiveLoans();
            List<Loan> overdue = service.getOverdueLoans();
            return new Object[]{loans, overdue.size()};
        }, data -> {
            @SuppressWarnings("unchecked")
            List<Loan> loans = (List<Loan>) data[0];
            int overdueCount = (Integer) data[1];

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Loan", "Book", "Member", "Issued", "Due", "Renewed", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            for (Loan l : loans) {
                model.addRow(new Object[]{
                        "#" + l.getId(),
                        l.getBookTitle() != null ? l.getBookTitle() : ("Book " + l.getBookId()),
                        l.getMemberName() != null ? l.getMemberName() : ("Member " + l.getMemberId()),
                        DateTimeUtil.format(l.getIssueDate()),
                        DateTimeUtil.format(l.getDueDate()),
                        l.getRenewedCount() + "/2",
                        l.isOverdue() ? "OVERDUE" : "ACTIVE"
                });
            }
            table.setModel(model);
            summary.setText(loans.size() + " active loan(s), " + overdueCount + " overdue. Loan period "
                    + config.getLoanPeriodDays() + " days, fine " + FineCalculator.money(config.getFinePerDay())
                    + "/day.");
        });
    }
}
