/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import exception.BusinessException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import models.MenuItem;
import service.MenuService;
import utils.InputHandler;
import view.ConsoleView;
import view.MenuView;
import service.OrderService;

/**
 *
 * @author ASUS
 */
public class MenuController {

    private static final String[] MENU_OPTIONS = {"Add new Item", "Search Item", "Delete Item By ID", "Display all items no order"};
    private static final String[] SEARCH_OPTIONS = {"Search by ID", "Search by Name"};

    private MenuService menuService;
    private ConsoleView console = new ConsoleView();
    private MenuView menuView = new MenuView();
    private Scanner scanner;
    private OrderService orderService;

    public MenuController(MenuService menuService, Scanner scanner, OrderService orderService) {
        this.menuService = menuService;
        this.scanner = scanner;
        this.orderService = orderService;
    }

    

    public void run() {
        int choice = -1;
        do {
            console.showMenu("Menu Management", MENU_OPTIONS, "Back");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                console.showMessage("Value must be an integer.");
                continue;
            }

            switch (choice) {
                case 1:
                    addItem();
                    break;
                case 2:
                    searchItem();
                    break;
                case 3:
                    deleteItem();
                    break;
                case 4:
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
        String id = InputHandler.readNonBlank(scanner, "Enter item ID: ");
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
            console.showMessage("Error saving file: " + e.getMessage());
        }
    }

    private void searchItem() {
        console.showMenu("Search Item Menu", SEARCH_OPTIONS, "Exit");
        int choice = -1;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            console.showMessage("Value must be an integer.");
            return;
        }
        switch (choice) {
            case 1:
                String id = InputHandler.readNonBlank(scanner, "Enter Item ID to search: ");
                MenuItem item = menuService.findById(id);
                if (item != null) {
                    menuView.showItem(item);
                } else {
                    console.showMessage("Item not found.");
                }
                break;
            case 2:
                String name = InputHandler.readNonBlank(scanner, "Enter Item Name to search: ");
                List<MenuItem> list = menuService.findByName(name);
                if (list.isEmpty()) {
                    console.showMessage("Item not found");
                } else {
                    for (MenuItem i : list) {
                        menuView.showItem(i);
                    }
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
            console.showError("Item not found!");
            return;
        }
        try {
            String Confirmed = InputHandler.readConfirmed(scanner, "Confirm Yes OR NO (y/n): ");
            menuService.deleteItem(id);
            menuService.save();
            console.showMessage("Item deleted successfully!");
        } catch (BusinessException e) {
            console.showError(e.getMessage());
        } catch (IOException e) {
            console.showError("Error saving file: " + e.getMessage());
        }
    }

    private void displayItemsNoOrder() {
        List<MenuItem> items = menuService.getUnorderedItems(orderService.getAllOrders());
        if (items.isEmpty()) {
            System.out.println("All items have been ordered.");
            return;
        }
        System.out.println("---Items no orders---");
        for (MenuItem item : items) {
            System.out.println(item);
        }
    }
}
