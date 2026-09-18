package lms.ui;

import lms.model.Book;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hand-drawn horizontal bar chart of book counts per subject, painted with
 * plain Swing/AWT (no charting library). Data comes live from
 * {@code LibraryService.subjectStats()}.
 */
public class SubjectChartPanel extends JPanel {

    private Map<String, Integer> data = new LinkedHashMap<>();

    public SubjectChartPanel() {
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        setPreferredSize(new Dimension(380, 240));
    }

    /** Replaces the chart data and repaints. */
    public void setData(Map<String, Integer> subjectCounts) {
        this.data = subjectCounts == null ? new LinkedHashMap<>() : subjectCounts;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int x0 = 20;
        int labelW = Math.max(120, Math.min(180, w / 3));
        int trackX = x0 + labelW;
        int trackW = w - trackX - 40;
        if (trackW < 40) {
            trackW = 40;
        }

        g2.setColor(UITheme.TEXT);
        g2.setFont(UITheme.SECTION);
        g2.drawString("Books by Subject", x0, 28);

        if (data.isEmpty()) {
            g2.setFont(UITheme.SMALL);
            g2.setColor(UITheme.MUTED);
            g2.drawString("No books in catalog yet.", x0, 60);
            g2.dispose();
            return;
        }

        int max = 1;
        for (int v : data.values()) {
            max = Math.max(max, v);
        }

        int rows = data.size();
        int startY = 52;
        int rowH = rows > 0 ? Math.min(42, Math.max(22, (getHeight() - startY - 10) / rows)) : 24;
        int barH = Math.min(16, rowH - 6);

        g2.setFont(UITheme.SMALL);
        int y = startY;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            String label = e.getKey();
            if (label.length() > 20) {
                label = label.substring(0, 17) + "...";
            }
            g2.setColor(UITheme.MUTED);
            g2.drawString(label, x0, y + barH);

            g2.setColor(UITheme.ACCENT_LIGHT);
            g2.fillRect(trackX, y, trackW, barH);

            int barW = (int) Math.round(trackW * e.getValue() / (double) max);
            g2.setColor(UITheme.ACCENT);
            g2.fillRect(trackX, y, Math.max(barW, 2), barH);

            g2.setColor(UITheme.TEXT);
            String count = String.valueOf(e.getValue());
            g2.drawString(count, trackX + trackW + 6, y + barH);

            y += rowH;
        }
        g2.dispose();
    }

    /** Convenience: builds the subject-count map from a book list. */
    public static Map<String, Integer> countBySubject(java.util.List<Book> books) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Book b : books) {
            counts.merge(b.getSubject(), 1, Integer::sum);
        }
        return counts;
    }
}
