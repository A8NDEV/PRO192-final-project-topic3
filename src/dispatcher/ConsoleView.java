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
        int idW = "ID".length();
        int nameW = "Name".length();
        int catW = "Category".length();
        int priceW = 12; // đủ cho tiêu đề "Price (VND)"

        for (MenuItem i : items) {
            idW = Math.max(idW, i.getItemId().length());
            nameW = Math.max(nameW, i.getItemName().length());
            catW = Math.max(catW, i.getCategory().length());
        }

        String fmt = "%-" + idW + "s  %-" + nameW + "s  %-" + catW + "s  %" + priceW + "s%n";
        int total = idW + nameW + catW + priceW + 6; // 6 = 3 khoảng cách x 2 dấu cách

        System.out.printf(fmt, "ID", "Name", "Category", "Price (VND)");
        System.out.println(dashes(total));
        for (MenuItem i : items) {
            System.out.printf(fmt, i.getItemId(), i.getItemName(), i.getCategory(),
                    String.format("%,.0f", i.getPrice()));
        }
    }

    public void showItemTable(List<MenuItem> items) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }

        int idW = "ID".length();
        int nameW = "Name".length();
        int catW = "Category".length();
        int priceW = 12; // "Price (VND)"
        int statusW = "Status".length();

        for (MenuItem i : items) {
            if (i != null) {
                if (i.getItemId() != null) idW = Math.max(idW, i.getItemId().length());
                if (i.getItemName() != null) nameW = Math.max(nameW, i.getItemName().length());
                if (i.getCategory() != null) catW = Math.max(catW, i.getCategory().length());
                statusW = Math.max(statusW, (i.isStatus() ? "Available" : "Unavailable").length());
            }
        }

        String fmt = "%-" + idW + "s  %-" + nameW + "s  %-" + catW + "s  %" + priceW + "s  %-" + statusW + "s%n";
        int total = idW + nameW + catW + priceW + statusW + 8; // 4 khoảng cách x 2 dấu cách

        System.out.printf(fmt, "ID", "Name", "Category", "Price (VND)", "Status");
        System.out.println(dashes(total));
        for (MenuItem i : items) {
            if (i != null) {
                System.out.printf(fmt, i.getItemId(), i.getItemName(), i.getCategory(),
                        String.format("%,.0f", i.getPrice()), (i.isStatus() ? "Available" : "Unavailable"));
            }
        }
    }

    private String dashes(int n) {
        return new String(new char[n]).replace('\0', '-');
    }
    
    // ===== Hiển thị tóm tắt đơn hàng Order =====
    public void showOrderSummary(Order order, double total) {
        System.out.println(String.format("%-6s | %-10s | Table %-5s | %-25s | %2d item(s) | %,.0f",
                order.getOrderId(), order.getOrderDate(), order.getTableNumber(),
                order.getCustomerName(), order.getItemsList().size(), total));
    }
}
