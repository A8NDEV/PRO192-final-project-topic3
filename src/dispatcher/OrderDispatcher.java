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
        "Create new order",
        "Add item to existing order",
        "Change item quantity",
        "Change order status",
        "Search order",
        "Sorted order by total (high to low)"
    };
    private static final String[] STATUS_OPTIONS = {"PREPARING", "COMPLETED", "CANCELLED"};
    private static final String[] SEARCH_OPTIONS = {"Search by table", "Search by order ID"};

    private final Scanner scanner = InputHandler.SCANNER;
    private final OutputHandler console = new OutputHandler();

    public OrderDispatcher() {
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

        Order existingOrder = OrderManager.findOpenOrderByTable(tableNumber);
        if (existingOrder != null) {
            console.showError("Table " + tableNumber + " already has pending order " + existingOrder.getOrderId()
                    + ". Please add items to that order instead.");
            return;
        }

        String customerName = InputHandler.readCustomerName(scanner, "Enter customer name: ");
        String customerPhone = InputHandler.readPhone(scanner, "Enter customer phone: ");

        boolean addItems = InputHandler.readConfirmed(scanner, "Do you want to add food items? (Y/N): ");
        if (!addItems) {
            console.showMessage("Order creation cancelled. No order was saved.");
            return;
        }

        List<OrderItem> items = new ArrayList<>();
        while (true) {
            console.showAvailableHeader();
            List<MenuItem> availableItems = MenuManager.getAvailableItems();
            console.showAvailableTable(availableItems);

            MenuItem menuItem = selectMenuItem();
            int quantity = readPositiveQuantity("Enter quantity: ");

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
            Order order = OrderManager.createOrder(tableNumber, customerName, customerPhone, items);
            console.showMessage("Order created successfully! Order ID: " + order.getOrderId());
        } catch (IllegalArgumentException e) {
            console.showError(e.getMessage());
        }
    }

    private void addItem() {
        List<Order> pendingOrders = OrderManager.getPendingOrders();
        if (pendingOrders.isEmpty()) {
            console.showMessage("There are no pending orders to update.");
            return;
        }

        console.showMessage("\n===== Pending Orders =====");
        for (Order order : pendingOrders) {
            console.showOrderSummary(order, OrderManager.calculateTotal(order));
        }

        String orderId = "";
        Order order = null;
        while (order == null) {
            orderId = InputHandler.readNonBlank(scanner, "Enter Order ID: ");
            order = OrderManager.findById(orderId);

            if (order == null) {
                console.showError("Order ID '" + orderId + "' does not exist. Please check the ID and try again.");
            } else if (!pendingOrders.contains(order)) {
                console.showError("Order '" + orderId + "' is not pending and cannot be modified.");
                order = null;
            }
        }

        console.showOrderBill(order, OrderManager.calculateTotal(order));

        while (true) {
            console.showAvailableHeader();
            List<MenuItem> availableItems = MenuManager.getAvailableItems();
            console.showAvailableTable(availableItems);

            MenuItem menuItem = selectMenuItem();
            int quantity = readPositiveQuantity("Enter quantity: ");

            try {
                OrderManager.addItem(orderId, menuItem, quantity);
                console.showMessage("Item added successfully.");
                console.showOrderBill(order, OrderManager.calculateTotal(order));
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
        if (!showOpenOrders()) {
            return;
        }
        Order order = readOrderId(true);
        if (order == null) {
            return;
        }

        console.showMessage("\nCurrent items in order " + order.getOrderId() + ":");
        console.showOrderBill(order, OrderManager.calculateTotal(order));

        String itemId = readItemIdInOrder(order);
        int quantity = readNonNegativeQuantity("Enter new quantity (0 to remove item): ");

        try {
            OrderManager.updateQuantity(order.getOrderId(), itemId, quantity);
            console.showMessage(quantity == 0
                    ? "Item removed from order " + order.getOrderId() + "."
                    : "Item quantity updated to " + quantity + ".");
            Order updatedOrder = OrderManager.findById(order.getOrderId());
            if (updatedOrder != null) {
                console.showMessage("\nUpdated order bill:");
                console.showOrderBill(updatedOrder, OrderManager.calculateTotal(updatedOrder));
            }
        } catch (IllegalArgumentException e) {
            console.showError(e.getMessage());
        }
    }

    private MenuItem selectMenuItem() {
        while (true) {
            String itemId = InputHandler.readNonBlank(scanner, "Enter item ID: ");
            MenuItem menuItem = MenuManager.findById(itemId);
            if (menuItem == null) {
                console.showError("Menu item ID '" + itemId
                        + "' does not exist. Please choose an item from the available menu.");
            } else if (!menuItem.isStatus()) {
                console.showError("Menu item '" + itemId + "' is currently unavailable. Please choose another item.");
            } else {
                return menuItem;
            }
        }
    }

    private int readPositiveQuantity(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (Validator.isPositiveInt(value)) {
                return Integer.parseInt(value);
            }
            console.showError("Quantity must be a positive integer greater than 0.");
        }
    }

    private String readItemIdInOrder(Order order) {
        while (true) {
            String id = InputHandler.readNonBlank(scanner, "Menu item ID in this order: ");
            for (OrderItem item : order.getItemsList()) {
                if (item.getMenuItem().getItemId().equalsIgnoreCase(id)) {
                    return item.getMenuItem().getItemId();
                }
            }
            console.showError("Menu item ID '" + id + "' is not in order " + order.getOrderId()
                    + ". Enter an item ID shown in this order.");
        }
    }

    private Order readOrderId(boolean mustBePending) {
        while (true) {
            String id = InputHandler.readNonBlank(scanner, "Enter order ID: ");
            Order order = OrderManager.findById(id);
            if (order == null) {
                console.showError("Order ID '" + id + "' does not exist. Check the ID and try again.");
                continue;
            }
            if (mustBePending && order.getStatus() != OrderStatus.PENDING) {
                console.showError("Order ID '" + id + "' is not in PENDING status.");
                continue;
            }
            return order;
        }
    }

    private boolean showOpenOrders() {
        List<Order> orders = OrderManager.getPendingOrders();
        if (orders.isEmpty()) {
            console.showMessage("No active (PENDING) orders are available.");
            return false;
        }
        console.showMessage("\n----- Active orders (PENDING) -----");
        for (Order order : orders) {
            console.showOrderSummary(order, OrderManager.calculateTotal(order));
        }
        return true;
    }

    private int readNonNegativeQuantity(String prompt) {
        while (true) {
            String raw = InputHandler.readNonBlank(scanner, prompt);
            try {
                int quantity = Integer.parseInt(raw);
                if (quantity >= 0) {
                    return quantity;
                }
                console.showError("Quantity cannot be negative. Enter 0 to remove the item or a positive number.");
            } catch (NumberFormatException e) {
                console.showError("'" + raw + "' is not a whole number. Enter 0, 1, 2, or another whole number.");
            }
        }
    }

    private void changeOrderStatus() {
        List<Order> openOrders = OrderManager.getOpenOrders();

        if (openOrders.isEmpty()) {
            console.showMessage("There are no open orders to manage.");
            return;
        }

        console.showMessage("\n===== Open Orders =====");
        for (Order order : openOrders) {
            console.showOrderSummary(order, OrderManager.calculateTotal(order));
        }

        String orderId = "";
        Order order = null;
        while (order == null) {
            orderId = InputHandler.readNonBlank(scanner, "Enter Order ID: ");
            order = OrderManager.findById(orderId);

            if (order == null) {
                console.showError("Order ID '" + orderId + "' does not exist.");
            } else if (order.getStatus() != OrderStatus.PENDING
                    && order.getStatus() != OrderStatus.PREPARING) {
                console.showError("Order '" + orderId + "' is no longer open and cannot be managed.");
                order = null;
            }
        }

        console.showOrderBill(order, OrderManager.calculateTotal(order));

        int choice = -1;
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
        }

        OrderStatus newStatus = OrderStatus.valueOf(STATUS_OPTIONS[choice - 1]);
        if (order.getStatus() == newStatus) {
            console.showError("Order is already in " + newStatus + " status.");
            return;
        }

        boolean confirmed = InputHandler.readConfirmed(scanner,
                "Confirm changing order " + orderId + " to " + newStatus + "? (Y/N): ");
        if (!confirmed) {
            console.showMessage("Status change cancelled.");
            return;
        }

        try {
            OrderManager.changeStatus(orderId, newStatus);
            console.showMessage("Order " + orderId + " status changed successfully to " + newStatus + ".");
        } catch (IllegalArgumentException e) {
            console.showError(e.getMessage());
        }
    }

    private void searchOrder() {
        int choice = -1;
        do {
            console.showMenu("Search Order", SEARCH_OPTIONS, "Back");
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
                    console.showMessage("Choose 0, 1, or 2.");
                    break;
            }
        } while (choice != 0);
    }

    private void searchByTable() {
        String keyword = InputHandler.readNonBlank(scanner, "Enter table number: ");

        List<Order> found = OrderManager.searchByTable(keyword);
        if (found.isEmpty()) {
            console.showError("No orders found for table '" + keyword + "'.");
            return;
        }
        console.showMessage("----- Orders of table " + keyword + " -----");
        for (Order order : found) {
            console.showOrderSummary(order, OrderManager.calculateTotal(order));
        }
    }

    private void searchByOrderId() {
        String orderId = InputHandler.readNonBlank(scanner, "Enter order ID: ");
        Order order = OrderManager.findById(orderId);
        if (order == null) {
            console.showError("Order '" + orderId + "' does not exist.");
            return;
        }
        console.showOrderBill(order, OrderManager.calculateTotal(order));
    }

    private void sortedOrders() {
        List<Order> sorted = OrderManager.sortedByTotalDesc();
        if (sorted.isEmpty()) {
            console.showMessage("No orders available.");
            return;
        }
        console.showMessage("\n----- Orders sorted by total (high to low) -----");
        for (Order order : sorted) {
            console.showOrderSummary(order, order.getTotal());
        }
    }
}
