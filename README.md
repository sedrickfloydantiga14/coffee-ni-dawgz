# 🐶☕ COFFEE NI DAWGZ — Offline Coffee Shop POS System

"Brewed for Good Dawgz 🐾"

A complete, production-grade, 100% offline Point-of-Sale (POS) system designed for coffee shops with a cute Golden Retriever mascot aesthetic, built in Java 21 and powered by a local SQLite database.

---

## 🚀 Features

* **100% Offline Operation**: Zero cloud or internet dependencies. All data is persisted locally in `coffee_ni_dawgz.db`.
* **Role-Based Authentication**:
  * **Admin**: Access to Sales Dashboard, Product CRUD & Size Pricing, Inventory Adjustments & Audit Logs, Employee Account Management, Sales Reports (Daily/Weekly/Monthly), Transaction History, System Settings (VAT & GCash), Database Backup & Restore.
  * **Cashier**: Visual Category Grid POS, Product Customization (Size, Temperature, Sugar %, Milk Choice, Extra Add-Ons), Shopping Cart, Senior/PWD/Student/Promo Discounts, 12% VAT calculation, Cash Payment (Auto-change calculation), GCash Payment (Offline manual verification flow), Receipt Generation (Printable & Digitally saved).
* **Product Customization**: Drink sizes (Small/Medium/Large), Temperature (Hot/Iced), Sugar Levels (0%-100%), Milk options, Extra Toppings/Shots.
* **Inventory Control & Low-Stock Alerts**: Product-level stock tracking with configurable low-stock threshold (`⚠ LOW STOCK`) and out-of-stock blocking (`🔴 OUT OF STOCK`). Automatic stock restoration on cancelled orders.
* **Receipts & Digital Copies**: Real-time receipt display, thermal printing support, digital copy auto-saved to `receipts/` directory.
* **Database Backup & Restore**: One-click database export/import for disaster recovery.

---

## 🔑 Default Login Credentials

| Role | Username | Default Password | Access Level |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full System & Management Access |
| **Cashier** | `cashier` | `cashier123` | POS Sales & Receipt Operations |

---

## 🛠️ Technology Stack

* **Language**: Java 21 (Oracle GraalVM JDK)
* **GUI Framework**: Java Swing with Custom Golden Retriever Theme & Java2D Mascot Graphics
* **Database**: SQLite (via `lib/sqlite-jdbc.jar`)
* **Security**: SHA-256 password hashing with salt
* **Architecture**: Clean layered architecture (`models`, `dao`, `services`, `ui`, `database`, `utils`)

---

## 📂 Project Structure

```
NewProject/
├── lib/
│   └── sqlite-jdbc.jar          # Local SQLite JDBC Driver
├── receipts/                    # Digitally saved order receipts
├── src/
│   └── com/coffeenidawgz/
│       ├── dao/                # Data Access Objects (User, Product, Order, Inventory, etc.)
│       ├── database/           # SQLite Schema & Seeder (DatabaseManager.java)
│       ├── models/             # Data Models (User, Product, Order, Item, AddOn, etc.)
│       ├── services/           # Business Logic Services (POSService, AuthService, BackupService, etc.)
│       ├── ui/                 # Swing GUI Panels & Dialogs (POSPanel, DashboardPanel, MainFrame, etc.)
│       ├── utils/              # Utilities (MascotIcon, PasswordHasher, ReceiptGenerator, etc.)
│       └── Main.java           # Main Entry Point
├── build.ps1                   # Compilation script
├── run.bat                     # Application launch script
├── coffee_ni_dawgz.db          # Local SQLite Database (auto-generated)
├── Gemini.md                   # System specifications document
└── README.md                   # Project documentation
```

## ⚙️ How to Build and Run

### 1. Launch via Executable (.EXE)
Double-click `CoffeeNiDawgz.exe` in the root folder to start the POS system immediately.

### 2. Package into Standalone Executables
Run the packaging script to generate the executable launcher, fat JAR, and standalone portable bundle with embedded JRE:
```powershell
.\package_exe.ps1
```

Generated Outputs:
- **`CoffeeNiDawgz.exe`**: Native Win32 launcher in root directory (silent background execution, no console popup).
- **`CoffeeNiDawgz.jar`**: Self-contained executable fat JAR.
- **`dist/CoffeeNiDawgz/CoffeeNiDawgz.exe`**: Portable Windows application directory bundled with embedded Java 21 runtime (runs on any Windows PC without Java pre-installed).

---

## 💡 Key POS Workflows

### ☕ Creating a Cash Sale Order
1. Login as `cashier` (`cashier123`).
2. Select product category tab (e.g. **COFFEE** or **ICED DRINKS**).
3. Click a product card (e.g. **Iced Latte**).
4. In the customization modal, select Size (**Large**), Sugar Level (**50%**), Milk (**Fresh Milk**), Add-Ons (**Extra Caramel**), and quantity. Click **ADD TO CART**.
5. Select a discount if applicable (e.g. **Senior Citizen 20%**).
6. Under Payment Method, select **CASH**.
7. Enter cash received amount (e.g., `500.00`).
8. Click **COMPLETE TRANSACTION 🐾**.
9. The transaction completes, inventory is deducted, and the **Receipt Dialog** pops up with options to **Print** or **Save Digitally**.

### 📱 Offline GCash Payments
1. In the POS checkout panel, select **GCASH**.
2. A prompt displays shop GCash Account Details (`0917-123-4567` / `COFFEE NI DAWGZ POS`).
3. After customer pays via GCash app, cashier verifies reference number on customer's device.
4. Cashier checks **"GCash Payment Verified Offline"** and clicks **COMPLETE TRANSACTION**.

### 💾 Backup & Restore Database
1. Login as `admin` (`admin123`).
2. Navigate to **Backup/Restore** sidebar menu.
3. Click **CREATE DATABASE BACKUP** to save a `.db` snapshot file.
4. To restore, click **RESTORE FROM BACKUP**, pick the `.db` file, and confirm.
