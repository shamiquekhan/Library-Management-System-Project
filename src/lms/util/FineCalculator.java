package lms.util;

import java.time.LocalDate;

public final class FineCalculator {

    private FineCalculator() {
    }

    public static double calculate(LocalDate dueDate, LocalDate returnDate, double perDay) {
        if (returnDate == null || dueDate == null) {
            return 0.0;
        }
        long daysLate = DateTimeUtil.daysBetween(dueDate, returnDate);
        if (daysLate <= 0) {
            return 0.0;
        }
        return daysLate * perDay;
    }

    public static String money(double amount) {
        return String.format("Rs. %.2f", amount);
    }
}