package lms.ui;

import lms.reports.ReportGenerator;
import lms.service.LibraryService;
import lms.util.Config;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Reports page (librarian): generates the same CSV/TXT reports as the console
 * via {@link ReportGenerator} and shows where the file was written.
 */
public class ReportsPanel extends JPanel {

    private final Config config;
    private final LibraryService service;
    private final JTextArea output = new JTextArea(10, 40);

    public ReportsPanel(Config config, LibraryService service) {
        this.config = config;
        this.service = service;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        add(SectionPanel.headerStrip("Reports", SectionPanel.todayLabel()), BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel heading = new JLabel("Generate Reports");
        heading.setFont(UITheme.SECTION);
        card.add(heading, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 0));
        buttons.setOpaque(false);
        JButton booksButton = new JButton("Books CSV");
        JButton membersButton = new JButton("Members CSV");
        JButton loansButton = new JButton("Active Loans TXT");
        UITheme.styleButton(booksButton);
        UITheme.styleButton(membersButton);
        UITheme.styleButton(loansButton);
        booksButton.addActionListener(e -> generate("books"));
        membersButton.addActionListener(e -> generate("members"));
        loansButton.addActionListener(e -> generate("loans"));
        buttons.add(booksButton);
        buttons.add(membersButton);
        buttons.add(loansButton);

        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setOpaque(false);
        buttonRow.add(buttons, BorderLayout.NORTH);
        card.add(buttonRow, BorderLayout.CENTER);

        output.setEditable(false);
        output.setFont(UITheme.SMALL);
        output.setBackground(UITheme.SURFACE_ALT);
        output.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        output.setText("Generated reports are written to the '" + config.getReportsDir() + "/' directory.\n\n"
                + "  · Books CSV — full inventory (ID, ISBN, title, author, subject, status)\n"
                + "  · Members CSV — all registered members\n"
                + "  · Active Loans TXT — formatted loans table with status and fines");
        card.add(new JScrollPane(output), BorderLayout.SOUTH);

        add(card, BorderLayout.NORTH);
    }

    private void generate(String kind) {
        UiWorker.run(() -> {
            ReportGenerator rg = new ReportGenerator(config);
            return switch (kind) {
                case "books" -> rg.booksCsv(service.getAllBooks());
                case "members" -> rg.membersCsv(service.getAllMembers());
                default -> rg.loansTxt(service.getActiveLoans());
            };
        }, path -> output.append("\n[ok] Report written to " + path));
    }
}
