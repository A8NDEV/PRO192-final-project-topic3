package service;

import exception.BusinessException;
import java.io.IOException;
import java.util.List;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import repository.OrderRepository;


public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(String orderDate, String tableNumber,
                             String customerName, String customerPhone) {
        Order order = new Order(orderRepository.nextId(), orderDate, tableNumber,
                customerName, customerPhone);
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
        OrderItem oi = findItem(requireOrder(orderId), itemId);
        if (oi == null) {
            throw new BusinessException("Item " + itemId + " is not in order " + orderId + ".");
        }
        oi.setQuantity(newQuantity);
        persist();
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

    private void persist() {
        try {
            orderRepository.save();
        } catch (IOException e) {
            throw new BusinessException("Cannot save orders: " + e.getMessage());
        }
    }
}