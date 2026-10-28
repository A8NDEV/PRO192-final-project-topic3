/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.MenuItem;
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
            throw new BusinessException.DuplicateItemException(item.getItemId());
        }
        menuRepository.add(item);
    }

    public void save() throws IOException {
        menuRepository.save();
    }

    public List<MenuItem> getAvailableItems() {
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menuRepository.findAll()) {
            if (item != null && item.isStatus()) {
                result.add(item);
            }
        }
        return result;
    }

    public List<MenuItem> getMenuList() {
        return menuRepository.findAll();
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

    public void updateItem(MenuItem updatedItem) {
        if (updatedItem == null || updatedItem.getItemId() == null) {
            throw new BusinessException("Item cannot be null.");
        }

        MenuItem currentItem = findById(updatedItem.getItemId());
        if (currentItem == null) {
            throw new BusinessException.ItemNotFoundException(updatedItem.getItemId());
        }

        if (updatedItem.getItemName() == null || updatedItem.getItemName().trim().isEmpty()) {
            throw new BusinessException("Item name cannot be blank.");
        }
        if (updatedItem.getPrice() <= 0) {
            throw new BusinessException("Price must be greater than 0.");
        }
        menuRepository.update(updatedItem);
    }

    public boolean isItemOrdered(String itemId, List<Order> orders) {
        if (itemId == null || orders == null || orders.isEmpty()) {
            return false;
        }

        String targetId = itemId.trim();

        for (Order order : orders) {
            if (order == null || order.getItemsList() == null) {
                continue;
            }

            for (OrderItem item : order.getItemsList()) {
                if (item == null || item.getMenuItem() == null) {
                    continue;
                }

                String currentId = item.getMenuItem().getItemId();
                if (currentId != null && targetId.equalsIgnoreCase(currentId.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteItem(String id, List<Order> orders) {
        MenuItem item = findById(id);
        if (item == null) {
            throw new BusinessException.ItemNotFoundException(id);
        }
        if (isItemOrdered(id, orders)) {
            throw new BusinessException("Cannot delete item '" + id
                    + "' because it has already been ordered.");
        }
        menuRepository.delete(id);
    }
}
