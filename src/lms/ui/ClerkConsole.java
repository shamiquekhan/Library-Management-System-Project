package lms.ui;

import lms.exception.LMSException;
import lms.model.Clerk;
import lms.model.Person;
import lms.service.LibraryService;
import lms.util.Config;
import lms.util.Console;
import lms.util.DateTimeUtil;
import lms.util.FineCalculator;
import lms.util.TablePrinter;

import java.time.LocalDate;

public class ClerkConsole {

    private final Config config;
    private final LibraryService service;
    private final Clerk clerk;

    public ClerkConsole(Config config, LibraryService service, Clerk clerk) {
        this.config = config;
        this.service = service;
        this.clerk = clerk;
    }

    public void run() {
        int choice;
        do {
            System.out.println();
            System.out.println("----- CLERK MENU (" + clerk.getName() + ") -----");
            System.out.println("  1. Issue a Book");
            System.out.println("  2. Return a Book");
            System.out.println("  3. Renew a Loan");
            System.out.println("  4. Collect Fine");
            System.out.println("  5. Register New Member");
            System.out.println("  6. Search Books");
            System.out.println("  7. View All Books");
            System.out.println("  8. View Member Loans and Fines");
            System.out.println("  9. Update Member Contact Info");
            System.out.println(" 10. Change My Password");
            System.out.println(" 11. Dashboard");
            System.out.println("  0. Logout");
            choice = Console.readInt("Enter choice: ", 0, 11);
            switch (choice) {
                case 1:
                    issueBook();
                    break;
                case 2:
                    returnBook();
                    break;
                case 3:
                    renewLoan();
                    break;
                case 4:
                    collectFine();
                    break;
                case 5:
                    ConsoleUI.registerMemberFlow(service);
                    break;
                case 6:
                    ConsoleUI.searchFlow(service);
                    break;
                case 7:
                    ConsoleUI.viewAllBooksFlow(service);
                    break;
                case 8:
                    viewMemberLoans();
                    break;
                case 9:
                    updateMemberContact();
                    break;
                case 10:
                    ConsoleUI.changePasswordFlow(service, clerk);
                    break;
                case 11:
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

    private void issueBook() {
        try {
            int memberId = Console.readInt("Member ID: ", 1, 999999);
            int bookId = Console.readInt("Book ID: ", 1, 999999);
            lms.model.Loan loan = service.issueBook(bookId, memberId);
            System.out.println("[ok] Issued. Due on " + DateTimeUtil.format(loan.getDueDate()) + ".");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void returnBook() {
        try {
            int bookId = Console.readInt("Book ID: ", 1, 999999);
            double fine = service.returnBook(bookId);
            if (fine > 0) {
                System.out.println("[ok] Returned with fine " + FineCalculator.money(fine) + " (collect at 'Collect Fine').");
            } else {
                System.out.println("[ok] Returned on time, no fine.");
            }
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void renewLoan() {
        try {
            int bookId = Console.readInt("Book ID: ", 1, 999999);
            LocalDate due = service.renewLoan(bookId);
            System.out.println("[ok] Renewed. New due date " + DateTimeUtil.format(due) + ".");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void collectFine() {
        try {
            int memberId = Console.readInt("Member ID: ", 1, 999999);
            double amount = service.collectFine(memberId);
            System.out.println("[ok] Collected " + FineCalculator.money(amount) + ".");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void viewMemberLoans() {
        try {
            int memberId = Console.readInt("Member ID: ", 1, 999999);
            Person m = service.getMember(memberId);
            System.out.println("Member: " + m.getName() + " (ID " + m.getId() + ")");
            TablePrinter.printLoans(service.getMemberLoans(memberId), config.getFinePerDay());
            double fines = service.getOutstandingFines(memberId);
            System.out.printf("Recorded unpaid fine: %s%n", FineCalculator.money(fines));
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void updateMemberContact() {
        try {
            int memberId = Console.readInt("Member ID: ", 1, 999999);
            String phone = Console.readLine("New phone: ");
            String address = Console.readLine("New address: ");
            service.updateMemberProfile(memberId, phone, address);
            System.out.println("[ok] Member contact updated.");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }
}