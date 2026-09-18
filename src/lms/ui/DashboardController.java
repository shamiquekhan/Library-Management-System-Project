package lms.ui;

import lms.model.HoldRequest;
import lms.model.Loan;
import lms.model.Person;
import lms.service.DashboardService;
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
public final class DashboardController {

    private DashboardController() { }

    /**
     * Renders the complete dashboard: banner, overview KPIs, collection
     * status, subject chart, attention loans and members — all queried live.
     */
    public static void display(DashboardService dash) throws lms.exception.LMSException {
        DashboardUI.showHeader();

        DashboardUI.showStatistics(
                dash.getBookCount(),
                dash.getMemberCount(),
                dash.getActiveLoansCount(),
                dash.getOverdueLoansCount(),
                dash.getAccruingFinesTotal());

        Map<String, Long> status = dash.getStatusBreakdown();
        DashboardUI.showCollectionStatus(
                status.getOrDefault("AVAILABLE", 0L).intValue(),
                status.getOrDefault("ISSUED", 0L).intValue(),
                status.getOrDefault("RESERVED", 0L).intValue());

        List<DashboardUI.SubjectCount> subjects = new ArrayList<>();
        for (Map.Entry<String, Integer> e : dash.getSubjectStats().entrySet()) {
            subjects.add(new DashboardUI.SubjectCount(e.getKey().toUpperCase(), e.getValue()));
        }
        subjects.sort(Comparator.comparing(DashboardUI.SubjectCount::getSubject));
        DashboardUI.showSubjectDistribution(subjects);

        List<Loan> active = new ArrayList<>(dash.getActiveLoans());
        active.sort((a, b) -> {
            if (a.isOverdue() != b.isOverdue()) {
                return a.isOverdue() ? -1 : 1;
            }
            return a.getDueDate().compareTo(b.getDueDate());
        });
        List<DashboardUI.LoanRow> loans = new ArrayList<>();
        for (Loan l : active) {
            String statusTxt = l.isOverdue()
                    ? "OVERDUE " + DateTimeUtil.daysBetween(l.getDueDate(), DateTimeUtil.today()) + "D"
                    : "DUE IN " + DateTimeUtil.daysBetween(DateTimeUtil.today(), l.getDueDate()) + "D";
            loans.add(new DashboardUI.LoanRow(
                    l.getId(),
                    l.getBookTitle() != null ? l.getBookTitle() : "ID " + l.getBookId(),
                    l.getMemberName() != null ? l.getMemberName() : "ID " + l.getMemberId(),
                    DateTimeUtil.format(l.getIssueDate()),
                    DateTimeUtil.format(l.getDueDate()),
                    statusTxt));
        }
        DashboardUI.showLoansNeedingAttention(loans);

        List<DashboardUI.MemberRow> members = new ArrayList<>();
        for (Person m : dash.getAllMembers()) {
            members.add(new DashboardUI.MemberRow(
                    m.getId(),
                    m.getName(),
                    dash.getMemberLoanCount(m.getId()),
                    dash.getMemberAccruingFines(m.getId())));
        }
        DashboardUI.showMembers(members);

        DashboardUI.showFooter();
    }
}
