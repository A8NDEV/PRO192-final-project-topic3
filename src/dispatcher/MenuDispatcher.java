/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dispatcher;

import business.MenuManager;
import business.OrderManager;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import utils.InputHandler;
import utils.OutputHandler;
import utils.Validator;

/**
 *
 * @author ASUS
 */
public class MenuDispatcher {

    private static final String[] MENU_OPTIONS = {
        "Add new Item",
        "Update Item By ID",
        "Search Item",
        "Display available items",
        "Display all items no order"
    };
    private static final String[] SEARCH_OPTIONS = {"Search by ID", "Search by Name"};

    private final MenuManager menuManager;
    private final OutputHandler console = new OutputHandler();
    private final Scanner scanner;
    private final OrderManager orderManager;

    public MenuDispatcher(MenuManager menuManager, Scanner scanner, OrderManager orderManager) {
        this.menuManager = menuManager;
        this.scanner = scanner;
        this.orderManager = orderManager;
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
                    displayAvailable();
                    break;
                case 5:
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
            id = InputHandler.readNonBlank(scanner, "Enter item ID: ").toUpperCase();
            if (menuManager.findById(id) != null) {
                console.showError("Item ID '" + id + "' already exists! Please enter a different ID.");
                continue;
            }
            break;
        }

        String name = Validator.normalizeName(InputHandler.readNonBlank(scanner, "Enter item Name: "));
        String category = Validator.normalizeName(InputHandler.readNonBlank(scanner, "Enter Category: "));
        double price = InputHandler.readValidPrice(scanner, "Enter Price: ");
        boolean status = InputHandler.readBoolean(scanner, "Show/Hide? (true/false): ");

        MenuItem newItem = new MenuItem(id, name, category, price, status);

        try {
            menuManager.addItem(newItem);
            menuManager.save();
            console.showMessage("Item added successfully!");
        } catch (IllegalArgumentException e) {
            console.showError(e.getMessage());
        } catch (IOException e) {
            console.showError("Error saving file: " + e.getMessage());
        }
    }

    private void updateItem() {
        String id = InputHandler.readNonBlank(scanner, "Enter item ID to update: ").toUpperCase();
        MenuItem currentItem = menuManager.findById(id);

        if (currentItem == null) {
            console.showError("Item ID '" + id + "' not found!");
            return;
        }

        console.showMessage("\n--- Current Item Details ---");
        console.showItem(currentItem);
        console.showMessage("Press [Enter] to keep current value.\n");

        String newName = Validator.normalizeName(InputHandler.readOptionalNonBlank(scanner,
                "Enter new Name [" + currentItem.getItemName() + "]: ", currentItem.getItemName()));

        String newCategory = Validator.normalizeName(InputHandler.readOptionalNonBlank(scanner,
                "Enter new Category [" + currentItem.getCategory() + "]: ", currentItem.getCategory()));

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
            menuManager.updateItem(updatedItem);
            menuManager.save();
            console.showMessage("Item updated successfully!");
        } catch (IllegalArgumentException e) {
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
                String id = InputHandler.readNonBlank(scanner, "Enter Item ID to search: ").toUpperCase();
                MenuItem item = menuManager.findById(id);
                if (item != null) {
                    console.showItem(item);
                } else {
                    console.showMessage("Item not found.");
                }
                break;
            case 2:
                String name = InputHandler.readNonBlank(scanner, "Enter Item Name to search: ");
                List<MenuItem> list = menuManager.findByName(name);
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

    private void displayAvailable() {
        if (menuManager.getMenuList().isEmpty()) {
            console.showMessage("No items available");
        }
        List<MenuItem> available = menuManager.getAvailableItems();
        if (available.isEmpty()) {
            console.showMessage("No available items.");
            return;
        }
        console.showAvailableHeader();
        console.showAvailableTable(available);
    }

    private void displayItemsNoOrder() {
        if (orderManager == null) {
            console.showError("Order service is not configured.");
            return;
        }

        int count = 0;
        List<MenuItem> menuList = menuManager.getMenuList();
        List<Order> orderList = orderManager.getAllOrders();

        for (MenuItem item : menuList) {
            boolean found = false;
            for (Order order : orderList) {
                for (OrderItem orderItem : order.getItemsList()) {
                    if (item.getItemId().equalsIgnoreCase(orderItem.getMenuItem().getItemId())) {
                        found = true;
                        break;
                    }
                    if (found == true) {
                        continue;
                    } else {
                        console.showMessage("\n===== Items with no orders =====");
                        console.showItem(item);
                        count++;
                    }
                }
            }
        }
        if (count == 0) {
            console.showMessage("All items have orders.");
        }
    }
}
