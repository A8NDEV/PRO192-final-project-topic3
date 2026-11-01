import business.MenuManager;
import business.OrderManager;
import dispatcher.AppDispatcher;
import dispatcher.MenuDispatcher;
import dispatcher.OrderDispatcher;
import java.util.Scanner;

/**
 * Điểm khởi chạy: tạo các đối tượng và nối (wiring) chúng lại với nhau bằng
 * hàm tạo (Dependency Injection), sau đó chạy menu chính.
 *
 * @author ASUS
 */
public class Main {
    public static void main(String[] args) {
        String menuFilePath = "data/menu.txt";
        String orderFilePath = "data/orders.txt";

        // MenuManager phải tạo trước vì OrderManager cần tra món khi đọc file order.
        MenuManager menuManager = new MenuManager(menuFilePath);
        OrderManager orderManager = new OrderManager(orderFilePath, menuManager);

        Scanner scanner = new Scanner(System.in);

        MenuDispatcher menuDispatcher = new MenuDispatcher(menuManager, scanner, orderManager);
        OrderDispatcher orderDispatcher = new OrderDispatcher(orderManager, menuManager, scanner);

        AppDispatcher appDispatcher = new AppDispatcher(scanner, menuDispatcher, orderDispatcher);
        appDispatcher.run();
    }
}
