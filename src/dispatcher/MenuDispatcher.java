package dispatcher;

import business.BusinessException;
import business.MenuService;
import business.OrderService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import models.MenuItem;
import models.Order;
import utils.InputHandler;

/**
 *
 * @author ASUS
 */
public class MenuDispatcher {

    private static final String[] MENU_OPTIONS = {
        "Add new Item",
        "Update Item By ID",
        "Search Item",
        "Delete Item By ID",
        "Display available items",
        "Display all items no order"
    };
    private static final String[] SEARCH_OPTIONS = {"Search by ID", "Search by Name"};

    private final MenuService menuService;
    private final ConsoleView console = new ConsoleView();
    private final Scanner scanner;
    private final OrderService orderService;

    public MenuDispatcher(MenuService menuService, Scanner scanner, OrderService orderService) {
        this.menuService = menuService;
        this.scanner = scanner;
        this.orderService = orderService;
    }

    public void run() {
        int choice = -1;
        do {
            console.showMenu("Menu Management", MENU_OPTIONS, "Back");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                console.showMessage("Value must be an integer.");
                continue;
            }

            switch (choice) {
                case 1:
                    addItem();
                    break;
                case 2:
                    updateItem();
                    break;
                case 3:
                    searchItem();
                    break;
                case 4:
                    deleteItem();
                    break;
                case 5:
                    displayAvailable();
                    break;
                case 6:
                    displayItemsNoOrder();
                    break;
                case 0:
                    break;
                default:
                    console.showMessage("Invalid choice.");
                    break;
            }
        } while (choice != 0);
    }

    private void addItem() {
        String id;
        while (true) {
            id = InputHandler.readNonBlank(scanner, "Enter item ID: ");
            if (menuService.findById(id) != null) {
                console.showError("Item ID '" + id + "' already exists! Please enter a different ID.");
                continue;
            }
            break;
        }

        String name = InputHandler.readNonBlank(scanner, "Enter item Name: ");
        String category = InputHandler.readNonBlank(scanner, "Enter Category: ");
        double price = InputHandler.readValidPrice(scanner, "Enter Price: ");
        boolean status = InputHandler.readBoolean(scanner, "Show/Hide? (true/false): ");

        MenuItem newItem = new MenuItem(id, name, category, price, status);

        try {
            menuService.addItem(newItem);
            menuService.save();
            console.showMessage("Item added successfully!");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        } catch (IOException e) {
            console.showError("Error saving file: " + e.getMessage());
        }
    }

    private void updateItem() {
        String id = InputHandler.readNonBlank(scanner, "Enter item ID to update: ");
        MenuItem currentItem = menuService.findById(id);

        if (currentItem == null) {
            console.showError("Item ID '" + id + "' not found!");
            return;
        }

        console.showMessage("\n--- Current Item Details ---");
        console.showItem(currentItem);
        console.showMessage("Press [Enter] to keep current value.\n");

        String newName = InputHandler.readOptionalNonBlank(scanner,
                "Enter new Name [" + currentItem.getItemName() + "]: ", currentItem.getItemName());

        String newCategory = InputHandler.readOptionalNonBlank(scanner,
                "Enter new Category [" + currentItem.getCategory() + "]: ", currentItem.getCategory());

        double newPrice = InputHandler.readOptionalPrice(scanner,
                "Enter new Price [" + String.format("%,.0f", currentItem.getPrice()) + "]: ", currentItem.getPrice());

        boolean newStatus = InputHandler.readOptionalBoolean(scanner,
                "Show/Hide? (true/false) [" + currentItem.isStatus() + "]: ", currentItem.isStatus());

        if (!InputHandler.readConfirmed(scanner, "Confirm update this item? (y/n): ")) {
            console.showMessage("Update cancelled. No changes made.");
            return;
        }

        MenuItem updatedItem = new MenuItem(currentItem.getItemId(), newName, newCategory, newPrice, newStatus);

        try {
            menuService.updateItem(updatedItem);
            menuService.save();
            console.showMessage("Item updated successfully!");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        } catch (IOException e) {
            console.showError("Error saving file: " + e.getMessage());
        }
    }

    private void searchItem() {
        console.showMenu("Search Item Menu", SEARCH_OPTIONS, "Exit");
        int choice = -1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            console.showMessage("Value must be an integer.");
            return;
        }
        switch (choice) {
            case 1:
                String id = InputHandler.readNonBlank(scanner, "Enter Item ID to search: ");
                MenuItem item = menuService.findById(id);
                if (item != null) {
                    console.showItem(item);
                } else {
                    console.showMessage("Item not found.");
                }
                break;
            case 2:
                String name = InputHandler.readNonBlank(scanner, "Enter Item Name to search: ");
                List<MenuItem> list = menuService.findByName(name);
                if (list.isEmpty()) {
                    console.showMessage("Item not found.");
                } else {
                    console.showItemTable(list);
                }
                break;

            case 0:
                break;
            default:
                console.showMessage("Invalid option.");
                break;
        }
    }

    private void deleteItem() {
        String id = InputHandler.readNonBlank(scanner, "Enter Item ID: ");
        MenuItem item = menuService.findById(id);

        if (item == null) {
            console.showError("Item not found.");
            return;
        }

        console.showItem(item);

        List<Order> allOrders = (orderService != null) ? orderService.getAllOrders() : Collections.emptyList();

        if (menuService.isItemOrdered(id, allOrders)) {
            console.showError("Item has orders. Cannot delete.");
            return;
        }

        if (!InputHandler.readConfirmed(scanner, "Confirm delete (y/n): ")) {
            console.showMessage("Cancelled.");
            return;
        }

        try {
            menuService.deleteItem(id, allOrders);
            menuService.save();
            console.showMessage("Item deleted successfully!");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        } catch (IOException e) {
            console.showError("Error saving file: " + e.getMessage());
        }
    }

    private void displayAvailable() {
        if (menuService.getMenuList().isEmpty()) {
            console.showMessage("No items available.");
            return;
        }
        List<MenuItem> available = menuService.getAvailableItems();
        if (available.isEmpty()) {
            console.showMessage("No items are currently available.");
            return;
        }
        console.showAvailableHeader();
        console.showAvailableTable(available);
    }

    private void displayItemsNoOrder() {
        if (orderService == null) {
            console.showError("Order service is not configured.");
            return;
        }
        List<MenuItem> items = menuService.getUnorderedItems(orderService.getAllOrders());
        if (items.isEmpty()) {
            console.showMessage("All items have been ordered.");
            return;
        }
        console.showMessage("\n===== Items with no orders =====");
        console.showItemTable(items);
    }
}
