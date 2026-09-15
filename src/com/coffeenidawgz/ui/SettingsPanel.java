package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.SettingDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final SettingDAO settingDAO = new SettingDAO();

    private JTextField txtShopName;
    private JTextField txtSlogan;
    private JTextField txtVatRate;
    private JTextField txtGCashNumber;
    private JTextField txtGCashName;
    private JTextField txtLowStockThreshold;
    private JTextArea txtHeader;
    private JTextArea txtFooter;

    public SettingsPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("SYSTEM SETTINGS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9), 1, true),
                new EmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtShopName = new JTextField(25);
        txtSlogan = new JTextField(25);
        txtVatRate = new JTextField(25);
        txtGCashNumber = new JTextField(25);
        txtGCashName = new JTextField(25);
        txtLowStockThreshold = new JTextField(25);
        txtHeader = new JTextArea(3, 25);
        txtFooter = new JTextArea(3, 25);

        int row = 0;
        addFormRow(pnlForm, gbc, row++, "Shop Name:", txtShopName);
        addFormRow(pnlForm, gbc, row++, "Shop Slogan:", txtSlogan);
        addFormRow(pnlForm, gbc, row++, "VAT Rate (%):", txtVatRate);
        addFormRow(pnlForm, gbc, row++, "GCash Account Number:", txtGCashNumber);
        addFormRow(pnlForm, gbc, row++, "GCash Account Name:", txtGCashName);
        addFormRow(pnlForm, gbc, row++, "Low Stock Threshold:", txtLowStockThreshold);
        addFormRow(pnlForm, gbc, row++, "Receipt Header Text:", new JScrollPane(txtHeader));
        addFormRow(pnlForm, gbc, row++, "Receipt Footer Text:", new JScrollPane(txtFooter));

        JScrollPane scrollForm = new JScrollPane(pnlForm);
        scrollForm.setBorder(null);
        add(scrollForm, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.setOpaque(false);

        StyledButton btnSave = new StyledButton("Save All Settings", new Color(0x4E, 0x34, 0x2E), Color.WHITE, 6);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.addActionListener(e -> saveSettings());

        pnlBottom.add(btnSave);
        add(pnlBottom, BorderLayout.SOUTH);

        loadSettings();
    }

    private void addFormRow(JPanel pnl, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(0x3E, 0x27, 0x23));
        pnl.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        pnl.add(comp, gbc);
    }

    public void loadSettings() {
        txtShopName.setText(settingDAO.getSetting("SHOP_NAME", "COFFEE NI DAWGZ"));
        txtSlogan.setText(settingDAO.getSetting("SHOP_SLOGAN", "Brewed for Good Dawgz 🐾"));
        txtVatRate.setText(settingDAO.getSetting("VAT_RATE", "12.0"));
        txtGCashNumber.setText(settingDAO.getSetting("GCASH_NUMBER", "0917-123-4567"));
        txtGCashName.setText(settingDAO.getSetting("GCASH_NAME", "COFFEE NI DAWGZ POS"));
        txtLowStockThreshold.setText(settingDAO.getSetting("LOW_STOCK_THRESHOLD", "5"));
        txtHeader.setText(settingDAO.getSetting("RECEIPT_HEADER", "Welcome to Coffee ni Dawgz!\n123 Paws Street, Dogtown"));
        txtFooter.setText(settingDAO.getSetting("RECEIPT_FOOTER", "THANK YOU, DAWG! 🐾\nHave a pawsome day!"));
    }

    private void saveSettings() {
        try {
            double vat = Double.parseDouble(txtVatRate.getText().trim());
            int threshold = Integer.parseInt(txtLowStockThreshold.getText().trim());
            if (vat < 0 || threshold < 0) {
                JOptionPane.showMessageDialog(this, "VAT rate and Low Stock Threshold cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            settingDAO.updateSetting("SHOP_NAME", txtShopName.getText().trim());
            settingDAO.updateSetting("SHOP_SLOGAN", txtSlogan.getText().trim());
            settingDAO.updateSetting("VAT_RATE", String.valueOf(vat));
            settingDAO.updateSetting("GCASH_NUMBER", txtGCashNumber.getText().trim());
            settingDAO.updateSetting("GCASH_NAME", txtGCashName.getText().trim());
            settingDAO.updateSetting("LOW_STOCK_THRESHOLD", String.valueOf(threshold));
            settingDAO.updateSetting("RECEIPT_HEADER", txtHeader.getText().trim());
            settingDAO.updateSetting("RECEIPT_FOOTER", txtFooter.getText().trim());

            JOptionPane.showMessageDialog(this, "Settings saved successfully!", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for VAT Rate and Low Stock Threshold!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
}
