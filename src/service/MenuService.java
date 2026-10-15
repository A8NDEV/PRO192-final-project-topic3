/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import exception.DuplicateItemException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.MenuItem;
import repository.MenuRepository;

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

    public void addItem(MenuItem item) throws DuplicateItemException {
        if (findById(item.getItemId()) != null) {
            throw new DuplicateItemException(item.getItemId());
        }
        menuRepository.add(item);
    }

    public void save() throws IOException {
        menuRepository.save();
    }

    public List<MenuItem> getAvailableItems(){
        List<MenuItem> result = new ArrayList<>();
        for(MenuItem item : menuRepository.findAll()){
            if(item != null && item.isStatus()){
                result.add(item);
            }
        }
        return result;
    }

    public List<MenuItem> getMenuList() {
        return menuRepository.findAll();
    }

}
