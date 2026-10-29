package business;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import models.MenuItem;

/**
 *
 * @author ASUS
 */
/**
 * Thao tác trực tiếp với dữ liệu menu: giữ danh sách món trong bộ nhớ và
 * đọc/ghi file menu.txt theo chuẩn Java NIO và bảng mã UTF-8.
 */
public class MenuRepository {

    private static final String SEP = "\\|";
    private static final int FIELD_COUNT = 5;

    private final Path filePath;
    private final List<MenuItem> menuList;

    public MenuRepository(String filePath) {
        this.filePath = Paths.get(filePath);
        ensureFileExists();
        this.menuList = load();
    }

    public List<MenuItem> findAll() {
        return new ArrayList<>(menuList);
    }

    public void add(MenuItem item) {
        menuList.add(item);
    }

    public boolean update(MenuItem updatedItem) {
        if (updatedItem == null || updatedItem.getItemId() == null) {
            return false;
        }
        for (MenuItem item : menuList) {
            if (item.getItemId().equalsIgnoreCase(updatedItem.getItemId().trim())) {
                item.setItemName(updatedItem.getItemName());
                item.setCategory(updatedItem.getCategory());
                item.setPrice(updatedItem.getPrice());
                item.setStatus(updatedItem.isStatus());
                return true;
            }
        }
        return false;
    }

    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getItemId().equalsIgnoreCase(id.trim())) {
                menuList.remove(i);
                return true;
            }
        }
        return false;
    }

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

    private MenuItem parse(String line) {
        try {
            String[] parts = line.split(SEP);
            if (parts.length != FIELD_COUNT) {
                return null;
            }
            String id = parts[0];
            String name = parts[1];
            String category = parts[2];
            double price = Double.parseDouble(parts[3]);
            boolean status = Boolean.parseBoolean(parts[4]);
            return new MenuItem(id, name, category, price, status);
        } catch (Exception e) {
            return null;
        }
    }
}
