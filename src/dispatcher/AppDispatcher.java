/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dispatcher;

import java.util.Scanner;
import utils.OutputHandler;

/**
 *
 * @author ASUS
 */
public class AppDispatcher {

    private static final String[] MAIN_OPTIONS = {"Menu management", "Order management"};

    private final Scanner scanner;
    private final MenuDispatcher menuDispatcher;
    private final OrderDispatcher orderDispatcher;
    private final OutputHandler console = new OutputHandler();

    public AppDispatcher(Scanner scanner, MenuDispatcher menuDispatcher, OrderDispatcher orderDispatcher) {
        this.scanner = scanner;
        this.menuDispatcher = menuDispatcher;
        this.orderDispatcher = orderDispatcher;
    }
    
    
    
    
    

    public void run() {
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
                    menuDispatcher.run();
                    break;
                case 2:
                    orderDispatcher.run();
                    break;
                case 0:
                    break;
                default:
                    console.showMessage("Invalid choice.");
                    break;
            }
        } while (choice != 0);
    }
}
