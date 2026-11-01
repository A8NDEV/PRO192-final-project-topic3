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
        return Collections.emptyList();
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
        return 0;
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
        return "";
    }

    private Order parse(String line) {
        return null;
    }

    private void updateCounter(String orderId) {
    }
}
