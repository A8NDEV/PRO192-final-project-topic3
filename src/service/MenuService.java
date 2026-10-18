/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import exception.DuplicateItemException;
import exception.ItemNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.MenuItem;
import repository.MenuRepository;
import java.util.HashSet;
import java.util.Set;
import models.Order;
import models.OrderItem;

/**
 *
 * @author ASUS
 */
/**
 * Xử lý nghiệp vụ quản lý menu: kiểm tra trùng ID, tìm kiếm và thêm món. Dữ
 * liệu được lấy/lưu qua MenuRepository.
 */
public class MenuService {

    private MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public MenuItem findById(String id) {
        if (id == null) {
            return null;
        }
        String searchId = id.trim();

        for (MenuItem item : menuRepository.findAll()) {
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

        for (MenuItem item : menuRepository.findAll()) {
            if (item != null && item.getItemName() != null) {
                if (item.getItemName().toLowerCase().contains(lowerName)) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    public void addItem(MenuItem item) {
        if (findById(item.getItemId()) != null) {
            throw new DuplicateItemException(item.getItemId());
        }
        menuRepository.add(item);
    }

    public void save() throws IOException {
        menuRepository.save();
    }

    public List<MenuItem> getMenuList() {
        return menuRepository.findAll();
    }

    public void deleteItem(String id) {
        MenuItem item = findById(id);
        if (item == null) {
            throw new ItemNotFoundException(id);
        }
        menuRepository.delete(id);
    }

     //Hien thi mon chua co don
    public List<MenuItem> getUnorderedItems(List<Order> orders) {
        //danh sach cac mon da tung duoc order
        Set<String> listItemOrder = new HashSet<>();
        for (Order order : orders) {
            if (order.getItemsList() != null) {
                for (OrderItem item : order.getItemsList()) {
                    listItemOrder.add(item.getMenuItem().getItemId().toLowerCase());
                }
            }
        }

        //loc ra danh sach cac mon chua tung order
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menuRepository.findAll()) {
            if (!listItemOrder.contains(item.getItemId().toLowerCase())) {
                result.add(item);
            }
        }
        return result;
    }
}