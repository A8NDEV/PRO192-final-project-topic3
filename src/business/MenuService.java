/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.MenuItem;
import exception.DuplicateItemException;

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

    public MenuItem findById(String id) {
        if (id == null) {
            return null;
        }
        String searchId = id.trim();

        for (MenuItem item : menuList) {
            if (item.getItemId().equalsIgnoreCase(searchId)) {
                return item;
            }
        }
        return null;
    }

    public List<MenuItem> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String lowerName = name.trim().toLowerCase();
        List<MenuItem> result = new ArrayList<>();

        for (MenuItem item : menuList) {
            if (item != null && item.getItemName() != null) {
                if (item.getItemName().toLowerCase().contains(lowerName)) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    public void addItem(MenuItem item) throws DuplicateItemException {
        if (findById(item.getItemId()) != null) {
            throw new DuplicateItemException(item.getItemId());
        }
        menuList.add(item);
    }

    public List<MenuItem> getMenuList() {
        return new ArrayList<>(menuList);
    }

}
