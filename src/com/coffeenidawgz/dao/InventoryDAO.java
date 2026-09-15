package com.coffeenidawgz.dao;

import com.coffeenidawgz.database.DatabaseManager;
import com.coffeenidawgz.models.InventoryLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public boolean logAdjustment(int productId, int quantityChanged, int prevQty, int newQty, String reason, int userId) {
        String sql = "INSERT INTO inventory_logs (product_id, quantity_changed, previous_quantity, new_quantity, reason, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setInt(2, quantityChanged);
            pstmt.setInt(3, prevQty);
            pstmt.setInt(4, newQty);
            pstmt.setString(5, reason);
            pstmt.setInt(6, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<InventoryLog> getLogsForProduct(int productId) {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT l.*, p.name as product_name, u.username FROM inventory_logs l " +
                     "JOIN products p ON l.product_id = p.id " +
                     "JOIN users u ON l.user_id = u.id " +
                     "WHERE l.product_id = ? ORDER BY l.id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<InventoryLog> getAllLogs() {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT l.*, p.name as product_name, u.username FROM inventory_logs l " +
                     "JOIN products p ON l.product_id = p.id " +
                     "JOIN users u ON l.user_id = u.id " +
                     "ORDER BY l.id DESC LIMIT 100";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private InventoryLog extractLog(ResultSet rs) throws SQLException {
        InventoryLog log = new InventoryLog();
        log.setId(rs.getInt("id"));
        log.setProductId(rs.getInt("product_id"));
        log.setProductName(rs.getString("product_name"));
        log.setQuantityChanged(rs.getInt("quantity_changed"));
        log.setPreviousQuantity(rs.getInt("previous_quantity"));
        log.setNewQuantity(rs.getInt("new_quantity"));
        log.setReason(rs.getString("reason"));
        log.setUserId(rs.getInt("user_id"));
        log.setUsername(rs.getString("username"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
        return log;
    }
}
