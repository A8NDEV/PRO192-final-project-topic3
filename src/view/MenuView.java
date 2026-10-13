package view;

import models.MenuItem;

/**
 * Hiển thị phần Menu ra console. Chỉ in, không đọc input và không xử lý logic.
 */
public class MenuView {

    public void showMainMenu() {
        System.out.println("\n===== Menu Management =====");
        System.out.println("1. Add new Item");
        System.out.println("2. Search Item");
        System.out.println("3. Order management");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    public void showSearchMenu() {
        System.out.println("\n===== Search Item Menu =====");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Name");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    public void showNotInteger() {
        System.out.println("Value must be an integer.");
    }

    public void showInvalidChoice() {
        System.out.println("Invalid choice.");
    }

    public void showInvalidOption() {
        System.out.println("Invalid option.");
    }

    public void showAddSuccess() {
        System.out.println("Item added successfully!");
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }

    public void showSaveError(String message) {
        System.out.println("Error saving file: " + message);
    }

    public void showItem(MenuItem item) {
        System.out.println("- " + item);
    }

    // Tìm theo ID không thấy
    public void showItemNotFound() {
        System.out.println("Item not found.");
    }

    // Tìm theo tên không thấy
    public void showNoItemsFound() {
        System.out.println("Item not found");
    }
}
