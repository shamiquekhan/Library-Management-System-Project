package lms.util;

import java.time.LocalDate;
import java.util.Scanner;

public final class Console {

    private static final Scanner IN = new Scanner(System.in);

    private Console() {
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return IN.nextLine().trim();
    }

    public static String readNonEmpty(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  [!] Value cannot be empty.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("  [!] Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] '" + value + "' is not a valid number. Try again.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("  [!] '" + value + "' is not a valid amount.");
            }
        }
    }

    public static void pause() {
        readLine("  Press ENTER to continue...");
    }
}