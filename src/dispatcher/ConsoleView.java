package dispatcher;

import models.MenuItem;
import models.Order;

/**
 *
 * @author ASUS
 */
/**
 * Lớp hiển thị Console dùng chung cho toàn bộ ứng dụng (Menu, Order, Thông báo).
 * Nội dung thông báo do nơi gọi truyền vào, view chỉ đảm nhiệm in ra màn hình.
 */
public class ConsoleView {

    // ===== Các hàm in thông báo và menu chung =====
    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }

    // Menu dạng danh sách: đánh số 1..n theo thứ tự options, 0 là lựa chọn thoát/quay lại
    public void showMenu(String title, String[] options, String zeroLabel) {
        System.out.println("\n===== " + title + " =====");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println("0. " + zeroLabel);
        System.out.print("Choose: ");
    }

    // ===== Các hàm hiển thị cho Menu =====
    public void showMainMenu() {
        System.out.println("\n===== Menu Management =====");
        System.out.println("1. Add new Item");
        System.out.println("2. Search Item");
        System.out.println("3. Order management");
        System.out.println("4. Display available items");
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

    public void showSaveError(String message) {
        System.out.println("Error saving file: " + message);
    }

    public void showItem(MenuItem item) {
        System.out.println("- " + item);
    }

    public void showItemNotFound() {
        System.out.println("Item not found.");
    }

    public void showNoItemsFound() {
        System.out.println("Item not found");
    }

    public void showAvailableHeader() {
        System.out.println("\n===== Available Menu Items =====");
    }

    // ===== Các hàm hiển thị cho Order =====
    public void showOrderSummary(Order order, double total) {
        System.out.println(String.format("%-6s | %-10s | Table %-5s | %-25s | %2d item(s) | %,.0f",
                order.getOrderId(), order.getOrderDate(), order.getTableNumber(),
                order.getCustomerName(), order.getItemsList().size(), total));
    }
}
