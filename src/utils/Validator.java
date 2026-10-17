/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

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

        // Dùng 'uuuu' thay vì 'yyyy' thì chế độ STRICT mới chặn được ngày như 31/02
    public static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static boolean isPositiveInt(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (!trimmed.matches("^[0-9]+$")) {
            return false;
        }
        try {
            return Integer.parseInt(trimmed) > 0;
        } catch (NumberFormatException e) {
            return false; // số quá lớn, tràn int
        }
    }

    public static boolean isValidPhone(String value) {
        return value != null && value.trim().matches("^0[0-9]{9}$");
    }

    public static boolean isValidDate(String value) {
        if (value == null) {
            return false;
        }
        try {
            LocalDate.parse(value.trim(), DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
