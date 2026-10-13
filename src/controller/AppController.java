package controller;

import java.util.Scanner;
import java.util.function.Supplier;
import view.ConsoleView;

/**
 *
 * @author ASUS
 */
/**
 * Điều hướng menu chính: chọn vào menu con Menu hoặc Order.
 */
public class AppController {

    private static final String[] MAIN_OPTIONS = {"Menu management", "Order management"};

    private final Scanner scanner;
    private final MenuController menuController;
    private final Supplier<OrderController> orderControllerFactory;
    private final ConsoleView console = new ConsoleView();

    public AppController(Scanner scanner, MenuController menuController,
                         Supplier<OrderController> orderControllerFactory) {
        this.scanner = scanner;
        this.menuController = menuController;
        this.orderControllerFactory = orderControllerFactory;
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
                    orderControllerFactory.get().run();
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
