package service;

import java.util.ArrayList;
import java.util.List;
import models.MenuItem;
import models.Order;
import models.OrderItem;

public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private int counter = 1;

    // Tạo đơn mới cho một bàn
    public Order createOrder(String orderDate, String tableNumber,
                             String customerName, String customerPhone) {
        String id = String.format("O%04d", counter++);
        Order order = new Order(id, orderDate, tableNumber, customerName, customerPhone);
        orders.add(order);
        return order;
    }

    // Thêm món kèm số lượng vào đơn đã có (món trùng thì cộng dồn)
    public void addItem(String orderId, MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item not found.");
        }
        if (!menuItem.isStatus()) {
            throw new IllegalArgumentException("Item " + menuItem.getItemId() + " is currently unavailable.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        Order order = requireOrder(orderId);
        OrderItem existing = findItem(order, menuItem.getItemId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            order.getItemsList().add(new OrderItem(menuItem, quantity));
        }
    }

    // Sửa số lượng món trong đơn
    public void updateQuantity(String orderId, String itemId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        OrderItem oi = findItem(requireOrder(orderId), itemId);
        if (oi == null) {
            throw new IllegalArgumentException("Item " + itemId + " is not in order " + orderId + ".");
        }
        oi.setQuantity(newQuantity);
    }

    // Tổng tiền của một đơn (tính động)
    public double calculateTotal(Order order) {
        double sum = 0;
        for (OrderItem oi : order.getItemsList()) {
            sum += oi.getMenuItem().getPrice() * oi.getQuantity();
        }
        return sum;
    }

    // Hàm hỗ trợ cho người làm lưu/đọc file và tìm kiếm
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    // Nạp đơn đọc từ file, cập nhật bộ đếm để mã mới không trùng
    public void loadOrders(List<Order> loaded) {
        orders.clear();
        orders.addAll(loaded);
        int max = 0;
        for (Order o : loaded) {
            max = Math.max(max, Integer.parseInt(o.getOrderId().substring(1)));
        }
        counter = max + 1;
    }

    private OrderItem findItem(Order order, String itemId) {
        for (OrderItem oi : order.getItemsList()) {
            if (oi.getMenuItem().getItemId().equalsIgnoreCase(itemId)) {
                return oi;
            }
        }
        return null;
    }

    // Tìm đơn theo mã, không thấy thì báo lỗi (chỉ dùng nội bộ)
    private Order requireOrder(String orderId) {
        for (Order o : orders) {
            if (o.getOrderId().equalsIgnoreCase(orderId)) {
                return o;
            }
        }
        throw new IllegalArgumentException("Order " + orderId + " not found.");
    }
}