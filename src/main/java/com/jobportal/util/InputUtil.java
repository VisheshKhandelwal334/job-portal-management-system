package com.jobportal.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Pattern;

/** Validated console input helpers (exception handling for bad input). */
public final class InputUtil {

    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9]{10}$");

    private InputUtil() { }

    private static String readLine() {
        try {
            String line = IN.readLine();
            if (line == null) {          // Ctrl+D / end of input
                System.out.println("\nInput closed. Exiting.");
                System.exit(0);
            }
            return line.trim();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Console read failed", e);
        }
    }

    public static String text(String prompt) {
        System.out.print(prompt);
        return readLine();
    }

    public static String required(String prompt) {
        while (true) {
            String s = text(prompt);
            if (!s.isEmpty()) return s;
            System.out.println("  ! This field cannot be empty.");
        }
    }

    /** Returns the existing value if the user just presses Enter. */
    public static String optional(String prompt, String current) {
        String s = text(prompt + " [" + (current == null ? "" : current) + "]: ");
        return s.isEmpty() ? current : s;
    }

    public static int integer(String prompt) {
        while (true) {
            String s = text(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a whole number.");
            }
        }
    }

    public static double decimal(String prompt, double min, double max) {
        while (true) {
            String s = text(prompt);
            try {
                double d = Double.parseDouble(s);
                if (d >= min && d <= max) return d;
                System.out.printf("  ! Value must be between %.2f and %.2f.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a valid number.");
            }
        }
    }

    public static String email(String prompt) {
        while (true) {
            String s = text(prompt).toLowerCase(Locale.ROOT);
            if (EMAIL.matcher(s).matches()) return s;
            System.out.println("  ! Invalid email format.");
        }
    }

    public static String phone(String prompt) {
        while (true) {
            String s = text(prompt);
            if (PHONE.matcher(s).matches()) return s;
            System.out.println("  ! Phone must be exactly 10 digits.");
        }
    }

    public static String password(String prompt) {
        while (true) {
            java.io.Console c = null;
            String s;
            if (c != null) {
                char[] chars = c.readPassword(prompt);
                s = chars == null ? "" : new String(chars);
            } else {
                s = text(prompt);
            }
            if (s.length() >= 6) return s;
            System.out.println("  ! Password must be at least 6 characters.");
        }
    }

    public static boolean confirm(String prompt) {
        return text(prompt + " (y/n): ").equalsIgnoreCase("y");
    }

    public static void pause() {
        text("\nPress Enter to continue...");
    }
}
