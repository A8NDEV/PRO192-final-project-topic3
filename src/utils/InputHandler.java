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
}
