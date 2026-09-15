package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.OrderDAO;
import com.coffeenidawgz.models.Order;
import com.coffeenidawgz.utils.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final OrderDAO orderDAO = new OrderDAO();
    private JLabel lblReportTitle, lblTotalSales, lblTransactions, lblDiscounts, lblVat, lblCashSales, lblGCashSales;
    private DefaultTableModel tblReportModel;

    public ReportsPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Toolbar
        JPanel pnlTop = new JPanel(new BorderLayout(10, 10));
        pnlTop.setOpaque(false);

        lblReportTitle = new JLabel("SALES REPORTS");
        lblReportTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblReportTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        pnlTop.add(lblReportTitle, BorderLayout.WEST);

        JPanel pnlPeriodBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlPeriodBtns.setOpaque(false);

        StyledButton btnDaily = new StyledButton("Today's Report", new Color(0x4E, 0x34, 0x2E), Color.WHITE, 6);
        btnDaily.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDaily.addActionListener(e -> generateReport(1));

        StyledButton btnWeekly = new StyledButton("Past 7 Days", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 6);
        btnWeekly.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnWeekly.addActionListener(e -> generateReport(7));

        StyledButton btnMonthly = new StyledButton("Past 30 Days", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnMonthly.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMonthly.addActionListener(e -> generateReport(30));

        pnlPeriodBtns.add(btnDaily);
        pnlPeriodBtns.add(btnWeekly);
        pnlPeriodBtns.add(btnMonthly);

        pnlTop.add(pnlPeriodBtns, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // Center Content
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setOpaque(false);

        // KPI Summary Panel
        JPanel pnlSummary = new JPanel(new GridLayout(2, 3, 10, 10));
        pnlSummary.setOpaque(false);

        lblTotalSales = new JLabel("₱0.00", JLabel.CENTER);
        lblTransactions = new JLabel("0", JLabel.CENTER);
        lblDiscounts = new JLabel("₱0.00", JLabel.CENTER);
        lblVat = new JLabel("₱0.00", JLabel.CENTER);
        lblCashSales = new JLabel("₱0.00", JLabel.CENTER);
        lblGCashSales = new JLabel("₱0.00", JLabel.CENTER);

        pnlSummary.add(createCard("Total Revenue", lblTotalSales));
        pnlSummary.add(createCard("Total Transactions", lblTransactions));
        pnlSummary.add(createCard("Total Discounts Given", lblDiscounts));
        pnlSummary.add(createCard("Total VAT Collected", lblVat));
        pnlSummary.add(createCard("Cash Payments", lblCashSales));
        pnlSummary.add(createCard("GCash Payments", lblGCashSales));

        pnlCenter.add(pnlSummary, BorderLayout.NORTH);

        // Detail Table
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlTable.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)),
                "Included Transaction Orders",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(0x3E, 0x27, 0x23)
        ));

        String[] cols = {"Order #", "Date", "Cashier", "Subtotal", "Discount", "VAT", "Total", "Payment Method"};
        tblReportModel = new DefaultTableModel(cols, 0);
        JTable tbl = new JTable(tblReportModel);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setRowHeight(24);

        pnlTable.add(new JScrollPane(tbl), BorderLayout.CENTER);
        pnlCenter.add(pnlTable, BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);

        generateReport(1);
    }

    private JPanel createCard(String title, JLabel lblValue) {
        JPanel pnl = new JPanel(new BorderLayout(4, 4));
        pnl.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel lblT = new JLabel(title, JLabel.CENTER);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(new Color(0x6D, 0x4C, 0x41));

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(new Color(0x3E, 0x27, 0x23));

        pnl.add(lblT, BorderLayout.NORTH);
        pnl.add(lblValue, BorderLayout.CENTER);
        return pnl;
    }

    public void generateReport(int days) {
        lblReportTitle.setText(days == 1 ? "TODAY'S SALES REPORT 📊" : (days == 7 ? "WEEKLY SALES REPORT (7 DAYS) 📊" : "MONTHLY SALES REPORT (30 DAYS) 📊"));

        List<Order> allOrders = orderDAO.getAllOrders();
        LocalDate cutoff = LocalDate.now().minusDays(days - 1);

        double totalRev = 0;
        int totalTrans = 0;
        double totalDisc = 0;
        double totalVat = 0;
        double cashRev = 0;
        double gcashRev = 0;

        tblReportModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

        for (Order o : allOrders) {
            if (!"Completed".equalsIgnoreCase(o.getStatus())) continue;
            if (o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().isBefore(cutoff)) continue;

            totalRev += o.getTotal();
            totalTrans++;
            totalDisc += o.getDiscountAmount();
            totalVat += o.getVatAmount();

            if ("CASH".equalsIgnoreCase(o.getPaymentMethod())) cashRev += o.getTotal();
            else gcashRev += o.getTotal();

            tblReportModel.addRow(new Object[]{
                    o.getOrderNumber(),
                    o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "N/A",
                    o.getCashierName(),
                    CurrencyFormatter.format(o.getSubtotal()),
                    CurrencyFormatter.format(o.getDiscountAmount()),
                    CurrencyFormatter.format(o.getVatAmount()),
                    CurrencyFormatter.format(o.getTotal()),
                    o.getPaymentMethod()
            });
        }

        lblTotalSales.setText(CurrencyFormatter.format(totalRev));
        lblTransactions.setText(String.valueOf(totalTrans));
        lblDiscounts.setText(CurrencyFormatter.format(totalDisc));
        lblVat.setText(CurrencyFormatter.format(totalVat));
        lblCashSales.setText(CurrencyFormatter.format(cashRev));
        lblGCashSales.setText(CurrencyFormatter.format(gcashRev));
    }
}
