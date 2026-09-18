package lms.ui;

import lms.concurrency.BackupService;
import lms.exception.LMSException;
import lms.model.Clerk;
import lms.model.Librarian;
import lms.model.Member;
import lms.model.Person;
import lms.service.LibraryService;
import lms.util.Config;
import lms.util.Console;
import lms.util.TablePrinter;

public class ConsoleUI {

    private final Config config;
    private final LibraryService service;
    private final BackupService backupService;

    public ConsoleUI(Config config, LibraryService service, BackupService backupService) {
        this.config = config;
        this.service = service;
        this.backupService = backupService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("========== LIBRARY MANAGEMENT SYSTEM ==========");
            System.out.println("  1. Login as Librarian");
            System.out.println("  2. Login as Clerk");
            System.out.println("  3. Login as Member");
            System.out.println("  0. Exit");
            int choice = Console.readInt("Enter choice: ", 0, 3);
            switch (choice) {
                case 1:
                    loginAs("LIBRARIAN");
                    break;
                case 2:
                    loginAs("CLERK");
                    break;
                case 3:
                    loginAs("MEMBER");
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void loginAs(String role) {
        String username = Console.readNonEmpty("Username: ");
        String password = Console.readNonEmpty("Password: ");
        try {
            Person person = service.login(username, password, role);
            System.out.println("[ok] Welcome, " + person.getName() + " (" + person.getRole() + ").");
            if (person instanceof Librarian) {
                new LibrarianConsole(config, service, backupService, (Librarian) person).run();
            } else if (person instanceof Clerk) {
                new ClerkConsole(config, service, (Clerk) person).run();
            } else {
                new MemberConsole(config, service, (Member) person).run();
            }
        } catch (LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    public static void registerMemberFlow(LibraryService service) {
        try {
            String name = Console.readNonEmpty("Member name: ");
            String username = Console.readNonEmpty("Username: ");
            String password = Console.readNonEmpty("Password: ");
            String phone = Console.readLine("Phone: ");
            String address = Console.readLine("Address: ");
            int id = service.registerMember(new lms.model.Member(0, name, phone, address, username, password));
            System.out.println("[ok] Member registered with ID " + id + ".");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    public static void changePasswordFlow(LibraryService service, lms.model.Person person) {
        try {
            String oldPass = Console.readNonEmpty("Current password: ");
            String newPass = Console.readNonEmpty("New password: ");
            service.changePassword(person.getId(), oldPass, newPass);
            System.out.println("[ok] Password changed.");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    public static void searchFlow(LibraryService service) {
        System.out.println("Search by: 1. Title  2. Author  3. Subject");
        int by = Console.readInt("Choice: ", 1, 3);
        String field = by == 1 ? "title" : by == 2 ? "author" : "subject";
        String keyword = Console.readNonEmpty("Keyword: ");
        try {
            TablePrinter.printBooks(service.searchBooks(field, keyword));
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    public static void viewAllBooksFlow(LibraryService service) {
        try {
            TablePrinter.printBooks(service.getAllBooks());
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }
}