package business;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import models.OrderStatus;

public class OrderService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

 
    public Order createOrder(String tableNumber, String customerName, String customerPhone) {
        ensureNoPendingOrderForTable(tableNumber);
        String today = LocalDate.now().format(DATE_FMT);
        Order order = new Order(orderRepository.nextId(), today, tableNumber, customerName, customerPhone);
        orderRepository.add(order);
        persist();
        return order;
    }


    public Order createOrder(String tableNumber, String customerName, String customerPhone,
                             List<OrderItem> items) {
        ensureNoPendingOrderForTable(tableNumber);
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Cannot create an order without any items.");
        }

        String today = LocalDate.now().format(DATE_FMT);
        Order order = new Order(orderRepository.nextId(), today, tableNumber, customerName, customerPhone);
        for (OrderItem item : items) {
            if (item == null || item.getMenuItem() == null || item.getQuantity() <= 0) {
                throw new BusinessException("Every order item must have a valid menu item and a positive quantity.");
            }
            if (!item.getMenuItem().isStatus()) {
                throw new BusinessException("Item " + item.getMenuItem().getItemId()
                        + " is currently unavailable.");
            }
            order.getItemsList().add(new OrderItem(item.getMenuItem(), item.getQuantity()));
        }

        orderRepository.add(order);
        persist();
        return order;
    }

    public void addItem(String orderId, MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new BusinessException("Menu item does not exist. Please check the menu item ID.");
        }
        if (!menuItem.isStatus()) {
            throw new BusinessException("Item " + menuItem.getItemId() + " is currently unavailable.");
        }
        if (quantity <= 0) {
            throw new BusinessException("Quantity to add must be a positive integer greater than 0.");
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
            throw new BusinessException("Quantity cannot be negative. Enter 0 to remove the item.");
        }

        Order order = requireOrder(orderId);
        requirePending(order);

        OrderItem item = findItem(order, itemId);
        if (item == null) {
            throw new BusinessException("Menu item " + itemId + " is not in order " + orderId + ".");
        }

        if (newQuantity == 0) {
            order.getItemsList().remove(item);
            persist();
            return;
        }

        item.setQuantity(newQuantity);
        persist();
    }

    public void removeItem(String orderId, String itemId) {
        Order order = requireOrder(orderId);
        requirePending(order);

        OrderItem item = findItem(order, itemId);
        if (item == null) {
            throw new BusinessException("Menu item " + itemId + " is not in order " + orderId + ".");
        }

        order.getItemsList().remove(item);
        persist();
    }

    public void changeStatus(String orderId, OrderStatus newStatus) {
        if (newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.CANCELLED) {
            throw new BusinessException("Order status can only be changed to COMPLETED or CANCELLED.");
        }

        Order order = requireOrder(orderId);
        requirePending(order);
        order.setStatus(newStatus);
        persist();
    }

    /** Backward-compatible cancellation method. */
    public void cancelOrder(String orderId) {
        changeStatus(orderId, OrderStatus.CANCELLED);
    }

    public List<Order> getOpenOrders() {
        List<Order> pending = new ArrayList<>();
        for (Order order : orderRepository.findAll()) {
            if (order.getStatus() == OrderStatus.PENDING) {
                pending.add(order);
            }
        }
        return pending;
    }

    public Order findOpenOrderByTable(String tableNumber) {
        if (tableNumber == null) {
            return null;
        }
        String table = tableNumber.trim();
        for (Order order : orderRepository.findAll()) {
            if (order.getStatus() == OrderStatus.PENDING
                    && order.getTableNumber() != null
                    && order.getTableNumber().equalsIgnoreCase(table)) {
                return order;
            }
        }
        return null;
    }

    public Order findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> searchByTable(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lowerKey = keyword.trim().toLowerCase();
        List<Order> result = new ArrayList<>();
        for (Order order : orderRepository.findAll()) {
            if (order != null && order.getTableNumber() != null) {
                if (order.getTableNumber().toLowerCase().contains(lowerKey)) {
                    result.add(order);
                }
            }
        }
        return result;
    }

    public Order searchByOrderId(String orderId) {
        if (orderId == null) {
            return null;
        }
        return orderRepository.findById(orderId.trim());
    }

    public void save() throws IOException {
        orderRepository.save();
    }

    public double calculateTotal(Order order) {
        double total = 0;
        for (OrderItem item : order.getItemsList()) {
            total += item.getMenuItem().getPrice() * item.getQuantity();
        }
        return total;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
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
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BusinessException("Order ID '" + orderId + "' does not exist. Please check the ID and try again.");
        }
        return order;
    }

    private void requirePending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Order " + order.getOrderId() + " is "
                    + order.getStatus() + " and cannot be modified. Only PENDING orders can be changed.");
        }
    }

    private void ensureNoPendingOrderForTable(String tableNumber) {
        Order existing = findOpenOrderByTable(tableNumber);
        if (existing != null) {
            throw new BusinessException("Table " + tableNumber + " already has pending order "
                    + existing.getOrderId() + ". Add items to that order instead.");
        }
    }

    private void persist() {
        try {
            orderRepository.save();
        } catch (IOException e) {
            throw new BusinessException("Cannot save orders: " + e.getMessage());
        }
    }

    public List<Order> sortedByTotalDesc() {
        List<Order> sorted = orderRepository.findAll();
        sorted.sort((a, b) -> Double.compare(calculateTotal(b), calculateTotal(a)));
        return sorted;
    }
}
