package lms.ui;

import lms.model.Loan;
import lms.model.Member;
import lms.model.Person;
import lms.service.DashboardService;
import lms.service.LibraryService;
import lms.util.Config;
import lms.util.FineCalculator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Members administration page (staff only): lists all members with live loan
 * counts and recorded unpaid fines, supports registration and contact updates.
 */
public class MembersPanel extends JPanel {

    private final Config config;
    private final LibraryService service;
    private final DashboardService dashboard;

    private final JTable table = new JTable();
    private final JLabel countLabel = new JLabel(" ");
    private List<Person> members = List.of();

    public MembersPanel(Config config, LibraryService service, Person user) {
        this.config = config;
        this.service = service;
        this.dashboard = new DashboardService(config, service);

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        add(SectionPanel.headerStrip("Members", SectionPanel.todayLabel()), BorderLayout.NORTH);

        JPanel toolbar = new JPanel();
        toolbar.setOpaque(false);
        toolbar.setLayout(new javax.swing.BoxLayout(toolbar, javax.swing.BoxLayout.X_AXIS));
        JButton addButton = new JButton("Register Member");
        UITheme.stylePrimaryButton(addButton);
        addButton.addActionListener(e -> registerDialog());
        JButton contactButton = new JButton("Update Contact");
        UITheme.styleButton(contactButton);
        contactButton.addActionListener(e -> updateContactDialog());
        JButton detailsButton = new JButton("View Details");
        UITheme.styleButton(detailsButton);
        detailsButton.addActionListener(e -> showDetails());
        toolbar.add(addButton);
        toolbar.add(javax.swing.Box.createHorizontalStrut(6));
        toolbar.add(contactButton);
        toolbar.add(javax.swing.Box.createHorizontalStrut(6));
        toolbar.add(detailsButton);
        toolbar.add(javax.swing.Box.createHorizontalGlue());
        add(toolbar, BorderLayout.NORTH);

        TableStyle.apply(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UITheme.SURFACE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        countLabel.setFont(UITheme.SMALL);
        countLabel.setForeground(UITheme.MUTED);
        countLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 0));
        tableCard.add(countLabel, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    /** Reloads the member table from the service layer. */
    public void refresh() {
        UiWorker.run(() -> {
            List<Person> people = service.getAllMembers();
            int[] loans = new int[people.size()];
            double[] fines = new double[people.size()];
            for (int i = 0; i < people.size(); i++) {
                loans[i] = dashboard.getMemberLoanCount(people.get(i).getId());
                fines[i] = dashboard.getMemberUnpaidFines(people.get(i).getId());
            }
            return new Object[]{people, loans, fines};
        }, data -> {
            @SuppressWarnings("unchecked")
            List<Person> people = (List<Person>) data[0];
            int[] loans = (int[]) data[1];
            double[] fines = (double[]) data[2];
            render(people, loans, fines);
        });
    }

    private void render(List<Person> people, int[] loans, double[] fines) {
        members = people;
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Username", "Phone", "Address", "Active Loans", "Unpaid Fine"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (int i = 0; i < people.size(); i++) {
            Person p = people.get(i);
            model.addRow(new Object[]{
                    p.getId(), p.getName(), p.getUsername(), p.getPhone(), p.getAddress(),
                    loans[i], FineCalculator.money(fines[i])
            });
        }
        table.setModel(model);
        countLabel.setText("Showing " + people.size() + " member(s).");
    }

    private Person selectedMember() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a member in the table first.", "Members",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return members.get(row);
    }

    private void registerDialog() {
        JTextField name = new JTextField();
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JTextField phone = new JTextField();
        JTextField address = new JTextField();
        Object[] form = {
                UITheme.fieldLabel("Name"), name,
                UITheme.fieldLabel("Username"), username,
                UITheme.fieldLabel("Password"), password,
                UITheme.fieldLabel("Phone"), phone,
                UITheme.fieldLabel("Address"), address
        };
        int ok = JOptionPane.showConfirmDialog(this, form, "Register Member", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        if (name.getText().trim().isEmpty() || username.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and username are required.", "Register Member",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Member m = new Member(0, name.getText().trim(), phone.getText().trim(), address.getText().trim(),
                username.getText().trim(), new String(password.getPassword()));
        UiWorker.run(() -> service.registerMember(m), id -> {
            JOptionPane.showMessageDialog(this, "Member registered with ID " + id + ".", "Register Member",
                    JOptionPane.INFORMATION_MESSAGE);
            refresh();
        });
    }

    private void updateContactDialog() {
        Person p = selectedMember();
        if (p == null) {
            return;
        }
        JTextField phone = new JTextField(p.getPhone());
        JTextField address = new JTextField(p.getAddress());
        Object[] form = {
                UITheme.fieldLabel("Phone"), phone,
                UITheme.fieldLabel("Address"), address
        };
        int ok = JOptionPane.showConfirmDialog(this, form, "Update Contact — " + p.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        UiWorker.runVoid(() -> service.updateMemberProfile(p.getId(), phone.getText().trim(), address.getText().trim()),
                this::refresh);
    }

    private void showDetails() {
        Person p = selectedMember();
        if (p == null) {
            return;
        }
        UiWorker.run(() -> {
            List<Loan> loans = service.getMemberLoans(p.getId());
            double unpaid = service.getOutstandingFines(p.getId());
            double accruing = 0;
            int overdue = 0;
            for (Loan l : loans) {
                if (l.isOverdue()) {
                    overdue++;
                    accruing += FineCalculator.calculate(l.getDueDate(), lms.util.DateTimeUtil.today(),
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

            StringBuilder sb = new StringBuilder();
            sb.append(p.getName()).append("  (ID ").append(p.getId()).append(")\n");
            sb.append(p.getUsername()).append("  ·  ").append(p.getPhone()).append("  ·  ")
                    .append(p.getAddress()).append("\n\n");
            sb.append("Active loans: ").append(loans.size()).append('\n');
            sb.append("Overdue: ").append(overdue).append('\n');
            sb.append("Fines accruing: ").append(FineCalculator.money(accruing)).append('\n');
            sb.append("Recorded unpaid: ").append(FineCalculator.money(unpaid)).append('\n');
            JOptionPane.showMessageDialog(this, sb.toString(), "Member Details", JOptionPane.PLAIN_MESSAGE);
        });
    }
}
