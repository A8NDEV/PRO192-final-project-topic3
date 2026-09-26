/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dispatcher;

import business.MenuService;
import java.io.IOException;
import java.util.Scanner;
import models.MenuItem;
import utils.CustomException;
import utils.FileHandler;
import utils.InputHandler;

/**
 *
 * @author ASUS
 */
public class MenuDispatcher {

    private MenuService menuService;
    private String filePath;
    private Scanner scanner;

    public MenuDispatcher(MenuService menuService, String filePath) {
        this.menuService = menuService;
        this.filePath = filePath;
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        int choice = -1;
        do {
            System.out.println("\n===== Menu Management =====");
            System.out.println("1. Add new Item");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Value must be an integer.");
                continue;
            }

            switch (choice) {
                case 1:
                    addItem();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid choice.");
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
            FileHandler.saveMenuToFile(menuService.getMenuList(), filePath);
            System.out.println("Item added successfully!");
        } catch (CustomException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
}
