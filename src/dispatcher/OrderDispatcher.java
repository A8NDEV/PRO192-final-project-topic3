package dispatcher;

import business.BusinessException;
import business.MenuService;
import business.OrderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import models.OrderStatus;
import utils.InputHandler;
import utils.Validator;


public class OrderDispatcher {

    private static final String[] ORDER_OPTIONS = {
        "Create new order for a table",
        "Add item to existing order",
        "Change item quantity",
        "Manage order status",
        "Search order",
        "Sorted order by total (high to low)"
    };

    private static final String[] STATUS_OPTIONS = {
        "Mark as COMPLETED",
        "Mark as CANCELLED"
    };

    private static final String[] SEARCH_OPTIONS = {
        "Search by table",
        "Search by order ID"
    };

    private static final int MAX_INVALID_ATTEMPTS = 3;

    private final OrderService orderService;
    private final MenuService menuService;
    private final Scanner scanner;
    private final ConsoleView console = new ConsoleView();
    private final OrderView orderView = new OrderView();

    public OrderDispatcher(OrderService orderService, MenuService menuService, Scanner scanner) {
        this.orderService = orderService;
        this.menuService = menuService;
        this.scanner = scanner;
    }

    public void run() {
        int choice = -1;
        do {
            console.showMenu("Order Management", ORDER_OPTIONS, "Back");
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                console.showMessage("Please enter an integer menu option.");
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
                    console.showMessage("Invalid choice. Choose an option from the menu.");
            }
        } while (choice != 0);
    }

    
    private void createOrder() {
        String table = readTable();
        Order existing = orderService.findOpenOrderByTable(table);
        if (existing != null) {
            console.showError("Table " + table + " already has pending order "
                    + existing.getOrderId() + ". Please add items to that order instead.");
            return;
        }

        String name = readValid("Customer name: ", Validator::getCustomerNameError);
        String phone = readValid("Customer phone (starts with 0 or 84): ", Validator::getPhoneError);

        if (!readYesNo("Add food items to this new order now? (y/n): ")) {
            console.showMessage("Order creation cancelled. No order was saved.");
            return;
        }

        List<OrderItem> draftItems = new ArrayList<>();
        boolean keepAdding = true;
        while (keepAdding) {
            MenuItem item = readAvailableMenuItem();
            if (item == null) {
                console.showMessage("Stopped adding items. No order was saved.");
                return;
            }

            int quantity = readPositiveQuantity("Quantity: ");
            addToDraft(draftItems, item, quantity);
            keepAdding = readYesNo("Add another item? (y/n): ");
        }

        if (draftItems.isEmpty()) {
            console.showMessage("No items were added. No order was saved.");
            return;
        }

        try {
            Order order = orderService.createOrder(table, name, phone, draftItems);
            console.showMessage("Order " + order.getOrderId() + " created successfully on "
                    + order.getOrderDate() + ".");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    private void addToDraft(List<OrderItem> draftItems, MenuItem item, int quantity) {
        for (OrderItem existing : draftItems) {
            if (existing.getMenuItem().getItemId().equalsIgnoreCase(item.getItemId())) {
                existing.setQuantity(existing.getQuantity() + quantity);
                return;
            }
        }
        draftItems.add(new OrderItem(item, quantity));
    }

    private void addItem() {
        if (!showOpenOrders()) {
            return;
        }

        Order order = readOrderById(true);
        if (order == null) {
            return;
        }

        MenuItem item = readAvailableMenuItem();
        if (item == null) {
            return;
        }

        int quantity = readPositiveQuantity("Quantity to add: ");
        try {
            orderService.addItem(order.getOrderId(), item, quantity);
            console.showMessage("Item added to order " + order.getOrderId() + ".");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    private void changeQuantity() {
        if (!showOpenOrders()) {
            return;
        }

        Order order = readOrderById(true);
        if (order == null) {
            return;
        }

        String itemId = readItemIdInOrder(order);
        if (itemId == null) {
            return;
        }

        int quantity = readNonNegativeQuantity("New quantity (0 removes the item): ");
        try {
            orderService.updateQuantity(order.getOrderId(), itemId, quantity);
            if (quantity == 0) {
                console.showMessage("Item removed from order " + order.getOrderId() + ".");
            } else {
                console.showMessage("Item quantity updated to " + quantity + ".");
            }
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    private void manageOrderStatus() {
        if (!showOpenOrders()) {
            return;
        }

        Order order = readOrderById(true);
        if (order == null) {
            return;
        }

        console.showMenu("Manage Order Status", STATUS_OPTIONS, "Back");
        int choice = readMenuChoice();
        if (choice == 0) {
            console.showMessage("No changes made.");
            return;
        }

        OrderStatus newStatus;
        if (choice == 1) {
            newStatus = OrderStatus.COMPLETED;
        } else if (choice == 2) {
            newStatus = OrderStatus.CANCELLED;
        } else {
            console.showMessage("Invalid option. No changes made.");
            return;
        }

        if (!readYesNo("Change order " + order.getOrderId() + " to "
                + newStatus + "? (y/n): ")) {
            console.showMessage("No changes made.");
            return;
        }

        try {
            orderService.changeStatus(order.getOrderId(), newStatus);
            console.showMessage("Order " + order.getOrderId() + " status changed to " + newStatus + ".");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    private int readMenuChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            console.showMessage("Please enter an integer menu option.");
            return -1;
        }
    }


    private Order readOrderById(boolean mustBePending) {
        int attempts = 0;
        while (true) {
            String id = readLine("Order ID: ");
            Order order = orderService.findById(id);

            if (order == null) {
                attempts++;
                console.showError("Order ID '" + id + "' does not exist. Please check the ID and try again.");
                if (!handleInvalidAttempt(attempts, "Order ID")) {
                    return null;
                }
                if (attempts >= MAX_INVALID_ATTEMPTS) {
                    attempts = 0;
                }
                continue;
            }

            if (mustBePending && order.getStatus() != OrderStatus.PENDING) {
                console.showError("Order " + order.getOrderId() + " is " + order.getStatus()
                        + " and cannot be modified. Only PENDING orders can be changed.");
                return null;
            }
            return order;
        }
    }

    private String readItemIdInOrder(Order order) {
        int attempts = 0;
        while (true) {
            String id = readLine("Menu item ID in this order: ");
            boolean belongs = false;
            for (OrderItem item : order.getItemsList()) {
                if (item.getMenuItem().getItemId().equalsIgnoreCase(id)) {
                    belongs = true;
                    break;
                }
            }

            if (belongs) {
                return id;
            }

            attempts++;
            console.showError("Menu item ID '" + id + "' is not in order "
                    + order.getOrderId() + ". Enter an item ID shown in this order.");
            if (!handleInvalidAttempt(attempts, "Menu item ID")) {
                return null;
            }
            if (attempts >= MAX_INVALID_ATTEMPTS) {
                attempts = 0;
            }
        }
    }

    private MenuItem readAvailableMenuItem() {
        int attempts = 0;
        while (true) {
            String itemId = readLine("Menu item ID: ");
            MenuItem item = menuService.findById(itemId);

            if (item == null) {
                attempts++;
                console.showError("Menu item ID '" + itemId + "' does not exist. Please check the ID.");
            } else if (!item.isStatus()) {
                attempts++;
                console.showError("Menu item '" + itemId + "' is currently unavailable. Choose an available item.");
            } else {
                return item;
            }

            if (!handleInvalidAttempt(attempts, "Menu item ID")) {
                return null;
            }
            if (attempts >= MAX_INVALID_ATTEMPTS) {
                attempts = 0;
            }
        }
    }

    private boolean handleInvalidAttempt(int attempts, String idType) {
        if (attempts < MAX_INVALID_ATTEMPTS) {
            console.showMessage("Attempts remaining: " + (MAX_INVALID_ATTEMPTS - attempts) + ".");
            return true;
        }

        console.showMessage("You have entered an invalid " + idType + " 3 times.");
        return readYesNo("Do you want to continue trying? (y/n): ");
    }

    private int readPositiveQuantity(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            try {
                int quantity = Integer.parseInt(raw);
                if (quantity > 0) {
                    return quantity;
                }
                console.showError("Quantity must be a positive integer greater than 0.");
            } catch (NumberFormatException e) {
                console.showError("Quantity must be a whole number, for example 1, 2, or 3.");
            }
        }
    }

    private int readNonNegativeQuantity(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            try {
                int quantity = Integer.parseInt(raw);
                if (quantity >= 0) {
                    return quantity;
                }
                console.showError("Quantity cannot be negative. Enter 0 to remove the item or a positive integer.");
            } catch (NumberFormatException e) {
                console.showError("Quantity must be a whole number, for example 0, 1, 2, or 3.");
            }
        }
    }

    private void searchOrder() {
        int choice = -1;
        do {
            console.showMenu("Search Order", SEARCH_OPTIONS, "Back");
            choice = readMenuChoice();
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
                    console.showMessage("Invalid option. Choose 0, 1, or 2.");
            }
        } while (choice != 0);
    }

    private void searchByTable() {
        String keyword = InputHandler.readNonBlank(scanner, "Enter Table to search : ");
        List<Order> found = orderService.searchByTable(keyword);
        if (found.isEmpty()) {
            console.showMessage("No orders found for table " + keyword + ".");
            return;
        }
        console.showMessage("\n----- Orders of table " + keyword + " -----");
        for (Order order : found) {
            orderView.showOrderSummary(order, orderService.calculateTotal(order));
        }
    }

    private void searchByOrderId() {
        Order order = readOrderById(false);
        if (order != null) {
            orderView.showOrderSummary(order, orderService.calculateTotal(order));
        }
    }

    private boolean showOpenOrders() {
        List<Order> openOrders = orderService.getOpenOrders();
        if (openOrders.isEmpty()) {
            console.showMessage("No PENDING orders are available.");
            return false;
        }
        console.showMessage("\n----- Pending orders -----");
        for (Order order : openOrders) {
            orderView.showOrderSummary(order, orderService.calculateTotal(order));
        }
        return true;
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt);
            if (answer.equalsIgnoreCase("y")) {
                return true;
            }
            if (answer.equalsIgnoreCase("n")) {
                return false;
            }
            console.showMessage("Please enter Y or N.");
        }
    }

    private String readTable() {
        while (true) {
            String raw = readLine("Table: ");
            String table = Validator.normalizeTable(raw);
            if (table == null) {
                console.showError("Invalid table. Use one letter followed by a number, for example A1 or B12.");
                continue;
            }
            if (!table.equals(raw)) {
                console.showMessage("Table set to " + table + ".");
            }
            return table;
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readValid(String prompt, Function<String, String> errorChecker) {
        while (true) {
            String value = readLine(prompt);
            String error = errorChecker.apply(value);
            if (error == null) {
                return value;
            }
            console.showError(error);
        }
    }

    private void sortedOrders() {
        List<Order> sorted = orderService.sortedByTotalDesc();
        if (sorted.isEmpty()) {
            console.showMessage("No orders available.");
            return;
        }
        console.showMessage("\n----- Orders sorted by total (high to low) -----");
        for (Order order : sorted) {
            orderView.showOrderSummary(order, orderService.calculateTotal(order));
        }
    }
}
