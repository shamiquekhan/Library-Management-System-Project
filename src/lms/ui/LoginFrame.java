package lms.ui;

import lms.exception.LMSException;
import lms.model.Person;
import lms.service.DashboardService;
import lms.service.LibraryService;
import lms.util.Config;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Swing sign-in screen. Replaces the console role menu: the user picks a role
 * (Librarian / Clerk / Member) and credentials are checked against SQLite via
 * the existing {@link LibraryService#login}. The database call runs off the
 * EDT through {@link UiWorker}.
 */
public class LoginFrame extends JFrame {

    private final LibraryService service;
    private final Config config;
    private final DashboardService dashboardService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JComboBox<String> roleBox;
    private final JButton loginButton;

    public LoginFrame(Config config, LibraryService service, DashboardService dashboardService) {
        this.service = service;
        this.config = config;
        this.dashboardService = dashboardService;

        setTitle("Library Management System — Sign In");
        setSize(460, 470);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        JPanel card = new JPanel();
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(40, 48, 40, 48)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(400, 380));

        JLabel title = new JLabel("LIBRARY");
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setForeground(UITheme.ACCENT);
        title.setAlignmentX(0.5f);

        JLabel subtitle = new JLabel("Library Management System");
        subtitle.setFont(UITheme.BODY);
        subtitle.setForeground(UITheme.MUTED);
        subtitle.setAlignmentX(0.5f);

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        roleBox = new JComboBox<>(new String[]{"Librarian", "Clerk", "Member"});
        loginButton = new JButton("SIGN IN");
        UITheme.stylePrimaryButton(loginButton);
        loginButton.setAlignmentX(0.5f);
        loginButton.addActionListener(e -> login());

        // Enter key submits from either field.
        passwordField.addActionListener(e -> login());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(UITheme.fieldLabel("Sign in as"));
        card.add(roleBox);
        card.add(Box.createVerticalStrut(14));
        card.add(UITheme.fieldLabel("Username"));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(UITheme.fieldLabel("Password"));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginButton);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BACKGROUND);
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        center.add(card, BorderLayout.CENTER);

        JLabel hint = new JLabel("Demo logins: admin/admin123 · clerk/clerk123 · alice/alice123 · bobby/bobby123");
        hint.setFont(UITheme.SMALL);
        hint.setForeground(UITheme.MUTED);
        hint.setHorizontalAlignment(JLabel.CENTER);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        center.add(hint, BorderLayout.SOUTH);

        root.add(center, BorderLayout.CENTER);
        add(root);
    }

    private void login() {
        final String username = usernameField.getText().trim();
        final String password = new String(passwordField.getPassword());
        final String role = ((String) roleBox.getSelectedItem()).toUpperCase();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.", "Sign In",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        UiWorker.run(() -> service.login(username, password, role), person -> {
            loginButton.setEnabled(true);
            if (person != null) {
                onLoginSuccess(person);
            }
        });
    }

    private void onLoginSuccess(Person person) {
        dispose();
        new DashboardFrame(person, config, service, dashboardService).setVisible(true);
    }
}
