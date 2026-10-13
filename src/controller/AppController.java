package controller;

import java.util.Scanner;
import service.MenuService;
import service.OrderService;
import view.AppView;

/**
 *
 * @author ASUS
 */
/**
 * Điều hướng menu chính: chọn vào menu con Menu hoặc Order.
 */
public class AppController {

    private final MenuService menuService;
    private final OrderService orderService;
    private final Scanner scanner = new Scanner(System.in);
    private final AppView view = new AppView();
    private final MenuController menuController;

    public AppController(MenuService menuService, OrderService orderService) {
        this.menuService = menuService;
        this.orderService = orderService;
        this.menuController = new MenuController(menuService, scanner);
    }

    public void run() {
        int choice = -1;
        do {
            view.showMainMenu();

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                view.showNotInteger();
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
                    view.showInvalidChoice();
                    break;
            }
        } while (choice != 0);
    }
}
