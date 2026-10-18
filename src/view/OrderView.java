package view;

import models.Order;


    /**
 * Hiển thị riêng cho đơn hàng. Chỉ in, không đọc input và không xử lý logic
 * (tổng tiền do nơi gọi tính rồi truyền vào).
 */
public class OrderView {

    // Một dòng tóm tắt: mã đơn, ngày, bàn, khách, số món, tổng tiền
    public void showOrderSummary(Order order, double total) {
        System.out.println(String.format("%-6s | %-10s | Table %-5s | %-25s | %2d item(s) | %,.0f",
                order.getOrderId(), order.getOrderDate(), order.getTableNumber(),
                order.getCustomerName(), order.getItemsList().size(), total));
    }
}
 

