/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dispatcher;

import business.MenuManager;
import business.OrderManager;
import java.util.Scanner;
import utils.OutputHandler;

public class OrderDispatcher {

    private static final String[] ORDER_OPTIONS = {
        "Create new order for a table",
        "Add item to existing order",
        "Change item quantity",
        "Change order status",
        "Search order",
        "Sorted order by total (high to low)"
    };
    private static final String[] STATUS_OPTIONS = {"PREPARING", "COMPLETED", "CANCELLED"};
    private static final String[] SEARCH_OPTIONS = {"Search by table", "Search by order ID"};

    private final OrderManager orderManager;
    private final MenuManager menuManager;
    private final Scanner scanner;
    private final OutputHandler console = new OutputHandler();

    public OrderDispatcher(OrderManager orderManager, MenuManager menuManager, Scanner scanner) {
        this.orderManager = orderManager;
        this.menuManager = menuManager;
        this.scanner = scanner;
    }

    public void run() {
        int choice = -1;
        do {
            console.showMenu("Order Management", ORDER_OPTIONS, "Back");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                console.showMessage("Value must be an integer.");
                continue;
            }

            switch (choice) {
                case 1:
                    createOrder();
                    break;
                case 2:
                    addItem();
                    break;
                case 3:
                    changeQuantity();
                    break;
                case 4:
                    manageOrderStatus();
                    break;
                case 5:
                    searchOrder();
                    break;
                case 6:
                    sortedOrders();
                    break;
                case 0:
                    break;
                default:
                    console.showMessage("Choose 0 to 6 Options.");
                    break;
            }
        } while (choice != 0);
    }

    private void createOrder() {
    }

    private void addItem() {
    }

    private void changeQuantity() {
    }

    private void manageOrderStatus() {
    }

    private void searchOrder() {
    }

    private void sortedOrders() {
    }
}
