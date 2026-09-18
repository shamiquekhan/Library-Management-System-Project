package lms.ui;

import lms.exception.LMSException;
import lms.model.Member;
import lms.service.LibraryService;
import lms.util.Config;
import lms.util.Console;
import lms.util.FineCalculator;
import lms.util.TablePrinter;

public class MemberConsole {

    private final Config config;
    private final LibraryService service;
    private final Member member;

    public MemberConsole(Config config, LibraryService service, Member member) {
        this.config = config;
        this.service = service;
        this.member = member;
    }

    public void run() {
        int choice;
        do {
            System.out.println();
            System.out.println("----- MEMBER MENU (" + member.getName() + ") -----");
            System.out.println("  1. Search Books");
            System.out.println("  2. View All Books");
            System.out.println("  3. My Loans");
            System.out.println("  4. Place Hold Request");
            System.out.println("  5. My Hold Requests");
            System.out.println("  6. My Fines");
            System.out.println("  7. Change My Password");
            System.out.println("  8. My Dashboard");
            System.out.println("  0. Logout");
            choice = Console.readInt("Enter choice: ", 0, 8);
            switch (choice) {
                case 1:
                    ConsoleUI.searchFlow(service);
                    break;
                case 2:
                    ConsoleUI.viewAllBooksFlow(service);
                    break;
                case 3:
                    myLoans();
                    break;
                case 4:
                    placeHold();
                    break;
                case 5:
                    myHolds();
                    break;
                case 6:
                    myFines();
                    break;
                case 7:
                    ConsoleUI.changePasswordFlow(service, member);
                    break;
                case 8:
                    new DashboardConsole(new lms.service.DashboardService(config, service), member.getId()).runMember();
                    break;
                case 0:
                    System.out.println("[ok] Logged out.");
                    break;
                default:
                    break;
            }
        } while (choice != 0);
    }

    private void myLoans() {
        try {
            TablePrinter.printLoans(service.getMemberLoans(member.getId()), config.getFinePerDay());
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void placeHold() {
        try {
            int bookId = Console.readInt("Book ID: ", 1, 999999);
            int position = service.placeHold(bookId, member.getId());
            System.out.println("[ok] Hold placed. You are number " + position + " in the queue.");
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void myHolds() {
        try {
            TablePrinter.printHolds(service.getMemberHolds(member.getId()));
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void myFines() {
        try {
            double unpaid = service.getOutstandingFines(member.getId());
            System.out.printf("Recorded unpaid fine: %s%n", FineCalculator.money(unpaid));
            var loans = service.getMemberLoans(member.getId());
            int overdueCount = 0;
            double accruing = 0;
            for (lms.model.Loan l : loans) {
                if (l.isOverdue()) {
                    overdueCount++;
                    accruing += lms.util.FineCalculator.calculate(l.getDueDate(), lms.util.DateTimeUtil.today(), config.getFinePerDay());
                }
            }
            if (overdueCount > 0) {
                System.out.printf("You have %d overdue loan(s) accruing %s/day.%n", overdueCount, FineCalculator.money(config.getFinePerDay()));
            }
        } catch (lms.exception.LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }
}