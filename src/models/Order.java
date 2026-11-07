/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class Order implements Comparable<Order> {

    private String orderId;
    private String orderDate;
    private String tableNumber;
    private String customerName;
    private String customerPhone;
    private List<OrderItem> itemsList = new ArrayList<>();
    private OrderStatus status;

    public Order(String orderId, String orderDate, String tableNumber,
                 String customerName, String customerPhone) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.tableNumber = tableNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.status = OrderStatus.PENDING;
    }

    public String getOrderId() { return orderId; }
    public String getOrderDate() { return orderDate; }
    public String getTableNumber() { return tableNumber; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public List<OrderItem> getItemsList() { return itemsList; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public double getTotal() {
        double total = 0;
        for (OrderItem item : itemsList) {
            if (item != null && item.getMenuItem() != null) {
                total += item.getMenuItem().getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    @Override
    public int compareTo(Order other) {
        if (other == null) {
            return -1;
        }
        return Double.compare(other.getTotal(), this.getTotal());
    }
}
