package lms.ui;

import lms.model.HoldRequest;
import lms.model.Loan;
import lms.service.DashboardService;
import lms.util.DateTimeUtil;
import lms.util.FineCalculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Renders dashboard screens for the console UI: KPI summary rows,
 * status breakdown and subject bar chart, and attention lists.
 */
public final class DashboardRenderer {

    private DashboardRenderer() {
    }

    /** KPI block tailored to a logged-in member: own loans and own fines. */
    public static void printMemberKpis(DashboardService dash, int memberId, String title) throws lms.exception.LMSException {
        System.out.println();
        System.out.println("  " + title);
        System.out.println("  " + repeat('=', 74));
        int myLoans = dash.getMemberLoanCount(memberId);
        int overdue = 0;
        int dueSoon = 0;
        for (Loan l : dash.getActiveLoans()) {
            if (l.getMemberId() != memberId) {
                continue;
            }
            if (l.isOverdue()) {
                overdue++;
            } else if (!l.getDueDate().isAfter(DateTimeUtil.today().plusDays(3))) {
                dueSoon++;
            }
        }
        double accruing = dash.getMemberAccruingFines(memberId);
        double unpaid = dash.getMemberUnpaidFines(memberId);

        System.out.printf("  %-16s %4d book(s) in catalog, %d member(s)%n", "Library:", dash.getBookCount(), dash.getMemberCount());
        System.out.printf("  %-16s %4d   (%d overdue, %d due in <=3 days)%n", "My Loans:", myLoans, overdue, dueSoon);
        System.out.printf("  %-16s %4s accruing, %s recorded unpaid%n", "My Fines:", FineCalculator.money(accruing), FineCalculator.money(unpaid));
        System.out.println("  " + repeat('=', 74));
    }

    /** One-line summary of the most important KPIs. */
    public static void printKpiRow(DashboardService dash, String title) throws lms.exception.LMSException {
        System.out.println();
        System.out.println("  " + title);
        System.out.println("  " + repeat('=', 74));
        int books = dash.getBookCount();
        int members = dash.getMemberCount();
        int active = dash.getActiveLoansCount();
        int overdue = dash.getOverdueLoansCount();
        int dueSoon = dash.getDueSoonCount(3);
        double accruing = dash.getAccruingFinesTotal();

        System.out.printf("  %-16s %4d%n", "Total Books:", books);
        System.out.printf("  %-16s %4d%n", "Members:", members);
        System.out.printf("  %-16s %4d   (%d overdue, %d due in <=3 days)%n", "Active Loans:", active, overdue, dueSoon);
        System.out.printf("  %-16s %4s   (%s/day rate)%n", "Fines Accruing:", FineCalculator.money(accruing), FineCalculator.money(dash.getFinePerDay()));
        System.out.println("  " + repeat('=', 74));
    }

    /** Two-column layout: status breakdown next to subject distribution bars. */
    public static void printCollectionCharts(DashboardService dash) throws lms.exception.LMSException {
        System.out.println();
        System.out.println("  COLLECTION STATUS & SUBJECTS");
        System.out.println("  " + repeat('-', 74));

        Map<String, Long> status = dash.getStatusBreakdown();
        Map<String, Integer> subjects = dash.getSubjectStats();
        int maxSubject = 0;
        for (int v : subjects.values()) {
            maxSubject = Math.max(maxSubject, v);
        }

        List<String> left = new ArrayList<>();
        for (Map.Entry<String, Long> e : status.entrySet()) {
            left.add(String.format("%-12s %3d", e.getKey(), e.getValue()));
        }

        List<Map.Entry<String, Integer>> subjectRows = new ArrayList<>(subjects.entrySet());
        int rows = Math.max(left.size(), subjectRows.size());
        for (int row = 0; row < rows; row++) {
            String l = row < left.size() ? left.get(row) : "";
            String r = "";
            if (row < subjectRows.size()) {
                Map.Entry<String, Integer> e = subjectRows.get(row);
                int bars = maxSubject == 0 ? 0 : (int) Math.round(24.0 * e.getValue() / maxSubject);
                r = String.format("%-26s |%s %d", e.getKey(), repeat('#', bars), e.getValue());
            }
            System.out.printf("  %-18s %s%n", l, r);
        }
        System.out.println("  " + repeat('-', 74));
    }

    /** Table of loans needing staff attention: overdue first, then due soonest. */
    public static void printAttentionLoans(List<Loan> activeLoans, double finePerDay) {
        printAttentionLoans(activeLoans, finePerDay, "LOANS NEEDING ATTENTION");
    }

    /** Same table with a custom heading (e.g. member-facing "MY LOANS"). */
    public static void printAttentionLoans(List<Loan> activeLoans, double finePerDay, String title) {
        System.out.println();
        System.out.println("  " + title);
        System.out.println("  " + repeat('-', 74));
        if (activeLoans == null || activeLoans.isEmpty()) {
            System.out.println("  (none — all loans are healthy)");
            return;
        }

        List<Loan> sorted = new ArrayList<>(activeLoans);
        sorted.sort(Comparator
                .comparing((Loan l) -> !l.isOverdue())           // overdue first
                .thenComparing(Loan::getDueDate));               // then due soonest

        int shown = 0;
        System.out.printf("  %-5s %-28s %-16s %-13s %9s%n", "Loan", "Book", "Member", "Status", "Fine");
        System.out.println("  " + repeat('-', 74));
        for (Loan l : sorted) {
            if (shown >= 8) {
                System.out.println("  ... and " + (sorted.size() - shown) + " more");
                break;
            }
            boolean overdue = l.isOverdue();
            String status = overdue
                    ? "OVERDUE " + DateTimeUtil.daysBetween(l.getDueDate(), DateTimeUtil.today()) + "d"
                    : "due in " + DateTimeUtil.daysBetween(DateTimeUtil.today(), l.getDueDate()) + "d";
            double fine = overdue
                    ? FineCalculator.calculate(l.getDueDate(), DateTimeUtil.today(), finePerDay)
                    : 0.0;
            String book = l.getBookTitle() != null ? l.getBookTitle() : "ID " + l.getBookId();
            String member = l.getMemberName() != null ? l.getMemberName() : "ID " + l.getMemberId();
            System.out.printf("  %-5d %-28s %-16s %-13s %9s%n",
                    l.getId(), truncate(book, 28), truncate(member, 16), status, FineCalculator.money(fine));
            shown++;
        }
        System.out.println("  " + repeat('-', 74));
    }

    /** Hold queue grouped by book, in queue order. */
    public static void printHoldsSummary(List<HoldRequest> holds) {
        System.out.println();
        System.out.println("  HOLD QUEUE (pending, in priority order)");
        System.out.println("  " + repeat('-', 74));
        if (holds == null || holds.isEmpty()) {
            System.out.println("  (no pending hold requests)");
            return;
        }
        String currentBook = null;
        int position = 0;
        for (HoldRequest h : holds) {
            if (!h.getBookTitle().equals(currentBook)) {
                currentBook = h.getBookTitle();
                position = 0;
                System.out.printf("  %s (ID %d):%n", truncate(currentBook, 40), h.getBookId());
            }
            position++;
            System.out.printf("     %d. %-24s requested %s%n", position,
                    h.getMemberName() != null ? h.getMemberName() : "Member " + h.getMemberId(), h.getRequestDate());
        }
        System.out.println("  " + repeat('-', 74));
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
