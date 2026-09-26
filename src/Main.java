/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import business.MenuService;
import dispatcher.MenuDispatcher;
import java.util.List;
import models.MenuItem;
import utils.FileHandler;

/**
 *
 * @author ASUS
 */
public class Main {
    public static void main(String[] args) {
        String menuFilePath = "data/menu.txt";

        List<MenuItem> loadedMenu = FileHandler.loadMenuFromFile(menuFilePath);
        MenuService menuService = new MenuService(loadedMenu);

        MenuDispatcher menuDispatcher = new MenuDispatcher(menuService, menuFilePath);
        menuDispatcher.showMenu();
    }
}
