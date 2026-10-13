package exception;

/**
 *
 * @author ASUS
 */
/**
 * Ném ra khi thêm món có itemId đã tồn tại trong menu.
 */
public class DuplicateItemException extends BusinessException {

    public DuplicateItemException(String itemId) {
        super("Item ID '" + itemId + "' already exists!");
    }
}
