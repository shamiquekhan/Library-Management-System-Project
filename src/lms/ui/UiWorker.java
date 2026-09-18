package lms.ui;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.function.Consumer;

/**
 * Thin wrapper over {@link SwingWorker} used by every panel so that all
 * database/service calls run off the Event Dispatch Thread and failures are
 * reported consistently. This is the GUI's only concurrency primitive —
 * panels never spawn raw threads.
 */
public final class UiWorker {

    private UiWorker() {
    }

    /** Supplier that may throw checked exceptions (service methods declare LMSException). */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /** Runnable that may throw checked exceptions (void service methods). */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Runs {@code task} off the EDT, then feeds the result to {@code onSuccess} on the EDT. */
    public static <T> void run(ThrowingSupplier<T> task, Consumer<T> onSuccess) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.get();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception ex) {
                    showError(ex);
                }
            }
        }.execute();
    }

    /** Runs a void task off the EDT, then runs {@code onSuccess} on the EDT. */
    public static void runVoid(ThrowingRunnable task, Runnable onSuccess) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    onSuccess.run();
                } catch (Exception ex) {
                    showError(ex);
                }
            }
        }.execute();
    }

    /** Uniform error dialog for background-task failures (unwraps cause chains). */
    public static void showError(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage() == null ? cause.toString() : cause.getMessage();
        JOptionPane.showMessageDialog(null, message, "Operation Failed", JOptionPane.ERROR_MESSAGE);
    }
}
