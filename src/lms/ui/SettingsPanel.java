package lms.ui;

import lms.model.Person;
import lms.service.LibraryService;
import lms.util.Config;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Settings page: change the signed-in user's password (validated by the
 * service) and view the library policy values loaded from config.
 */
public class SettingsPanel extends JPanel {

    private final Config config;
    private final LibraryService service;
    private final Person user;

    private final JPasswordField currentField = new JPasswordField();
    private final JPasswordField newField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();

    public SettingsPanel(Config config, LibraryService service, Person user) {
        this.config = config;
        this.service = service;
        this.user = user;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        add(SectionPanel.headerStrip("Settings", SectionPanel.todayLabel()), BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(passwordCard());
        row.add(policyCard());
        add(row, BorderLayout.NORTH);
    }

    private JPanel passwordCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Change Password — " + user.getName());
        heading.setFont(UITheme.SECTION);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        card.add(heading, gc);

        gc.gridwidth = 1;
        gc.gridy = 1;
        card.add(UITheme.fieldLabel("Current password"), gc);
        gc.gridx = 1;
        card.add(currentField, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        card.add(UITheme.fieldLabel("New password"), gc);
        gc.gridx = 1;
        card.add(newField, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        card.add(UITheme.fieldLabel("Confirm new"), gc);
        gc.gridx = 1;
        card.add(confirmField, gc);

        JButton save = new JButton("Save Password");
        UITheme.stylePrimaryButton(save);
        save.addActionListener(e -> changePassword());
        gc.gridx = 1;
        gc.gridy = 4;
        card.add(save, gc);
        return card;
    }

    private JPanel policyCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel("Library Policy");
        heading.setFont(UITheme.SECTION);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        card.add(heading, gc);

        gc.gridwidth = 1;
        String[][] values = {
                {"Loan period", config.getLoanPeriodDays() + " days"},
                {"Max loans per member", String.valueOf(config.getMaxLoansPerMember())},
                {"Fine per day", lms.util.FineCalculator.money(config.getFinePerDay())},
                {"Overdue scan interval", config.getOverdueCheckSeconds() + " s"},
                {"Backup interval", config.getBackupIntervalSeconds() + " s"},
                {"Database", config.getDbPath()}
        };
        for (int i = 0; i < values.length; i++) {
            gc.gridy = i + 1;
            gc.gridx = 0;
            JLabel k = new JLabel(values[i][0]);
            k.setFont(UITheme.LABEL);
            k.setForeground(UITheme.MUTED);
            card.add(k, gc);
            gc.gridx = 1;
            JLabel v = new JLabel(values[i][1]);
            v.setFont(UITheme.BODY);
            card.add(v, gc);
        }
        return card;
    }

    private void changePassword() {
        String current = new String(currentField.getPassword());
        String next = new String(newField.getPassword());
        String confirm = new String(confirmField.getPassword());
        if (current.isEmpty() || next.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in the current and new password.", "Change Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!next.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Change Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        UiWorker.runVoid(() -> service.changePassword(user.getId(), current, next), () -> {
            currentField.setText("");
            newField.setText("");
            confirmField.setText("");
            JOptionPane.showMessageDialog(this, "Password changed.", "Change Password",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
