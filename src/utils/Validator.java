/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ASUS
 */
public class Validator {

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

    public static String getCustomerNameError(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Name cannot be blank.";
        }

        String trimmed = value.trim();

        if (trimmed.length() > 50) {
            return "Name cannot exceed 50 characters.";
        }

        if (!trimmed.matches("^[a-zA-Z0-9 ]+$")) {
            return "Name can only contain letters, numbers, and spaces.";
        }

        if (!trimmed.matches(".*[a-zA-Z].*")) {
            return "Name must contain at least one letter.";
        }

        return null;
    }

    public static boolean isValidCustomerName(String value) {
        return getCustomerNameError(value) == null;
    }

    // Chuan hoa ten: xoa khoang trang thua va viet hoa chu cai dau moi tu
    public static String normalizeName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String[] words = value.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            sb.append(Character.toUpperCase(w.charAt(0)))
                    .append(w.substring(1).toLowerCase());
            if (i < words.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    // SDT: bat dau bang 0 (10 so) hoac 84 (11 so), 9 so cuoi khong giong het nhau.
    public static String getPhoneError(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Phone cannot be blank.";
        }
        String trimmed = value.trim();
        if (!trimmed.matches("^(0|84)\\d{9}$")) {
            return "Phone must start with 0 (10 digits) or 84 (11 digits).";
        }
        String lastNine = trimmed.substring(trimmed.length() - 9);
        if (lastNine.matches("(\\d)\\1{8}")) {
            return "The last 9 digits cannot all be the same.";
        }
        return null;
    }

    public static boolean isValidPhone(String value) {
        return getPhoneError(value) == null;
    }

    // Chuan hoa cach dat ten cho table (1 chu cai tu dong in hoa va kem theo so, vi
    // du : A1, B12)
    public static String normalizeTable(String value) {
        if (value == null) {
            return null;
        }
        Matcher m = Pattern.compile("^([A-Z])(\\d+)$").matcher(value.trim().toUpperCase());
        if (!m.matches()) {
            return null;
        }
        String digits = m.group(2).replaceFirst("^0+", "");
        if (digits.isEmpty()) {
            return null;
        }
        String table = m.group(1) + digits;
        return table.length() <= 5 ? table : null;
    }

    public static boolean isPositiveInt(String value) {
        if (value == null || !value.matches("^[0-9]+$")) {
            return false;
        }
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
