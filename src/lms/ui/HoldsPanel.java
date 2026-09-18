package lms.ui;

import lms.model.HoldRequest;
import lms.model.Member;
import lms.model.Person;
import lms.service.LibraryService;
import lms.util.Config;

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
 * Holds page. Staff mode shows the full pending queue (priority = request
 * date). Member mode lists the signed-in member's holds and lets them place
 * a hold by book ID — the same rules as the console (available books,
 * duplicates and limits are enforced by the service layer).
 */
public class HoldsPanel extends JPanel {

    private final LibraryService service;
    private final Person user;
    private final boolean memberMode;

    private final JTable table = new JTable();
    private final JLabel summary = new JLabel(" ");
    private final JTextField bookIdField = new JTextField(8);

    public HoldsPanel(Config config, LibraryService service, Person user, boolean memberMode) {
        this.service = service;
        this.user = user;
        this.memberMode = memberMode;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        add(SectionPanel.headerStrip(memberMode ? "My Holds" : "Holds", SectionPanel.todayLabel()),
                BorderLayout.NORTH);

        JPanel tableCard = buildTableCard();
        if (memberMode) {
            JPanel center = new JPanel(new BorderLayout(0, 12));
            center.setOpaque(false);
            center.add(buildActionBar(), BorderLayout.NORTH);
            center.add(tableCard, BorderLayout.CENTER);
            add(center, BorderLayout.CENTER);
        } else {
            add(tableCard, BorderLayout.CENTER);
        }

        refresh();
    }

    private JPanel buildActionBar() {
        JPanel actionCard = new JPanel(new BorderLayout());
        actionCard.setBackground(UITheme.SURFACE);
        actionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.add(UITheme.fieldLabel("Book ID"));
        bookIdField.setFont(UITheme.BODY);
        row.add(bookIdField);
        JButton placeButton = new JButton("Place Hold");
        UITheme.stylePrimaryButton(placeButton);
        placeButton.addActionListener(e -> placeHold());
        row.add(placeButton);

        actionCard.add(row, BorderLayout.CENTER);
        return actionCard;
    }

    private JPanel buildTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UITheme.SURFACE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel heading = new JLabel(memberMode ? "My Hold Requests" : "Pending Hold Queue (priority order)");
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

    private void placeHold() {
        if (!(user instanceof Member member)) {
            return;
        }
        int bookId;
        try {
            bookId = Integer.parseInt(bookIdField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Book ID must be a number.", "Place Hold",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        UiWorker.run(() -> service.placeHold(bookId, member.getId()), position -> {
            JOptionPane.showMessageDialog(this,
                    "Hold placed. You are number " + position + " in the queue.",
                    "Place Hold", JOptionPane.INFORMATION_MESSAGE);
            bookIdField.setText("");
            refresh();
        });
    }

    /** Reloads the hold table from the service layer. */
    public void refresh() {
        UiWorker.run(() -> memberMode
                ? service.getMemberHolds(((Member) user).getId())
                : service.getAllPendingHolds(), this::render);
    }

    private void render(List<HoldRequest> holds) {
        Object[] columns = memberMode
                ? new Object[]{"Hold", "Book", "Requested", "Status"}
                : new Object[]{"Hold", "Book", "Member", "Requested", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (HoldRequest h : holds) {
            Object[] row = memberMode
                    ? new Object[]{
                            "#" + h.getId(),
                            h.getBookTitle() != null ? h.getBookTitle() : ("Book " + h.getBookId()),
                            lms.util.DateTimeUtil.format(h.getRequestDate()),
                            h.isFulfilled() ? "FULFILLED" : "PENDING"}
                    : new Object[]{
                            "#" + h.getId(),
                            h.getBookTitle() != null ? h.getBookTitle() : ("Book " + h.getBookId()),
                            h.getMemberName() != null ? h.getMemberName() : ("Member " + h.getMemberId()),
                            lms.util.DateTimeUtil.format(h.getRequestDate()),
                            h.isFulfilled() ? "FULFILLED" : "PENDING"};
            model.addRow(row);
        }
        table.setModel(model);
        summary.setText(holds.isEmpty()
                ? "No pending hold requests."
                : holds.size() + " hold request(s). Earliest request is fulfilled first when the book is returned.");
    }
}
