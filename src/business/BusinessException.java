package business;

/**
 *
 * @author ASUS
 */
/**
 * Exception gốc cho các lỗi nghiệp vụ trong hệ thống (unchecked, nên không cần
 * khai báo throws). Quản lý tập trung các ngoại lệ nghiệp vụ cụ thể bằng static nested class.
 * Ném ra kèm message mô tả lỗi, tầng gọi (Dispatcher) sẽ bắt và hiển thị cho người dùng.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    // Ngoại lệ khi trùng ID món ăn
    public static class DuplicateItemException extends BusinessException {
        public DuplicateItemException(String itemId) {
            super("Item ID '" + itemId + "' already exists!");
        }
    }

    // Ngoại lệ khi không tìm thấy món ăn
    public static class ItemNotFoundException extends BusinessException {
        public ItemNotFoundException(String itemId) {
            super("Item ID '" + itemId + "' not found!");
        }
    }
}
