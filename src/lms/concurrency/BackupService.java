package lms.concurrency;

import lms.util.ActivityLog;
import lms.util.Config;
import lms.util.Database;
import lms.exception.LMSException;
import lms.exception.OperationFailedException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupService {

    private final Config config;
    private ScheduledExecutorService executor;

    public BackupService(Config config) {
        this.config = config;
    }

    public synchronized String backupNow() {
        try {
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path source = Paths.get(config.getDbPath());
            Path target = Paths.get(config.getBackupsDir(), "library-backup-" + stamp + ".db");
            if (!Files.exists(source)) {
                ActivityLog.log("Backup skipped: database file not created yet.");
                return null;
            }
            Files.createDirectories(Paths.get(config.getBackupsDir()));
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            int books = countBooks();
            int activeLoans = countActiveLoans();
            int members = countMembers();
            String summary = "books=" + books + ", activeLoans=" + activeLoans + ", members=" + members;
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Paths.get(config.getBackupsDir(), "backup-manifest.txt").toFile(), true)))) {
                out.println(stamp + "  " + target.getFileName() + "  " + summary);
            }
            ActivityLog.log("Backup created: " + target + " (" + summary + ")");
            return target.toString();
        } catch (Exception e) {
            ActivityLog.log("Backup failed: " + e.getMessage());
            return null;
        }
    }

    private int countBooks() throws SQLException {
        try (Connection c = Database.open(config); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM book")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int countActiveLoans() throws SQLException {
        try (Connection c = Database.open(config); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM loan WHERE return_date IS NULL")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int countMembers() throws SQLException {
        try (Connection c = Database.open(config); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM person WHERE role='MEMBER'")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "backup-service");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(this::backupNow, 10, config.getBackupIntervalSeconds(), TimeUnit.SECONDS);
        ActivityLog.log("Backup service started (every " + config.getBackupIntervalSeconds() + "s).");
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}