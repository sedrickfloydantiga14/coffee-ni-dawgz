package com.coffeenidawgz.ui;

import com.coffeenidawgz.services.BackupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupRestorePanel extends JPanel {
    private final BackupService backupService = new BackupService();

    public BackupRestorePanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("DATABASE BACKUP & RESTORE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel(new GridLayout(1, 2, 20, 20));
        pnlCenter.setOpaque(false);

        // Backup Box
        JPanel pnlBackup = new JPanel(new BorderLayout(10, 10));
        pnlBackup.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlBackup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9), 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblBTitle = new JLabel("Backup Local Database", JLabel.CENTER);
        lblBTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBTitle.setForeground(new Color(0x3E, 0x27, 0x23));

        JTextArea txtBInfo = new JTextArea("Exports all SQLite application data into a standalone backup file:\n\n" +
                "• Products & Prices\n" +
                "• Inventory & Stock History\n" +
                "• Cashier & Admin Users\n" +
                "• Orders & Transactions\n" +
                "• Sales Reports & System Settings\n\n" +
                "Recommended: Perform regular backups at the end of each business day.");
        txtBInfo.setEditable(false);
        txtBInfo.setOpaque(false);
        txtBInfo.setLineWrap(true);
        txtBInfo.setWrapStyleWord(true);
        txtBInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        StyledButton btnBackup = new StyledButton("CREATE DATABASE BACKUP", new Color(0x4E, 0x34, 0x2E), Color.WHITE, 6);
        btnBackup.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBackup.addActionListener(e -> performBackup());

        pnlBackup.add(lblBTitle, BorderLayout.NORTH);
        pnlBackup.add(txtBInfo, BorderLayout.CENTER);
        pnlBackup.add(btnBackup, BorderLayout.SOUTH);

        // Restore Box
        JPanel pnlRestore = new JPanel(new BorderLayout(10, 10));
        pnlRestore.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlRestore.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9), 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblRTitle = new JLabel("Restore Database", JLabel.CENTER);
        lblRTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRTitle.setForeground(new Color(0xD3, 0x2F, 0x2F));

        JTextArea txtRInfo = new JTextArea("Restores application data from a previously created SQLite backup file.\n\n" +
                "WARNING:\n" +
                "Restoring a database backup will overwrite the current live database file!\n\n" +
                "A confirmation prompt will be requested before restoring.");
        txtRInfo.setEditable(false);
        txtRInfo.setOpaque(false);
        txtRInfo.setLineWrap(true);
        txtRInfo.setWrapStyleWord(true);
        txtRInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        StyledButton btnRestore = new StyledButton("RESTORE FROM BACKUP", new Color(0xC6, 0x28, 0x28), Color.WHITE, 6);
        btnRestore.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRestore.addActionListener(e -> performRestore());

        pnlRestore.add(lblRTitle, BorderLayout.NORTH);
        pnlRestore.add(txtRInfo, BorderLayout.CENTER);
        pnlRestore.add(btnRestore, BorderLayout.SOUTH);

        pnlCenter.add(pnlBackup);
        pnlCenter.add(pnlRestore);

        add(pnlCenter, BorderLayout.CENTER);
    }

    private void performBackup() {
        JFileChooser fc = new JFileChooser();
        String defaultName = "coffee_ni_dawgz_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db";
        fc.setSelectedFile(new File(defaultName));

        int choice = fc.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File dest = fc.getSelectedFile();
            boolean ok = backupService.backupDatabase(dest);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Database backup created successfully at:\n" + dest.getAbsolutePath(), "Backup Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create database backup!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performRestore() {
        JFileChooser fc = new JFileChooser();
        int choice = fc.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File backupFile = fc.getSelectedFile();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "ARE YOU SURE YOU WANT TO RESTORE THIS BACKUP?\n\n" +
                    "File: " + backupFile.getAbsolutePath() + "\n\n" +
                    "This action will REPLACE all current live data with the backup file data!",
                    "Confirm Database Restore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = backupService.restoreDatabase(backupFile);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Database restored successfully! Please restart application if needed.", "Restore Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to restore database from backup file!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
