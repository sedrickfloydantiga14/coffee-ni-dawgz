package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.UserDAO;
import com.coffeenidawgz.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeManagementPanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    private JTable tblUsers;
    private DefaultTableModel tableModel;

    public EmployeeManagementPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("EMPLOYEE MANAGEMENT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        pnlTop.add(lblTitle, BorderLayout.WEST);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtns.setOpaque(false);

        StyledButton btnAdd = new StyledButton("Create Cashier Account", new Color(0x4E, 0x34, 0x2E), Color.WHITE, 6);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.addActionListener(e -> openCreateCashierDialog());

        StyledButton btnStatus = new StyledButton("Toggle Status (Active/Inactive)", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 6);
        btnStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnStatus.addActionListener(e -> toggleUserStatus());

        StyledButton btnReset = new StyledButton("Reset Password", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.addActionListener(e -> openResetPasswordDialog());

        pnlBtns.add(btnAdd);
        pnlBtns.add(btnStatus);
        pnlBtns.add(btnReset);

        pnlTop.add(pnlBtns, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Full Name", "Role", "Status", "Date Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUsers = new JTable(tableModel);
        tblUsers.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblUsers.setRowHeight(28);

        JScrollPane scroll = new JScrollPane(tblUsers);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)));
        add(scroll, BorderLayout.CENTER);

        refreshUsers();
    }

    public void refreshUsers() {
        tableModel.setRowCount(0);
        List<User> list = userDAO.getAllUsers();
        for (User u : list) {
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    u.getStatus(),
                    u.getCreatedAt() != null ? u.getCreatedAt().toString() : "N/A"
            });
        }
    }

    private void openCreateCashierDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Cashier Account", true);
        dlg.setSize(380, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlForm = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlForm.setBorder(new EmptyBorder(15, 20, 15, 20));

        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JTextField txtFullName = new JTextField();
        JComboBox<String> cboRole = new JComboBox<>(new String[]{"CASHIER", "ADMIN"});

        pnlForm.add(new JLabel("Username:")); pnlForm.add(txtUser);
        pnlForm.add(new JLabel("Password:")); pnlForm.add(txtPass);
        pnlForm.add(new JLabel("Full Name:")); pnlForm.add(txtFullName);
        pnlForm.add(new JLabel("Role:")); pnlForm.add(cboRole);

        dlg.add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnCreate = new JButton("Create User");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCreate.setBackground(new Color(0x4E, 0x34, 0x2E));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            String name = txtFullName.getText().trim();
            String role = (String) cboRole.getSelectedItem();

            if (u.isEmpty() || p.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = userDAO.createUser(u, p, role, name);
            if (ok) {
                dlg.dispose();
                refreshUsers();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to create user (username may already exist)!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBtns.add(btnCancel);
        pnlBtns.add(btnCreate);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void toggleUserStatus() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 4);
        String newStatus = "ACTIVE".equalsIgnoreCase(currentStatus) ? "INACTIVE" : "ACTIVE";

        boolean ok = userDAO.updateUserStatus(id, newStatus);
        if (ok) {
            refreshUsers();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update user status!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openResetPasswordDialog() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to reset password!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        String newPass = JOptionPane.showInputDialog(this, "Enter new password for " + username + ":");
        if (newPass != null && !newPass.trim().isEmpty()) {
            boolean ok = userDAO.resetPassword(id, newPass.trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password reset successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
