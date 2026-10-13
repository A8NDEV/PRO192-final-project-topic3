package controller;

import java.util.Scanner;
import service.MenuService;
import service.OrderService;
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

    private final MenuService menuService;
    private final OrderService orderService;
    private final Scanner scanner = new Scanner(System.in);
    private final ConsoleView console = new ConsoleView();
    private final MenuController menuController;

    public AppController(MenuService menuService, OrderService orderService) {
        this.menuService = menuService;
        this.orderService = orderService;
        this.menuController = new MenuController(menuService, scanner);
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
                    new OrderController(orderService, menuService.getMenuList(), scanner).run();
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
