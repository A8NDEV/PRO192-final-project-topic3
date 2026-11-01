package business;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import models.OrderStatus;

/**
 * Quản lý đơn hàng: giữ danh sách order trong bộ nhớ, xử lý nghiệp vụ (tạo
 * order, thêm món, đổi số lượng, đổi trạng thái, tìm kiếm, sắp xếp) và đọc/ghi
 * file orders.txt theo bảng mã UTF-8. Cần MenuManager để tra món theo ID.
 */
public class OrderManager {

    private static final String SEP = "\\|";
    private static final int FIELD_COUNT = 7;

    private final Path filePath;
    private final MenuManager menuManager;
    private final List<Order> orders = new ArrayList<>();
    private int counter = 1;

    public OrderManager(String filePath, MenuManager menuManager) {
        this.filePath = Paths.get(filePath);
        this.menuManager = menuManager;
        ensureFileExists();
        load();
    }

    // ===== Nghiệp vụ =====

    public Order createOrder(String tableNumber, String customerName, String customerPhone,
                             List<OrderItem> items) {
        ensureNoPendingOrderForTable(tableNumber);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an order without any items.");
        }

        Order order = new Order(nextId(), today(), tableNumber, customerName, customerPhone);
        for (OrderItem item : items) {
            if (item == null || item.getMenuItem() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Every order item must have a valid menu item and a positive quantity.");
            }
            if (!item.getMenuItem().isStatus()) {
                throw new IllegalArgumentException("Item " + item.getMenuItem().getItemId()
                        + " is currently unavailable.");
            }
            order.getItemsList().add(new OrderItem(item.getMenuItem(), item.getQuantity()));
        }

        orders.add(order);
        persist();
        return order;
    }

    public void addItem(String orderId, MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item does not exist. Please check the menu item ID.");
        }
        if (!menuItem.isStatus()) {
            throw new IllegalArgumentException("Item " + menuItem.getItemId() + " is currently unavailable.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be a positive integer greater than 0.");
        }

        Order order = requireOrder(orderId);
        requirePending(order);

        OrderItem existing = findItem(order, menuItem.getItemId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            order.getItemsList().add(new OrderItem(menuItem, quantity));
        }
        persist();
    }

    public void updateQuantity(String orderId, String itemId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative. Enter 0 to remove the item.");
        }

        Order order = requireOrder(orderId);
        requirePending(order);

        OrderItem item = findItem(order, itemId);
        if (item == null) {
            throw new IllegalArgumentException("Menu item " + itemId + " is not in order " + orderId + ".");
        }

        if (newQuantity == 0) {
            order.getItemsList().remove(item);
        } else {
            item.setQuantity(newQuantity);
        }
        persist();
    }

    public void changeStatus(String orderId, OrderStatus newStatus) {
        if (newStatus != OrderStatus.PREPARING
                && newStatus != OrderStatus.COMPLETED
                && newStatus != OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order status can only be changed to PREPARING, COMPLETED, or CANCELLED.");
        }

        Order order = requireOrder(orderId);
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order " + order.getOrderId() + " is already "
                    + order.getStatus() + " and cannot be modified.");
        }
        if (order.getStatus() == newStatus) {
            throw new IllegalArgumentException("Order " + order.getOrderId() + " is already " + newStatus + ".");
        }
        order.setStatus(newStatus);
        persist();
    }

    public List<Order> getOpenOrders() {
        List<Order> open = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PREPARING) {
                open.add(order);
            }
        }
        return open;
    }

    public Order findOpenOrderByTable(String tableNumber) {
        if (tableNumber == null) {
            return null;
        }
        String table = tableNumber.trim();
        for (Order order : orders) {
            if ((order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PREPARING)
                    && order.getTableNumber() != null
                    && order.getTableNumber().equalsIgnoreCase(table)) {
                return order;
            }
        }
        return null;
    }

    public Order findById(String orderId) {
        if (orderId == null) {
            return null;
        }
        for (Order order : orders) {
            if (order.getOrderId().equalsIgnoreCase(orderId)) {
                return order;
            }
        }
        return null;
    }

    public List<Order> searchByTable(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lowerKey = keyword.trim().toLowerCase();
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getTableNumber() != null
                    && order.getTableNumber().toLowerCase().contains(lowerKey)) {
                result.add(order);
            }
        }
        return result;
    }

    public double calculateTotal(Order order) {
        double total = 0;
        for (OrderItem item : order.getItemsList()) {
            total += item.getMenuItem().getPrice() * item.getQuantity();
        }
        return total;
    }

    // Trả về bản sao của danh sách để bên ngoài không thêm/xóa trực tiếp được.
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    // Sắp xếp giảm dần theo tổng tiền (Comparator), không làm đổi thứ tự danh sách gốc.
    public List<Order> sortedByTotalDesc() {
        List<Order> sorted = new ArrayList<>(orders);
        sorted.sort((a, b) -> Double.compare(calculateTotal(b), calculateTotal(a)));
        return sorted;
    }

    // Các món trong menu chưa từng xuất hiện trong bất kỳ order nào.
    public List<MenuItem> getUnorderedItems() {
        Set<String> orderedIds = new HashSet<>();
        for (Order order : orders) {
            for (OrderItem oi : order.getItemsList()) {
                orderedIds.add(oi.getMenuItem().getItemId().toLowerCase());
            }
        }

        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menuManager.getMenuList()) {
            if (!orderedIds.contains(item.getItemId().toLowerCase())) {
                result.add(item);
            }
        }
        return result;
    }

    private String nextId() {
        return String.format("O%04d", counter++);
    }

    private String today() {
        LocalDate d = LocalDate.now();
        return String.format("%02d/%02d/%d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

    private OrderItem findItem(Order order, String itemId) {
        for (OrderItem item : order.getItemsList()) {
            if (item.getMenuItem().getItemId().equalsIgnoreCase(itemId)) {
                return item;
            }
        }
        return null;
    }

    private Order requireOrder(String orderId) {
        Order order = findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order ID '" + orderId + "' does not exist. Please check the ID and try again.");
        }
        return order;
    }

    private void requirePending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order " + order.getOrderId() + " is "
                    + order.getStatus() + " and cannot be modified. Only PENDING orders can be changed.");
        }
    }

    private void ensureNoPendingOrderForTable(String tableNumber) {
        Order existing = findOpenOrderByTable(tableNumber);
        if (existing != null) {
            throw new IllegalArgumentException("Table " + tableNumber + " already has pending order "
                    + existing.getOrderId() + ". Add items to that order instead.");
        }
    }

    // ===== Đọc/ghi file =====

    // Ghi file sau mỗi thay đổi; lỗi file được gói thành IllegalArgumentException để Dispatcher hiển thị.
    private void persist() {
        try {
            save();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot save orders: " + e.getMessage());
        }
    }

    private void save() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (Order order : orders) {
                writer.write(format(order));
                writer.newLine();
            }
        }
    }

    private void ensureFileExists() {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            System.out.println("Cannot create orders file: " + e.getMessage());
        }
    }

    private void load() {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                Order order = parse(line);
                if (order == null) {
                    System.out.println("Skipping invalid order line: " + line);
                    continue;
                }
                orders.add(order);
                updateCounter(order.getOrderId());
            }
        } catch (IOException e) {
            System.out.println("Cannot read orders file: " + e.getMessage());
        }
    }

    // Order -> 1 dòng text
    private String format(Order order) {
        StringBuilder items = new StringBuilder();
        for (OrderItem oi : order.getItemsList()) {
            if (items.length() > 0) {
                items.append(",");
            }
            items.append(oi.getMenuItem().getItemId()).append(":").append(oi.getQuantity());
        }
        return order.getOrderId() + "|" + order.getOrderDate() + "|" + order.getTableNumber()
                + "|" + order.getCustomerName() + "|" + order.getCustomerPhone()
                + "|" + order.getStatus().name() + "|" + items.toString();
    }

    // 1 dòng text -> Order (trả về null nếu dòng sai định dạng)
    private Order parse(String line) {
        try {
            String[] p = line.split(SEP, -1);
            if (p.length != FIELD_COUNT) {
                return null;
            }
            Order order = new Order(p[0], p[1], p[2], p[3], p[4]);
            order.setStatus(OrderStatus.valueOf(p[5]));
            if (!p[6].isEmpty()) {
                for (String entry : p[6].split(",")) {
                    String[] kv = entry.split(":");
                    MenuItem menuItem = menuManager.findById(kv[0]);
                    if (menuItem == null) {
                        System.out.println("Skipping item " + kv[0] + " in order " + p[0]
                                + ": not in menu.");
                        continue;
                    }
                    order.getItemsList().add(new OrderItem(menuItem, Integer.parseInt(kv[1])));
                }
            }
            return order;
        } catch (RuntimeException e) {
            return null;
        }
    }

    // Cập nhật counter để mã đơn mới không trùng mã đã có trong file
    private void updateCounter(String orderId) {
        try {
            int number = Integer.parseInt(orderId.substring(1));
            if (number + 1 > counter) {
                counter = number + 1;
            }
        } catch (RuntimeException e) {
            // Mã đơn không đúng dạng O<số>: bỏ qua, không ảnh hưởng counter.
        }
    }
}
