package dispatcher;

import java.util.List;
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

    public void showAvailableTable(List<MenuItem> items) {
        showItemTable(items);
    }

    public void showItemTable(List<MenuItem> items) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }

        System.out.printf("%-6s | %-25s | %-15s | %12s | %-12s%n", "ID", "Name", "Category", "Price (VND)", "Status");
        System.out.println("---------------------------------------------------------------------------------");
        for (MenuItem i : items) {
            if (i != null) {
                System.out.printf("%-6s | %-25s | %-15s | %,12.0f | %-12s%n",
                        i.getItemId(), i.getItemName(), i.getCategory(), i.getPrice(), (i.isStatus() ? "Available" : "Unavailable"));
            }
        }
    }

    // ===== Hiển thị tóm tắt đơn hàng Order =====
    public void showOrderSummary(Order order, double total) {
        System.out.println(String.format("%-6s | %-10s | Table %-5s | %-25s | %2d item(s) | %,.0f",
                order.getOrderId(), order.getOrderDate(), order.getTableNumber(),
                order.getCustomerName(), order.getItemsList().size(), total));
    }

    // Hien thi hoa don tam truoc khi luu order.
    public void showDraftBill(String table, String customerName, String phone, List<models.OrderItem> items) {
        System.out.println("\n================ ORDER BILL PREVIEW ================");
        System.out.println("Table         : " + table);
        System.out.println("Customer      : " + customerName);
        System.out.println("Phone         : " + phone);
        showDraftItems(items);
        double total = 0;
        for (models.OrderItem item : items) {
            total += item.getMenuItem().getPrice() * item.getQuantity();
        }
        System.out.printf("Grand total   : %,.0f VND%n", total);
        System.out.println("====================================================");
    }

    // Hien thi cac mon trong hoa don tam, dung khi xem bill va chinh sua.
    public void showDraftItems(List<models.OrderItem> items) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items have been added.");
            return;
        }
        System.out.printf("%-8s %-24s %8s %14s %14s%n", "Item ID", "Item name", "Quantity", "Unit price", "Line total");
        System.out.println("--------------------------------------------------------------------------");
        for (models.OrderItem item : items) {
            double price = item.getMenuItem().getPrice();
            double lineTotal = price * item.getQuantity();
            System.out.printf("%-8s %-24s %8d %14s %14s%n",
                    item.getMenuItem().getItemId(), item.getMenuItem().getItemName(), item.getQuantity(),
                    String.format("%,.0f", price), String.format("%,.0f", lineTotal));
        }
    }

    // Hien thi hoa don day du cua order da ton tai.
    public void showOrderBill(Order order, double total) {
        System.out.println("\n==================== ORDER BILL ====================");
        System.out.println("Order ID      : " + order.getOrderId());
        System.out.println("Date          : " + order.getOrderDate());
        System.out.println("Table         : " + order.getTableNumber());
        System.out.println("Customer      : " + order.getCustomerName());
        System.out.println("Phone         : " + order.getCustomerPhone());
        System.out.println("Status        : " + order.getStatus());
        showDraftItems(order.getItemsList());
        System.out.printf("Grand total   : %,.0f VND%n", total);
        System.out.println("====================================================");
    }
}
