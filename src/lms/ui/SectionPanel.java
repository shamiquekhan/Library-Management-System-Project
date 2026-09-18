package lms.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * White content section with a Serif heading and an optional button row on
 * the right. Building block for dashboard sections and list pages.
 */
public class SectionPanel extends JPanel {

    private final JPanel content;
    private final JPanel buttonRow;

    public SectionPanel(String title) {
        this(title, null, null);
    }

    public SectionPanel(String title, String[] buttons, Consumer<String> handler) {
        super(new BorderLayout());
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel heading = new JLabel(title);
        heading.setFont(UITheme.SECTION);
        heading.setForeground(UITheme.TEXT);
        header.add(heading, BorderLayout.WEST);

        buttonRow = new JPanel();
        buttonRow.setOpaque(false);
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        if (buttons != null) {
            for (String label : buttons) {
                JButton b = new JButton(label);
                UITheme.styleButton(b);
                b.addActionListener(e -> handler.accept(label));
                buttonRow.add(b);
                buttonRow.add(Box.createHorizontalStrut(8));
            }
        }
        header.add(buttonRow, BorderLayout.EAST);

        content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(Box.createVerticalStrut(12), BorderLayout.NORTH);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    /** Sets the main body of this section. */
    public SectionPanel body(JPanel panel) {
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
        return this;
    }

    /** Access to the trailing button row for later mutation. */
    public JPanel getButtonRow() {
        return buttonRow;
    }

    /** Standard helper for list pages: heading on the left, date/subtitle on the right. */
    public static JPanel headerStrip(String left, String right) {
        JPanel strip = new JPanel(new BorderLayout());
        strip.setOpaque(false);
        strip.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel l = new JLabel(left);
        l.setFont(UITheme.TITLE);
        l.setForeground(UITheme.TEXT);

        JLabel r = new JLabel(right);
        r.setFont(UITheme.SMALL);
        r.setForeground(UITheme.MUTED);

        strip.add(l, BorderLayout.WEST);
        strip.add(r, BorderLayout.EAST);
        strip.setAlignmentX(Component.LEFT_ALIGNMENT);
        return strip;
    }

    /** Today's date, formatted for the header strip. */
    public static String todayLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
}
