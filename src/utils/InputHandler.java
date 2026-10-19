/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.time.LocalDate;
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

        public static int readTableNumber(Scanner scanner, String prompt) {
        return readPositiveInt(scanner, prompt, "Table number must be a positive integer.");
    }

    public static int readQuantity(Scanner scanner, String prompt) {
        return readPositiveInt(scanner, prompt, "Quantity must be a positive integer.");
    }

    private static int readPositiveInt(Scanner scanner, String prompt, String errorMessage) {
        String value;
        do {
            System.out.print(prompt);
            value = scanner.nextLine();
            if (!Validator.isPositiveInt(value)) {
                System.out.println(errorMessage);
            }
        } while (!Validator.isPositiveInt(value));
        return Integer.parseInt(value.trim());
    }

    public static String readPhone(Scanner scanner, String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = scanner.nextLine();
            if (!Validator.isValidPhone(value)) {
                System.out.println("Invalid phone. Must be 10 digits and start with 0.");
            }
        } while (!Validator.isValidPhone(value));
        return value.trim();
    }

    public static LocalDate readDate(Scanner scanner, String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = scanner.nextLine();
            if (!Validator.isValidDate(value)) {
                System.out.println("Invalid date. Format: dd/MM/yyyy.");
            }
        } while (!Validator.isValidDate(value));
        return LocalDate.parse(value.trim(), Validator.DATE_FORMAT);
    }
    
    public static String readConfirmed(Scanner scanner, String promt){
        String value;
        do{
            System.out.print(promt);
            value = scanner.nextLine().trim();
            if(!Validator.isNotBlank(value) || !Validator.isConfirmed(value)){
                System.out.println("Invalid value. Enter Y or N");
            }
        } while(!Validator.isNotBlank(value) || !Validator.isConfirmed(value));
        return value.trim();
    }
}
