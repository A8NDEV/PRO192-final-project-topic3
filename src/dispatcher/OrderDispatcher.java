package dispatcher;

import business.OrderService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import models.MenuItem;
import models.Order;

public class OrderDispatcher {
    private final OrderService manager;
    private final List<MenuItem> menu;
    private final Scanner sc;

    // Tên: 1-50 ký tự, gồm chữ cái (có dấu), số, dấu cách đơn; phải có ít nhất 1 chữ cái
    private static final String NAME_REGEX =
            "^(?=.{1,50}$)(?=.*\\p{L})[\\p{L}\\p{M}\\p{N}]+(\\s[\\p{L}\\p{M}\\p{N}]+)*$";
    // SĐT: đúng 10 chữ số, bắt đầu bằng 0
    private static final String PHONE_REGEX = "^0\\d{9}$";
    // Bàn: [VIP hoặc 1 chữ cái]? + số. Ví dụ: 2, A1, VIP12 (đã in hoa)
    private static final String TABLE_REGEX = "^(VIP|[A-Z])?(\\d+)$";
    // Ngày: bắt buộc đủ dd/MM/yyyy (năm 4 số)
    private static final String DATE_REGEX = "^\\d{2}/\\d{2}/\\d{4}$";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // manager: dùng chung 1 đối tượng cho cả nhóm; menu: danh sách món từ phần Menu
    public OrderDispatcher(OrderService manager, List<MenuItem> menu, Scanner sc) {
        this.manager = manager;
        this.menu = menu;
        this.sc = sc;
    }

    public void run() {
        int choice;
        do {
            System.out.println("\n===== ORDER =====");
            System.out.println("1. Create new order for a table");
            System.out.println("2. Add item to existing order");
            System.out.println("3. Update item quantity");
            System.out.println("0. Back");
            choice = readInt("Choose: ");
            switch (choice) {
                case 1: createOrder(); break;
                case 2: addItem(); break;
                case 3: updateQuantity(); break;
                case 0: break;
                default: System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    // 1. Tạo đơn mới cho một bàn, có thể thêm món ngay sau đó
    private void createOrder() {
        String date = readDate("Order date (dd/MM/yyyy): ");
        String table = readTable("Table (e.g. 2, A1, VIP12): ");
        String name = readValid("Customer name: ", NAME_REGEX,
                "Name must be 1-50 characters, letters/numbers only, at least one letter. Example: anh Kien vip 123");
        String phone = readValid("Customer phone (10 digits, starts with 0): ", PHONE_REGEX,
                "Invalid phone. Must be 10 digits and start with 0. Example: 0901234567");

        Order order = manager.createOrder(date, table, name, phone);
        System.out.println("Created order " + order.getOrderId());

        String more = readLine("Add items now? (y/n): ");
        while (more.equalsIgnoreCase("y")) {
            addItemTo(order.getOrderId());
            more = readLine("Add another item? (y/n): ");
        }
    }

    // 2. Thêm món vào đơn đã có
    private void addItem() {
        String orderId = readLine("Order ID: ");
        addItemTo(orderId);
    }

    private void addItemTo(String orderId) {
        String itemId = readLine("Menu item ID: ");
        int qty = readInt("Quantity: ");
        try {
            manager.addItem(orderId, findMenuItem(itemId), qty);
            System.out.println("Item added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 3. Sửa số lượng món trong đơn
    private void updateQuantity() {
        String orderId = readLine("Order ID: ");
        String itemId = readLine("Menu item ID: ");
        int qty = readInt("New quantity: ");
        try {
            manager.updateQuantity(orderId, itemId, qty);
            System.out.println("Quantity updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private MenuItem findMenuItem(String itemId) {
        for (MenuItem m : menu) {
            if (m.getItemId().equalsIgnoreCase(itemId)) {
                return m;
            }
        }
        return null;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // Hỏi lại đến khi nhập đúng định dạng
    private String readValid(String prompt, String regex, String error) {
        while (true) {
            String s = readLine(prompt);
            if (s.matches(regex)) {
                return s;
            }
            System.out.println(error);
        }
    }

    // Bàn: tự in hoa, bỏ số 0 đứng đầu phần số, tối đa 5 ký tự (đếm sau khi sửa)
    private String readTable(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            Matcher m = Pattern.compile(TABLE_REGEX).matcher(raw.toUpperCase());
            if (!m.matches()) {
                System.out.println("Invalid table. Use number, 1 letter + number, or VIP + number. Examples: 2, A1, VIP12");
                continue;
            }
            String prefix = m.group(1) == null ? "" : m.group(1);
            String digits = m.group(2).replaceFirst("^0+", "");
            if (digits.isEmpty()) {
                System.out.println("Table number cannot be 0.");
                continue;
            }
            String table = prefix + digits;
            if (table.length() > 5) {
                System.out.println("Table is too long (max 5 characters). Example: VIP12, A1234");
                continue;
            }
            if (!table.equals(raw)) {
                System.out.println("Table set to " + table);
            }
            return table;
        }
    }

    // Ngày phải đủ dd/MM/yyyy và là ngày có thật (không nhận 31/02/2026)
    private String readDate(String prompt) {
        while (true) {
            String s = readValid(prompt, DATE_REGEX,
                    "Date must be full dd/MM/yyyy. Example: 26/10/2026");
            try {
                LocalDate.parse(s, DATE_FMT);
                return s;
            } catch (DateTimeParseException e) {
                System.out.println("That date does not exist. Example: 26/10/2026");
            }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
}