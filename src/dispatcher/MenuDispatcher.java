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

    private final OutputHandler console = new OutputHandler();
    private final Scanner scanner = InputHandler.SCANNER;

    public MenuDispatcher() {
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
            if (MenuManager.findById(id) != null) {
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
            MenuManager.addItem(newItem);
            MenuManager.save();
            console.showMessage("Item added successfully!");
        } catch (IllegalArgumentException e) {
            console.showError(e.getMessage());
        } catch (IOException e) {
            console.showError("Error saving file: " + e.getMessage());
        }
    }

    private void updateItem() {
        String id = InputHandler.readNonBlank(scanner, "Enter item ID to update: ").toUpperCase();
        MenuItem currentItem = MenuManager.findById(id);

        if (currentItem == null) {
            console.showError("Item ID '" + id + "' not found!");
            return;
        }

        console.showMessage("\n--- Current Item Details ---");
        console.showItem(currentItem);

        String newName = Validator.normalizeName(InputHandler.readNonBlank(scanner, "Enter new item Name: "));
        String newCategory = Validator.normalizeName(InputHandler.readNonBlank(scanner, "Enter new Category: "));
        double newPrice = InputHandler.readValidPrice(scanner, "Enter new Price: ");
        boolean newStatus = InputHandler.readBoolean(scanner, "Show/Hide? (true/false): ");

        if (!InputHandler.readConfirmed(scanner, "Confirm update this item? (y/n): ")) {
            console.showMessage("Update cancelled. No changes made.");
            return;
        }

        MenuItem updatedItem = new MenuItem(currentItem.getItemId(), newName, newCategory, newPrice, newStatus);

        try {
            MenuManager.updateItem(updatedItem);
            MenuManager.save();
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
                MenuItem item = MenuManager.findById(id);
                if (item != null) {
                    console.showItem(item);
                } else {
                    console.showMessage("Item not found.");
                }
                break;
            case 2:
                String name = InputHandler.readNonBlank(scanner, "Enter Item Name to search: ");
                List<MenuItem> list = MenuManager.findByName(name);
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
        if (MenuManager.getMenuList().isEmpty()) {
            console.showMessage("No items available.");
            return;
        }
        List<MenuItem> available = MenuManager.getAvailableItems();
        if (available.isEmpty()) {
            console.showMessage("No available items.");
            return;
        }
        console.showAvailableHeader();
        console.showAvailableTable(available);
    }

    private void displayItemsNoOrder() {
        List<MenuItem> unordered = OrderManager.getUnorderedItems();
        if (unordered.isEmpty()) {
            console.showMessage("All items have orders.");
            return;
        }
        console.showMessage("\n===== Items with no orders =====");
        console.showItemTable(unordered);
    }
}
