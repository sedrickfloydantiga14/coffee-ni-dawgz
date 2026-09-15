package com.coffeenidawgz.ui;

import com.coffeenidawgz.models.Order;
import com.coffeenidawgz.services.ReceiptGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;

public class ReceiptDialog extends JDialog {
    private final Order order;
    private final ReceiptGenerator generator = new ReceiptGenerator();
    private JTextArea txtReceipt;

    public ReceiptDialog(Frame owner, Order order) {
        super(owner, "Receipt — " + order.getOrderNumber(), true);
        this.order = order;
        initUI();
    }

    private void initUI() {
        setSize(460, 640);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(new Color(0x3E, 0x27, 0x23));
        JLabel lblTitle = new JLabel("TRANSACTION COMPLETE!");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(0xFF, 0xEC, 0xB3));
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        txtReceipt = new JTextArea();
        txtReceipt.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtReceipt.setEditable(false);
        txtReceipt.setMargin(new Insets(15, 20, 15, 20));
        txtReceipt.setText(generator.generateReceiptText(order));
        txtReceipt.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(txtReceipt);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtns.setBackground(new Color(0xEF, 0xE5, 0xD8));

        StyledButton btnPrint = new StyledButton("Print Receipt", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 6);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrint.addActionListener(e -> {
            try {
                boolean done = txtReceipt.print();
                if (done) {
                    JOptionPane.showMessageDialog(this, "Receipt sent to printer!", "Print", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing error: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        StyledButton btnSave = new StyledButton("Save Receipt", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.addActionListener(e -> {
            try {
                File file = generator.saveReceiptToFile(order);
                JOptionPane.showMessageDialog(this, "Receipt saved digitally to:\n" + file.getAbsolutePath(), "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving receipt: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        StyledButton btnClose = new StyledButton("Done", new Color(0x4E, 0x34, 0x2E), Color.WHITE, 6);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.addActionListener(e -> dispose());

        pnlBtns.add(btnPrint);
        pnlBtns.add(btnSave);
        pnlBtns.add(btnClose);
        add(pnlBtns, BorderLayout.SOUTH);
    }
}
