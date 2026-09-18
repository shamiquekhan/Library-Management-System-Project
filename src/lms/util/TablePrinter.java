package lms.util;

import lms.model.Book;
import lms.model.HoldRequest;
import lms.model.Loan;
import lms.model.Person;

import java.io.PrintStream;
import java.util.List;

public final class TablePrinter {

    /** Defaults to System.out; the GUI points this at its output pane. */
    private static PrintStream out = System.out;

    private TablePrinter() {
    }

    public static void setOut(PrintStream ps) {
        out = ps == null ? System.out : ps;
    }

    public static void printBooks(List<Book> books) {
        if (books == null || books.isEmpty()) {
            out.println("  (no books to display)");
            return;
        }
        out.printf("  %-4s  %-16s  %-34s  %-22s  %-24s  %-9s%n", "ID", "ISBN", "Title", "Author", "Subject", "Status");
        out.println("  " + repeat('-', 118));
        for (Book b : books) {
            out.printf("  %-4d  %-16s  %-34s  %-22s  %-24s  %-9s%n", b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(), b.getSubject(), b.getStatus());
        }
        out.println("  Total: " + books.size() + " book(s).");
    }

    public static void printLoans(List<Loan> loans, double finePerDay) {
        if (loans == null || loans.isEmpty()) {
            out.println("  (no loans to display)");
            return;
        }
        out.printf("  %-6s  %-32s  %-20s  %-12s  %-12s  %-13s  %10s%n", "Loan", "Book", "Member", "Issued", "Due", "Status", "Fine(Rs)");
        out.println("  " + repeat('-', 110));
        for (Loan l : loans) {
            String status;
            double fine;
            if (l.getReturnDate() != null) {
                status = "RETURNED";
                fine = l.getFineAmount();
            } else if (l.isOverdue()) {
                long d = DateTimeUtil.daysBetween(l.getDueDate(), DateTimeUtil.today());
                status = "OVERDUE " + d + "d";
                fine = FineCalculator.calculate(l.getDueDate(), DateTimeUtil.today(), finePerDay);
            } else {
                status = "ACTIVE";
                fine = 0.0;
            }
            String book = l.getBookTitle() != null ? l.getBookTitle() : "ID " + l.getBookId();
            String member = l.getMemberName() != null ? l.getMemberName() : "ID " + l.getMemberId();
            out.printf("  %-6d  %-32s  %-20s  %-12s  %-12s  %-13s  %10.2f%n", l.getId(), truncate(book, 32), truncate(member, 20), l.getIssueDate(), l.getDueDate(), status, fine);
        }
        out.println("  Total: " + loans.size() + " loan(s).");
    }

    public static void printMembers(List<Person> members) {
        if (members == null || members.isEmpty()) {
            out.println("  (no members to display)");
            return;
        }
        out.printf("  %-4s  %-20s  %-12s  %-14s  %-30s  %s%n", "ID", "Name", "Username", "Phone", "Address", "Role");
        out.println("  " + repeat('-', 100));
        for (Person p : members) {
            out.printf("  %-4d  %-20s  %-12s  %-14s  %-30s  %s%n", p.getId(), p.getName(), p.getUsername(), p.getPhone(), p.getAddress(), p.getRole());
        }
        out.println("  Total: " + members.size() + " member(s).");
    }

    public static void printHolds(List<HoldRequest> holds) {
        if (holds == null || holds.isEmpty()) {
            out.println("  (no hold requests to display)");
            return;
        }
        out.printf("  %-4s  %-30s  %-20s  %-12s  %s%n", "ID", "Book", "Member", "Requested", "Status");
        out.println("  " + repeat('-', 90));
        for (HoldRequest h : holds) {
            String status = h.isFulfilled() ? "FULFILLED - book at desk" : "PENDING";
            out.printf("  %-4d  %-30s  %-20s  %-12s  %s%n", h.getId(), h.getBookTitle(), h.getMemberName(), h.getRequestDate(), status);
        }
        out.println("  Total: " + holds.size() + " hold request(s).");
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }
}
