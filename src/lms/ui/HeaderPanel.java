package lms.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;

/**
 * Top strip of the main window: page title on the left, today's date on the
 * right. The title is updated by the sidebar as the user navigates.
 */
public class HeaderPanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel userLabel;

    public HeaderPanel(String userName, String role) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)));

        titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(UITheme.TITLE);
        titleLabel.setForeground(UITheme.TEXT);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        userLabel = new JLabel(userName + "  ·  " + role);
        userLabel.setFont(UITheme.SMALL);
        userLabel.setForeground(UITheme.MUTED);
        userLabel.setHorizontalAlignment(JLabel.RIGHT);

        JLabel dateLabel = new JLabel(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setFont(UITheme.SMALL);
        dateLabel.setForeground(UITheme.MUTED);
        dateLabel.setHorizontalAlignment(JLabel.RIGHT);

        right.add(userLabel);
        right.add(dateLabel);

        add(titleLabel, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    /** Updates the page title shown in the header. */
    public void setPageTitle(String title) {
        titleLabel.setText(title);
    }
}
