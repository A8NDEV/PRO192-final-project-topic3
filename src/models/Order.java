package models;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String orderDate;
    private int tableNumber;
    private String customerName;
    private String customerPhone;
    private List<OrderItem> itemsList = new ArrayList<>();
    private OrderStatus status;

    public Order(String orderId, String orderDate, int tableNumber,
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
    public int getTableNumber() { return tableNumber; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public List<OrderItem> getItemsList() { return itemsList; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}