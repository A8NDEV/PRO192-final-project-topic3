package view;

/**
 *
 * @author ASUS
 */
/**
 * Các hàm in chung dùng cho mọi màn hình: thông báo, lỗi và menu. Nội dung
 * thông báo do nơi gọi truyền vào (exception, controller...), view chỉ in.
 */
public class ConsoleView {

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }

    // Menu dạng danh sách: đánh số 1..n theo thứ tự options, 0 là lựa chọn thoát/quay lại
    public void showMenu(String title, String[] options, String zeroLabel) {
        System.out.println("\n===== " + title + " =====");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println("0. " + zeroLabel);
        System.out.print("Choose: ");
    }
}


