package com.coffeenidawgz.services;

import com.coffeenidawgz.dao.SettingDAO;
import com.coffeenidawgz.models.AddOn;
import com.coffeenidawgz.models.Order;
import com.coffeenidawgz.models.OrderItem;
import com.coffeenidawgz.utils.CurrencyFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ReceiptGenerator {
    private final SettingDAO settingDAO = new SettingDAO();

    public String generateReceiptText(Order order) {
        String shopName = settingDAO.getSetting("SHOP_NAME", "COFFEE NI DAWGZ");
        String slogan = settingDAO.getSetting("SHOP_SLOGAN", "Brewed for Good Dawgz 🐾");
        String header = settingDAO.getSetting("RECEIPT_HEADER", "");
        String footer = settingDAO.getSetting("RECEIPT_FOOTER", "THANK YOU, DAWG! 🐾");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append(centerString(shopName, 40)).append("\n");
        sb.append(centerString("\"" + slogan + "\"", 40)).append("\n");
        if (!header.isEmpty()) {
            for (String hLine : header.split("\n")) {
                sb.append(centerString(hLine, 40)).append("\n");
            }
        }
        sb.append("========================================\n\n");

        sb.append(String.format("Order #:    %s\n", order.getOrderNumber()));
        sb.append(String.format("Cashier:    %s\n", order.getCashierName()));
        if (order.getCreatedAt() != null) {
            sb.append(String.format("Date:       %s\n", order.getCreatedAt().format(dateFormatter)));
            sb.append(String.format("Time:       %s\n", order.getCreatedAt().format(timeFormatter)));
        }
        sb.append("----------------------------------------\n\n");

        for (OrderItem item : order.getItems()) {
            String itemHeader = String.format("%s (%s)", item.getProductName(), item.getSize());
            String priceStr = CurrencyFormatter.format(item.getTotalPrice());
            String qtyStr = String.format("%d x %s", item.getQuantity(), CurrencyFormatter.format(item.getUnitPrice()));

            sb.append(String.format("%-28s %11s\n", itemHeader, priceStr));
            sb.append(String.format("  %-38s\n", qtyStr));

            if (!"N/A".equalsIgnoreCase(item.getTemperature())) {
                sb.append(String.format("  Temp: %s\n", item.getTemperature()));
            }
            if (!"N/A".equalsIgnoreCase(item.getSugarLevel())) {
                sb.append(String.format("  Sugar: %s\n", item.getSugarLevel()));
            }
            if (!"N/A".equalsIgnoreCase(item.getMilkOption())) {
                sb.append(String.format("  Milk: %s\n", item.getMilkOption()));
            }
            if (item.getAddOns() != null && !item.getAddOns().isEmpty()) {
                sb.append("  Add-ons: ");
                for (int i = 0; i < item.getAddOns().size(); i++) {
                    AddOn a = item.getAddOns().get(i);
                    sb.append(a.getName());
                    if (i < item.getAddOns().size() - 1) sb.append(", ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("%-25s %14s\n", "Subtotal:", CurrencyFormatter.format(order.getSubtotal())));
        if (order.getDiscountAmount() > 0) {
            sb.append(String.format("%-25s -%13s\n", "Discount (" + order.getDiscountName() + "):", CurrencyFormatter.format(order.getDiscountAmount())));
        }
        sb.append(String.format("%-25s %14s\n", "VAT (12%):", CurrencyFormatter.format(order.getVatAmount())));
        sb.append("========================================\n");
        sb.append(String.format("%-25s %14s\n", "TOTAL:", CurrencyFormatter.format(order.getTotal())));
        sb.append("========================================\n\n");

        sb.append(String.format("Payment Method: %s\n", order.getPaymentMethod()));
        sb.append(String.format("%-25s %14s\n", "Amount Paid:", CurrencyFormatter.format(order.getAmountReceived())));
        sb.append(String.format("%-25s %14s\n", "Change:", CurrencyFormatter.format(order.getChangeAmount())));
        sb.append("\n========================================\n");
        for (String fLine : footer.split("\n")) {
            sb.append(centerString(fLine, 40)).append("\n");
        }
        sb.append("========================================\n");

        return sb.toString();
    }

    public File saveReceiptToFile(Order order) throws IOException {
        File dir = new File("receipts");
        if (!dir.exists()) dir.mkdirs();

        String fileName = order.getOrderNumber().replace("#", "").replace(" ", "_") + ".txt";
        File file = new File(dir, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(generateReceiptText(order));
        }

        return file;
    }

    private String centerString(String text, int width) {
        if (text.length() >= width) return text;
        int leftPadding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leftPadding; i++) sb.append(" ");
        sb.append(text);
        return sb.toString();
    }
}
