/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import dispatcher.MenuDispatcher;
import dispatcher.OrderDispatcher;
import java.util.Scanner;
import utils.InputHandler;
import utils.OutputHandler;

/**
 *
 * @author ASUS
 */
public class Main {

    private static final String[] MAIN_OPTIONS = {"Menu management", "Order management"};

    public static void main(String[] args) {
        Scanner scanner = InputHandler.SCANNER;
        OutputHandler console = new OutputHandler();
        int choice = -1;

        do {
            console.showMenu("Table-side Ordering", MAIN_OPTIONS, "Exit");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                console.showMessage("Value must be an integer.");
                continue;
            }

            switch (choice) {
                case 1:
                    new MenuDispatcher().run();
                    break;
                case 2:
                    new OrderDispatcher().run();
                    break;
                case 0:
                    console.showMessage("Goodbye!");
                    break;
                default:
                    console.showMessage("Invalid choice.");
                    break;
            }
        } while (choice != 0);
    }
}
