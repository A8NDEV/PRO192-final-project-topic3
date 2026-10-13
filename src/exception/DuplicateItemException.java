package exception;

/**
 *
 * @author ASUS
 */
/**
 * Ném ra khi thêm món có itemId đã tồn tại trong menu.
 */
public class DuplicateItemException extends AppException {

    public DuplicateItemException(String itemId) {
        super("Item ID '" + itemId + "' already exists!");
    }
}
