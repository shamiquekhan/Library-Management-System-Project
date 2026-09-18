package lms.ui;

import java.util.List;

/**
 * ANSI terminal renderer for the dashboard. Pure presentation: every value
 * arrives already computed from {@code DashboardService} via
 * {@link DashboardController}. Gold/amber for the identity band, green for
 * healthy values, red for anything needing attention.
 */
public final class DashboardUI {

    // Colors are emitted only when attached to a real terminal. When stdin or
    // stdout is piped (smoke tests, log capture) System.console() is null and
    // every code below resolves to "", keeping output plain and greppable.
    private static final boolean ANSI_ENABLED = System.console() != null;

    private static String ansi(String code) {
        return ANSI_ENABLED ? code : "";
    }

    private static final String RESET = ansi("\u001B[0m");
    private static final String BOLD  = ansi("\u001B[1m");
    private static final String GOLD  = ansi("\u001B[33m");
    private static final String RED   = ansi("\u001B[31m");
    private static final String GREEN = ansi("\u001B[32m");
    private static final String CYAN  = ansi("\u001B[36m");
    private static final String DIM   = ansi("\u001B[2m");

    private static final int WIDTH = 100;

    private DashboardUI() { }

    /** Best-effort ANSI screen clear (no-op when output is piped). */
    public static void clearScreen() {
        if (!ANSI_ENABLED) {
            return;
        }
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Clears the terminal and prints the ASCII-art identity band. */
    public static void showHeader() {
        clearScreen();
        System.out.println();
        System.out.println(GOLD + BOLD +
                "  ██╗     ██╗██████╗ ██████╗  █████╗ ██████╗ ██╗   ██╗" +
                RESET);
        System.out.println(GOLD + BOLD +
                "  ██║     ██║██╔══██╗██╔══██╗██╔══██╗██╔══██╗╚██╗ ██╔╝" +
                RESET);
        System.out.println(GOLD + BOLD +
                "  ██║     ██║██████╔╝██████╔╝███████║██████╔╝ ╚████╔╝ " +
                RESET);
        System.out.println(GOLD + BOLD +
                "  ██║     ██║██╔══██╗██╔══██╗██╔══██║██╔══██╗  ╚██╔╝  " +
                RESET);
        System.out.println(GOLD + BOLD +
                "  ███████╗██║██████╔╝██║  ██║██║  ██║██║  ██║   ██║   " +
                RESET);
        System.out.println(GOLD + BOLD +
                "  ╚══════╝╚═╝╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   " +
                RESET);
        System.out.println();
        printLine();
        System.out.printf(
                "%s%s%-30s %70s%s%n",
                DIM, " ",
                "LIBRARY MANAGEMENT SYSTEM",
                "LIVE FROM SQLITE",
                RESET);
        printLine();
        System.out.println();
    }

    /** SYSTEM OVERVIEW: the five headline KPIs. */
    public static void showStatistics(
            int totalBooks,
            int members,
            int activeLoans,
            int overdue,
            double fines) {
        System.out.println(BOLD + "SYSTEM OVERVIEW" + RESET);
        System.out.println();
        printStat("TOTAL BOOKS", String.valueOf(totalBooks), CYAN);
        printStat("MEMBERS", String.valueOf(members), CYAN);
        printStat("ACTIVE LOANS", String.valueOf(activeLoans), GREEN);
        printStat("OVERDUE", String.valueOf(overdue), overdue > 0 ? RED : GREEN);
        printStat("FINES ACCRUING", String.format("Rs. %.2f", fines), fines > 0 ? RED : GREEN);
        System.out.println();
        printLine();
        System.out.println();
    }

    private static void printStat(String title, String value, String color) {
        System.out.printf("%-22s %s%s%-15s%s%n", title, color, BOLD, value, RESET);
    }

    /** 01 — collection health at a glance. */
    public static void showCollectionStatus(int available, int issued, int reserved) {
        System.out.println(BOLD + "01  COLLECTION STATUS" + RESET);
        System.out.println();
        System.out.printf(
                "AVAILABLE : %s%d%s     ISSUED : %s%d%s     RESERVED : %s%d%s%n",
                GREEN, available, RESET,
                GOLD, issued, RESET,
                CYAN, reserved, RESET);
        System.out.println();
        printLine();
        System.out.println();
    }

    /** Gold bar chart of books per subject. */
    public static void showSubjectDistribution(List<SubjectCount> subjects) {
        System.out.println(BOLD + "BOOKS PER SUBJECT" + RESET);
        System.out.println();
        int max = 1;
        for (SubjectCount subject : subjects) {
            max = Math.max(max, subject.getCount());
        }
        for (SubjectCount subject : subjects) {
            int barLength = (int) ((double) subject.getCount() / max * 40);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLength; i++) {
                bar.append("█");
            }
            System.out.printf(
                    "%-22s %s%-40s%s %d%n",
                    subject.getSubject(),
                    GOLD,
                    bar,
                    RESET,
                    subject.getCount());
        }
        System.out.println();
    }

    /** 02 — attention table, overdue first (caller passes rows pre-sorted). */
    public static void showLoansNeedingAttention(List<LoanRow> loans) {
        System.out.println(BOLD + "02  LOANS NEEDING ATTENTION" + RESET);
        System.out.println();
        System.out.printf(
                "%-5s %-28s %-20s %-12s %-12s %-12s%n",
                "ID", "BOOK", "MEMBER", "ISSUED", "DUE", "STATUS");
        printThinLine();
        for (LoanRow loan : loans) {
            String statusColor =
                    loan.getStatus().toUpperCase().contains("OVERDUE") ? RED : GREEN;
            System.out.printf(
                    "%-5s %-28s %-20s %-12s %-12s %s%-12s%s%n",
                    loan.getId(),
                    truncate(loan.getBook(), 28),
                    truncate(loan.getMember(), 20),
                    loan.getIssuedDate(),
                    loan.getDueDate(),
                    statusColor,
                    loan.getStatus(),
                    RESET);
        }
        System.out.println();
    }

    /** 03 — members and their accruing fines. */
    public static void showMembers(List<MemberRow> members) {
        System.out.println(BOLD + "03  MEMBERS OVERVIEW" + RESET);
        System.out.println();
        System.out.printf(
                "%-8s %-28s %-12s %-15s%n",
                "ID", "NAME", "LOANS", "ACCRUING");
        printThinLine();
        for (MemberRow member : members) {
            String color = member.getAccruing() > 0 ? RED : GREEN;
            System.out.printf(
                    "%-8s %-28s %-12d %sRs. %-10.2f%s%n",
                    member.getId(),
                    truncate(member.getName(), 28),
                    member.getLoans(),
                    color,
                    member.getAccruing(),
                    RESET);
        }
        System.out.println();
    }

    /** Footer: read-only notice and pointer back to the main menu. */
    public static void showFooter() {
        printLine();
        System.out.printf("%sREAD-ONLY DASHBOARD%s", DIM, RESET);
        System.out.println();
        System.out.println(DIM + "Sign in through the main menu to perform operations." + RESET);
        System.out.println();
    }

    private static void printLine() {
        System.out.println("=".repeat(WIDTH));
    }

    private static void printThinLine() {
        System.out.println("-".repeat(WIDTH));
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /** Immutable subject/count pair for the bar chart. */
    public static class SubjectCount {
        private final String subject;
        private final int count;

        public SubjectCount(String subject, int count) {
            this.subject = subject;
            this.count = count;
        }

        public String getSubject() {
            return subject;
        }

        public int getCount() {
            return count;
        }
    }

    /** Immutable loan row for the attention table. */
    public static class LoanRow {
        private final int id;
        private final String book;
        private final String member;
        private final String issuedDate;
        private final String dueDate;
        private final String status;

        public LoanRow(int id, String book, String member, String issuedDate,
                       String dueDate, String status) {
            this.id = id;
            this.book = book;
            this.member = member;
            this.issuedDate = issuedDate;
            this.dueDate = dueDate;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getBook() {
            return book;
        }

        public String getMember() {
            return member;
        }

        public String getIssuedDate() {
            return issuedDate;
        }

        public String getDueDate() {
            return dueDate;
        }

        public String getStatus() {
            return status;
        }
    }

    /** Immutable member row for the members table. */
    public static class MemberRow {
        private final int id;
        private final String name;
        private final int loans;
        private final double accruing;

        public MemberRow(int id, String name, int loans, double accruing) {
            this.id = id;
            this.name = name;
            this.loans = loans;
            this.accruing = accruing;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getLoans() {
            return loans;
        }

        public double getAccruing() {
            return accruing;
        }
    }
}
