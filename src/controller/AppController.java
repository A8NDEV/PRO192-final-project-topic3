package controller;

import java.util.Scanner;

import view.ConsoleView;

/**
 *
 * @author ASUS
 */
/**
 * Điều hướng menu chính: chọn vào menu con Item hoặc Order.
 */
public class AppController {

    private static final String[] MAIN_OPTIONS = {"Menu management", "Order management"};

    private final Scanner scanner;
    private final MenuController menuController;
    private final OrderController orderController;
    private final ConsoleView console = new ConsoleView();

    public AppController(Scanner scanner, MenuController menuController, OrderController orderController) {
        this.scanner = scanner;
        this.menuController = menuController;
        this.orderController = orderController;
    }
    
    
    
    
    

    public void run() {
        int choice = -1;
        do {
            console.showMenu("Table-side Ordering", MAIN_OPTIONS, "Exit");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                console.showMessage("Value must be an integer.");
                continue;
            }

            switch (choice) {
                case 1:
                    menuController.run();
                    break;
                case 2:
                    orderController.run();
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
