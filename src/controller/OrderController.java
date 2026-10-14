package controller;

import exception.BusinessException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import models.MenuItem;
import models.Order;
import service.OrderService;
import utils.InputHandler;
import utils.Validator;
import view.ConsoleView;

public class OrderController {

    private static final String[] ORDER_OPTIONS = {
        "Create new order for a table",
        "Add item to existing order",
        "Update item quantity"
    };

    private final OrderService orderService;
    private final List<MenuItem> menu;
    private final Scanner scanner;
    private final ConsoleView console = new ConsoleView();

    public OrderController(OrderService orderService, List<MenuItem> menu, Scanner scanner) {
        this.orderService = orderService;
        this.menu = menu;
        this.scanner = scanner;
    }

    public void run() {
        int choice = -1;
        do {
            console.showMenu("Order Management", ORDER_OPTIONS, "Back");

            try {
                choice = Integer.parseInt(scanner.nextLine());
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
                    updateQuantity();
                    break;
                case 0:
                    break;
                default:
                    console.showMessage("Invalid choice.");
                    break;
            }
        } while (choice != 0);
    }

    private void createOrder() {
        String date = readValid("Order date (dd/MM/yyyy): ", Validator::isValidDate, "Invalid date. Must be a real date, e.g. 26/10/2026");

        String table = readTable();

        String name = readValid("Customer name: ", Validator::isValidCustomerName, "Name must be 1-50 characters, letters/numbers only, at least one letter." );

        String phone = readValid( "Customer phone (10 digits, starts with 0): ", Validator::isValidPhone, "Invalid phone. Example: 0901234567");

        Order order;
        try {
            order = orderService.createOrder(date, table, name, phone);
        } catch (BusinessException e) {
            console.showError(e.getMessage());
            return;
        }

        console.showMessage("Created order " + order.getOrderId());

        String more = readLine("Add items now? (y/n): ");
        while (more.equalsIgnoreCase("y")) {
            addItemTo(order.getOrderId());
            more = readLine("Add another item? (y/n): ");
        }
    }

    private void addItem() {
        addItemTo(InputHandler.readNonBlank(scanner, "Order ID: "));
    }

    private void addItemTo(String orderId) {
        String itemId = InputHandler.readNonBlank(scanner, "Menu item ID: ");

        MenuItem menuItem = findMenuItem(itemId);
        if (menuItem == null) {
            console.showMessage("Item not found.");
            return;
        }

        int qty = readInt("Quantity: ");

        try {
            orderService.addItem(orderId, menuItem, qty);
            console.showMessage("Item added.");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    private void updateQuantity() {
        String orderId = InputHandler.readNonBlank(scanner, "Order ID: ");
        String itemId = InputHandler.readNonBlank(scanner, "Menu item ID: ");
        int qty = readInt("New quantity: ");

        try {
            orderService.updateQuantity(orderId, itemId, qty);
            console.showMessage("Quantity updated.");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    private String readTable() {
        while (true) {
            String raw = readLine("Table (e.g. 2, A1, VIP12): ");
            String table = Validator.normalizeTable(raw);

            if (table == null) {
                console.showMessage("Invalid table. Use a number, 1 letter + number, or VIP + number "+ "(max 5 characters, not 0). Examples: 2, A1, VIP12");
                continue;
            }

            if (!table.equals(raw)) {
                console.showMessage("Table set to " + table);
            }

            return table;
        }
    }

    private MenuItem findMenuItem(String itemId) {
        for (MenuItem m : menu) {
            if (m.getItemId().equalsIgnoreCase(itemId)) {
                return m;
            }
        }
        return null;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                console.showMessage("Please enter a number.");
            }
        }
    }

    private String readValid(
            String prompt,
            Predicate<String> validator,
            String error
    ) {
        while (true) {
            String value = readLine(prompt);

            if (validator.test(value)) {
                return value;
            }

            console.showMessage(error);
        }
    }
}
