/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author ASUS
 */
/**
 * Exception dùng chung cho các lỗi trong hệ thống. Ví dụ: trùng itemId, không
 * tìm thấy dữ liệu, input không hợp lệ... Ném ra kèm message mô tả lỗi, tầng
 * gọi (Dispatcher) sẽ bắt và hiển thị cho người dùng.
 */
public class CustomException extends Exception {

    public CustomException(String message) {
        super(message);
    }
}
