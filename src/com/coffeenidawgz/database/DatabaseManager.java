package com.coffeenidawgz.database;

import com.coffeenidawgz.utils.PasswordHasher;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:coffee_ni_dawgz.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " + // ADMIN or CASHIER
                    "full_name TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'ACTIVE', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // 2. categories table
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'ACTIVE'" +
                    ");");

            // 3. products table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "category_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT, " +
                    "small_price REAL DEFAULT 0, " +
                    "medium_price REAL DEFAULT 0, " +
                    "large_price REAL DEFAULT 0, " +
                    "stock_quantity INTEGER DEFAULT 0, " +
                    "low_stock_threshold INTEGER DEFAULT 5, " +
                    "image_path TEXT, " +
                    "status TEXT NOT NULL DEFAULT 'ACTIVE', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(category_id) REFERENCES categories(id)" +
                    ");");

            // Migration check: ensure image_path column exists in products table
            try {
                stmt.execute("ALTER TABLE products ADD COLUMN image_path TEXT;");
            } catch (SQLException ignored) {
                // Column already exists
            }

            // 4. orders table
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_number TEXT UNIQUE NOT NULL, " +
                    "cashier_id INTEGER NOT NULL, " +
                    "subtotal REAL NOT NULL, " +
                    "discount_amount REAL DEFAULT 0, " +
                    "discount_name TEXT, " +
                    "vat_amount REAL NOT NULL, " +
                    "total REAL NOT NULL, " +
                    "payment_method TEXT NOT NULL, " + // CASH, GCASH
                    "amount_received REAL NOT NULL, " +
                    "change_amount REAL NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'Completed', " + // Pending, Preparing, Completed, Cancelled
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(cashier_id) REFERENCES users(id)" +
                    ");");

            // 5. order_items table
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "product_name TEXT NOT NULL, " +
                    "size TEXT NOT NULL, " +
                    "temperature TEXT NOT NULL, " +
                    "sugar_level TEXT NOT NULL, " +
                    "milk_option TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "unit_price REAL NOT NULL, " +
                    "total_price REAL NOT NULL, " +
                    "FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(product_id) REFERENCES products(id)" +
                    ");");

            // 6. add_ons table
            stmt.execute("CREATE TABLE IF NOT EXISTS add_ons (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'ACTIVE'" +
                    ");");

            // 7. order_item_addons table
            stmt.execute("CREATE TABLE IF NOT EXISTS order_item_addons (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_item_id INTEGER NOT NULL, " +
                    "addon_id INTEGER NOT NULL, " +
                    "addon_name TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "FOREIGN KEY(order_item_id) REFERENCES order_items(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(addon_id) REFERENCES add_ons(id)" +
                    ");");

            // 8. inventory_logs table
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER NOT NULL, " +
                    "quantity_changed INTEGER NOT NULL, " +
                    "previous_quantity INTEGER NOT NULL, " +
                    "new_quantity INTEGER NOT NULL, " +
                    "reason TEXT NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(product_id) REFERENCES products(id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");");

            // 9. discounts table
            stmt.execute("CREATE TABLE IF NOT EXISTS discounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL, " +
                    "type TEXT NOT NULL, " + // PERCENTAGE or FIXED
                    "value REAL NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'ACTIVE'" +
                    ");");

            // 10. settings table
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "setting_key TEXT UNIQUE NOT NULL, " +
                    "setting_value TEXT NOT NULL" +
                    ");");

            // Seed initial data if empty
            seedInitialData(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Seed Users
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String adminPass = PasswordHasher.hashPassword("admin123");
                String cashierPass = PasswordHasher.hashPassword("cashier123");

                String sqlUser = "INSERT INTO users (username, password_hash, role, full_name, status) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUser)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, adminPass);
                    pstmt.setString(3, "ADMIN");
                    pstmt.setString(4, "Admin Dawg");
                    pstmt.setString(5, "ACTIVE");
                    pstmt.executeUpdate();

                    pstmt.setString(1, "cashier");
                    pstmt.setString(2, cashierPass);
                    pstmt.setString(3, "CASHIER");
                    pstmt.setString(4, "Juan Cashier");
                    pstmt.setString(5, "ACTIVE");
                    pstmt.executeUpdate();
                }
            }
        }

        // Seed Categories
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String[] cats = {"COFFEE", "NON-COFFEE", "ICED DRINKS", "HOT DRINKS", "SNACKS", "ADD-ONS"};
                String sql = "INSERT INTO categories (name) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (String cat : cats) {
                        pstmt.setString(1, cat);
                        pstmt.executeUpdate();
                    }
                }
            }
        }

        // Seed Products
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Get category IDs
                int catCoffee = getCategoryIdByName(conn, "COFFEE");
                int catNonCoffee = getCategoryIdByName(conn, "NON-COFFEE");
                int catIced = getCategoryIdByName(conn, "ICED DRINKS");
                int catHot = getCategoryIdByName(conn, "HOT DRINKS");
                int catSnacks = getCategoryIdByName(conn, "SNACKS");

                String sqlP = "INSERT INTO products (category_id, name, description, small_price, medium_price, large_price, stock_quantity, low_stock_threshold, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlP)) {
                    // Americano
                    insertProduct(pstmt, catCoffee, "Americano", "Classic rich espresso diluted with hot water", 80, 100, 120, 30, 5, "images/Americano.jpg");
                    // Latte
                    insertProduct(pstmt, catCoffee, "Latte", "Smooth espresso with steamed milk and light foam", 90, 110, 130, 25, 5, "images/Latte.jpg");
                    // Cappuccino
                    insertProduct(pstmt, catCoffee, "Cappuccino", "Espresso topped with thick creamy milk foam", 90, 110, 130, 20, 5, "images/Latte.jpg");
                    // Mocha
                    insertProduct(pstmt, catCoffee, "Mocha", "Rich espresso blended with sweet chocolate & milk", 100, 120, 140, 18, 5, "images/Mocha.jpg");
                    // Spanish Latte
                    insertProduct(pstmt, catCoffee, "Spanish Latte", "Espresso with condensed milk & creamy texture", 105, 125, 145, 22, 5, "images/Latte.jpg");

                    // Iced Drinks
                    insertProduct(pstmt, catIced, "Iced Latte", "Chilled espresso with cold milk over ice", 95, 115, 135, 25, 5, "images/Latte.jpg");
                    insertProduct(pstmt, catIced, "Iced Mocha", "Rich chocolate espresso served cold", 105, 125, 145, 20, 5, "images/IcedMocha.jpg");
                    insertProduct(pstmt, catIced, "Iced Americano", "Refreshing cold espresso over ice", 85, 105, 125, 35, 5, "images/Americano.jpg");

                    // Hot Drinks
                    insertProduct(pstmt, catHot, "Hot Latte", "Warm espresso with silky steamed milk", 90, 110, 130, 20, 5, "images/Latte.jpg");
                    insertProduct(pstmt, catHot, "Hot Chocolate", "Rich warm Belgian cocoa drink", 85, 105, 125, 15, 5, "images/Hotchocolate.jpg");
                    insertProduct(pstmt, catHot, "Hot Americano", "Classic warm espresso brew", 80, 100, 120, 25, 5, "images/Americano.jpg");

                    // Non-Coffee
                    insertProduct(pstmt, catNonCoffee, "Chocolate", "Creamy chocolate drink served hot or iced", 85, 105, 125, 18, 5, "images/Hotchocolate.jpg");
                    insertProduct(pstmt, catNonCoffee, "Matcha Latte", "Premium Uji matcha blended with fresh milk", 110, 130, 150, 15, 5, "images/Matchalatte.jpg");
                    insertProduct(pstmt, catNonCoffee, "Milk Tea", "Brewed black tea with sweet creamy milk", 80, 100, 120, 20, 5, "images/Matchalatte.jpg");

                    // Snacks
                    insertProduct(pstmt, catSnacks, "Chocolate Cookie", "Freshly baked chewy chocolate chip cookie", 50, 50, 50, 15, 5, "images/Fudgebrownies.jpg");
                    insertProduct(pstmt, catSnacks, "Fudge Brownie", "Decadent dark chocolate brownie slice", 65, 65, 65, 12, 5, "images/Fudgebrownies.jpg");
                }
            }
        }

        // Migrate existing NULL image_paths for default products if needed
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE products SET image_path = 'images/Americano.jpg' WHERE (image_path IS NULL OR image_path = '') AND LOWER(name) LIKE '%americano%'");
            stmt.executeUpdate("UPDATE products SET image_path = 'images/IcedMocha.jpg' WHERE (image_path IS NULL OR image_path = '') AND LOWER(name) LIKE '%iced mocha%'");
            stmt.executeUpdate("UPDATE products SET image_path = 'images/Mocha.jpg' WHERE (image_path IS NULL OR image_path = '') AND LOWER(name) LIKE '%mocha%' AND LOWER(name) NOT LIKE '%iced%'");
            stmt.executeUpdate("UPDATE products SET image_path = 'images/Latte.jpg' WHERE (image_path IS NULL OR image_path = '') AND (LOWER(name) LIKE '%latte%' OR LOWER(name) LIKE '%cappuccino%') AND LOWER(name) NOT LIKE '%matcha%'");
            stmt.executeUpdate("UPDATE products SET image_path = 'images/Matchalatte.jpg' WHERE (image_path IS NULL OR image_path = '') AND (LOWER(name) LIKE '%matcha%' OR LOWER(name) LIKE '%tea%')");
            stmt.executeUpdate("UPDATE products SET image_path = 'images/Hotchocolate.jpg' WHERE (image_path IS NULL OR image_path = '') AND (LOWER(name) LIKE '%chocolate%' OR LOWER(name) LIKE '%cocoa%') AND LOWER(name) NOT LIKE '%cookie%'");
            stmt.executeUpdate("UPDATE products SET image_path = 'images/Fudgebrownies.jpg' WHERE (image_path IS NULL OR image_path = '') AND (LOWER(name) LIKE '%brownie%' OR LOWER(name) LIKE '%cookie%')");

            // Remove products that have no dedicated image in the images folder
            stmt.executeUpdate("UPDATE products SET status = 'INACTIVE' WHERE name = 'Blueberry Cake'");
            stmt.executeUpdate("UPDATE products SET status = 'INACTIVE' WHERE name = 'Ham & Cheese Sandwich'");
        } catch (SQLException ignored) {}

        // Seed Add-Ons
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM add_ons")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sqlA = "INSERT INTO add_ons (name, price) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlA)) {
                    insertAddOn(pstmt, "Extra Shot", 25.0);
                    insertAddOn(pstmt, "Extra Syrup", 15.0);
                    insertAddOn(pstmt, "Whipped Cream", 20.0);
                    insertAddOn(pstmt, "Extra Milk", 15.0);
                }
            }
        }

        // Seed Discounts
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM discounts")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sqlD = "INSERT INTO discounts (name, type, value) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlD)) {
                    insertDiscount(pstmt, "Senior Citizen (20%)", "PERCENTAGE", 20.0);
                    insertDiscount(pstmt, "PWD (20%)", "PERCENTAGE", 20.0);
                    insertDiscount(pstmt, "Student (10%)", "PERCENTAGE", 10.0);
                    insertDiscount(pstmt, "Promo (₱50 Off)", "FIXED", 50.0);
                }
            }
        }

        // Seed Settings
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM settings")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sqlS = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlS)) {
                    insertSetting(pstmt, "SHOP_NAME", "COFFEE NI DAWGZ");
                    insertSetting(pstmt, "SHOP_SLOGAN", "Brewed for Good Dawgz 🐾");
                    insertSetting(pstmt, "VAT_RATE", "12.0");
                    insertSetting(pstmt, "GCASH_NUMBER", "0917-123-4567");
                    insertSetting(pstmt, "GCASH_NAME", "COFFEE NI DAWGZ POS");
                    insertSetting(pstmt, "LOW_STOCK_THRESHOLD", "5");
                    insertSetting(pstmt, "RECEIPT_HEADER", "Welcome to Coffee ni Dawgz!\n123 Paws Street, Dogtown");
                    insertSetting(pstmt, "RECEIPT_FOOTER", "THANK YOU, DAWG! 🐾\nHave a pawsome day!");
                }
            }
        }
    }

    private static int getCategoryIdByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT id FROM categories WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 1;
    }

    private static void insertProduct(PreparedStatement pstmt, int catId, String name, String desc,
                                      double sPrice, double mPrice, double lPrice, int stock, int threshold, String imagePath) throws SQLException {
        pstmt.setInt(1, catId);
        pstmt.setString(2, name);
        pstmt.setString(3, desc);
        pstmt.setDouble(4, sPrice);
        pstmt.setDouble(5, mPrice);
        pstmt.setDouble(6, lPrice);
        pstmt.setInt(7, stock);
        pstmt.setInt(8, threshold);
        pstmt.setString(9, imagePath);
        pstmt.executeUpdate();
    }

    private static void insertAddOn(PreparedStatement pstmt, String name, double price) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setDouble(2, price);
        pstmt.executeUpdate();
    }

    private static void insertDiscount(PreparedStatement pstmt, String name, String type, double value) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, type);
        pstmt.setDouble(3, value);
        pstmt.executeUpdate();
    }

    private static void insertSetting(PreparedStatement pstmt, String key, String value) throws SQLException {
        pstmt.setString(1, key);
        pstmt.setString(2, value);
        pstmt.executeUpdate();
    }
}
