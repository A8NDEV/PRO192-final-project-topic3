package business;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.MenuItem;

/**
 * Quản lý menu: giữ danh sách món trong bộ nhớ, xử lý nghiệp vụ (kiểm tra trùng
 * ID, thêm, sửa, tìm kiếm) và đọc/ghi file menu.txt theo bảng mã UTF-8.
 */
public class MenuManager {

    private static final String SEP = "\\|";
    private static final int FIELD_COUNT = 5;

    private final Path filePath;
    private final List<MenuItem> menuList;

    public MenuManager(String filePath) {
        this.filePath = Paths.get(filePath);
        ensureFileExists();
        this.menuList = load();
    }

    // ===== Nghiệp vụ =====

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
            if (item.getItemName() != null && item.getItemName().toLowerCase().contains(lowerName)) {
                result.add(item);
            }
        }
        return result;
    }

    public List<MenuItem> getAvailableItems() {
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menuList) {
            if (item.isStatus()) {
                result.add(item);
            }
        }
        return result;
    }

    // Trả về bản sao của danh sách để bên ngoài không thêm/xóa trực tiếp được.
    public List<MenuItem> getMenuList() {
        return new ArrayList<>(menuList);
    }

    public void addItem(MenuItem item) {
        if (findById(item.getItemId()) != null) {
            throw new IllegalArgumentException("Item ID '" + item.getItemId() + "' already exists!");
        }
        menuList.add(item);
    }

    public void updateItem(MenuItem updatedItem) {
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

        currentItem.setItemName(updatedItem.getItemName());
        currentItem.setCategory(updatedItem.getCategory());
        currentItem.setPrice(updatedItem.getPrice());
        currentItem.setStatus(updatedItem.isStatus());
    }

    // ===== Đọc/ghi file =====

    public void save() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (MenuItem item : menuList) {
                writer.write(item.getItemId() + "|" + item.getItemName() + "|"
                        + item.getCategory() + "|" + item.getPrice() + "|" + item.isStatus());
                writer.newLine();
            }
        }
    }

    private void ensureFileExists() {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            System.out.println("Cannot create menu file: " + e.getMessage());
        }
    }

    private List<MenuItem> load() {
        List<MenuItem> items = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                MenuItem item = parse(line);
                if (item == null) {
                    System.out.println("Skipping invalid menu line: " + line);
                    continue;
                }
                items.add(item);
            }
        } catch (IOException e) {
            System.out.println("Cannot read menu file: " + e.getMessage());
        }
        return items;
    }

    // 1 dòng text -> MenuItem (trả về null nếu dòng sai định dạng)
    private MenuItem parse(String line) {
        try {
            String[] parts = line.split(SEP);
            if (parts.length != FIELD_COUNT) {
                return null;
            }
            return new MenuItem(parts[0], parts[1], parts[2],
                    Double.parseDouble(parts[3]), Boolean.parseBoolean(parts[4]));
        } catch (Exception e) {
            return null;
        }
    }
}
