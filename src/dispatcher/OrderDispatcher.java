/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dispatcher;

import business.MenuManager;
import business.OrderManager;
import models.Order;
import models.OrderItem;
import models.OrderStatus;

import java.util.List;
import java.util.Scanner;

import utils.InputHandler;
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
        if (!showOpenOrders()) {
            return;
        }
        Order order = readOrderId(true);
        if (order == null) {
            return;
        }
        console.showMessage("\nCurrent items in order " + order.getOrderId() + ":");
        console.showOrderBill(order, orderManager.calculateTotal(order));

        String itemId = readItemIdInOrder(order);
        if (itemId == null) {
            return;
        }
        int quantity = readNonNegativeQuantity("Enter new quantity (0 to remove item): ");
        if (quantity < 0) {
            return;

        }
        try {
            orderManager.updateQuantity(order.getOrderId(), itemId, quantity);
            console.showMessage(quantity == 0 ? "Item removed from order." + order.getOrderId() + "." : "Item quantity updated to " + quantity + ".");
            Order updatedOrder = orderManager.findById(order.getOrderId());
            if (updatedOrder != null) {
                console.showMessage("\nUpdated order bill:");
                console.showOrderBill(updatedOrder, orderManager.calculateTotal(updatedOrder));
            }
        } catch (IllegalArgumentException e) {
            console.showError(e.getMessage());
        }
    }


    private String readItemIdInOrder(Order order) {

        int attempts = 0;
        while (true) {
            String id = InputHandler.readNonBlank(scanner, "Menu item ID in this order: ");
            for (OrderItem item : order.getItemsList()) {
                if (item.getMenuItem().getItemId().equalsIgnoreCase(id)) return id;
            }
            console.showError("Menu item ID '" + id + "' is not in order " + order.getOrderId() + ". Enter an item ID shown in this order.");
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("item ID in order")) return null;
                attempts = 0;
            }
        }
    }
    private Order readOrderId(boolean mustBePending) {
        int attempts = 0;
        while(true){
            String id = InputHandler.readNonBlank(scanner, "Enter order ID: ");
            Order order = orderManager.findById(id);
            String error = null;
            if(order == null) {
                error = "Order ID '" + id + "' does not exist. Check the ID and try again.";
            } else if(mustBePending && order.getStatus() != OrderStatus.PENDING) {
                error = "Order ID '" + id + "' is not in PENDING status.";
            }
            if(error == null) {
                return order;
            }
            console.showError(error);
            attempts++;
            if(attempts >= 3) {
                if(!continueAfterInvalid("Order ID")) {
                    return null;
                }
                attempts = 0;
            }
        }
    }

    private  boolean showOpenOrders() {
        List<Order> orders = orderManager.getOpenOrders();
        if (orders.isEmpty()) {
            console.showMessage("No active (PENDING or PREPARING) orders are available.");
            return false;
        }
        console.showMessage("\n----- Active orders (PENDING / PREPARING) -----");
        for (Order order : orders) console.showOrderSummary(order, orderManager.calculateTotal(order));
        return true;
    }

    private int readNonNegativeQuantity(String prompt) {
        int attempts = 0;
        while (true) {
            String raw = InputHandler.readNonBlank(scanner, prompt);
            try {
                int quantity = Integer.parseInt(raw);
                if (quantity >= 0) return quantity;
                console.showError("Quantity cannot be negative. Enter 0 to remove the item or a positive number.");
            } catch (NumberFormatException e) {
                console.showError("'" + raw + "' is not a whole number. Enter 0, 1, 2, or another whole number.");
            }
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("quantity")) return -1;
                attempts = 0;
            }
        }
    }

    private boolean continueAfterInvalid(String fieldName) {
        while(true) {
            String answer = InputHandler.readNonBlank(scanner, "Do you want to try again for " + fieldName + "? (Y/N): ");
            if(answer.equalsIgnoreCase("Y")) {
                return true;
            } else if(answer.equalsIgnoreCase("N")) {
                return false;
            } else {
                console.showError("Invalid input. Please enter 'Y' or 'N'.");
            }
        }
    }

    private void manageOrderStatus() {
    }

    private void searchOrder() {
        int choice = -1;
        do {
            console.showMenu("Search Order: ", SEARCH_OPTIONS, "Back");
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                console.showMessage("Value must be an integer.");
                continue;
            }
            switch (choice) {
                case 1:
                    searchByTable();
                    break;
                case 2:
                    searchByOrderId();
                    break;
                case 0:
                    break;
                default:
                    console.showMessage("CChoose 0, 1, or 2..");
                    break;
            }
        } while (choice != 0);
    }

    private void searchByTable() {
        String keyword = InputHandler.readNonBlank(scanner, "Enter table number: ");

        List<Order> found = orderManager.searchByTable(keyword);
        if (found.isEmpty()) {
            console.showError("No orders found for table '" + keyword + "'.");
            return;
        }
        console.showMessage("----- Orders of table " + keyword + " -----");
        for (Order order : found) {
            console.showOrderSummary(order, orderManager.calculateTotal(order));
        }
    }

    private void searchByOrderId() {
        String orderId = InputHandler.readNonBlank(scanner, "Enter order ID: ");
        Order order = orderManager.findById(orderId);
        if (order == null) {
            console.showError("Order '" + orderId + "' does not exist.");
            return;
        }
        console.showOrderBill(order, orderManager.calculateTotal(order));
    }

    private void sortedOrders() {
    }
}
