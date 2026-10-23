package repository;

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
/**
 * Thao tác trực tiếp với dữ liệu menu: giữ danh sách món trong bộ nhớ và
 * đọc/ghi file menu.txt.
 */
public class MenuRepository {

    private final String filePath;
    private final List<MenuItem> menuList;

    public MenuRepository(String filePath) {
        this.filePath = filePath;
        this.menuList = load();
    }

    public List<MenuItem> findAll() {
        return new ArrayList<>(menuList);
    }

    public void add(MenuItem item) {
        menuList.add(item);
    }

    public void save() throws IOException {
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (MenuItem item : menuList) {
                writer.write(item.getItemId() + "|" + item.getItemName() + "|"
                        + item.getCategory() + "|" + item.getPrice() + "|" + item.isStatus());
                writer.newLine();
            }
        }
    }

    private List<MenuItem> load() {
        List<MenuItem> items = new ArrayList<>();
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
                    items.add(new MenuItem(id, name, category, price, status));
                } catch (Exception e) {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            // File chưa tồn tại (lần đầu chạy chương trình) -> trả về danh sách rỗng
        } catch (IOException e) {
            // Lỗi đọc file khác
        }
        return items;
    }
    
    public boolean delete(String id){
        if(id == null){
            return false;
        }
        for(int i = 0; i < menuList.size(); i++){
            if(menuList.get(i).getItemId().equalsIgnoreCase(id.trim())){
                menuList.remove(i);
                return true;
            }
        }
        return false;
    }
}
