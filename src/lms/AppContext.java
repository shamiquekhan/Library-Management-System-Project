package lms;

import lms.concurrency.BackupService;
import lms.concurrency.OverdueMonitor;
import lms.service.DashboardService;
import lms.service.LibraryService;
import lms.util.ActivityLog;
import lms.util.Config;
import lms.util.Database;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Shared application bootstrap for the console app ({@code lms.Main}).
 * Owns config, database initialisation and services, and starts/stops the
 * background daemons exactly once.
 */
public class AppContext {

    private final Config config;
    private final LibraryService service;
    private final DashboardService dashboard;
    private final OverdueMonitor monitor;
    private final BackupService backup;

    private AppContext(Config config, LibraryService service, DashboardService dashboard,
                       OverdueMonitor monitor, BackupService backup) {
        this.config = config;
        this.service = service;
        this.dashboard = dashboard;
        this.monitor = monitor;
        this.backup = backup;
    }

    /**
     * Initialises config and database (seeding on first run), then builds
     * the service layer. Returns null on database failure (message printed).
     */
    public static AppContext create() {
        Config config = new Config();
        ActivityLog.init(config);

        Database db = new Database(config);
        try {
            db.init();
            boolean seeded = db.seedDemoData();
            if (seeded) {
                System.out.println("[i] First run: demo data seeded (see README for login credentials).");
                ActivityLog.log("Demo data seeded (sample books, staff, members).");
            }
        } catch (SQLException e) {
            System.out.println("[x] Could not initialise database: " + e.getMessage());
            ActivityLog.log("DB init failed: " + e.getMessage());
            return null;
        }

        LibraryService service = new LibraryService(config);
        DashboardService dashboard = new DashboardService(config, service);
        return new AppContext(config, service, dashboard, new OverdueMonitor(config), new BackupService(config));
    }

    /** Starts the background daemons. */
    public void startServices() {
        monitor.start();
        backup.start();
    }

    /** Stops the background daemons. */
    public void stopServices() {
        monitor.stop();
        backup.stop();
    }

    public Config getConfig() {
        return config;
    }

    public LibraryService getService() {
        return service;
    }

    public DashboardService getDashboard() {
        return dashboard;
    }

    public BackupService getBackup() {
        return backup;
    }
}
