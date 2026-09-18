package lms.ui;

import lms.exception.LMSException;
import lms.model.HoldRequest;
import lms.model.Loan;
import lms.model.Person;
import lms.service.DashboardService;
import lms.util.Console;
import lms.util.DateTimeUtil;
import lms.util.FineCalculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Connects the ANSI {@link DashboardUI} to live data from
 * {@link DashboardService}. No SQL here — the service/DAO layer is the only
 * source of numbers; this class only adapts models to UI rows.
 */
public class DashboardConsole {

    private final DashboardService dash;
    private final int viewerMemberId; // -1 for staff (librarian/clerk)

    public DashboardConsole(DashboardService dash) {
        this(dash, -1);
    }

    public DashboardConsole(DashboardService dash, int viewerMemberId) {
        this.dash = dash;
        this.viewerMemberId = viewerMemberId;
    }

    public void runStaff() {
        while (true) {
            System.out.println();
            System.out.println("--- DASHBOARD ---");
            System.out.println("  1. Full Dashboard (ANSI view)");
            System.out.println("  2. Overview (KPIs)");
            System.out.println("  3. Circulation Drill-Down");
            System.out.println("  4. Collection Drill-Down");
            System.out.println("  5. Members Overview");
            System.out.println("  6. Hold Queue");
            System.out.println("  0. Back");
            int choice = Console.readInt("Enter choice: ", 0, 6);
            try {
                switch (choice) {
                    case 1:
                        DashboardController.display(dash);
                        pause();
                        break;
                    case 2:
                        DashboardRenderer.printKpiRow(dash, "LIBRARY OVERVIEW");
                        DashboardRenderer.printCollectionCharts(dash);
                        break;
                    case 3:
                        showCirculation();
                        break;
                    case 4:
                        DashboardRenderer.printCollectionCharts(dash);
                        break;
                    case 5:
                        showMembers();
                        break;
                    case 6:
                        List<HoldRequest> holds = dash.getPendingHolds();
                        DashboardRenderer.printHoldsSummary(holds);
                        System.out.println("  Total pending hold(s): " + holds.size());
                        break;
                    case 0:
                        return;
                    default:
                        break;
                }
            } catch (LMSException e) {
                System.out.println("[x] " + e.getMessage());
            }
        }
    }

    /** One-shot member dashboard (prints overview, no submenu loop). */
    public void runMember() {
        try {
            DashboardRenderer.printMemberKpis(dash, viewerMemberId, "MY LIBRARY DASHBOARD");
            List<Loan> loans = new ArrayList<>();
            for (Loan l : dash.getActiveLoans()) {
                if (l.getMemberId() == viewerMemberId) {
                    loans.add(l);
                }
            }
            DashboardRenderer.printAttentionLoans(loans, dash.getFinePerDay(), "MY LOANS");
        } catch (LMSException e) {
            System.out.println("[x] " + e.getMessage());
        }
    }

    private void showCirculation() throws LMSException {
        int active = dash.getActiveLoansCount();
        int overdue = dash.getOverdueLoansCount();
        int dueSoon = dash.getDueSoonCount(3);
        System.out.println();
        System.out.println("  CIRCULATION SNAPSHOT");
        System.out.println("  " + repeat('-', 74));
        System.out.printf("  Active loans: %d   Overdue: %d   Due within 3 days: %d%n", active, overdue, dueSoon);
        System.out.println("  " + repeat('-', 74));
        DashboardRenderer.printAttentionLoans(dash.getActiveLoans(), dash.getFinePerDay());
    }

    private void showMembers() throws LMSException {
        List<Person> members = dash.getAllMembers();
        System.out.println();
        System.out.println("  MEMBERS OVERVIEW");
        System.out.println("  " + repeat('-', 74));
        if (members.isEmpty()) {
            System.out.println("  (no members registered)");
            return;
        }
        System.out.printf("  %-4s %-22s %-10s %-11s %12s%n", "ID", "Name", "Loans", "Accruing", "Unpaid Fine");
        System.out.println("  " + repeat('-', 74));
        for (Person m : members) {
            int loans = dash.getMemberLoanCount(m.getId());
            double accruing = dash.getMemberAccruingFines(m.getId());
            double unpaid = dash.getMemberUnpaidFines(m.getId());
            System.out.printf("  %-4d %-22s %-10d %-11s %12s%n",
                    m.getId(), truncate(m.getName(), 22), loans,
                    FineCalculator.money(accruing), FineCalculator.money(unpaid));
        }
        System.out.println("  " + repeat('-', 74));
        System.out.println("  Total: " + members.size() + " member(s).");
    }

    private static void pause() {
        System.out.print("Press ENTER to continue...");
        try {
            byte[] buf = new byte[64];
            System.in.read(buf);
        } catch (java.io.IOException e) {
            // ignore; the menu loop re-renders anyway
        }
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
