package dispatcher;

import business.BusinessException;
import business.MenuService;
import business.OrderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
        "Change order status", 
        "Search order",
        "Sorted order by total (high to low)"
    };
    private static final String[] STATUS_OPTIONS = {"PREPARING", "COMPLETED", "CANCELLED"};
    private static final String[] SEARCH_OPTIONS = {"Search by table", "Search by order ID"};
    private final OrderService orderService;
    private final MenuService menuService;
    private final Scanner scanner;
    private final ConsoleView console = new ConsoleView();

    public OrderDispatcher(OrderService orderService, MenuService menuService, Scanner scanner) {
        this.orderService = orderService;
        this.menuService = menuService;
        this.scanner = scanner;
    }

    public void run() {
        int choice;
        do {
            console.showMenu("Order Management", ORDER_OPTIONS, "Back");
            choice = readMenuChoice("Order menu");
            switch (choice) {
                case 1: createOrder();
                 break;
                case 2: addItem();
                 break;
                case 3: changeQuantity();
                 break;
                case 4: manageOrderStatus();
                 break;
                case 5: searchOrder();
                 break;
                case 6: sortedOrders();
                 break;
                case 0: break;
                default: console.showError("Choose 0 to 6 from the Order Management menu.");
            }
        } while (choice != 0);
    }

    // Function 1: collect items first, review the bill, then save only after confirmation.
    private void createOrder() {
        String table = readTable();
        if (table == null) return;
        Order existing = orderService.findOpenOrderByTable(table);
        if (existing != null) {
            console.showError("Table " + table + " already has PENDING order " + existing.getOrderId()
                    + ". Please add items to that order instead of creating a duplicate.");
            return;
        }
        String name = readCustomerName();
        if (name == null) return;
        String phone = readCustomerPhone();
        if (phone == null) return;

        List<OrderItem> draft = new ArrayList<>();
        while (true) {
            console.showMessage("\nAvailable menu items:");
            console.showAvailableTable(menuService.getAvailableItems());
            MenuItem item = readAvailableMenuItem();
            if (item == null) return;
            int quantity = readPositiveQuantity("Quantity for " + item.getItemName() + ": ");
            if (quantity < 0) return;
            addToDraft(draft, item, quantity);
            if (!readYesNo("Add another item? (Y/N): ")) break;
        }
        if (draft.isEmpty()) {
            console.showError("An order must contain at least one item. No order was saved.");
            return;
        }

        while (true) {
            console.showDraftBill(table, name, phone, draft);
            console.showMessage("1. Confirm and create order\n2. Edit items\n0. Cancel without saving");
            int choice = readChoiceRange("Bill confirmation", 0, 2);
            if (choice == -1 || choice == 0) {
                console.showMessage("Order creation cancelled. No order was saved.");
                return;
            }
            if (choice == 1) {
                try {
                    Order order = orderService.createOrder(table, name, phone, draft);
                    console.showMessage("Order " + order.getOrderId() + " created successfully on "
                            + order.getOrderDate() + ".");
                } catch (BusinessException e) {
                    console.showError(e.getMessage());
                }
                return;
            }
            if (!editDraftItems(draft)) return;
            if (draft.isEmpty()) {
                console.showError("The bill has no items. Add at least one item or cancel the order.");
                if (!readYesNo("Return to edit items? (Y/N): ")) return;
            }
        }
    }

    private boolean editDraftItems(List<OrderItem> draft) {
        while (true) {
            console.showMessage("\nEdit items: enter an item ID to add/change, or 0 to finish editing.");
            console.showDraftItems(draft);
            console.showAvailableTable(menuService.getAvailableItems());
            String id = readLine("Menu item ID (0 to finish): ");
            if (id == null) return false;
            if (id.equals("0")) return true;
            if (id.isEmpty()) {
                console.showError("Menu item ID cannot be blank. Enter an ID from the menu or 0 to finish.");
                if (!continueAfterInvalid("menu item ID")) return false;
                continue;
            }
            MenuItem item = menuService.findById(id);
            if (item == null || !item.isStatus()) {
                console.showError(item == null ? "Menu item ID '" + id + "' does not exist. Check the ID in the menu."
                        : "Menu item '" + id + "' is unavailable. Choose an available item.");
                if (!continueAfterInvalid("item ID")) return false;
                continue;
            }
            int quantity = readNonNegativeQuantity("Quantity to set for " + item.getItemName() + " (0 removes it): ");
            if (quantity < 0) return false;
            setDraftQuantity(draft, item, quantity);
        }
    }

    private void setDraftQuantity(List<OrderItem> draft, MenuItem item, int quantity) {
        for (int i = 0; i < draft.size(); i++) {
            OrderItem current = draft.get(i);
            if (current.getMenuItem().getItemId().equalsIgnoreCase(item.getItemId())) {
                if (quantity == 0) draft.remove(i);
                else current.setQuantity(quantity);
                return;
            }
        }
        if (quantity > 0) draft.add(new OrderItem(item, quantity));
        else console.showMessage("Item was not in the draft, so nothing was removed.");
    }

    private void addToDraft(List<OrderItem> draft, MenuItem item, int quantity) {
        for (OrderItem current : draft) {
            if (current.getMenuItem().getItemId().equalsIgnoreCase(item.getItemId())) {
                current.setQuantity(current.getQuantity() + quantity);
                return;
            }
        }
        draft.add(new OrderItem(item, quantity));
    }

    // Function 2: add to an existing PENDING order; quantity is added, never replaced.
    private void addItem() {
        if (!showOpenOrders()) return;
        Order order = readOrderById(true);
        if (order == null) return;
        console.showMessage("\nCurrent items in order " + order.getOrderId() + ":");
        console.showOrderBill(order, orderService.calculateTotal(order));
        while (true) {
            console.showMessage("\nAvailable menu items:");
            console.showAvailableTable(menuService.getAvailableItems());
            MenuItem item = readAvailableMenuItem();
            if (item == null) return;
            int oldQuantity = getQuantity(order, item.getItemId());
            int addQuantity = readPositiveQuantity("Quantity to add: ");
            if (addQuantity < 0) return;
            try {
                orderService.addItem(order.getOrderId(), item, addQuantity);
                int newQuantity = oldQuantity + addQuantity;
                console.showMessage("Added " + addQuantity + " of " + item.getItemName()
                        + ". Quantity changed from " + oldQuantity + " to " + newQuantity + ".");
                order = orderService.findById(order.getOrderId());
                console.showOrderBill(order, orderService.calculateTotal(order));
            } catch (BusinessException e) {
                console.showError(e.getMessage());
                return;
            }
            if (!readYesNo("Add another item to this order? (Y/N): ")) return;
        }
    }

    private int getQuantity(Order order, String itemId) {
        for (OrderItem item : order.getItemsList()) {
            if (item.getMenuItem().getItemId().equalsIgnoreCase(itemId)) return item.getQuantity();
        }
        return 0;
    }

    // Function 3 is retained for the teammate's existing quantity-change implementation.
    private void changeQuantity() {
        if (!showOpenOrders()) return;
        Order order = readOrderById(true);
        if (order == null) return;
        String itemId = readItemIdInOrder(order);
        if (itemId == null) return;
        int quantity = readNonNegativeQuantity("New quantity (0 removes the item): ");
        if (quantity < 0) return;
        try {
            orderService.updateQuantity(order.getOrderId(), itemId, quantity);
            console.showMessage(quantity == 0 ? "Item removed from order " + order.getOrderId() + "."
                    : "Item quantity updated to " + quantity + ".");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    // Function 4: show the bill and confirm before changing status.
    private void manageOrderStatus() {
        if (!showOpenOrders()) return;
        Order order = readOrderForStatusChange();
        if (order == null) return;
        console.showMessage("\nOrder details before changing status:");
        console.showOrderBill(order, orderService.calculateTotal(order));
        console.showMenu("Manage Order Status", STATUS_OPTIONS, "Back");
        int choice = readChoiceRange("Status selection", 0, STATUS_OPTIONS.length);
        if (choice == -1 || choice == 0) {
            console.showMessage("No changes made.");
            return;
        }
        OrderStatus newStatus;
        if (choice == 1) {
            newStatus = OrderStatus.PREPARING;
        } else if (choice == 2) {
            newStatus = OrderStatus.COMPLETED;
        } else {
            newStatus = OrderStatus.CANCELLED;
        }
        if (!readYesNo("Confirm changing order " + order.getOrderId() + " to " + newStatus + "? (Y/N): ")) {
            console.showMessage("Status unchanged. Order remains " + order.getStatus() + ".");
            return;
        }
        try {
            orderService.changeStatus(order.getOrderId(), newStatus);
            console.showMessage("Order " + order.getOrderId() + " status changed to " + newStatus + ".");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        }
    }

    private Order readOrderForStatusChange() {
        int attempts = 0;
        while (true) {
            String id = readLine("Order ID: ");
            Order order = orderService.findById(id);
            String error = null;
            if (order == null) {
                error = "Order ID '" + id + "' does not exist. Check the ID and try again.";
            } else if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
                error = "Order " + id + " is already " + order.getStatus() + " and cannot be modified.";
            }
            if (error == null) return order;
            console.showError(error);
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("Order ID")) return null;
                attempts = 0;
            }
        }
    }

    private Order readOrderById(boolean mustBePending) {
        int attempts = 0;
        while (true) {
            String id = readLine("Order ID: ");
            Order order = orderService.findById(id);
            String error = null;
            if (order == null) error = "Order ID '" + id + "' does not exist. Check the ID and try again.";
            else if (mustBePending && order.getStatus() != OrderStatus.PENDING)
                error = "Order " + id + " is " + order.getStatus() + " and cannot be modified. Only PENDING orders can be changed.";
            if (error == null) return order;
            console.showError(error);
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("Order ID")) return null;
                attempts = 0;
            }
        }
    }

    private String readItemIdInOrder(Order order) {
        int attempts = 0;
        while (true) {
            String id = readLine("Menu item ID in this order: ");
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

    private MenuItem readAvailableMenuItem() {
        int attempts = 0;
        while (true) {
            String id = readLine("Menu item ID: ");
            MenuItem item = menuService.findById(id);
            String error = null;
            if (item == null) error = "Menu item ID '" + id + "' does not exist. Check the menu and try again.";
            else if (!item.isStatus()) error = "Menu item '" + id + "' is currently unavailable. Choose an available item.";
            if (error == null) return item;
            console.showError(error);
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("menu item ID")) return null;
                attempts = 0;
            }
        }
    }

    private int readPositiveQuantity(String prompt) {
        int attempts = 0;
        while (true) {
            String raw = readLine(prompt);
            try {
                int quantity = Integer.parseInt(raw);
                if (quantity > 0) return quantity;
                console.showError("Quantity must be a positive whole number greater than 0.");
            } catch (NumberFormatException e) {
                console.showError("'" + raw + "' is not a whole number. Enter a quantity such as 1, 2, or 3.");
            }
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("quantity")) return -1;
                attempts = 0;
            }
        }
    }

    private int readNonNegativeQuantity(String prompt) {
        int attempts = 0;
        while (true) {
            String raw = readLine(prompt);
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

    private String readTable() {
        int attempts = 0;
        while (true) {
            String raw = readLine("Table: ");
            String table = Validator.normalizeTable(raw);
            if (table != null) {
                if (!table.equals(raw)) console.showMessage("Table set to " + table + ".");
                return table;
            }
            console.showError("Table '" + raw + "' is invalid. Use one letter followed by a number, for example A1 or B12.");
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("table number")) return null;
                attempts = 0;
            }
        }
    }

    private String readCustomerName() {
        int attempts = 0;
        while (true) {
            String value = readLine("Customer name: ");
            String error = Validator.getCustomerNameError(value);
            if (error == null) return value;
            console.showError(error);
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("customer name")) return null;
                attempts = 0;
            }
        }
    }

    private String readCustomerPhone() {
        int attempts = 0;
        while (true) {
            String value = readLine("Customer phone (starts with 0 or 84): ");
            String error = Validator.getPhoneError(value);
            if (error == null) return value;
            console.showError(error);
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid("customer phone")) return null;
                attempts = 0;
            }
        }
    }

    private boolean continueAfterInvalid(String field) {
        while (true) {
            String answer = readLine("You entered " + field + " incorrectly 3 times. Continue entering it? (Y/N): ");
            if (answer.equalsIgnoreCase("Y")) return true;
            if (answer.equalsIgnoreCase("N")) {
                console.showMessage("Current operation cancelled. No further changes were made.");
                return false;
            }
            console.showError("Please enter Y to retry " + field + " or N to cancel this operation.");
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt);
            if (answer.equalsIgnoreCase("Y")) return true;
            if (answer.equalsIgnoreCase("N")) return false;
            console.showError("Please enter Y or N.");
        }
    }

    private int readMenuChoice(String field) {
        return readChoiceRange(field, 0, ORDER_OPTIONS.length);
    }

    private int readChoiceRange(String field, int min, int max) {
        int attempts = 0;
        while (true) {
            String raw = readLine("Choose: ");
            try {
                int choice = Integer.parseInt(raw);
                if (choice >= min && choice <= max) return choice;
                console.showError("Choice '" + raw + "' is outside the allowed range " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                console.showError("'" + raw + "' is not a whole-number menu choice.");
            }
            attempts++;
            if (attempts >= 3) {
                if (!continueAfterInvalid(field)) return -1;
                attempts = 0;
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private boolean showOpenOrders() {
        List<Order> orders = orderService.getOpenOrders();
        if (orders.isEmpty()) {
            console.showMessage("No active (PENDING or PREPARING) orders are available.");
            return false;
        }
        console.showMessage("\n----- Active orders (PENDING / PREPARING) -----");
        for (Order order : orders) console.showOrderSummary(order, orderService.calculateTotal(order));
        return true;
    }

    private void searchOrder() {
        while (true) {
            console.showMenu("Search Order", SEARCH_OPTIONS, "Back");
            int choice = readChoiceRange("search menu", 0, 2);
            if (choice == -1 || choice == 0) return;
            if (choice == 1) searchByTable();
            else searchByOrderId();
        }
    }

    private void searchByTable() {
        String keyword = InputHandler.readNonBlank(scanner, "Enter Table to search: ");
        List<Order> found = orderService.searchByTable(keyword);
        if (found.isEmpty()) {
            console.showMessage("No orders found for table " + keyword + ".");
            return;
        }
        console.showMessage("\n----- Orders of table " + keyword + " -----");
        for (Order order : found) console.showOrderSummary(order, orderService.calculateTotal(order));
    }

    private void searchByOrderId() {
        String id = readLine("Order ID to search: ");
        Order order = orderService.findById(id);
        if (order == null) console.showError("Order ID '" + id + "' does not exist.");
        else console.showOrderBill(order, orderService.calculateTotal(order));
    }

    private void sortedOrders() {
        List<Order> sorted = orderService.sortedByTotalDesc();
        if (sorted.isEmpty()) {
            console.showMessage("No orders available.");
            return;
        }
        console.showMessage("\n----- Orders sorted by total (high to low) -----");
        for (Order order : sorted) console.showOrderSummary(order, orderService.calculateTotal(order));
    }
}
