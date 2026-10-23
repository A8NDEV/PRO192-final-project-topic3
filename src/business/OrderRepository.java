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
import models.Order;
import models.OrderItem;
import models.OrderStatus;


public class OrderRepository {

    private static final String SEP = "\\|";
    private static final int FIELD_COUNT = 7;

    private final Path filePath;
    private final MenuRepository menuRepository;
    private final List<Order> orders = new ArrayList<>();
    private int counter = 1;

    public OrderRepository(String filePath, MenuRepository menuRepository) {
        this.filePath = Paths.get(filePath);
        this.menuRepository = menuRepository;
        ensureFileExists();
        load();
    }

    public String nextId() {
        return String.format("O%04d", counter++);
    }

    public void add(Order order) {
        orders.add(order);
    }

    public Order findById(String orderId) {
        for (Order o : orders) {
            if (o.getOrderId().equalsIgnoreCase(orderId)) {
                return o;
            }
        }
        return null;
    }
    
    public boolean deleteById(String orderId) {
        Order order = findById(orderId);
            if (order == null) {
                 return false;
    }

    orders.remove(order);
    return true;
}


    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }

    public void save() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (Order order : orders) {
                writer.write(format(order));
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
            System.out.println("Cannot create orders file: " + e.getMessage());
        }
    }

    private void load() {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                Order order = parse(line);
                if (order == null) {
                    System.out.println("Skipping invalid order line: " + line);
                    continue;
                }
                orders.add(order);
                updateCounter(order.getOrderId());
            }
        } catch (IOException e) {
            System.out.println("Cannot read orders file: " + e.getMessage());
        }
    }

    private String format(Order order) {
        StringBuilder items = new StringBuilder();
        for (OrderItem oi : order.getItemsList()) {
            if (items.length() > 0) {
                items.append(",");
            }
            items.append(oi.getMenuItem().getItemId()).append(":").append(oi.getQuantity());
        }
        return String.join("|", order.getOrderId(), order.getOrderDate(), order.getTableNumber(),
                order.getCustomerName(), order.getCustomerPhone(), order.getStatus().name(),
                items.toString());
    }

    private Order parse(String line) {
        try {
            String[] p = line.split(SEP, -1);
            if (p.length != FIELD_COUNT) {
                return null;
            }
            Order order = new Order(p[0], p[1], p[2], p[3], p[4]);
            order.setStatus(OrderStatus.valueOf(p[5]));
            if (!p[6].isEmpty()) {
                for (String entry : p[6].split(",")) {
                    String[] kv = entry.split(":");
                    MenuItem menuItem = findMenuItem(kv[0]);
                    if (menuItem == null) {
                        System.out.println("Skipping item " + kv[0] + " in order " + p[0]
                                + ": not in menu.");
                        continue;
                    }
                    order.getItemsList().add(new OrderItem(menuItem, Integer.parseInt(kv[1])));
                }
            }
            return order;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private MenuItem findMenuItem(String itemId) {
        for (MenuItem m : menuRepository.findAll()) {
            if (m.getItemId().equalsIgnoreCase(itemId)) {
                return m;
            }
        }
        return null;
    }

    private void updateCounter(String orderId) {
        try {
            counter = Math.max(counter, Integer.parseInt(orderId.substring(1)) + 1);
        } catch (RuntimeException e) {

        }
    }
}