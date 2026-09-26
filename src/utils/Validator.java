/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

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
}
