package lms.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public final class ActivityLog {

    private static final Deque<String> RECENT = new LinkedList<>();
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Path logFile;

    private ActivityLog() {
    }

    public static void init(Config config) {
        logFile = Paths.get(config.getLogsDir(), "lms.log");
    }

    public static synchronized void log(String message) {
        String line = String.format("[%s] %s", LocalDateTime.now().format(FORMAT), message);
        RECENT.addLast(line);
        if (RECENT.size() > 50) {
            RECENT.removeFirst();
        }
        try {
            if (logFile != null) {
                Files.createDirectories(logFile.getParent());
                try (BufferedWriter w = new BufferedWriter(new FileWriter(logFile.toFile(), true))) {
                    w.write(line);
                    w.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("[log] Could not write to log file: " + e.getMessage());
        }
        System.out.println("  [log] " + message);
    }

    public static synchronized List<String> recent() {
        return new ArrayList<>(RECENT);
    }
}