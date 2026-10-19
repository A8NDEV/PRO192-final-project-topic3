package service;

import exception.BusinessException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import models.OrderStatus;
import repository.OrderRepository;


public class OrderService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Date tự cập nhật theo ngày hiện tại
    public Order createOrder(String tableNumber, String customerName, String customerPhone) {
        String today = LocalDate.now().format(DATE_FMT);
        Order order = new Order(orderRepository.nextId(), today, tableNumber, customerName, customerPhone);
        orderRepository.add(order);
        persist();
        return order;
    }

    public void addItem(String orderId, MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new BusinessException("Menu item not found.");
        }
        if (!menuItem.isStatus()) {
            throw new BusinessException("Item " + menuItem.getItemId() + " is currently unavailable.");
        }
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0.");
        }
        Order order = requireOrder(orderId);
        requireEditable(order);
        OrderItem existing = findItem(order, menuItem.getItemId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            order.getItemsList().add(new OrderItem(menuItem, quantity));
        }
        persist();
    }

    public void updateQuantity(String orderId, String itemId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0.");
        }
        Order order = requireOrder(orderId);
        requireEditable(order);
        OrderItem oi = findItem(order, itemId);
        if (oi == null) {
            throw new BusinessException("Item " + itemId + " is not in order " + orderId + ".");
        }
        oi.setQuantity(newQuantity);
        persist();
    }

    // delete món theo ý muốn 
    public void removeItem(String orderId, String itemId) {
        Order order = requireOrder(orderId);
        requireEditable(order);
        OrderItem oi = findItem(order, itemId);
        if (oi == null) {
            throw new BusinessException("Item " + itemId + " is not in order " + orderId + ".");
        }
        order.getItemsList().remove(oi);
        persist();
    }

    // Đóng đơn: đổi trạng thái sang CANCELLED
    public void cancelOrder(String orderId) {
        Order order = requireOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order " + orderId + " is already cancelled.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        persist();
    }

    // hiển thị các đơn hàng đang mở
    public List<Order> getOpenOrders() {
        List<Order> open = new ArrayList<>();
        for (Order order : orderRepository.findAll()) {
            if (order.getStatus() == OrderStatus.PENDING) {
                open.add(order);
            }
        }
        return open;
    }

    public Order findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    // Một bàn có thể có nhiều đơn nên trả về danh sách
    public List<Order> searchByTable(String tableNumber) {
        List<Order> result = new ArrayList<>();
        if (tableNumber == null) {
            return result;
        }
        String key = tableNumber.trim();
        for (Order order : orderRepository.findAll()) {
            if (order.getTableNumber() != null && order.getTableNumber().equalsIgnoreCase(key)) {
                result.add(order);
            }
        }
        return result;
    }

    // Mã đơn là duy nhất nên trả về 1 đơn, hoặc null nếu không thấy
    public Order searchByOrderId(String orderId) {
        if (orderId == null) {
            return null;
        }
        String key = orderId.trim();
        for (Order order : orderRepository.findAll()) {
            if (order.getOrderId().equalsIgnoreCase(key)) {
                return order;
            }
        }
        return null;
    }

    public void save() throws IOException {
        orderRepository.save();
    }

    public double calculateTotal(Order order) {
        double sum = 0;
        for (OrderItem oi : order.getItemsList()) {
            sum += oi.getMenuItem().getPrice() * oi.getQuantity();
        }
        return sum;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
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
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BusinessException("Order " + orderId + " not found.");
        }
        return order;
    }

    // Đơn đã hủy chỉ có thể xem
    private void requireEditable(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order " + order.getOrderId() + " is cancelled and cannot be modified.");
        }
    }

    private void persist() {
        try {
            orderRepository.save();
        } catch (IOException e) {
            throw new BusinessException("Cannot save orders: " + e.getMessage());
        }
    }
}