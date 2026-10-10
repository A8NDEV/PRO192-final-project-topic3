package business;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import models.MenuItem;
import models.Order;
import models.OrderItem;

public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private int counter = 1;

    // 1. Tạo order mới cho một bàn
    public Order createOrder(String orderDate, int tableNumber,
                             String customerName, String customerPhone) {
        String id = String.format("O%04d", counter++);
        Order order = new Order(id, orderDate, tableNumber, customerName, customerPhone);
        orders.add(order);
        return order;
    }

    // 2. Thêm món kèm số lượng (món đã có thì cộng dồn)
    public void addItem(String orderId, MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item not found.");
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

    // 3. Cập nhật số lượng món trong order đã có
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

    // 4a. Tìm theo số bàn
    public List<Order> searchByTableNumber(int tableNumber) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getTableNumber() == tableNumber) {
                result.add(o);
            }
        }
        return result;
    }

    // 4b. Tìm theo mã order (null nếu không có)
    public Order searchByOrderId(String orderId) {
        for (Order o : orders) {
            if (o.getOrderId().equalsIgnoreCase(orderId)) {
                return o;
            }
        }
        return null;
    }

    // 5. Sắp xếp giảm dần theo tổng tiền (Comparator)
    public List<Order> sortByTotalDescending() {
        List<Order> sorted = new ArrayList<>(orders);
        sorted.sort((a, b) -> Double.compare(calculateTotal(b), calculateTotal(a)));
        return sorted;
    }

    // 6. Món trong menu chưa có order nào
    public List<MenuItem> getMenuItemsWithZeroOrders(List<MenuItem> menu) {
        Set<String> ordered = new HashSet<>();
        for (Order o : orders) {
            for (OrderItem oi : o.getItemsList()) {
                ordered.add(oi.getMenuItem().getItemId().toLowerCase());
            }
        }
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem m : menu) {
            if (!ordered.contains(m.getItemId().toLowerCase())) {
                result.add(m);
            }
        }
        return result;
    }

    // total tính động = tổng (giá x số lượng)
    public double calculateTotal(Order order) {
        double sum = 0;
        for (OrderItem oi : order.getItemsList()) {
            sum += oi.getMenuItem().getPrice() * oi.getQuantity();
        }
        return sum;
    }

    private OrderItem findItem(Order order, String itemId) {
        for (OrderItem oi : order.getItemsList()) {
            if (oi.getMenuItem().getItemId().equalsIgnoreCase(itemId)) {
                return oi;
            }
        }
        return null;
    }

    private Order requireOrder(String orderId) {
        Order o = searchByOrderId(orderId);
        if (o == null) {
            throw new IllegalArgumentException("Order " + orderId + " not found.");
        }
        return o;
    }
}