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

    // ===== In thông báo chung =====
    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }

    // ===== In menu danh sách lựa chọn động (1..n, 0 là thoát/quay lại) =====
    public void showMenu(String title, String[] options, String zeroLabel) {
        System.out.println("\n===== " + title + " =====");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println("0. " + zeroLabel);
        System.out.print("Choose: ");
    }

    // ===== Hiển thị món ăn trong Menu =====
    public void showAvailableHeader() {
        System.out.println("\n===== Available Menu Items =====");
    }

    public void showItem(MenuItem item) {
        System.out.println("- " + item);
    }

    // ===== Hiển thị tóm tắt đơn hàng Order =====
    public void showOrderSummary(Order order, double total) {
        System.out.println(String.format("%-6s | %-10s | Table %-5s | %-25s | %2d item(s) | %,.0f",
                order.getOrderId(), order.getOrderDate(), order.getTableNumber(),
                order.getCustomerName(), order.getItemsList().size(), total));
    }
}
