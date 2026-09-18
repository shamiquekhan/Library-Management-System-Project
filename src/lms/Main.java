package lms;

import lms.ui.ConsoleUI;
import lms.util.ActivityLog;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Application entry point.
 *
 * Default           — launches the Swing desktop application (login window).
 * {@code --cli}     — runs the original terminal UI (used by the smoke test
 *                     and for headless environments).
 *
 * If the JVM has no display available (server/CI), the Swing UI cannot start,
 * so the console UI runs automatically instead.
 */
public class Main {

    private static final String BANNER =
        "+----------------------------------------------------------+\n" +
        "|            LIBRARY MANAGEMENT SYSTEM                      |\n" +
        "|            Subject : Programming in Java                  |\n" +
        "|            Developed by : Shamique Khan                   |\n" +
        "|            Reg. No : 25BAI10187                            |\n" +
        "+----------------------------------------------------------+";

    public static void main(String[] args) {
        boolean cliRequested = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "cli".equalsIgnoreCase(arg)) {
                cliRequested = true;
            }
        }

        boolean headless = GraphicsEnvironment.isHeadless();

        if (!cliRequested && !headless) {
            runSwing();
        } else {
            if (!cliRequested) {
                System.out.println("[i] No display detected — falling back to the console UI.");
            }
            runConsole();
        }
    }

    private static void runSwing() {
        SwingUtilities.invokeLater(() -> {
            try {
                AppContext app = AppContext.create();
                if (app == null) {
                    System.err.println("Database initialisation failed — see console/log output.");
                    System.exit(1);
                    return;
                }
                app.startServices();
                new lms.ui.LoginFrame(app.getConfig(), app.getService(), app.getDashboard()).setVisible(true);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    app.stopServices();
                    ActivityLog.log("Session ended.");
                }));
            } catch (Exception e) {
                System.err.println("Failed to start UI: " + e.getMessage());
                System.exit(1);
            }
        });
    }

    private static void runConsole() {
        System.out.println(BANNER);

        AppContext app = AppContext.create();
        if (app == null) {
            return;
        }

        app.startServices();

        try {
            new ConsoleUI(app.getConfig(), app.getService(), app.getBackup()).run();
        } finally {
            app.stopServices();
            System.out.println("Goodbye! - Shamique Khan (25BAI10187)");
            ActivityLog.log("Session ended.");
        }
    }
}
