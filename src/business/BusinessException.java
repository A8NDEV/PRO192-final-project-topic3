/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package business;

/**
 *
 * @author ASUS
 */
/**
 * Exception gốc cho các lỗi nghiệp vụ trong hệ thống (unchecked, nên không cần
 * khai báo throws). Mỗi trường hợp lỗi cụ thể (trùng itemId, không tìm thấy dữ
 * liệu...) là một lớp con riêng. Ném ra kèm message mô tả lỗi, tầng gọi
 * (Controller) sẽ bắt và hiển thị cho người dùng.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
