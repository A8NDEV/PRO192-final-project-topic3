package dispatcher;

import java.util.Scanner;

/**
 *
 * @author ASUS
 */
/**
 * Điều hướng menu chính: chọn vào menu con Item hoặc Order.
 */
public class AppDispatcher {

    private static final String[] MAIN_OPTIONS = {"Menu management", "Order management"};

    private final Scanner scanner;
    private final MenuDispatcher menuDispatcher;
    private final OrderDispatcher orderDispatcher;
    private final ConsoleView console = new ConsoleView();

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
