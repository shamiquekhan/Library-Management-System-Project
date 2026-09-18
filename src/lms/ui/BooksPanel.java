package lms.ui;

import lms.exception.LMSException;
import lms.model.Book;
import lms.model.Person;
import lms.service.LibraryService;
import lms.util.Config;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
 * Catalog page: search (title/author/subject) and full book CRUD. Staff can
 * add, edit and delete; members get a read-only catalog. All operations go
 * through {@link LibraryService} — the panel never touches SQL.
 */
public class BooksPanel extends JPanel {

    private final LibraryService service;
    private final Person user;

    private final JComboBox<String> fieldBox = new JComboBox<>(new String[]{"Title", "Author", "Subject"});
    private final JTextField searchField = new JTextField(18);
    private final JTable table = new JTable();
    private final JLabel countLabel = new JLabel(" ");

    public BooksPanel(Config config, LibraryService service, Person user) {
        this.service = service;
        this.user = user;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        add(SectionPanel.headerStrip(user.getRole().equals("MEMBER") ? "Catalog" : "Books",
                SectionPanel.todayLabel()), BorderLayout.NORTH);

        // --- Toolbar: search + actions -----------------------------------
        JPanel toolbar = new JPanel();
        toolbar.setOpaque(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));

        fieldBox.setFont(UITheme.BODY);
        searchField.setFont(UITheme.BODY);
        searchField.setMaximumSize(searchField.getPreferredSize());

        JButton searchButton = new JButton("Search");
        UITheme.stylePrimaryButton(searchButton);
        JButton clearButton = new JButton("Clear");
        UITheme.styleButton(clearButton);

        toolbar.add(fieldBox);
        toolbar.add(javax.swing.Box.createHorizontalStrut(6));
        toolbar.add(searchField);
        toolbar.add(javax.swing.Box.createHorizontalStrut(6));
        toolbar.add(searchButton);
        toolbar.add(javax.swing.Box.createHorizontalStrut(6));
        toolbar.add(clearButton);
        toolbar.add(Box.createHorizontalGlue());

        boolean staff = !"MEMBER".equals(user.getRole());
        if (staff) {
            JButton addButton = new JButton("Add Book");
            JButton editButton = new JButton("Edit");
            JButton deleteButton = new JButton("Delete");
            UITheme.stylePrimaryButton(addButton);
            UITheme.styleButton(editButton);
            UITheme.styleDangerButton(deleteButton);
            addButton.addActionListener(e -> addBookDialog());
            editButton.addActionListener(e -> editSelected());
            deleteButton.addActionListener(e -> deleteSelected());
            toolbar.add(addButton);
            toolbar.add(javax.swing.Box.createHorizontalStrut(6));
            toolbar.add(editButton);
            toolbar.add(javax.swing.Box.createHorizontalStrut(6));
            toolbar.add(deleteButton);
        }

        add(toolbar, BorderLayout.NORTH);

        // --- Table --------------------------------------------------------
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

        searchButton.addActionListener(e -> runSearch());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            fieldBox.setSelectedIndex(0);
            loadAll();
        });
        searchField.addActionListener(e -> runSearch());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && staff) {
                    editSelected();
                }
            }
        });

        loadAll();
    }

    private void loadAll() {
        UiWorker.run(service::getAllBooks, this::render);
    }

    private void runSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAll();
            return;
        }
        String field = ((String) fieldBox.getSelectedItem()).toLowerCase();
        UiWorker.run(() -> service.searchBooks(field, keyword), this::render);
    }

    private void render(List<Book> books) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "ISBN", "Title", "Author", "Subject", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (Book b : books) {
            model.addRow(new Object[]{b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(), b.getSubject(),
                    b.getStatus()});
        }
        table.setModel(model);
        countLabel.setText("Showing " + books.size() + " book(s).");
    }

    private Book selectedBook() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a book in the table first.", "Books",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int id = (Integer) table.getValueAt(row, 0);
        try {
            return service.getBookById(id);
        } catch (LMSException e) {
            UiWorker.showError(e);
            return null;
        }
    }

    private void addBookDialog() {
        JTextField isbn = new JTextField();
        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField subject = new JTextField();
        Object[] form = {
                UITheme.fieldLabel("ISBN"), isbn,
                UITheme.fieldLabel("Title"), title,
                UITheme.fieldLabel("Author"), author,
                UITheme.fieldLabel("Subject"), subject
        };
        int ok = JOptionPane.showConfirmDialog(this, form, "Add Book", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        if (isbn.getText().trim().isEmpty() || title.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ISBN and Title are required.", "Add Book",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Book b = new Book(0, isbn.getText().trim(), title.getText().trim(), author.getText().trim(),
                subject.getText().trim(), lms.model.BookStatus.AVAILABLE);
        UiWorker.run(() -> service.addBook(b), id -> {
            JOptionPane.showMessageDialog(this, "Book added with ID " + id + ".", "Add Book",
                    JOptionPane.INFORMATION_MESSAGE);
            loadAll();
        });
    }

    private void editSelected() {
        Book b = selectedBook();
        if (b == null) {
            return;
        }
        JTextField isbn = new JTextField(b.getIsbn());
        JTextField title = new JTextField(b.getTitle());
        JTextField author = new JTextField(b.getAuthor());
        JTextField subject = new JTextField(b.getSubject());
        Object[] form = {
                UITheme.fieldLabel("ISBN"), isbn,
                UITheme.fieldLabel("Title"), title,
                UITheme.fieldLabel("Author"), author,
                UITheme.fieldLabel("Subject"), subject
        };
        int ok = JOptionPane.showConfirmDialog(this, form, "Edit Book #" + b.getId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        b.setIsbn(isbn.getText().trim());
        b.setTitle(title.getText().trim());
        b.setAuthor(author.getText().trim());
        b.setSubject(subject.getText().trim());
        UiWorker.runVoid(() -> service.updateBook(b), this::loadAll);
    }

    private void deleteSelected() {
        Book b = selectedBook();
        if (b == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + b.getTitle() + "\"?", "Delete Book", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        UiWorker.runVoid(() -> service.deleteBook(b.getId()), this::loadAll);
    }
}
