/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import exception.AppException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import models.MenuItem;
import service.MenuService;
import service.OrderService;
import utils.InputHandler;
import view.MenuView;

/**
 *
 * @author ASUS
 */
public class MenuController {

    private MenuService menuService;
    private OrderService orderService = new OrderService();
    private MenuView view = new MenuView();
    private Scanner scanner;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        int choice = -1;
        do {
            view.showMainMenu();

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                view.showNotInteger();
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
                    new OrderController(orderService, menuService.getMenuList(), scanner).run();
                    break;
                case 4:
                    displayAvailable();
                    break;
                case 0:
                    break;
                default:
                    view.showInvalidChoice();
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
            view.showAddSuccess();
        } catch (AppException e) {
            view.showError(e.getMessage());
        } catch (IOException e) {
            view.showSaveError(e.getMessage());
        }
    }

    private void searchItem() {
        view.showSearchMenu();
        int choice = -1;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            view.showNotInteger();
            return;
        }
        switch (choice) {
            case 1:
                String id = InputHandler.readNonBlank(scanner, "Enter Item ID to search: ");
                MenuItem item = menuService.findById(id);
                if (item != null) {
                    view.showItem(item);
                } else {
                    view.showItemNotFound();
                }
                break;
            case 2:
                String name = InputHandler.readNonBlank(scanner, "Enter Item Name to search: ");
                List<MenuItem> list = menuService.findByName(name);
                if (list.isEmpty()) {
                    view.showNoItemsFound();
                } else {
                    for (MenuItem i : list) {
                        view.showItem(i);
                    }
                }
                break;

            case 0:
                break;
            default:
                view.showInvalidOption();
                break;
        }
    }

    private void displayAvailable() {
        if(menuService.getMenuList().isEmpty()) {
            view.showMenuEmpty();
            return ;
        }
        List<MenuItem> available = menuService.getAvailableItems();
        if(available.isEmpty()) {
            view.showNoAvailableItems();
            return;
        }
                
        view.showAvailableHeader();
        for (MenuItem item : available) {
            view.showItem(item);
        }
    }
}
