package view;

/**
 *
 * @author ASUS
 */
/**
 * Hiển thị menu chính của ứng dụng ra console. Chỉ in, không đọc input.
 */
public class AppView {

    public void showMainMenu() {
        System.out.println("\n===== Table-side Ordering =====");
        System.out.println("1. Menu management");
        System.out.println("2. Order management");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    public void showNotInteger() {
        System.out.println("Value must be an integer.");
    }

    public void showInvalidChoice() {
        System.out.println("Invalid choice.");
    }
}
