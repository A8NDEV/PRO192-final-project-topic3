/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dispatcher;

import business.MenuManager;
import business.OrderManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import models.OrderStatus;
import utils.InputHandler;
import utils.OutputHandler;
import utils.Validator;

public class OrderDispatcher {

    private static final String[] ORDER_OPTIONS = {
            "Create new order for a table",
            "Add item to existing order",
            "Change item quantity",
            "Change order status",
            "Search order",
            "Sorted order by total (high to low)"
    };
    private static final String[] STATUS_OPTIONS = { "PREPARING", "COMPLETED", "CANCELLED" };
    private static final String[] SEARCH_OPTIONS = { "Search by table", "Search by order ID" };

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
                    changeOrderStatus();
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

        String tableNumber = InputHandler.readTable(scanner, "Enter table number: ");
        String customerName = InputHandler.readCustomerName(scanner, "Enter customer name: ");
        String customerPhone = InputHandler.readPhone(scanner, "Enter customer phone: ");
        boolean addItems = InputHandler.readConfirmed(scanner, "Do you want to add food items? (Y/N): ");

        if (!addItems) {
            console.showMessage("Order creation cancelled.");
            return;
        }

        List<OrderItem> items = new ArrayList<>();

        while (true) {
            console.showAvailableHeader();

            List<MenuItem> availableItems = menuManager.getAvailableItems();
            console.showAvailableTable(availableItems);
            String itemId = InputHandler.readNonBlank(scanner, "Enter item ID: ");
            MenuItem menuItem = menuManager.findById(itemId);

            if (menuItem == null || !menuItem.isStatus()) {
                console.showError("Item ID '" + itemId + "' does not exist or is unavailable.");
                continue;
            }

            int quantity = 0;
            boolean validQuantity = false;

            while (!validQuantity) {
                System.out.print("Enter quantity: ");
                String value = scanner.nextLine().trim();

                try {
                    quantity = Integer.parseInt(value);
                    if (quantity <= 0) {
                        console.showError("Quantity must be greater than 0.");
                    } else {
                        validQuantity = true;
                    }
                } catch (NumberFormatException e) {
                    console.showError("Quantity must be a positive integer.");
                }
            }

            boolean itemExists = false;

            for (OrderItem item : items) {
                if (item.getMenuItem().getItemId().equalsIgnoreCase(menuItem.getItemId())) {
                    item.setQuantity(item.getQuantity() + quantity);
                    itemExists = true;
                    break;
                }
            }

            if (!itemExists) {
                items.add(new OrderItem(menuItem, quantity));
            }

            boolean addAnother = InputHandler.readConfirmed(scanner, "Do you want to add another item? (Y/N): ");
            if (!addAnother) {
                break;
            }
        }

        console.showDraftBill(tableNumber, customerName, customerPhone, items);
        boolean confirmed = InputHandler.readConfirmed(scanner, "Confirm and create this order? (Y/N): ");

        if (!confirmed) {
            console.showMessage("Order creation cancelled.");
            return;
        }

        try {
            Order order = orderManager.createOrder(tableNumber, customerName, customerPhone, items);
            console.showMessage("Order created successfully! Order ID: " + order.getOrderId());

        } catch (IllegalArgumentException e) {
            console.showError(e.getMessage());
        }
    }

    private void addItem() {
        List<Order> openOrders = orderManager.getOpenOrders();
        if (openOrders.isEmpty()) {
            console.showMessage("There are no pending orders to update.");
            return;
        }

        console.showMessage("\n===== Pending Orders =====");
        for (Order order : openOrders) {
            console.showOrderSummary(order, orderManager.calculateTotal(order));
        }

        String orderId = InputHandler.readNonBlank(scanner, "Enter Order ID: ");
        Order order = orderManager.findById(orderId);

        if (order == null) {
            console.showError("Order ID '" + orderId + "' does not exist.");
            return;
        }

        if (!openOrders.contains(order)) {
            console.showError("Order '" + orderId + "' is not pending and cannot be modified.");
            return;
        }

        console.showOrderBill(order, orderManager.calculateTotal(order));
        while (true) {
            console.showAvailableHeader();
            List<MenuItem> availableItems = menuManager.getAvailableItems();
            console.showAvailableTable(availableItems);
            String itemId = InputHandler.readNonBlank(scanner, "Enter Item ID: ");
            MenuItem menuItem = menuManager.findById(itemId);

            if (menuItem == null || !menuItem.isStatus()) {
                console.showError( "Item ID '" + itemId + "' does not exist or is currently unavailable.");
                continue;
            }

            int quantity = 0;
            boolean validQuantity = false;
            while (!validQuantity) {
                System.out.print("Enter quantity: ");
                String value = scanner.nextLine().trim();

                try {
                    quantity = Integer.parseInt(value);
                    if (quantity <= 0) {
                        console.showError( "Quantity must be greater than 0.");
                    } else { validQuantity = true;}

                } catch (NumberFormatException e) {
                    console.showError( "Quantity must be a positive integer.");
                }
            }

            try {
                orderManager.addItem(orderId, menuItem, quantity);
                console.showMessage("Item added successfully.");
                console.showOrderBill(order, orderManager.calculateTotal(order));
            } catch (IllegalArgumentException e) {
                console.showError(e.getMessage());
                return;
            }

            boolean addAnother = InputHandler.readConfirmed(scanner, "Do you want to add another item? (Y/N): ");
            if (!addAnother) {
                break;
            }
        }
    }

    private void changeQuantity() {
    }

    private void changeOrderStatus() {
    }

    private void searchOrder() {
    }

    private void sortedOrders() {
    }
}
