package lms.service;

import lms.exception.LMSException;
import lms.model.Book;
import lms.model.BookStatus;
import lms.model.HoldRequest;
import lms.model.Loan;
import lms.model.Person;
import lms.util.Config;
import lms.util.DateTimeUtil;
import lms.util.FineCalculator;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Assembles a snapshot of KPI data for the dashboard screens.
 * Read-only: aggregates over the same service methods the rest of the UI uses.
 */
public class DashboardService {

    private final Config config;
    private final LibraryService service;

    public DashboardService(Config config, LibraryService service) {
        this.config = config;
        this.service = service;
    }

    public double getFinePerDay() {
        return config.getFinePerDay();
    }

    public int getActiveLoansCount() throws LMSException {
        return service.getActiveLoans().size();
    }

    public int getOverdueLoansCount() throws LMSException {
        return service.getOverdueLoans().size();
    }

    public int getBookCount() throws LMSException {
        return service.getAllBooks().size();
    }

    public int getMemberCount() throws LMSException {
        return service.getAllMembers().size();
    }

    /** Fines accruing right now on overdue, still-out loans. */
    public double getAccruingFinesTotal() throws LMSException {
        double total = 0;
        for (Loan l : service.getActiveLoans()) {
            if (l.isOverdue()) {
                total += FineCalculator.calculate(l.getDueDate(), DateTimeUtil.today(), config.getFinePerDay());
            }
        }
        return total;
    }

    public List<Loan> getActiveLoans() throws LMSException {
        return service.getActiveLoans();
    }

    public List<Loan> getOverdueLoans() throws LMSException {
        return service.getOverdueLoans();
    }

    public Map<String, Integer> getSubjectStats() throws LMSException {
        return service.subjectStats();
    }

    /** Available / Issued / Reserved counts over all books. */
    public Map<String, Long> getStatusBreakdown() throws LMSException {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (BookStatus s : BookStatus.values()) {
            counts.put(s.name(), 0L);
        }
        for (Book b : service.getAllBooks()) {
            counts.merge(b.getStatus().name(), 1L, Long::sum);
        }
        return counts;
    }

    public List<HoldRequest> getPendingHolds() throws LMSException {
        return service.getAllPendingHolds();
    }

    /** Active loans due within the next {@code days} days (not yet overdue). */
    public int getDueSoonCount(int days) throws LMSException {
        LocalDate today = DateTimeUtil.today();
        int count = 0;
        for (Loan l : service.getActiveLoans()) {
            if (!l.isOverdue() && !l.getDueDate().isAfter(today.plusDays(days))) {
                count++;
            }
        }
        return count;
    }

    public int getMemberLoanCount(int memberId) throws LMSException {
        return service.getMemberLoans(memberId).size();
    }

    /** Fines accruing on this member's overdue loans right now. */
    public double getMemberAccruingFines(int memberId) throws LMSException {
        double total = 0;
        for (Loan l : service.getMemberLoans(memberId)) {
            if (l.isOverdue()) {
                total += FineCalculator.calculate(l.getDueDate(), DateTimeUtil.today(), config.getFinePerDay());
            }
        }
        return total;
    }

    public double getMemberUnpaidFines(int memberId) throws LMSException {
        return service.getOutstandingFines(memberId);
    }

    public List<Person> getAllMembers() throws LMSException {
        return service.getAllMembers();
    }
}
