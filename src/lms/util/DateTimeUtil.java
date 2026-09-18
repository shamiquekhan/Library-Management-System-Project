package lms.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateTimeUtil() {
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static String format(LocalDate d) {
        return d == null ? "-" : d.format(FORMAT);
    }

    public static LocalDate parse(String s) {
        return LocalDate.parse(s);
    }

    public static long daysBetween(LocalDate from, LocalDate to) {
        return to.toEpochDay() - from.toEpochDay();
    }
}