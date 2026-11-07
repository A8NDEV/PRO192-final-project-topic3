/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.util.Scanner;

/**
 *
 * @author ASUS
 */
public class InputHandler {

    public static final Scanner SCANNER = new Scanner(System.in);

    public static String readNonBlank(Scanner scanner, String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = scanner.nextLine();
            if (!Validator.isNotBlank(value)) {
                System.out.println("Cannot be blank.");
            } else if (Validator.containsDelimiter(value)) {
                System.out.println("Cannot contain '|' character.");
            }
        } while (!Validator.isNotBlank(value) || Validator.containsDelimiter(value));
        return value.trim();
    }

    public static double readValidPrice(Scanner scanner, String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = scanner.nextLine();
            if (!Validator.isValidPrice(value)) {
                System.out.println("Invalid price.");
            }
        } while (!Validator.isValidPrice(value));
        return Double.parseDouble(value);
    }

    public static boolean readBoolean(Scanner scanner, String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = scanner.nextLine();
            if (!Validator.isValidBoolean(value)) {
                System.out.println("Invalid value. Enter true or false.");
            }
        } while (!Validator.isValidBoolean(value));
        return Boolean.parseBoolean(value.trim());
    }

    public static boolean readConfirmed(Scanner scanner, String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = scanner.nextLine().trim();
            if (!Validator.isConfirmed(value)) {
                System.out.println("Invalid value. Enter Y or N");
            }
        } while (!Validator.isConfirmed(value));
        return value.equalsIgnoreCase("y");
    }

    public static String readCustomerName(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            String error = Validator.getCustomerNameError(value);
            if (error != null) {
                System.out.println(error);
                continue;
            }
            return Validator.normalizeName(value);
        }
    }

    public static String readPhone(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            String error = Validator.getPhoneError(value);
            if (error != null) {
                System.out.println(error);
                continue;
            }
            return value.trim();
        }
    }

    public static String readTable(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            String table = Validator.normalizeTable(value);
            if (table == null) {
                System.out.println("Invalid table name (e.g., A1, B12). Max 5 characters.");
                continue;
            }
            return table;
        }
    }
}
