/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ASUS
 */
public class Validator {

    private static final String NAME_REGEX = "^(?=.{1,50}$)(?=.*\\p{L})[\\p{L}\\p{M}\\p{N}]+(\\s[\\p{L}\\p{M}\\p{N}]+)*$";
    private static final String PHONE_REGEX = "^0\\d{9}$";
    private static final String DATE_REGEX = "^\\d{2}/\\d{2}/\\d{4}$";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern TABLE_PATTERN = Pattern.compile("^(VIP|[A-Z])?(\\d+)$");
    private static final int TABLE_MAX_LENGTH = 5;

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean containsDelimiter(String value) {
        return value != null && value.contains("|");
    }

    public static boolean isValidBoolean(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false");
    }

    public static boolean isValidPrice(String value) {
        if (value == null) {
            return false;
        }

        String trimmed = value.trim();

        if (!trimmed.matches("^[0-9]+(\\.[0-9]{1,2})?$")) {
            return false;
        }

        try {
            double price = Double.parseDouble(trimmed);
            return price > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isConfirmed(String value) {
        if (value == null) {
            return false;
        }
        String choice = value.trim().toLowerCase();
        return choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("n");
    }

    public static boolean isValidCustomerName(String value) {
        return value != null && value.matches(NAME_REGEX);
    }

    public static boolean isValidPhone(String value) {
        return value != null && value.matches(PHONE_REGEX);
    }


    public static boolean isValidDate(String value) {
        if (value == null || !value.matches(DATE_REGEX)) {
            return false;
        }
        try {
            LocalDate.parse(value, DATE_FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }


    public static String normalizeTable(String value) {
        if (value == null) {
            return null;
        }
        Matcher m = TABLE_PATTERN.matcher(value.trim().toUpperCase());
        if (!m.matches()) {
            return null;
        }
        String prefix = m.group(1) == null ? "" : m.group(1);
        String digits = m.group(2).replaceFirst("^0+", "");
        if (digits.isEmpty()) {
            return null;
        }
        String table = prefix + digits;
        return table.length() <= TABLE_MAX_LENGTH ? table : null;
    }
}