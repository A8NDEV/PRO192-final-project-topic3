/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import controller.MenuController;
import repository.MenuRepository;
import service.MenuService;

/**
 *
 * @author ASUS
 */
public class Main {
    public static void main(String[] args) {
        String menuFilePath = "data/menu.txt";

        MenuRepository menuRepository = new MenuRepository(menuFilePath);
        MenuService menuService = new MenuService(menuRepository);

        MenuController menuController = new MenuController(menuService);
        menuController.run();
    }
}
