package lms.ui;

import lms.concurrency.BackupService;
import lms.exception.LMSException;
import lms.model.Book;
import lms.model.BookStatus;
import lms.model.Librarian;
import lms.model.Person;
import lms.reports.ReportGenerator;
import lms.service.LibraryService;
import lms.util.Config;
import lms.util.Console;
import lms.util.DateTimeUtil;
import lms.util.TablePrinter;

import java.time.LocalDate;
import java.util.Map;

public class LibrarianConsole {

    private final Config config;
    private final LibraryService service;
    private final BackupService backupService;
    private final Librarian librarian;

    public LibrarianConsole(Config config, LibraryService service, BackupService backupService, Librarian librarian) {
        this.config = config;
        this.service = service;
        this.backupService = backupService;
        this.librarian = librarian;
    }

    public void run() {
        int choice;
        do {
            System.out.println();
            System.out.println("----- LIBRARIAN MENU (" + librarian.getName() + ") -----");
            System.out.println("  1. Add Book");
            System.out.println("  2. Update Book Details");
            System.out.println("  3. Remove Book");
            System.out.println("  4. Search Books");
            System.out.println("  5. View All Books");
            System.out.println("  6. Register New Member");
            System.out.println("  7. View All Members");
            System.out.println("  8. View Active Loans");
            System.out.println("  9. View Overdue Loans");
            System.out.println(" 10. Subject Statistics");
            System.out.println(" 11. Import Books from CSV");
            System.out.println(" 12. Generate Reports");
            System.out.println(" 13. Backup Database Now");
            System.out.println(" 14. Change My Password");
            System.out.println(" 15. Dashboard");
            System.out.println("  0. Logout");
            choice = Console.readInt("Enter choice: ", 0, 15);
            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    updateBook();
                    break;
                case 3:
                    removeBook();
                    break;
                case 4:
                    ConsoleUI.searchFlow(service);
                    break;
                case 5:
                    ConsoleUI.viewAllBooksFlow(service);
                    break;
                case 6:
                    ConsoleUI.registerMemberFlow(service);
                    break;
                case 7:
                    viewMembers();
                    break;
                case 8:
                    viewLoans(false);
                    break;
                case 9:
                    viewLoans(true);
                    break;
                case 10:
                    showStatistics();
                    break;
                case 11:
                    importBooks();
                    break;
                case 12:
                    generateReports();
                    break;
                case 13:
                    runBackup();
                    break;
                case 14:
                    ConsoleUI.changePasswordFlow(service, librarian);
                    break;
                case 15:
                    new DashboardConsole(new lms.service.DashboardService(config, service)).runStaff();
                    break;
                case 0:
                    System.out.println("[ok] Logged out.");
                    break;
                default:
                    break;
            }
        } while (choice != 0);
    }

    private void addBook() {
        try {
            String isbn = Console.readNonEmpty("ISBN: ");
            String title = Console.readNonEmpty("Title: ");
            String author = Console.readNonEmpty("Author: ");
            String subject = Console.readNonEmpty("Subject: ");
            Book b = new lms.model.Book(0, isbn, title, author, subject, lms.model.BookStatus.AVAILABLE);
            int id = service.addBook(b);
            System.out.println("[ok] Book added with ID " + id + ".");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void updateBook() {
        try {
            int id = Console.readInt("Book ID: ", 1, 999999);
            Book b = service.getBookById(id);
            System.out.println("Leave a field blank to keep the current value.");
            String v = Console.readLine("ISBN [" + b.getIsbn() + "]: "); if (!v.isEmpty()) b.setIsbn(v);
            v = Console.readLine("Title [" + b.getTitle() + "]: "); if (!v.isEmpty()) b.setTitle(v);
            v = Console.readLine("Author [" + b.getAuthor() + "]: "); if (!v.isEmpty()) b.setAuthor(v);
            v = Console.readLine("Subject [" + b.getSubject() + "]: "); if (!v.isEmpty()) b.setSubject(v);
            service.updateBook(b);
            System.out.println("[ok] Book updated.");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void removeBook() {
        try {
            int id = Console.readInt("Book ID: ", 1, 999999);
            service.deleteBook(id);
            System.out.println("[ok] Book removed.");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void viewMembers() {
        try {
            TablePrinter.printMembers(service.getAllMembers());
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void viewLoans(boolean overdueOnly) {
        try {
            if (overdueOnly) {
                TablePrinter.printLoans(service.getOverdueLoans(), config.getFinePerDay());
            } else {
                TablePrinter.printLoans(service.getActiveLoans(), config.getFinePerDay());
            }
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void showStatistics() {
        try {
            Map<String, Integer> stats = service.subjectStats();
            System.out.println("  Books per subject:");
            int total = 0;
            for (Map.Entry<String, Integer> e : stats.entrySet()) {
                System.out.printf("   %-28s %d%n", e.getKey(), e.getValue());
                total += e.getValue();
            }
            System.out.println("  Total books: " + total);
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void importBooks() {
        try {
            String path = Console.readLine("CSV file path [import/sample_books.csv]: ");
            if (path.isEmpty()) path = "import/sample_books.csv";
            int n = service.importBooks(path);
            System.out.println("[ok] " + n + " book(s) imported.");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void generateReports() {
        ReportGenerator rg = new ReportGenerator(config);
        try {
            int t = Console.readInt("Report: 1. Books CSV  2. Members CSV  3. Active Loans TXT  Choice: ", 1, 3);
            String path;
            if (t == 1) path = rg.booksCsv(service.getAllBooks());
            else if (t == 2) path = rg.membersCsv(service.getAllMembers());
            else path = rg.loansTxt(service.getActiveLoans());
            System.out.println("[ok] Report written to " + path);
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void runBackup() {
        String path = backupService.backupNow();
        if (path != null) {
            System.out.println("[ok] Backup created: " + path);
        } else {
            System.out.println("[x] Backup failed, check logs.");
        }
    }
}