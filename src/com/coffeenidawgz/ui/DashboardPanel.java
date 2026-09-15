package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.OrderDAO;
import com.coffeenidawgz.dao.ProductDAO;
import com.coffeenidawgz.models.Order;
import com.coffeenidawgz.models.Product;
import com.coffeenidawgz.utils.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private JLabel lblTodaySales;
    private JLabel lblTransactions;
    private JLabel lblItemsSold;
    private JLabel lblBestSeller;
    private JLabel lblLowStock;

    private DefaultTableModel tblBestSellersModel;
    private DefaultTableModel tblRecentOrdersModel;

    public DashboardPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("ADMIN DASHBOARD");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        pnlHeader.add(lblTitle);

        StyledButton btnRefresh = new StyledButton("Refresh Data", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 6);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> refreshDashboardData());
        pnlHeader.add(btnRefresh);

        add(pnlHeader, BorderLayout.NORTH);

        // Center Content: Cards + Tables
        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setOpaque(false);

        // 5 Summary KPI Cards
        JPanel pnlCards = new JPanel(new GridLayout(1, 5, 12, 12));
        pnlCards.setOpaque(false);

        lblTodaySales = new JLabel("₱0.00", JLabel.CENTER);
        lblTransactions = new JLabel("0", JLabel.CENTER);
        lblItemsSold = new JLabel("0", JLabel.CENTER);
        lblBestSeller = new JLabel("N/A", JLabel.CENTER);
        lblLowStock = new JLabel("0", JLabel.CENTER);

        pnlCards.add(createKpiCard("Today's Sales", lblTodaySales, new Color(0x3E, 0x27, 0x23)));
        pnlCards.add(createKpiCard("Transactions", lblTransactions, new Color(0x5D, 0x40, 0x37)));
        pnlCards.add(createKpiCard("Items Sold", lblItemsSold, new Color(0x79, 0x55, 0x48)));
        pnlCards.add(createKpiCard("Best Seller", lblBestSeller, new Color(0x8D, 0x6E, 0x63)));
        pnlCards.add(createKpiCard("Low Stock Alert", lblLowStock, new Color(0xD3, 0x2F, 0x2F)));

        pnlCenter.add(pnlCards);
        pnlCenter.add(Box.createVerticalStrut(20));

        // Tables Split: Best Sellers Rank & Recent Transactions
        JPanel pnlTables = new JPanel(new GridLayout(1, 2, 15, 15));
        pnlTables.setOpaque(false);

        // Best Sellers Panel
        JPanel pnlBestSellers = new JPanel(new BorderLayout(5, 5));
        pnlBestSellers.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlBestSellers.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)),
                "Best-Selling Products Ranking",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(0x3E, 0x27, 0x23)
        ));

        String[] colsBest = {"Rank", "Product Name", "Qty Sold", "Total Revenue"};
        tblBestSellersModel = new DefaultTableModel(colsBest, 0);
        JTable tblBest = new JTable(tblBestSellersModel);
        tblBest.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblBest.setRowHeight(26);
        pnlBestSellers.add(new JScrollPane(tblBest), BorderLayout.CENTER);

        // Recent Transactions Panel
        JPanel pnlRecent = new JPanel(new BorderLayout(5, 5));
        pnlRecent.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlRecent.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)),
                "Recent Transactions",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(0x3E, 0x27, 0x23)
        ));

        String[] colsRecent = {"Order #", "Cashier", "Total", "Method", "Status"};
        tblRecentOrdersModel = new DefaultTableModel(colsRecent, 0);
        JTable tblRecent = new JTable(tblRecentOrdersModel);
        tblRecent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblRecent.setRowHeight(26);
        pnlRecent.add(new JScrollPane(tblRecent), BorderLayout.CENTER);

        pnlTables.add(pnlBestSellers);
        pnlTables.add(pnlRecent);

        pnlCenter.add(pnlTables);
        add(pnlCenter, BorderLayout.CENTER);

        refreshDashboardData();
    }

    private JPanel createKpiCard(String title, JLabel lblValue, Color headerColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(0xFF, 0xFA, 0xF0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9), 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTitle = new JLabel(title, JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(headerColor);

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(new Color(0x3E, 0x27, 0x23));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    public void refreshDashboardData() {
        lblTodaySales.setText(CurrencyFormatter.format(orderDAO.getTodaySales()));
        lblTransactions.setText(String.valueOf(orderDAO.getTodayTransactionCount()));
        lblItemsSold.setText(String.valueOf(orderDAO.getTodayItemsSold()));
        lblBestSeller.setText(orderDAO.getBestSellerName());

        List<Product> lowStock = productDAO.getLowStockProducts();
        lblLowStock.setText(String.valueOf(lowStock.size()));

        // Populate Best Sellers
        tblBestSellersModel.setRowCount(0);
        List<OrderDAO.BestSellerRecord> bestSellers = orderDAO.getBestSellers();
        for (OrderDAO.BestSellerRecord rec : bestSellers) {
            tblBestSellersModel.addRow(new Object[]{
                    "#" + rec.rank,
                    rec.productName,
                    rec.quantitySold,
                    CurrencyFormatter.format(rec.totalRevenue)
            });
        }

        // Populate Recent Orders
        tblRecentOrdersModel.setRowCount(0);
        List<Order> orders = orderDAO.getAllOrders();
        int count = 0;
        for (Order o : orders) {
            if (count++ >= 15) break;
            tblRecentOrdersModel.addRow(new Object[]{
                    o.getOrderNumber(),
                    o.getCashierName(),
                    CurrencyFormatter.format(o.getTotal()),
                    o.getPaymentMethod(),
                    o.getStatus()
            });
        }
    }
}
