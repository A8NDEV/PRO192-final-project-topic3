# Kiến trúc dự án Table-side Ordering

Dự án được tổ chức theo chuẩn kiến trúc 4 packages bắt buộc: **models**, **business**, **dispatcher**, **utils** (cùng class khởi chạy `Main` ở default package).

## 1. Vai trò từng package

| Package | Chứa | Được làm | Không được làm |
|---|---|---|---|
| `models` | `MenuItem`, `Order`, `OrderItem`, `OrderStatus` | Thuộc tính, constructor, getter/setter, hàm gắn với dữ liệu của chính nó | Dùng `Scanner`, `System.out`, gọi class ở package khác |
| `business` | `MenuService`, `OrderService`, `MenuRepository`, `OrderRepository`, `BusinessException`, `DuplicateItemException`, `ItemNotFoundException` | Xử lý logic nghiệp vụ, quản lý danh sách, đọc/ghi file `data/*.txt`, ném các exception nghiệp vụ | Dùng `Scanner`, gọi trực tiếp `dispatcher` hay `view` |
| `dispatcher` | `AppDispatcher`, `MenuDispatcher`, `OrderDispatcher`, `ConsoleView`, `MenuView`, `OrderView` | Điều phối tương tác: in menu, nhận lựa chọn người dùng, đọc input qua `utils`, gọi `business`, bắt exception, hiển thị ra console | Tự xử lý logic tính toán nghiệp vụ hay đọc/ghi file trực tiếp |
| `utils` | `Validator`, `InputHandler` | Hàm tiện ích dùng chung cho cả dự án (kiểm tra hợp lệ dữ liệu, đọc input từ `Scanner`) | Chứa nghiệp vụ riêng của một chức năng |

## 2. Quy tắc gọi nhau

```
Main → dispatcher (AppDispatcher, MenuDispatcher, OrderDispatcher, Views)
           ↓
       business (Services, Repositories, Exceptions)
           ↓
        models (MenuItem, Order, OrderItem, OrderStatus)
```
*(Tầng `utils` được `dispatcher` và `business` sử dụng chung, không gọi ngược lên).*

- Chỉ được gọi **theo chiều mũi tên**, không gọi ngược lại.
- `models` không phụ thuộc vào ai.
- `business` không biết `dispatcher` tồn tại.
- `Main` chỉ làm một việc: tạo các đối tượng và nối (wiring) chúng lại với nhau (repository → service → dispatcher), sau đó gọi `appDispatcher.run()`.

## 3. Luồng một chức năng

Ví dụ **thêm món vào menu**:

```
1. MenuDispatcher   : đọc id, tên, loại, giá, trạng thái (qua InputHandler/Validator)
2. MenuService      : kiểm tra trùng ID, ném DuplicateItemException nếu trùng
3. MenuRepository   : thêm món vào danh sách, ghi ra data/menu.txt
4. ConsoleView      : in "Item added successfully!" hoặc in lỗi
```

## 4. Xử lý lỗi

| Loại lỗi | Ném ở đâu | Bắt ở đâu | In bằng |
|---|---|---|---|
| Lỗi nghiệp vụ (trùng ID, không tìm thấy...) | `business` (Service), ném class con của `BusinessException` | `dispatcher` (Dispatcher) | `console.showError(e.getMessage())` |
| Lỗi đọc/ghi file | `business` (Repository, `IOException`) | `dispatcher` (Dispatcher) | `console.showMessage(...)` |
| Nhập sai định dạng | `utils` (`Validator`/`InputHandler`) | tại chỗ | thông báo ngay khi nhập |

## 5. Quy ước chung

- 4 Packages chính viết thường: `models`, `business`, `dispatcher`, `utils`.
- Hậu tố class rõ ràng: `...Dispatcher`, `...Service`, `...Repository`, `...View`, `...Exception`.
- Biến và hàm theo `camelCase`; hằng số và giá trị enum viết HOA.
- File do ai viết thì ghi `@author` của người đó ở đầu class để nhận biết.
- Môi trường: NetBeans 13, JDK 1.8. Không dùng cú pháp từ Java 9 trở lên (`var`, `List.of()`...).
