/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import utils.FileHandler;

/**
 *
 * @author ASUS
 */
public class OrderManager {

    private static final List<Order> orders = new ArrayList<>();
    private static int counter = 1;

    static {
        load();
    }

    private OrderManager() {
    }

    public static Order createOrder(String tableNumber, String customerName, String customerPhone,
            List<OrderItem> items) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Table number cannot be empty.");
        }

        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }

        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer phone cannot be empty.");
        }

        Order existingOrder = findOpenOrderByTable(tableNumber);

        if (existingOrder != null) {
            throw new IllegalArgumentException(
                    "Table " + tableNumber + " already has an open order: " + existingOrder.getOrderId());
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item.");
        }

        for (OrderItem item : items) {
            if (item == null || item.getMenuItem() == null) {
                throw new IllegalArgumentException("Order contains an invalid menu item.");
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than 0.");
            }

            MenuItem menuItem = MenuManager.findById(item.getMenuItem().getItemId());

            if (menuItem == null || !menuItem.isStatus()) {
                throw new IllegalArgumentException(
                        "Menu item " + item.getMenuItem().getItemId() + " does not exist or is currently unavailable.");
            }
        }

        String orderId = String.format("O%04d", counter++);
        String orderDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Order order = new Order(
                orderId,
                orderDate,
                tableNumber.trim(),
                customerName.trim(),
                customerPhone.trim());

        for (OrderItem item : items) {
            MenuItem menuItem = MenuManager.findById(item.getMenuItem().getItemId());
            order.getItemsList().add(new OrderItem(menuItem, item.getQuantity()));
        }

        orders.add(order);
        try {
            save();
        } catch (IOException e) {
            orders.remove(order);
            counter--;
            throw new IllegalArgumentException("Cannot save the new order: " + e.getMessage());
        }
        return order;
    }

    public static void addItem(String orderId, MenuItem menuItem, int quantity) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be empty.");
        }

        Order order = findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Order ID '" + orderId + "' does not exist.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order '" + orderId + "' is not PENDING and cannot be modified.");
        }

        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be empty.");
        }

        MenuItem existingMenuItem = MenuManager.findById(menuItem.getItemId());

        if (existingMenuItem == null || !existingMenuItem.isStatus()) {
            throw new IllegalArgumentException(
                    "Menu item '" + menuItem.getItemId() + "' does not exist or is currently unavailable.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }

        OrderItem existingItem = null;

        for (OrderItem item : order.getItemsList()) {
            if (item.getMenuItem().getItemId().equalsIgnoreCase(existingMenuItem.getItemId())) {
                existingItem = item;
                break;
            }
        }

        int oldQuantity = 0;
        boolean isNewItem = false;

        if (existingItem != null) {
            oldQuantity = existingItem.getQuantity();
            existingItem.setQuantity(oldQuantity + quantity);
        } else {
            existingItem = new OrderItem(existingMenuItem, quantity);
            order.getItemsList().add(existingItem);
            isNewItem = true;
        }

        try {
            save();
        } catch (IOException e) {
            if (isNewItem) {
                order.getItemsList().remove(existingItem);
            } else {
                existingItem.setQuantity(oldQuantity);
            }

            throw new IllegalArgumentException("Cannot save the updated order: " + e.getMessage());
        }
    }

    public static void updateQuantity(String orderId, String itemId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative. Enter 0 to remove the item.");
        }
        Order order = requireOrder(orderId);
        requirePending(order);

        OrderItem item = findItem(order, itemId);
        if (item == null) {
            throw new IllegalArgumentException("Menu item" + itemId + " not found in order " + orderId);
        }

        if (newQuantity == 0) {
            order.getItemsList().remove(item);
        } else {
            item.setQuantity(newQuantity);
        }
        persist();
    }

    public static void changeStatus(String orderId, OrderStatus newStatus) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be empty.");
        }

        Order order = findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Order ID '" + orderId + "' does not exist.");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("New order status cannot be empty.");
        }

        if (order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Order '" + orderId + "' is already " + order.getStatus() 
                              + " and cannot change status again."
                              + "");
        }

        if (newStatus == OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot change an order back to PENDING.");
        }

        if (order.getStatus() == newStatus) {
            throw new IllegalArgumentException("Order is already in " + newStatus + " status.");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        try {
            save();
        } catch (IOException e) {
            order.setStatus(oldStatus);
            throw new IllegalArgumentException("Cannot save the order status: " + e.getMessage());
        }
    }

    public static List<Order> getOpenOrders() {
        List<Order> openOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PREPARING) {
                openOrders.add(order);
            }
        }
        return openOrders;
    }

    public static List<Order> getPendingOrders() {
        List<Order> pendingOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PENDING) {
                pendingOrders.add(order);
            }
        }
        return pendingOrders;
    }

    public static Order findOpenOrderByTable(String tableNumber) {
        for (Order order : orders) {
            if (order.getTableNumber().equalsIgnoreCase(tableNumber)
                    && (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PREPARING)) {
                return order;
            }
        }
        return null;
    }

    public static Order findById(String orderId) {
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

    public static List<Order> searchByTable(String keyword) {
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

    public static double calculateTotal(Order order) {
        if (order == null) {
            return 0;
        }
        return order.getTotal();
    }

    public static List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public static List<Order> sortedByTotalDesc() {
        List<Order> sorted = new ArrayList<>(orders);
        Collections.sort(sorted);
        return sorted;
    }

    public static List<MenuItem> getUnorderedItems() {
        List<MenuItem> unordered = new ArrayList<>();
        List<MenuItem> allMenuItems = MenuManager.getMenuList();

        for (MenuItem item : allMenuItems) {
            boolean hasOrder = false;
            for (Order order : orders) {
                for (OrderItem orderItem : order.getItemsList()) {
                    if (item.getItemId().equalsIgnoreCase(orderItem.getMenuItem().getItemId())) {
                        hasOrder = true;
                        break;
                    }
                }
                if (hasOrder) {
                    break;
                }
            }
            if (!hasOrder) {
                unordered.add(item);
            }
        }
        return unordered;
    }

    private static OrderItem findItem(Order order, String itemId) {
        for (OrderItem item : order.getItemsList()) {
            if (item.getMenuItem().getItemId().equalsIgnoreCase(itemId)) {
                return item;
            }
        }
        return null;
    }

    private static Order requireOrder(String orderId) {
        Order order = findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return order;
    }

    private static void requirePending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order " + order.getOrderId() + " is " + order.getStatus() + ", not PENDING.");
        }
    }

    private static void persist() {
        try {
            save();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save orders: " + e.getMessage());
        }
    }

    public static void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Order order : orders) {
            lines.add(format(order));
        }
        FileHandler.writeFile("data/orders.txt", lines);
    }

    private static void load() {
        for (String line : FileHandler.readFile("data/orders.txt")) {
            Order order = parse(line);
            if (order == null) {
                System.out.println("Skip invalid order line: " + line);
                continue;
            }
            orders.add(order);
            updateCounter(order.getOrderId());
        }
    }

    private static String format(Order order) {
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

    private static Order parse(String line) {
        try {
            String[] p = line.split("\\|", -1);
            if (p.length != 7) {
                return null;
            }
            Order order = new Order(p[0], p[1], p[2], p[3], p[4]);
            order.setStatus(OrderStatus.valueOf(p[5]));
            if (!p[6].isEmpty()) {
                for (String entry : p[6].split(",")) {
                    String[] kv = entry.split(":");
                    MenuItem menuItem = MenuManager.findById(kv[0]);
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

    private static void updateCounter(String orderId) {
        try {
            int number = Integer.parseInt(orderId.substring(1));
            if (number + 1 > counter) {
                counter = number + 1;
            }
        } catch (RuntimeException e) {
        }
    }
}
