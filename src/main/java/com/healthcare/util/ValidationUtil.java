package com.healthcare.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Reusable input validation helpers. Every method throws ValidationException with a friendly message. */
public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[+]?[0-9 -]{7,15}$");

    private ValidationUtil() { }

    public static String required(String v, String field) throws ValidationException {
        if (v == null || v.trim().isEmpty()) throw new ValidationException(field + " is required.");
        return v.trim();
    }

    public static String email(String v) throws ValidationException {
        String e = required(v, "Email").toLowerCase();
        if (!EMAIL.matcher(e).matches()) throw new ValidationException("Please enter a valid email address.");
        return e;
    }

    public static String password(String v) throws ValidationException {
        if (v == null || v.length() < 8 || !v.matches(".*[A-Za-z].*") || !v.matches(".*\\d.*"))
            throw new ValidationException("Password must be at least 8 characters and contain a letter and a digit.");
        return v;
    }

    public static String optionalPhone(String v) throws ValidationException {
        if (v == null || v.trim().isEmpty()) return null;
        if (!PHONE.matcher(v.trim()).matches()) throw new ValidationException("Invalid phone number.");
        return v.trim();
    }

    public static LocalDate date(String v, String field) throws ValidationException {
        try { return LocalDate.parse(required(v, field)); }
        catch (DateTimeParseException e) { throw new ValidationException(field + " must be a valid date."); }
    }

    public static LocalDate optionalDate(String v, String field) throws ValidationException {
        return (v == null || v.trim().isEmpty()) ? null : date(v, field);
    }

    public static LocalTime time(String v, String field) throws ValidationException {
        try { return LocalTime.parse(required(v, field)); }
        catch (DateTimeParseException e) { throw new ValidationException(field + " must be a valid time."); }
    }

    public static int integer(String v, String field) throws ValidationException {
        try { return Integer.parseInt(required(v, field)); }
        catch (NumberFormatException e) { throw new ValidationException(field + " must be a number."); }
    }
}
