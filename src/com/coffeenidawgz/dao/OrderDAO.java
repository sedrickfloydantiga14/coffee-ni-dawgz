package com.coffeenidawgz.dao;

import com.coffeenidawgz.database.DatabaseManager;
import com.coffeenidawgz.models.AddOn;
import com.coffeenidawgz.models.Order;
import com.coffeenidawgz.models.OrderItem;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public synchronized String generateNextOrderNumber() {
        String sql = "SELECT MAX(id) FROM orders";
        int nextId = 1;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("ORDER #%06d", nextId);
    }

    public boolean createOrder(Order order) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Insert order
            String sqlOrder = "INSERT INTO orders (order_number, cashier_id, subtotal, discount_amount, discount_name, vat_amount, total, payment_method, amount_received, change_amount, status) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int orderId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, order.getOrderNumber());
                pstmt.setInt(2, order.getCashierId());
                pstmt.setDouble(3, order.getSubtotal());
                pstmt.setDouble(4, order.getDiscountAmount());
                pstmt.setString(5, order.getDiscountName());
                pstmt.setDouble(6, order.getVatAmount());
                pstmt.setDouble(7, order.getTotal());
                pstmt.setString(8, order.getPaymentMethod());
                pstmt.setDouble(9, order.getAmountReceived());
                pstmt.setDouble(10, order.getChangeAmount());
                pstmt.setString(11, order.getStatus() != null ? order.getStatus() : "Completed");

                pstmt.executeUpdate();
                try (ResultSet rsKey = pstmt.getGeneratedKeys()) {
                    if (rsKey.next()) {
                        orderId = rsKey.getInt(1);
                    }
                }
            }

            if (orderId == -1) {
                conn.rollback();
                return false;
            }
            order.setId(orderId);

            // 2. Insert order items & deduct stock
            String sqlItem = "INSERT INTO order_items (order_id, product_id, product_name, size, temperature, sugar_level, milk_option, quantity, unit_price, total_price) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlAddon = "INSERT INTO order_item_addons (order_item_id, addon_id, addon_name, price) VALUES (?, ?, ?, ?)";
            String sqlDeductStock = "UPDATE products SET stock_quantity = stock_quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND stock_quantity >= ?";

            for (OrderItem item : order.getItems()) {
                int itemId = -1;
                try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtItem.setInt(1, orderId);
                    pstmtItem.setInt(2, item.getProductId());
                    pstmtItem.setString(3, item.getProductName());
                    pstmtItem.setString(4, item.getSize());
                    pstmtItem.setString(5, item.getTemperature());
                    pstmtItem.setString(6, item.getSugarLevel());
                    pstmtItem.setString(7, item.getMilkOption());
                    pstmtItem.setInt(8, item.getQuantity());
                    pstmtItem.setDouble(9, item.getUnitPrice());
                    pstmtItem.setDouble(10, item.getTotalPrice());
                    pstmtItem.executeUpdate();

                    try (ResultSet rsItemKey = pstmtItem.getGeneratedKeys()) {
                        if (rsItemKey.next()) {
                            itemId = rsItemKey.getInt(1);
                        }
                    }
                }

                if (itemId != -1 && item.getAddOns() != null) {
                    for (AddOn addon : item.getAddOns()) {
                        try (PreparedStatement pstmtAddon = conn.prepareStatement(sqlAddon)) {
                            pstmtAddon.setInt(1, itemId);
                            pstmtAddon.setInt(2, addon.getId());
                            pstmtAddon.setString(3, addon.getName());
                            pstmtAddon.setDouble(4, addon.getPrice());
                            pstmtAddon.executeUpdate();
                        }
                    }
                }

                // Deduct stock if order is completed
                if ("Completed".equalsIgnoreCase(order.getStatus())) {
                    try (PreparedStatement pstmtDeduct = conn.prepareStatement(sqlDeductStock)) {
                        pstmtDeduct.setInt(1, item.getQuantity());
                        pstmtDeduct.setInt(2, item.getProductId());
                        pstmtDeduct.setInt(3, item.getQuantity());
                        int rows = pstmtDeduct.executeUpdate();
                        if (rows == 0) {
                            // Insufficient stock! Rollback!
                            conn.rollback();
                            return false;
                        }
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Get current order status
            String currentStatus = "";
            String sqlCheck = "SELECT status FROM orders WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                pstmt.setInt(1, orderId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) currentStatus = rs.getString(1);
                }
            }

            // Update order status
            String sqlUpdate = "UPDATE orders SET status = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, orderId);
                pstmt.executeUpdate();
            }

            // If changing to Cancelled from Completed, restore stock!
            if ("Cancelled".equalsIgnoreCase(newStatus) && "Completed".equalsIgnoreCase(currentStatus)) {
                String sqlGetItems = "SELECT product_id, quantity FROM order_items WHERE order_id = ?";
                String sqlRestoreStock = "UPDATE products SET stock_quantity = stock_quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement pstmtItems = conn.prepareStatement(sqlGetItems)) {
                    pstmtItems.setInt(1, orderId);
                    try (ResultSet rsItems = pstmtItems.executeQuery()) {
                        while (rsItems.next()) {
                            int productId = rsItems.getInt("product_id");
                            int qty = rsItems.getInt("quantity");
                            try (PreparedStatement pstmtRestore = conn.prepareStatement(sqlRestoreStock)) {
                                pstmtRestore.setInt(1, qty);
                                pstmtRestore.setInt(2, productId);
                                pstmtRestore.executeUpdate();
                            }
                        }
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as cashier_name FROM orders o " +
                     "JOIN users u ON o.cashier_id = u.id " +
                     "ORDER BY o.id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = extractOrder(rs);
                order.setItems(getOrderItems(conn, order.getId()));
                list.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getOrdersByCashier(int cashierId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as cashier_name FROM orders o " +
                     "JOIN users u ON o.cashier_id = u.id " +
                     "WHERE o.cashier_id = ? ORDER BY o.id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cashierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = extractOrder(rs);
                    order.setItems(getOrderItems(conn, order.getId()));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT o.*, u.full_name as cashier_name FROM orders o " +
                     "JOIN users u ON o.cashier_id = u.id " +
                     "WHERE o.id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = extractOrder(rs);
                    order.setItems(getOrderItems(conn, order.getId()));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OrderItem> getOrderItems(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ? ORDER BY id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setSize(rs.getString("size"));
                    item.setTemperature(rs.getString("temperature"));
                    item.setSugarLevel(rs.getString("sugar_level"));
                    item.setMilkOption(rs.getString("milk_option"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setTotalPrice(rs.getDouble("total_price"));

                    // Get item addons
                    item.setAddOns(getOrderItemAddons(conn, item.getId()));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private List<AddOn> getOrderItemAddons(Connection conn, int orderItemId) throws SQLException {
        List<AddOn> list = new ArrayList<>();
        String sql = "SELECT * FROM order_item_addons WHERE order_item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderItemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AddOn(
                            rs.getInt("addon_id"),
                            rs.getString("addon_name"),
                            rs.getDouble("price"),
                            "ACTIVE"
                    ));
                }
            }
        }
        return list;
    }

    // Dashboard & Analytics Queries
    public double getTodaySales() {
        String sql = "SELECT SUM(total) FROM orders WHERE status = 'Completed' AND date(created_at) = date('now', 'localtime')";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTodayTransactionCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = 'Completed' AND date(created_at) = date('now', 'localtime')";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTodayItemsSold() {
        String sql = "SELECT SUM(i.quantity) FROM order_items i " +
                     "JOIN orders o ON i.order_id = o.id " +
                     "WHERE o.status = 'Completed' AND date(o.created_at) = date('now', 'localtime')";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getBestSellerName() {
        String sql = "SELECT i.product_name, SUM(i.quantity) as total_qty FROM order_items i " +
                     "JOIN orders o ON i.order_id = o.id " +
                     "WHERE o.status = 'Completed' " +
                     "GROUP BY i.product_name ORDER BY total_qty DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    public List<BestSellerRecord> getBestSellers() {
        List<BestSellerRecord> list = new ArrayList<>();
        String sql = "SELECT i.product_name, SUM(i.quantity) as total_qty, SUM(i.total_price) as total_revenue " +
                     "FROM order_items i " +
                     "JOIN orders o ON i.order_id = o.id " +
                     "WHERE o.status = 'Completed' " +
                     "GROUP BY i.product_name ORDER BY total_qty DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int rank = 1;
            while (rs.next()) {
                list.add(new BestSellerRecord(
                        rank++,
                        rs.getString("product_name"),
                        rs.getInt("total_qty"),
                        rs.getDouble("total_revenue")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Order extractOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setCashierId(rs.getInt("cashier_id"));
        o.setCashierName(rs.getString("cashier_name"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setDiscountAmount(rs.getDouble("discount_amount"));
        o.setDiscountName(rs.getString("discount_name"));
        o.setVatAmount(rs.getDouble("vat_amount"));
        o.setTotal(rs.getDouble("total"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setAmountReceived(rs.getDouble("amount_received"));
        o.setChangeAmount(rs.getDouble("change_amount"));
        o.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
        return o;
    }

    public static class BestSellerRecord {
        public int rank;
        public String productName;
        public int quantitySold;
        public double totalRevenue;

        public BestSellerRecord(int rank, String productName, int quantitySold, double totalRevenue) {
            this.rank = rank;
            this.productName = productName;
            this.quantitySold = quantitySold;
            this.totalRevenue = totalRevenue;
        }
    }
}
