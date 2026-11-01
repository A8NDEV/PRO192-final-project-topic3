/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import business.MenuManager;
import business.OrderManager;
import dispatcher.AppDispatcher;
import dispatcher.MenuDispatcher;
import dispatcher.OrderDispatcher;
import java.util.Scanner;

/**
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
