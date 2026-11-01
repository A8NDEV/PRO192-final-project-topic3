/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import models.OrderStatus;
import utils.FileHandler;

/**
 *
 * @author ASUS
 */
public class OrderManager {

    private static final String SEP = "\\|";
    private static final int FIELD_COUNT = 7;

    private final String filePath;
    private final MenuManager menuManager;
    private final List<Order> orders = new ArrayList<>();
    private int counter = 1;

    public OrderManager(String filePath, MenuManager menuManager) {
        this.filePath = filePath;
        this.menuManager = menuManager;
    }

    public Order createOrder(String tableNumber, String customerName, String customerPhone,
                             List<OrderItem> items) {
        return null;
    }

    public void addItem(String orderId, MenuItem menuItem, int quantity) {
    }

    public void updateQuantity(String orderId, String itemId, int newQuantity) {
    }

    public void changeStatus(String orderId, OrderStatus newStatus) {
    }

    public List<Order> getOpenOrders() {
        List<Order> openOrders = new ArrayList<>();

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PENDING) {
            openOrders.add(order);
            }
        }
        return openOrders;
    }

    public Order findOpenOrderByTable(String tableNumber) {
        for (Order order : orders) {
            if (order.getTableNumber().equalsIgnoreCase(tableNumber)
            && order.getStatus() == OrderStatus.PENDING) {
               return order;
            }
        }
        return null;
    }

    public Order findById(String orderId) {
        for (Order order : orders) {
            if (order.getOrderId().equalsIgnoreCase(orderId)) {
                return order;
            }
        }
        return null;
    }

    public List<Order> searchByTable(String keyword) {
        return Collections.emptyList();
    }

    public double calculateTotal(Order order) {
        double total = 0;
        
        for (OrderItem item : order.getItemsList()) {
            total += item.getMenuItem().getPrice() * item.getQuantity();

        }
        return total;
    }

    public List<Order> getAllOrders() {
        return Collections.emptyList();
    }

    public List<Order> sortedByTotalDesc() {
        return Collections.emptyList();
    }

    public List<MenuItem> getUnorderedItems() {
        return Collections.emptyList();
    }

    public void save() throws IOException {
    }

    private void load() {
    }

    private String format(Order order) {
        String items = "";

        for (OrderItem item : order.getItemsList()) {
            if (!items.isEmpty()) {
                items += ",";

            }
            items += item.getMenuItem().getItemId() + ":" + item.getQuantity();

        }
        return order.getOrderId() + "|"
            + order.getOrderDate() + "|"
            + order.getTableNumber() + "|"
            + order.getCustomerName() + "|"
            + order.getCustomerPhone() + "|"
            + order.getStatus().name() + "|"
            + items;
    }

    private Order parse(String line) {
        try {
            String[] parts = line.split(SEP);
            
            if (parts.length != FIELD_COUNT) {
                return null;
            }
            Order order = new Order(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim()
            );
            order.setStatus(OrderStatus.valueOf(parts[5].trim()));

            String[] itemParts = parts[6].split(",");
            if (itemParts.length % 2 != 0) {
                return null;
            }

            for (int i = 0; i < itemParts.length; i += 2) {
                MenuItem menuItem = menuManager.findById(itemParts[i].trim());
                if (menuItem == null) {
                    return null;
                }
                int quantity = Integer.parseInt(itemParts[i + 1].trim());
                if (quantity <= 0) {
                    return null;
                }
                order.getItemsList().add(new OrderItem(menuItem, quantity));
            }

            return order;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateCounter(String orderId) {
    }
}
