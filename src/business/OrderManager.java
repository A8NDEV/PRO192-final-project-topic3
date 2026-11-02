/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package business;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        load();
    }

    public Order createOrder(String tableNumber, String customerName, String customerPhone,
            List<OrderItem> items) {
        return null;
    }

    public void addItem(String orderId, MenuItem menuItem, int quantity) {
    }

    public void updateQuantity(String orderId, String itemId, int newQuantity) {
        if(newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        Order order = requireOrder(orderId);
        requirePending(order);

        OrderItem item = findItem(order, itemId);
        if (item == null) {
            throw new IllegalArgumentException("Menu item" + itemId + " not found in order " + orderId);
        }

        if(newQuantity ==0) {
           order.getItemsList().remove(item);
        }else{
            
            item.setQuantity(newQuantity);
        }
        persist();
    }

    public void changeStatus(String orderId, OrderStatus newStatus) {
    }

    public List<Order> getOpenOrders() {
        List<Order> open = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PREPARING || order.getStatus() == OrderStatus.PREPARING) {
                open.add(order);
            }
        }
        return open;
    }

    public Order findOpenOrderByTable(String tableNumber) {
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
            if (order != null && order.getTableNumber() != null
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

    public List<Order> getAllOrders() {
        return Collections.emptyList();
    }

    public List<Order> sortedByTotalDesc() {
        return Collections.emptyList();
    }

    public List<MenuItem> getUnorderedItems() {
        return Collections.emptyList();
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
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return order;
    }

    private void requirePending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order " + order.getOrderId() + " is "+ order.getStatus() + ", not PENDING.");
        }

    }

    private void persist() {
        try {
            save();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save orders: " + e.getMessage());
        }
    }

    public void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Order order : orders) {
            lines.add(format(order));
        }
        FileHandler.writeFile(filePath, lines);
    }

    private void load() {
        for (String line : FileHandler.readFile(filePath)) {
            Order order = parse(line);
            if (order == null) {
                System.out.println("Skip invalid order line: " + line);
                continue;
            }
            orders.add(order);
            updateCounter(order.getOrderId());
        }
    }

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

    private void updateCounter(String orderId) {
        try {
            int number = Integer.parseInt(orderId.substring(1));
            if (number + 1 > counter) {
                counter = number + 1;
            }
        } catch (RuntimeException e) {
        }
    }
}
