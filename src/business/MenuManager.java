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
import utils.FileHandler;
import utils.Validator;

/**
 *
 * @author ASUS
 */
public class MenuManager {

    private static final List<MenuItem> menuList = load();

    private MenuManager() {
    }

    public static MenuItem findById(String id) {
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

    public static List<MenuItem> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lowerName = name.trim().replaceAll("\\s+", " ").toLowerCase();
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menuList) {
            if (item.getItemName() != null && item.getItemName().toLowerCase().contains(lowerName)) {
                result.add(item);
            }
        }
        return result;
    }

    public static void addItem(MenuItem item) {
        if (findById(item.getItemId()) != null) {
            throw new IllegalArgumentException("Item ID '" + item.getItemId() + "' already exists!");
        }
        item.setItemName(Validator.normalizeName(item.getItemName()));
        item.setCategory(Validator.normalizeName(item.getCategory()));
        menuList.add(item);
    }

    public static void updateItem(MenuItem updatedItem) {
        if (updatedItem == null || updatedItem.getItemId() == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }

        MenuItem currentItem = findById(updatedItem.getItemId());
        if (currentItem == null) {
            throw new IllegalArgumentException("Item ID '" + updatedItem.getItemId() + "' not found!");
        }
        if (updatedItem.getItemName() == null || updatedItem.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be blank.");
        }
        if (updatedItem.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }

        currentItem.setItemName(Validator.normalizeName(updatedItem.getItemName()));
        currentItem.setCategory(Validator.normalizeName(updatedItem.getCategory()));
        currentItem.setPrice(updatedItem.getPrice());
        currentItem.setStatus(updatedItem.isStatus());
    }

    public static List<MenuItem> getMenuList() {
        return new ArrayList<>(menuList);
    }

    public static List<MenuItem> getAvailableItems() {
        List<MenuItem> availableItems = new ArrayList<>();
        for (MenuItem item : menuList) {
            if (item.isStatus()) {
                availableItems.add(item);
            }
        }
        return availableItems;
    }

    public static void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (MenuItem item : menuList) {
            lines.add(item.getItemId() + "|" + item.getItemName() + "|"
                    + item.getCategory() + "|" + item.getPrice() + "|" + item.isStatus());
        }
        FileHandler.writeFile("data/menu.txt", lines);
    }

    private static List<MenuItem> load() {
        List<MenuItem> items = new ArrayList<>();
        List<String> lines = FileHandler.readFile("data/menu.txt");
        for (String line : lines) {
            MenuItem item = parse(line);
            if (item == null) {
                System.out.println("Skip invalid line: " + line);
                continue;
            }
            items.add(item);
        }
        return items;
    }

    private static MenuItem parse(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length != 5) {
                return null;
            }
            return new MenuItem(parts[0].trim(), Validator.normalizeName(parts[1]), Validator.normalizeName(parts[2]),
                    Double.parseDouble(parts[3].trim()), Boolean.parseBoolean(parts[4].trim()));
        } catch (Exception e) {
            return null;
        }
    }
}
