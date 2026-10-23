/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import business.MenuRepository;
import business.MenuService;
import business.OrderRepository;
import business.OrderService;
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

        MenuRepository menuRepository = new MenuRepository(menuFilePath);
        MenuService menuService = new MenuService(menuRepository);

        OrderRepository orderRepository = new OrderRepository(orderFilePath, menuRepository);
        OrderService orderService = new OrderService(orderRepository);

        Scanner scanner = new Scanner(System.in);
        
        MenuDispatcher menuDispatcher = new MenuDispatcher(menuService, scanner, orderService);
        OrderDispatcher orderDispatcher = new OrderDispatcher(orderService, menuService, scanner);

        AppDispatcher appDispatcher = new AppDispatcher(scanner, menuDispatcher, orderDispatcher);
        appDispatcher.run();
    }
}