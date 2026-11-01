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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    public Order createOrder(String tableNumber, String customerName, String customerPhone, List<OrderItem> items) {

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
            throw new IllegalArgumentException("Table " + tableNumber + " already has an open order: " + existingOrder.getOrderId());
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

            MenuItem menuItem = menuManager.findById(item.getMenuItem().getItemId());

            if (menuItem == null || !menuItem.isStatus()) {
                throw new IllegalArgumentException("Menu item " + item.getMenuItem().getItemId() + " does not exist or is currently unavailable.");
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
            MenuItem menuItem = menuManager.findById(
                    item.getMenuItem().getItemId());

            order.getItemsList().add( new OrderItem(menuItem, item.getQuantity()));
        }

        orders.add(order);

        return order;
    }

    public void addItem(String orderId, MenuItem menuItem, int quantity) {

        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be empty.");
        }

        Order order = findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException( "Order ID '" + orderId + "' does not exist.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order '" + orderId + "' is not PENDING and cannot be modified.");
        }

        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be empty.");
        }

        MenuItem existingMenuItem = menuManager.findById(menuItem.getItemId());

        if (existingMenuItem == null || !existingMenuItem.isStatus()) {
            throw new IllegalArgumentException( "Menu item '" + menuItem.getItemId() + "' does not exist or is currently unavailable.");
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

            throw new IllegalArgumentException( "Cannot save the updated order: " + e.getMessage());
        }
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
        List<String> lines = new ArrayList<>();

        for (Order order : orders) {
            lines.add(format(order));
        }

        FileHandler.writeFile(filePath, lines);
    }

    private void load() {
        List<String> lines = FileHandler.readFile(filePath);

        for (String line : lines) {
            Order order = parse(line);

            if (order == null) {
                System.out.println("Skipping invalid order line: " + line);
                continue;
            }

            orders.add(order);
            updateCounter(order.getOrderId());
        }
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
                    parts[4].trim());

            order.setStatus(OrderStatus.valueOf(parts[5].trim()));

            String[] itemParts = parts[6].split(",");

            for (String itemPart : itemParts) {
                String[] itemInfo = itemPart.split(":");

                if (itemInfo.length != 2) {
                    return null;
                }

                MenuItem menuItem = menuManager.findById(itemInfo[0].trim());

                if (menuItem == null) {
                    return null;
                }

                int quantity = Integer.parseInt(itemInfo[1].trim());

                if (quantity <= 0) {
                    return null;
                }

                order.getItemsList().add(
                        new OrderItem(menuItem, quantity));
            }

            return order;

        } catch (Exception e) {
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
            // Mã đơn không đúng dạng O<số>: bỏ qua, không ảnh hưởng counter.
        }
    }
}
