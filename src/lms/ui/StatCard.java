package lms.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Reusable KPI tile: muted uppercase title over a large value.
 * Used across the staff and member dashboards.
 */
public class StatCard extends JPanel {

    private final JLabel valueLabel;

    public StatCard(String title, String value) {
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UITheme.LABEL);
        titleLabel.setForeground(UITheme.MUTED);

        valueLabel = new JLabel(value);
        valueLabel.setFont(UITheme.VALUE);
        valueLabel.setForeground(UITheme.TEXT);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(titleLabel);
        add(javax.swing.Box.createVerticalStrut(8));
        add(valueLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    /** Colors the value (e.g. red for overdue counts). */
    public void setValueColor(Color color) {
        valueLabel.setForeground(color);
    }

    /** Replaces the big value font (used for money amounts). */
    public void setValueFont(Font font) {
        valueLabel.setFont(font);
    }
}
