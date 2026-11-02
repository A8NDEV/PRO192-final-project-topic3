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
        String tableNumber = "";
        String customerName = "";
        String customerPhone = "";

        int failedAttempts = 0;
        while (tableNumber.isEmpty()) {
            System.out.print("Enter table number: ");
            String value = scanner.nextLine().trim();
            String normalizedTable = Validator.normalizeTable(value);

            if (normalizedTable != null) {
                tableNumber = normalizedTable;
                if (!tableNumber.equals(value)) {
                    console.showMessage("Table number normalized to " + tableNumber + ".");
                }
            } else {
                console.showError("Invalid table number. Enter one letter followed by numbers, for example A1 or B12.");
                failedAttempts++;
                if (failedAttempts == 3) {
                    boolean retry = InputHandler.readConfirmed(scanner,
                            "You entered an invalid table number 3 times. Continue? (Y/N): ");
                    if (!retry) {
                        console.showMessage("Create order cancelled.");
                        return;
                    }
                    failedAttempts = 0;
                }
            }
        }

        Order existingOrder = orderManager.findOpenOrderByTable(tableNumber);
        if (existingOrder != null) {
            console.showError("Table " + tableNumber + " already has pending order " + existingOrder.getOrderId()
                    + ". Please add items to that order instead.");
            return;
        }

        failedAttempts = 0;
        while (customerName.isEmpty()) {
            System.out.print("Enter customer name: ");
            String value = scanner.nextLine().trim();
            String error = Validator.getCustomerNameError(value);

            if (error == null) {
                customerName = Validator.normalizeName(value);
            } else {
                console.showError(error);
                failedAttempts++;
                if (failedAttempts == 3) {
                    boolean retry = InputHandler.readConfirmed(
                            scanner, "You entered an invalid customer name 3 times. Continue? (Y/N): ");
                    if (!retry) {
                        console.showMessage("Create order cancelled.");
                        return;
                    }
                    failedAttempts = 0;
                }
            }
        }

        failedAttempts = 0;
        while (customerPhone.isEmpty()) {
            System.out.print("Enter customer phone: ");
            String value = scanner.nextLine().trim();
            String error = Validator.getPhoneError(value);
            if (error == null) {
                customerPhone = value;
            } else {
                console.showError(error);
                failedAttempts++;
                if (failedAttempts == 3) {
                    boolean retry = InputHandler.readConfirmed(scanner,
                            "You entered an invalid phone number 3 times. Continue? (Y/N): ");
                    if (!retry) {
                        console.showMessage("Create order cancelled.");
                        return;
                    }
                    failedAttempts = 0;
                }
            }
        }

        boolean addItems = InputHandler.readConfirmed(scanner, "Do you want to add food items? (Y/N): ");
        if (!addItems) {
            console.showMessage("Order creation cancelled. No order was saved.");
            return;
        }
        List<OrderItem> items = new ArrayList<>();

        while (true) {
            console.showAvailableHeader();
            List<MenuItem> availableItems = menuManager.getAvailableItems();
            console.showAvailableTable(availableItems);
            MenuItem menuItem = null;

            failedAttempts = 0;

            while (menuItem == null) {
                System.out.print("Enter item ID: ");
                String itemId = scanner.nextLine().trim();
                menuItem = menuManager.findById(itemId);
                if (menuItem == null) {
                    console.showError("Menu item ID '" + itemId
                            + "' does not exist. Please enter an ID from the available menu.");
                } else if (!menuItem.isStatus()) {
                    console.showError(
                            "Menu item '" + itemId + "' is currently unavailable. Please choose another item.");
                    menuItem = null;
                }

                if (menuItem == null) {
                    failedAttempts++;
                    if (failedAttempts == 3) {
                        boolean retry = InputHandler.readConfirmed(scanner,
                                "You entered an invalid item ID 3 times. Continue? (Y/N): ");
                        if (!retry) {
                            console.showMessage("Create order cancelled. No order was saved.");
                            return;
                        }
                        failedAttempts = 0;
                    }
                }
            }

            int quantity = 0;
            failedAttempts = 0;
            while (quantity <= 0) {
                System.out.print("Enter quantity: ");
                String value = scanner.nextLine().trim();

                if (Validator.isPositiveInt(value)) {
                    quantity = Integer.parseInt(value);
                } else {
                    console.showError("Quantity must be a positive integer greater than 0.");
                    failedAttempts++;
                    if (failedAttempts == 3) {
                        boolean retry = InputHandler.readConfirmed(scanner,
                                "You entered an invalid quantity 3 times. Continue? (Y/N): ");

                        if (!retry) {
                            console.showMessage("Create order cancelled. No order was saved.");
                            return;
                        }
                        failedAttempts = 0;
                    }
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
            console.showMessage("Order creation cancelled. No order was saved.");
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

        // Display pending orders
        console.showMessage("\n===== Pending Orders =====");

        for (Order order : openOrders) {
            console.showOrderSummary(order, orderManager.calculateTotal(order));
        }

        // 1. Validate Order ID
        String orderId = "";
        Order order = null;
        int failedAttempts = 0;

        while (order == null) {
            System.out.print("Enter Order ID: ");
            orderId = scanner.nextLine().trim();

            if (orderId.isEmpty()) {
                console.showError("Order ID cannot be empty. Please enter an existing pending Order ID.");
            } else {
                order = orderManager.findById(orderId);

                if (order == null) {
                    console.showError("Order ID '" + orderId + "' does not exist. Please check the ID and try again.");
                } else if (!openOrders.contains(order)) {
                    console.showError("Order '" + orderId + "' is not pending and cannot be modified.");
                    order = null;
                }
            }

            if (order == null) {
                failedAttempts++;

                if (failedAttempts == 3) {
                    boolean retry = InputHandler.readConfirmed(scanner,
                            "You entered an invalid Order ID 3 times. Continue? (Y/N): ");

                    if (!retry) {
                        console.showMessage("Add item operation cancelled.");
                        return;
                    }
                    failedAttempts = 0;
                }
            }
        }

        console.showOrderBill(order, orderManager.calculateTotal(order));

        // 2. Add items to the selected order
        while (true) {
            console.showAvailableHeader();

            List<MenuItem> availableItems = menuManager.getAvailableItems();
            console.showAvailableTable(availableItems);

            MenuItem menuItem = null;
            failedAttempts = 0;

            // Validate menu item ID
            while (menuItem == null) {
                System.out.print("Enter item ID: ");
                String itemId = scanner.nextLine().trim();

                menuItem = menuManager.findById(itemId);

                if (menuItem == null) {
                    console.showError("Menu item ID '" + itemId
                            + "' does not exist. Please choose an item from the available menu.");
                } else if (!menuItem.isStatus()) {
                    console.showError(
                            "Menu item '" + itemId + "' is currently unavailable. Please choose another item.");
                    menuItem = null;
                }

                if (menuItem == null) {
                    failedAttempts++;

                    if (failedAttempts == 3) {
                        boolean retry = InputHandler.readConfirmed(scanner,
                                "You entered an invalid item ID 3 times. Continue? (Y/N): ");

                        if (!retry) {
                            console.showMessage("Add item operation cancelled.");
                            return;
                        }
                        failedAttempts = 0;
                    }
                }
            }

            // Validate quantity
            int quantity = 0;
            failedAttempts = 0;

            while (quantity <= 0) {
                System.out.print("Enter quantity: ");
                String value = scanner.nextLine().trim();

                if (Validator.isPositiveInt(value)) {
                    quantity = Integer.parseInt(value);
                } else {
                    console.showError("Quantity must be a positive integer greater than 0.");
                    failedAttempts++;

                    if (failedAttempts == 3) {
                        boolean retry = InputHandler.readConfirmed(scanner,
                                "You entered an invalid quantity 3 times. Continue? (Y/N): ");

                        if (!retry) {
                            console.showMessage("Add item operation cancelled.");
                            return;
                        }
                        failedAttempts = 0;
                    }
                }
            }

            // Add item through business layer
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
        List<Order> allOrders = orderManager.getAllOrders();
        List<Order> openOrders = new java.util.ArrayList<>();

        for (Order order : allOrders) {
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PREPARING) {
                openOrders.add(order);
            }
        }

        if (openOrders.isEmpty()) {
            console.showMessage("There are no open orders to manage.");
            return;
        }

        console.showMessage("\n===== Open Orders =====");

        for (Order order : openOrders) {
            console.showOrderSummary(order, orderManager.calculateTotal(order));
        }

        String orderId = "";
        Order order = null;
        int failedAttempts = 0;

        while (order == null) {
            System.out.print("Enter Order ID: ");
            orderId = scanner.nextLine().trim();

            if (orderId.isEmpty()) {
                console.showError("Order ID cannot be empty.");
            } else {
                order = orderManager.findById(orderId);

                if (order == null) {
                    console.showError("Order ID '" + orderId + "' does not exist.");
                } else if (order.getStatus() != OrderStatus.PENDING
                        && order.getStatus() != OrderStatus.PREPARING) {
                    console.showError("Order '" + orderId + "' is no longer open and cannot be managed.");
                    order = null;
                }
            }

            if (order == null) {
                failedAttempts++;

                if (failedAttempts == 3) {
                    boolean continueInput = InputHandler.readConfirmed(
                            scanner,
                            "You entered an invalid Order ID 3 times. Continue? (Y/N): ");

                    if (!continueInput) {
                        console.showMessage("Status change operation cancelled.");
                        return;
                    }

                    failedAttempts = 0;
                }
            }
        }

        console.showOrderBill(order, orderManager.calculateTotal(order));

        int choice = -1;
        failedAttempts = 0;

        while (true) {
            console.showMenu("Choose New Order Status", STATUS_OPTIONS, "Cancel");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();

            try {
                choice = Integer.parseInt(input);

                if (choice == 0) {
                    console.showMessage("Status change cancelled.");
                    return;
                }

                if (choice >= 1 && choice <= STATUS_OPTIONS.length) {
                    break;
                }

                console.showError("Please choose a number from 0 to " + STATUS_OPTIONS.length + ".");

            } catch (NumberFormatException e) {
                console.showError("Status choice must be an integer.");
            }

            failedAttempts++;

            if (failedAttempts == 3) {
                boolean continueInput = InputHandler.readConfirmed(scanner,
                        "You entered an invalid status choice 3 times. Continue? (Y/N): ");

                if (!continueInput) {
                    console.showMessage("Status change cancelled.");
                    return;
                }

                failedAttempts = 0;
            }
        }

        OrderStatus newStatus = OrderStatus.valueOf(STATUS_OPTIONS[choice - 1]);

        if (order.getStatus() == newStatus) {
            console.showError("Order is already in " + newStatus + " status.");
            return;
        }

        failedAttempts = 0;
        boolean confirmed = false;

        while (true) {
            System.out.print("Confirm changing order " + orderId + " to " + newStatus + "? (Y/N): ");

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Y")) {
                confirmed = true;
                break;
            }

            if (input.equalsIgnoreCase("N")) {
                console.showMessage("Status change cancelled.");
                return;
            }

            console.showError("Please enter Y or N.");
            failedAttempts++;

            if (failedAttempts == 3) {
                boolean continueInput = InputHandler.readConfirmed(scanner,
                        "You entered an invalid confirmation 3 times. Continue? (Y/N): ");
                if (!continueInput) {
                    console.showMessage("Status change cancelled.");
                    return;
                }

                failedAttempts = 0;
            }
        }

        if (confirmed) {
            try {
                orderManager.changeStatus(orderId, newStatus);
                console.showMessage("Order " + orderId + " status changed successfully to " + newStatus + ".");
            } catch (IllegalArgumentException e) {
                console.showError(e.getMessage());
            }
        }
    }

    private void searchOrder() {
    }

    private void sortedOrders() {
        List<Order> orderList = orderManager.getAllOrders();
        if (orderList.isEmpty()) {
            console.showMessage("No orders available..");
            return;
        }

        List<Order> sorted = new ArrayList<>(orderList);
        sorted.sort((a, b) -> Double.compare(orderManager.calculateTotal(b), orderManager.calculateTotal(a)));
        console.showMessage("\n----- Orders sorted by total (high to low) -----");

        for (Order order : sorted) {
            double total = orderManager.calculateTotal(order);
            console.showOrderSummary(order, total);
        }
    }
}
