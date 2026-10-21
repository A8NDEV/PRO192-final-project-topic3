package controller;

import exception.BusinessException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import models.MenuItem;
import models.Order;
import service.OrderService;
import utils.InputHandler;
import utils.Validator;
import view.ConsoleView;
import view.OrderView;

public class OrderController {

    private static final String[] ORDER_OPTIONS = {
        "Create new order for a table",
        "Add item to existing order",
        "Change item quantity",
        "Remove item / Cancel order",
        "Search order"
    };

    private static final String[] REMOVE_OPTIONS = {
        "Remove one item from an order",
        "Cancel order (kept with status Cancelled for checking)"
    };

    private static final String[] SEARCH_OPTIONS = {
        "Search by table",
        "Search by order ID"
    };

    private final OrderService orderService;
    private final List<MenuItem> menu;
    private final Scanner scanner;
    private final ConsoleView console = new ConsoleView();
    private final OrderView orderView = new OrderView();

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
                    changeQuantity();
                    break;
                case 4:
                    removeOrCancel();
                    break;
                case 5:
                    searchOrder();
                    break;
                case 0:
                    break;
                default:
                    console.showMessage("Invalid choice.");
                    break;
            }
        } while (choice != 0);
    }

    // Mục 1: ngày đơn tự cập nhật theo thời gian thực, chỉ nhập bàn, tên và SĐT
    private void createOrder() {
        String table = readTable();
        String name = readValid("Customer name: ", Validator::getCustomerNameError);
        String phone = readValid("Customer phone (starts with 0 or 84): ", Validator::getPhoneError);

        Order order;
        try {
            order = orderService.createOrder(table, name, phone);
        } catch (BusinessException e) {
            console.showError(e.getMessage());
            return;
        }

        console.showMessage("Created order " + order.getOrderId() + " on " + order.getOrderDate());

        String more = readLine("Add items now? (y/n): ");
        while (more.equalsIgnoreCase("y")) {
            addItemTo(order.getOrderId());
            more = readLine("Add another item? (y/n): ");
        }
    }

    // Mục 2
    private void addItem() {
        if (!showOpenOrders()) {
            return;
        }
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

    // Mục 3: đặt lại số lượng của một món đã có trong đơn
    private void changeQuantity() {
        if (!showOpenOrders()) {
            return;
        }
        String orderId = InputHandler.readNonBlank(scanner, "Order ID: ");
        String itemId = InputHandler.readNonBlank(scanner, "Menu item ID: ");
        int qty = readInt("New quantity: ");

        try {
            orderService.updateQuantity(orderId, itemId, qty);
            console.showMessage("Quantity changed.");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    // Mục 4: xóa món lẻ trong đơn, hoặc đóng đơn (chuyển sang Cancelled, vẫn giữ để tra cứu)
    private void removeOrCancel() {
        console.showMenu("Remove Item / Cancel Order", REMOVE_OPTIONS, "Back");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            console.showMessage("Value must be an integer.");
            return;
        }

        switch (choice) {
            case 1:
                removeItem();
                break;
            case 2:
                cancelOrder();
                break;
            case 0:
                break;
            default:
                console.showMessage("Invalid option.");
                break;
        }
    }

    private void removeItem() {
        if (!showOpenOrders()) {
            return;
        }
        String orderId = InputHandler.readNonBlank(scanner, "Order ID: ");
        if (orderService.findById(orderId) == null) {
            console.showError("Order not found!");
            return;
        }
        String itemId = InputHandler.readNonBlank(scanner, "Menu item ID to remove: ");
        if (!confirm()) {
            console.showMessage("No changes made.");
            return;
        }

        try {
            orderService.removeItem(orderId, itemId);
            console.showMessage("Item removed from order.");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    private void cancelOrder() {
        if (!showOpenOrders()) {
            return;
        }
        String orderId = InputHandler.readNonBlank(scanner, "Order ID: ");
        if (orderService.findById(orderId) == null) {
            console.showError("Order not found!");
            return;
        }
        if (!confirm()) {
            console.showMessage("No changes made.");
            return;
        }

        try {
            orderService.cancelOrder(orderId);
            console.showMessage("Order cancelled. It is kept with status Cancelled.");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }
    //tìm theo bàn hoăc mã đơn

    private void searchOrder() {
        int choice = -1;
        do {
            console.showMenu("Search Order", SEARCH_OPTIONS, "Back");
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                console.showMessage("Value must be an integer.");
                return;
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
                    console.showMessage("Invalid option.");
                    break;
            }
        } while (choice != 0);
    }

    private void searchByTable() {
        String table = readTable();
        List<Order> found = orderService.searchByTable(table);
        if (found.isEmpty()) {
            console.showMessage("No orders found for table " + table + ".");
            return;
        }
        console.showMessage("\n----- Orders of table " + table + " -----");
        for (Order order : found) {
            orderView.showOrderSummary(order, orderService.calculateTotal(order));
        }
    }

    private void searchByOrderId() {
        String orderId = InputHandler.readNonBlank(scanner, "Order ID: ");
        Order order = orderService.searchByOrderId(orderId);
        if (order == null) {
            console.showError("Order not found!");
            return;
        }
        orderView.showOrderSummary(order, orderService.calculateTotal(order));
    }

    // In danh sách đơn đang mở để người dùng nhìn và chọn mã đơn; false nếu không có đơn nào
    private boolean showOpenOrders() {
        List<Order> openOrders = orderService.getOpenOrders();
        if (openOrders.isEmpty()) {
            console.showMessage("No open orders.");
            return false;
        }
        console.showMessage("\n----- Open orders -----");
        for (Order order : openOrders) {
            orderView.showOrderSummary(order, orderService.calculateTotal(order));
        }
        return true;
    }

    private boolean confirm() {
        return InputHandler.readConfirmed(scanner, "Confirm Yes OR No (y/n): ").equalsIgnoreCase("y");
    }

    // Table: 1 chữ cái + số
    private String readTable() {
        while (true) {
            String raw = readLine("Table : ");
            String table = Validator.normalizeTable(raw);

            if (table == null) {
                console.showMessage("Invalid table. A valid table is 1 letter followed by a number. Examples: A1, B12");
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

    // Hỏi lại đến khi hợp lệ; errorChecker trả về câu báo lỗi cụ thể, null nếu hợp lệ
    private String readValid(String prompt, Function<String, String> errorChecker) {
        while (true) {
            String value = readLine(prompt);
            String error = errorChecker.apply(value);
            if (error == null) {
                return value;
            }
            console.showMessage(error);
        }
    }
}
