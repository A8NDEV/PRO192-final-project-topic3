# PRO192 – Object-Oriented Programming with Java
## Final Project: Table-side Ordering Management System

A desktop console-based Java application to manage restaurant menu items and table-side ordering, implementing Core Java, Object-Oriented Programming (OOP), Collections Framework, File I/O, and robust Exception Handling.

---

## 📌 Project Overview

### 1. Objectives
- Apply **Object-Oriented Programming (OOP)**: Encapsulation, inheritance, polymorphism, abstract classes, and interfaces.
- Manage in-memory data using the **Java Collections Framework** (`ArrayList`, `List`, `Map`).
- Implement file persistence (`.txt` reading/writing) using Java I/O (`BufferedReader`, `BufferedWriter`, `Scanner`).
- Implement **Exception Handling** (`try-catch-finally`, custom exceptions) and input validation.
- Build an interactive, user-friendly **Console-Based User Interface (CLI)**.

### 2. Group Guidelines & Deliverables
- **Group Size:** 3–4 students
- **Duration:** 4–6 weeks
- **Environment:** Apache NetBeans 13 / JDK 1.8
- **Deliverables:**
  - Complete NetBeans Java Project Source Code
  - Technical Report (`REPORT.pdf`)
  - Presentation Slides (`SLIDES.pptx`)
- **Submission:** Via LMS as directed by the instructor.

---

## ⚙️ Functional Requirements

### 1. Menu Management
Each `MenuItem` contains:
- `itemId`: String (e.g., `M001`, `M002`, `M003`...) — *Unique identifier*
- `itemName`: String (e.g., *Fried Rice*, *Beef Steak*, *Chicken Salad*, *Pizza*...)
- `category`: String (*Main Course*, *Appetizer*, *Dessert*, *Beverage*...)
- `price`: double / long (e.g., `50000`, `80000`, `120000` VND)
- `status`: boolean (`true`: Available, `false`: Unavailable)

**Key Features:**
- [x] Add a new item to the menu (with unique `itemId` validation).
- [x] Update existing menu item information by `itemId`.
- [x] Search for a menu item by `itemId` or `itemName`.
- [x] Display all available menu items (`status == true`).

---

### 2. Table-side Order Management
Each `Order` contains:
- `orderId`: String (e.g., `O001`, `O002`, `O003`...) — *Unique identifier*
- `orderDate`: String / LocalDate (e.g., `26/10/2026`)
- `tableNumber`: int / String (e.g., `2`)
- `customerName`: String (e.g., *Nguyen Van An*)
- `customerPhone`: String (e.g., *0901234567*)
- `itemsList`: `List<OrderItem>` (holds `MenuItem` reference and ordered `quantity`)
- `total`: double (dynamically calculated: $\sum (\text{price} \times \text{quantity})$)
- `status`: String / Enum (`Pending`, `Preparing`, `Completed`, `Cancelled`)

**Key Features:**
- [x] Create a new order for a specific table and add items with quantities.
- [x] Update the quantity of ordered items in an existing order.
- [x] Search orders by `tableNumber` or `orderId`.
- [x] Sort orders by total amount in descending order (via `Comparable` / `Comparator`).
- [x] Display menu items that currently have zero orders.

---

### 3. File Handling & Persistence
- Automatically load menu data and order data from `.txt` files on startup.
- Save menu and order data back to text files when changes occur or on exit.
- Safely handle missing files (`FileNotFoundException`) and corrupt file lines (`IOException`).

---

## 🏗️ Project Architecture & Package Structure

The project follows a standard multi-layer package structure in NetBeans:

```text
TableOrderingSystem/
├── src/
│   ├── models/
│   │   ├── MenuItem.java
│   │   ├── Order.java
│   │   └── OrderItem.java
│   │
│   ├── business/
│   │   ├── MenuManager.java
│   │   └── OrderManager.java
│   │
│   ├── dispatcher/
│   │   ├── MenuDispatcher.java
│   │   └── AppController.java
│   │
│   ├── utils/
│   │   ├── FileHandler.java
│   │   ├── InputValidator.java
│   │   └── DataFormatter.java
│   │
│   └── Main.java
│
├── data/
│   ├── menu.txt
│   └── orders.txt
│
├── nbproject/
├── build.xml
└── README.md
