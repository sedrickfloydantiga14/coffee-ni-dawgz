package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.InventoryDAO;
import com.coffeenidawgz.dao.ProductDAO;
import com.coffeenidawgz.models.InventoryLog;
import com.coffeenidawgz.models.Product;
import com.coffeenidawgz.services.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InventoryPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private JTable tblStock;
    private DefaultTableModel stockTableModel;
    private JTable tblLogs;
    private DefaultTableModel logTableModel;

    public InventoryPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header & Actions
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("INVENTORY MANAGEMENT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        pnlTop.add(lblTitle, BorderLayout.WEST);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtns.setOpaque(false);

        StyledButton btnAddStock = new StyledButton("Add Stock", new Color(0x4E, 0x34, 0x2E), Color.WHITE, 6);
        btnAddStock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddStock.addActionListener(e -> adjustStockDialog(true));

        StyledButton btnRemoveStock = new StyledButton("Remove Stock", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 6);
        btnRemoveStock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemoveStock.addActionListener(e -> adjustStockDialog(false));

        StyledButton btnRefresh = new StyledButton("Refresh", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> refreshData());

        pnlBtns.add(btnAddStock);
        pnlBtns.add(btnRemoveStock);
        pnlBtns.add(btnRefresh);

        pnlTop.add(pnlBtns, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // Center Split Pane (Top: Stock Table, Bottom: Adjustment Audit Logs)
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.6);
        split.setOpaque(false);

        // Top Panel: Stock Table
        JPanel pnlStock = new JPanel(new BorderLayout());
        pnlStock.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlStock.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)),
                "Current Product Stock Levels",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(0x3E, 0x27, 0x23)
        ));

        String[] colsStock = {"ID", "Category", "Product Name", "Current Stock", "Low Threshold", "Status Warning"};
        stockTableModel = new DefaultTableModel(colsStock, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStock = new JTable(stockTableModel);
        tblStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblStock.setRowHeight(28);

        // Highlight stock warnings
        tblStock.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 5);
                if (!isSelected) {
                    if (status.contains("OUT OF STOCK")) {
                        c.setBackground(new Color(0xFF, 0xEB, 0xEE));
                        c.setForeground(new Color(0xC6, 0x28, 0x28));
                    } else if (status.contains("LOW STOCK")) {
                        c.setBackground(new Color(0xFF, 0xF3, 0xE0));
                        c.setForeground(new Color(0xEF, 0x6C, 0x00));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        pnlStock.add(new JScrollPane(tblStock), BorderLayout.CENTER);
        split.setTopComponent(pnlStock);

        // Bottom Panel: Audit Logs Table
        JPanel pnlLogs = new JPanel(new BorderLayout());
        pnlLogs.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlLogs.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)),
                "Inventory Adjustment History Log",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(0x3E, 0x27, 0x23)
        ));

        String[] colsLogs = {"Date/Time", "Product", "Qty Change", "Prev Qty", "New Qty", "Reason", "Adjusted By"};
        logTableModel = new DefaultTableModel(colsLogs, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLogs = new JTable(logTableModel);
        tblLogs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLogs.setRowHeight(24);

        pnlLogs.add(new JScrollPane(tblLogs), BorderLayout.CENTER);
        split.setBottomComponent(pnlLogs);

        add(split, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        // Stock Table
        stockTableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            String warn = "OK (In Stock)";
            if (p.isOutOfStock()) warn = "OUT OF STOCK";
            else if (p.isLowStock()) warn = "LOW STOCK";

            stockTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getCategoryName(),
                    p.getName(),
                    p.getStockQuantity(),
                    p.getLowStockThreshold(),
                    warn
            });
        }

        // Logs Table
        logTableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
        List<InventoryLog> logs = inventoryDAO.getAllLogs();
        for (InventoryLog log : logs) {
            logTableModel.addRow(new Object[]{
                    log.getCreatedAt() != null ? log.getCreatedAt().format(fmt) : "N/A",
                    log.getProductName(),
                    (log.getQuantityChanged() > 0 ? "+" : "") + log.getQuantityChanged(),
                    log.getPreviousQuantity(),
                    log.getNewQuantity(),
                    log.getReason(),
                    log.getUsername()
            });
        }
    }

    private void adjustStockDialog(boolean isAdding) {
        int row = tblStock.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the list first!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (Integer) stockTableModel.getValueAt(row, 0);
        Product product = productDAO.getProductById(productId);
        if (product == null) return;

        String action = isAdding ? "Add Stock" : "Remove Stock";
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), action + " — " + product.getName(), true);
        dlg.setSize(380, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlForm = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlForm.setBorder(new EmptyBorder(15, 20, 15, 20));

        pnlForm.add(new JLabel("Product:"));
        pnlForm.add(new JLabel("<html><b>" + product.getName() + "</b></html>"));

        pnlForm.add(new JLabel("Current Stock:"));
        pnlForm.add(new JLabel(String.valueOf(product.getStockQuantity())));

        pnlForm.add(new JLabel("Quantity to " + (isAdding ? "Add:" : "Remove:")));
        JSpinner spinQty = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        pnlForm.add(spinQty);

        pnlForm.add(new JLabel("Reason / Note:"));
        JTextField txtReason = new JTextField(isAdding ? "Restock shipment" : "Damaged / Expired / Adjustment");
        pnlForm.add(txtReason);

        dlg.add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtns.setBorder(new EmptyBorder(5, 20, 15, 20));
        pnlBtns.setOpaque(false);

        StyledButton btnCancel = new StyledButton("Cancel", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.setPreferredSize(new Dimension(100, 36));
        btnCancel.addActionListener(e -> dlg.dispose());

        StyledButton btnSave = new StyledButton("Confirm " + action, new Color(0x3E, 0x27, 0x23), new Color(0xFF, 0xEC, 0xB3), 6);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setPreferredSize(new Dimension(140, 36));

        btnSave.addActionListener(e -> {
            int changeQty = (Integer) spinQty.getValue();
            if (!isAdding) changeQty = -changeQty;

            int newStock = product.getStockQuantity() + changeQty;
            if (newStock < 0) {
                JOptionPane.showMessageDialog(dlg, "Inventory cannot become negative!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = productDAO.updateStock(product.getId(), newStock);
            if (ok) {
                inventoryDAO.logAdjustment(
                        product.getId(),
                        changeQty,
                        product.getStockQuantity(),
                        newStock,
                        txtReason.getText().trim(),
                        AuthService.getInstance().getCurrentUser().getId()
                );
                dlg.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to update stock!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
