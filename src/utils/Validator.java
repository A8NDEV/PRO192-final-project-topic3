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

    private static final String DATE_REGEX = "^\\d{2}/\\d{2}/\\d{4}$";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern TABLE_PATTERN = Pattern.compile("^([A-Z])(\\d+)$");
    private static final int TABLE_MAX_LENGTH = 5;
    private static final int NAME_MAX_LENGTH = 50;
    private static final Pattern NAME_INVALID_CHAR = Pattern.compile("[^\\p{L}\\p{M}\\p{N} ]");
    private static final Pattern LETTER = Pattern.compile("\\p{L}");
    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");

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

    //phân loại validate khi nhập name
    public static String getCustomerNameError(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Name cannot be blank.";
        }
        if (value.length() > NAME_MAX_LENGTH) {
            return "Name is too long (max " + NAME_MAX_LENGTH + " characters). You entered "
                    + value.length() + ".";
        }
        if (!value.equals(value.trim())) {
            return "Name cannot start or end with a space.";
        }
        Matcher invalid = NAME_INVALID_CHAR.matcher(value);
        if (invalid.find()) {
            return "Name can only contain letters, numbers and spaces. Invalid character: '"
                    + invalid.group() + "'.";
        }
        if (value.contains("  ")) {
            return "Name cannot have two spaces in a row.";
        }
        if (!LETTER.matcher(value).find()) {
            return "Name must contain at least one letter.";
        }
        return null;
    }

    public static boolean isValidCustomerName(String value) {
        return getCustomerNameError(value) == null;
    }

    // SĐT: bắt đầu bằng 0 (10 số) hoặc 84 (11 số), 9 số cuối không giống hệt nhau.
    public static String getPhoneError(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Phone cannot be blank.";
        }
        Matcher nonDigit = NON_DIGIT.matcher(value);
        if (nonDigit.find()) {
            return "Phone can only contain digits. Invalid character: '" + nonDigit.group() + "'.";
        }
        if (value.startsWith("84")) {
            if (value.length() != 11) {
                return "Phone starting with 84 must have 11 digits (you entered " + value.length() + ").";
            }
        } else if (value.startsWith("0")) {
            if (value.length() != 10) {
                return "Phone starting with 0 must have 10 digits (you entered " + value.length() + ").";
            }
        } else {
            return "Phone must start with 0 or 84.";
        }
        String lastNine = value.substring(value.length() - 9);
        if (lastNine.chars().distinct().count() == 1) {
            return "The last 9 digits cannot all be the same.";
        }
        return null;
    }

    public static boolean isValidPhone(String value) {
        return getPhoneError(value) == null;
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

    //Chuẩn hóa cách đặt tên cho table (1 chữ cái tự động in hoa và kèm theo số, ví dụ : A1, B12)
    public static String normalizeTable(String value) {
        if (value == null) {
            return null;
        }
        Matcher m = TABLE_PATTERN.matcher(value.trim().toUpperCase());
        if (!m.matches()) {
            return null;
        }
        String digits = m.group(2).replaceFirst("^0+", "");
        if (digits.isEmpty()) {
            return null;
        }
        String table = m.group(1) + digits;
        return table.length() <= TABLE_MAX_LENGTH ? table : null;
    }
}