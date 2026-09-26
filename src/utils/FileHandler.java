/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import models.MenuItem;

/**
 *
 * @author ASUS
 */
public class FileHandler {

    public static void saveMenuToFile(List<MenuItem> menuList, String filePath) throws IOException {
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (MenuItem item : menuList) {
                writer.write(item.getItemId() + "|" + item.getItemName() + "|"
                        + item.getCategory() + "|" + item.getPrice() + "|" + item.isStatus());
                writer.newLine();
            }
        }
    }

    public static List<MenuItem> loadMenuFromFile(String filePath) {
        List<MenuItem> menuList = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split("\\|");
                    if (parts.length != 5) {
                        System.out.println("Skipping invalid line: " + line);
                        continue;
                    }
                    String id = parts[0];
                    String name = parts[1];
                    String category = parts[2];
                    double price = Double.parseDouble(parts[3]);
                    boolean status = Boolean.parseBoolean(parts[4]);
                    menuList.add(new MenuItem(id, name, category, price, status));
                } catch (Exception e) {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            // File chưa tồn tại (lần đầu chạy chương trình) -> trả về danh sách rỗng
        } catch (IOException e) {
            // Lỗi đọc file khác
        }
        return menuList;
    }
}
