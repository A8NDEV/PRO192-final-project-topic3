# Kiến trúc dự án Table-side Ordering

Dự án dùng kiến trúc nhiều tầng: **Model – View – Controller – Service – Repository**, cộng thêm `exception` và `utils` dùng chung. Mỗi tầng chỉ làm đúng việc của mình để khi có lỗi biết ngay phải tìm ở đâu.

## 1. Vai trò từng package

| Package | Chứa | Được làm | Không được làm |
|---|---|---|---|
| `models` | `MenuItem`, `Order`, `OrderItem`, `OrderStatus` | Thuộc tính, constructor, getter/setter, hàm gắn với dữ liệu của chính nó | Dùng `Scanner`, `System.out`, gọi class ở package khác |
| `view` | `ConsoleView`, `MenuView` | In menu, thông báo, lỗi, dữ liệu ra console | Đọc input, gọi service hoặc repository, xử lý logic |
| `controller` | `AppController`, `MenuController`, `OrderController` | Nhận lựa chọn của người dùng, đọc input, validate bằng `utils`, gọi service, bắt exception, gọi view | Tự viết logic nghiệp vụ |
| `service` | `MenuService`, `OrderService` | Toàn bộ logic nghiệp vụ, kiểm tra quy định, ném exception khi vi phạm | Dùng `Scanner`, `System.out` |
| `repository` | `MenuRepository` | Giữ dữ liệu, thêm/tìm/lấy tất cả, đọc/ghi file | Kiểm tra quy định nghiệp vụ |
| `exception` | `BusinessException`, `DuplicateItemException` | Mỗi trường hợp lỗi là một class riêng, mang sẵn nội dung thông báo | Chứa logic xử lý |
| `utils` | `Validator`, `InputHandler` | Hàm dùng chung cho cả dự án (kiểm tra, đọc input) | Chứa nghiệp vụ riêng của một chức năng |

## 2. Quy tắc gọi nhau

```
Main → controller → service → repository → models
           ↓
         view
```

- Chỉ được gọi **theo chiều mũi tên**, không gọi ngược lại.
- `models` không gọi ai cả.
- `service` không biết `view` tồn tại. `view` không gọi `service` hay `repository`.
- `Main` chỉ làm một việc: tạo các đối tượng và nối chúng lại với nhau (repository → service → controller), không chứa logic.
- `exception` và `utils` được mọi tầng dùng, nhưng không gọi ngược lên `controller`, `service`, `view`.

## 3. Luồng một chức năng

Ví dụ **thêm món vào menu**:

```
1. MenuController   : đọc id, tên, loại, giá, trạng thái (qua InputHandler/Validator)
2. MenuService      : kiểm tra trùng ID, ném DuplicateItemException nếu trùng
3. MenuRepository   : thêm món vào danh sách, ghi ra data/menu.txt
4. ConsoleView      : in "Item added successfully!" hoặc in lỗi
```

## 4. Xử lý lỗi

| Loại lỗi | Ném ở đâu | Bắt ở đâu | In bằng |
|---|---|---|---|
| Lỗi nghiệp vụ (trùng ID, không tìm thấy...) | `service`, ném class con của `BusinessException` (unchecked, không cần `throws`) | `controller` | `ConsoleView.showError(e.getMessage())` |
| Lỗi đọc/ghi file | `repository` (`IOException`, checked) | `controller` | `ConsoleView.showMessage(...)` |
| Nhập sai định dạng | `Validator`/`InputHandler` nhắc nhập lại | tại chỗ | thông báo ngay khi nhập |

Nội dung thông báo lỗi nghiệp vụ nằm trong class exception (ví dụ `DuplicateItemException` tự ghép câu từ `itemId`). View chỉ in chuỗi được truyền vào, không biết lỗi là gì.

## 5. Hiện trạng và ngoại lệ đã biết

Phần **Menu** đã tách theo đúng các tầng trên. Các chỗ chưa đúng quy tắc:

- `utils/InputHandler` tự `System.out` khi nhắc nhập và báo lỗi nhập.
- `repository/MenuRepository` tự `System.out` dòng `Skipping invalid line` khi đọc file menu hỏng.
- Phần **Order** mới chỉ được chuyển vào đúng package, chưa tách:
  - `OrderController` còn chứa cả phần in, kiểm tra input (regex tên, SĐT, bàn, ngày) và tìm món trong menu.
  - `OrderService` còn tự giữ danh sách đơn trong bộ nhớ (chưa có `OrderRepository`) và còn ném `IllegalArgumentException` thay vì exception riêng.
  - Đơn hàng chưa được đọc/ghi ra `data/orders.txt`.

Khi tách Order, lấy phần Menu làm mẫu: `MenuController`, `ConsoleView`/`MenuView`, `MenuService`, `MenuRepository`, `DuplicateItemException`.

## 6. Quy ước chung

- Package viết thường: `models`, `service`, `controller`...
- Class viết hoa chữ cái đầu mỗi từ; hậu tố theo tầng: `...Controller`, `...Service`, `...Repository`, `...View`, `...Exception`.
- Biến và hàm theo `camelCase`; hằng số và giá trị enum viết HOA.
- File do ai viết thì ghi `@author` của người đó ở đầu class để nhận biết.
- Môi trường: NetBeans 13, JDK 1.8. Không dùng cú pháp từ Java 9 trở lên (`var`, `List.of()`...).
