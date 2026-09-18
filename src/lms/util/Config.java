package lms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    private final Properties props = new Properties();

    private final String dbPath;
    private final int loanPeriodDays;
    private final int maxLoansPerMember;
    private final double finePerDay;
    private final int overdueCheckSeconds;
    private final int backupIntervalSeconds;
    private final String reportsDir;
    private final String backupsDir;
    private final String logsDir;
    private final boolean seedDemoData;

    public Config() {
        this("config/app.properties");
    }

    public Config(String path) {
        setDefaults();
        File file = new File(path);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.out.println("[!] Could not read " + path + ", using defaults. (" + e.getMessage() + ")");
            }
        } else {
            try {
                file.getParentFile().mkdirs();
                try (OutputStream out = new FileOutputStream(file)) {
                    props.store(out, "Library Management System - configuration");
                    System.out.println("[i] Default configuration written to " + path);
                }
            } catch (IOException e) {
                System.out.println("[!] Could not create config file, using defaults. (" + e.getMessage() + ")");
            }
        }
        dbPath = props.getProperty("db.path", "data/library.db");
        loanPeriodDays = intOf("loan.period.days", 14);
        maxLoansPerMember = intOf("max.loans.per.member", 3);
        finePerDay = doubleOf("fine.per.day", 5.0);
        overdueCheckSeconds = intOf("overdue.check.seconds", 60);
        backupIntervalSeconds = intOf("backup.interval.seconds", 300);
        reportsDir = props.getProperty("dir.reports", "reports");
        backupsDir = props.getProperty("dir.backups", "backups");
        logsDir = props.getProperty("dir.logs", "logs");
        seedDemoData = Boolean.parseBoolean(props.getProperty("seed.demo.data", "true"));
        createDirectories();
    }

    private void setDefaults() {
        props.setProperty("db.path", "data/library.db");
        props.setProperty("loan.period.days", "14");
        props.setProperty("max.loans.per.member", "3");
        props.setProperty("fine.per.day", "5.0");
        props.setProperty("overdue.check.seconds", "60");
        props.setProperty("backup.interval.seconds", "300");
        props.setProperty("dir.reports", "reports");
        props.setProperty("dir.backups", "backups");
        props.setProperty("dir.logs", "logs");
        props.setProperty("seed.demo.data", "true");
    }

    private int intOf(String key, int fallback) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double doubleOf(String key, double fallback) {
        try {
            return Double.parseDouble(props.getProperty(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void createDirectories() {
        try {
            Path dbParent = Paths.get(dbPath).getParent();
            if (dbParent != null) {
                Files.createDirectories(dbParent);
            }
            Files.createDirectories(Paths.get(logsDir));
            Files.createDirectories(Paths.get(backupsDir));
            Files.createDirectories(Paths.get(reportsDir));
        } catch (IOException e) {
            System.out.println("[!] Could not create working directories: " + e.getMessage());
        }
    }

    public String getDbPath() {
        return dbPath;
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }

    public int getMaxLoansPerMember() {
        return maxLoansPerMember;
    }

    public double getFinePerDay() {
        return finePerDay;
    }

    public int getOverdueCheckSeconds() {
        return overdueCheckSeconds;
    }

    public int getBackupIntervalSeconds() {
        return backupIntervalSeconds;
    }

    public String getReportsDir() {
        return reportsDir;
    }

    public String getBackupsDir() {
        return backupsDir;
    }

    public String getLogsDir() {
        return logsDir;
    }

    public boolean isSeedDemoData() {
        return seedDemoData;
    }
}