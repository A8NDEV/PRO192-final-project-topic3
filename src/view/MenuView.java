package view;

import models.MenuItem;

/**
 *
 * @author ASUS
 */
/**
 * Hiển thị riêng cho món trong menu. Chỉ in, không đọc input và không xử lý
 * logic.
 */
public class MenuView {

    public void showItem(MenuItem item) {
        System.out.println("- " + item);
    }
}
