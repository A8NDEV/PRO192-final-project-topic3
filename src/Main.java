/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import controller.AppController;
import controller.MenuController;
import java.util.Scanner;
import repository.MenuRepository;
import service.MenuService;
import service.OrderService;

/**
 *
 * @author ASUS
 */
public class Main {
    public static void main(String[] args) {
        String menuFilePath = "data/menu.txt";

        MenuRepository menuRepository = new MenuRepository(menuFilePath);
        MenuService menuService = new MenuService(menuRepository);

        OrderService orderService = new OrderService();

        Scanner scanner = new Scanner(System.in);
        MenuController menuController = new MenuController(menuService, scanner);

        AppController appController = new AppController(scanner, menuController, menuService, orderService);
        
        appController.run();
    }
}
