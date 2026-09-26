/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package business;

import java.util.ArrayList;
import java.util.List;
import models.MenuItem;
import utils.CustomException;

/**
 *
 * @author ASUS
 */
/**
 * Xử lý nghiệp vụ quản lý menu: lưu danh sách món. và thực hiện các thao tác
 * CRUD.
 */
public class MenuService {

    private List<MenuItem> menuList;

    public MenuService(List<MenuItem> menuList) {
        this.menuList = menuList;
    }

    public void addItem(MenuItem item) throws CustomException {
        for (MenuItem m : menuList) {
            if (m.getItemId().equalsIgnoreCase(item.getItemId())) {
                throw new CustomException("Item ID '" + item.getItemId() + "' already exists!");
            }
        }
        menuList.add(item);
    }

    public List<MenuItem> getMenuList() {
        return new ArrayList<>(menuList);
    }

}
