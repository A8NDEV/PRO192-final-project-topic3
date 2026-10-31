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
/**
 * Doc va validate input tu console, dung chung cho moi Dispatcher (Menu,
 * Order...) de khong phai lap lai code hoi - nhap - kiem tra.
 */
public class InputHandler {

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

    public static String readOptionalNonBlank(Scanner scanner, String prompt, String dValue) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return dValue;
            }

            if (!Validator.containsDelimiter(value)) {
                return value;
            }

            System.out.println("Cannot contain '|' character.");
        }
    }

    public static double readOptionalPrice(Scanner scanner, String prompt, double dValue) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) return dValue;
            if (Validator.isValidPrice(value)) {
                return Double.parseDouble(value);
            }
            System.out.println("Invalid price. Must be a positive number.");
        }
    }

    public static boolean readOptionalBoolean(Scanner scanner, String prompt, boolean dStatus) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) return dStatus;
            if (Validator.isValidBoolean(value)) {
                return Boolean.parseBoolean(value);
            }
            System.out.println("Invalid value. Enter true or false.");
        }
    }
}
