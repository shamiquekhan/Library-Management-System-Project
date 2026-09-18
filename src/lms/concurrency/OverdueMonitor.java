package lms.concurrency;

import lms.util.ActivityLog;
import lms.util.Config;
import lms.util.Database;
import lms.util.DateTimeUtil;
import lms.util.FineCalculator;
import lms.model.Loan;
import lms.dao.LoanDao;
import lms.exception.LMSException;
import lms.exception.OperationFailedException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OverdueMonitor {

    private final Config config;
    private final LoanDao loanDao = new LoanDao();
    private ScheduledExecutorService executor;

    public OverdueMonitor(Config config) {
        this.config = config;
    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "overdue-monitor");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(this::scan, 3, config.getOverdueCheckSeconds(), TimeUnit.SECONDS);
        ActivityLog.log("Overdue monitor started (checks every " + config.getOverdueCheckSeconds() + "s).");
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void scan() {
        try (Connection c = Database.open(config)) {
            LocalDate today = DateTimeUtil.today();
            List<Loan> overdue = loanDao.findOverdue(c, today);
            for (Loan loan : overdue) {
                loanDao.updateFine(c, loan.getId(), FineCalculator.calculate(loan.getDueDate(), today, config.getFinePerDay()));
            }
            ActivityLog.log("Overdue scan: " + overdue.size() + " overdue loan(s), fines refreshed.");
        } catch (SQLException e) {
            ActivityLog.log("Overdue scan failed: " + e.getMessage());
        }
    }
}