package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.OrderDAO;
import com.coffeenidawgz.models.Order;
import com.coffeenidawgz.services.AuthService;
import com.coffeenidawgz.utils.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionsPanel extends JPanel {
    private final OrderDAO orderDAO = new OrderDAO();
    private JTable tblOrders;
    private DefaultTableModel tableModel;

    public TransactionsPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("TRANSACTION HISTORY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        pnlTop.add(lblTitle, BorderLayout.WEST);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtns.setOpaque(false);

        StyledButton btnView = new StyledButton("View Receipt Details", new Color(0x4E, 0x34, 0x2E), Color.WHITE, 6);
        btnView.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnView.addActionListener(e -> viewReceiptDetails());

        StyledButton btnCancelOrder = new StyledButton("Cancel Selected Order", new Color(0xC6, 0x28, 0x28), Color.WHITE, 6);
        btnCancelOrder.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancelOrder.addActionListener(e -> cancelSelectedOrder());

        StyledButton btnRefresh = new StyledButton("Refresh", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> refreshTransactions());

        pnlBtns.add(btnView);
        pnlBtns.add(btnCancelOrder);
        pnlBtns.add(btnRefresh);

        pnlTop.add(pnlBtns, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"ID", "Order #", "Date & Time", "Cashier", "Items Qty", "Subtotal", "VAT", "Total", "Payment Method", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders = new JTable(tableModel);
        tblOrders.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblOrders.setRowHeight(26);

        JScrollPane scroll = new JScrollPane(tblOrders);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)));
        add(scroll, BorderLayout.CENTER);

        refreshTransactions();
    }

    public void refreshTransactions() {
        tableModel.setRowCount(0);
        List<Order> orders = orderDAO.getAllOrders();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

        for (Order o : orders) {
            int totalItemQty = 0;
            if (o.getItems() != null) {
                for (var item : o.getItems()) totalItemQty += item.getQuantity();
            }

            tableModel.addRow(new Object[]{
                    o.getId(),
                    o.getOrderNumber(),
                    o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "N/A",
                    o.getCashierName(),
                    totalItemQty,
                    CurrencyFormatter.format(o.getSubtotal()),
                    CurrencyFormatter.format(o.getVatAmount()),
                    CurrencyFormatter.format(o.getTotal()),
                    o.getPaymentMethod(),
                    o.getStatus()
            });
        }
    }

    private void viewReceiptDetails() {
        int row = tblOrders.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to view!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        Order order = orderDAO.getOrderById(id);
        if (order != null) {
            ReceiptDialog dlg = new ReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), order);
            dlg.setVisible(true);
        }
    }

    private void cancelSelectedOrder() {
        if (!AuthService.getInstance().isAdmin()) {
            JOptionPane.showMessageDialog(this, "Only Admin accounts can authorize order cancellations!", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int row = tblOrders.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to cancel!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        String orderNum = (String) tableModel.getValueAt(row, 1);
        String status = (String) tableModel.getValueAt(row, 9);

        if ("Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This order is already cancelled!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel " + orderNum + "?\n\nThis will update order status to 'Cancelled' and automatically restore product stock levels.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = orderDAO.updateOrderStatus(id, "Cancelled");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Order cancelled & stock restored!", "Order Cancelled", JOptionPane.INFORMATION_MESSAGE);
                refreshTransactions();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel order!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
