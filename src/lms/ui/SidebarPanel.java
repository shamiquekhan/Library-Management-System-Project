package lms.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Left navigation column. Menu entries are chosen by the signed-in role:
 * staff get the full administration set, members get their own view.
 * The active entry is highlighted with the accent color.
 */
public class SidebarPanel extends JPanel {

    private final Map<String, JButton> buttons = new LinkedHashMap<>();
    private String activeKey;

    /**
     * @param nav       receives the key of the requested page
     * @param onSignOut invoked when the user clicks Sign Out
     */
    public SidebarPanel(String role, Consumer<String> nav, Runnable onSignOut) {
        setPreferredSize(new Dimension(220, 0));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));
        setLayout(new BorderLayout());

        add(createBrand(), BorderLayout.NORTH);
        add(createMenu(role, nav, onSignOut), BorderLayout.CENTER);
    }

    private JPanel createBrand() {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 22, 20, 22));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("LIBRARY");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(UITheme.ACCENT);

        JLabel subtitle = new JLabel("Management System");
        subtitle.setFont(UITheme.SMALL);
        subtitle.setForeground(UITheme.MUTED);

        panel.add(title);
        panel.add(Box.createVerticalStrut(3));
        panel.add(subtitle);
        return panel;
    }

    private JPanel createMenu(String role, Consumer<String> nav, Runnable onSignOut) {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 14, 14, 14));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        switch (role) {
            case "LIBRARIAN":
                addItem(panel, "dashboard", "Dashboard", nav);
                addItem(panel, "books", "Books", nav);
                addItem(panel, "members", "Members", nav);
                addItem(panel, "circulation", "Circulation", nav);
                addItem(panel, "holds", "Holds", nav);
                addItem(panel, "fines", "Fines", nav);
                addItem(panel, "reports", "Reports", nav);
                addItem(panel, "settings", "Settings", nav);
                break;
            case "CLERK":
                addItem(panel, "dashboard", "Dashboard", nav);
                addItem(panel, "books", "Books", nav);
                addItem(panel, "members", "Members", nav);
                addItem(panel, "circulation", "Circulation", nav);
                addItem(panel, "holds", "Holds", nav);
                addItem(panel, "fines", "Fines", nav);
                addItem(panel, "settings", "Settings", nav);
                break;
            default: // MEMBER
                addItem(panel, "dashboard", "Dashboard", nav);
                addItem(panel, "books", "Catalog", nav);
                addItem(panel, "myloans", "My Loans", nav);
                addItem(panel, "myholds", "My Holds", nav);
                addItem(panel, "myfines", "My Fines", nav);
                addItem(panel, "settings", "Settings", nav);
                break;
        }

        panel.add(Box.createVerticalGlue());

        JButton signOut = new JButton("Sign Out");
        styleNavItem(signOut);
        signOut.setForeground(UITheme.DANGER);
        signOut.addActionListener(e -> onSignOut.run());
        panel.add(signOut);
        panel.add(Box.createVerticalStrut(8));

        return panel;
    }

    private void addItem(JPanel panel, String key, String label, Consumer<String> nav) {
        JButton button = new JButton(label);
        styleNavItem(button);
        button.addActionListener(e -> {
            setActive(key);
            nav.accept(key);
        });
        buttons.put(key, button);
        panel.add(button);
        panel.add(Box.createVerticalStrut(3));
    }

    private void styleNavItem(JButton button) {
        button.setFont(UITheme.BODY);
        button.setForeground(UITheme.TEXT);
        button.setBackground(UITheme.SURFACE);
        button.setHorizontalAlignment(JLabel.LEFT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    /** Highlights the given page key (used on navigation from code, too). */
    public void setActive(String key) {
        activeKey = key;
        for (Map.Entry<String, JButton> e : buttons.entrySet()) {
            boolean active = e.getKey().equals(key);
            JButton b = e.getValue();
            b.setBackground(active ? UITheme.ACCENT_LIGHT : UITheme.SURFACE);
            b.setForeground(active ? UITheme.ACCENT : UITheme.TEXT);
        }
    }

    /** Currently highlighted page key. */
    public String getActive() {
        return activeKey;
    }
}
