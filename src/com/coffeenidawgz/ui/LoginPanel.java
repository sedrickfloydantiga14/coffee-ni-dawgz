package com.coffeenidawgz.ui;

import com.coffeenidawgz.services.AuthService;
import com.coffeenidawgz.utils.MascotIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(0x3E, 0x27, 0x23)); // Dark Coffee Brown

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(0xFF, 0xF8, 0xE7)); // Cream Amber
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9), 2, true),
                new EmptyBorder(30, 40, 35, 40)));

        // Mascot Icon
        JLabel lblMascot = new JLabel(MascotIcon.getMascotIcon(110, 110));
        lblMascot.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel lblTitle = new JLabel("COFFEE NI DAWGZ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Brewed for Good Dawgz");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSub.setForeground(new Color(0x8D, 0x6E, 0x63));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inputs
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(new Color(0x4E, 0x34, 0x2E));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setMaximumSize(new Dimension(280, 35));

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(new Color(0x4E, 0x34, 0x2E));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(280, 35));

        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblError.setForeground(new Color(0xD3, 0x2F, 0x2F));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        StyledButton btnLogin = new StyledButton("LOGIN", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 8);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(280, 42));

        btnLogin.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());

        // Assembly
        card.add(lblMascot);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTitle);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(25));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(5));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(15));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(5));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(10));
        card.add(lblError);
        card.add(Box.createVerticalStrut(10));
        card.add(btnLogin);

        add(card);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter username and password!");
            return;
        }

        boolean success = AuthService.getInstance().login(username, password);
        if (success) {
            lblError.setText(" ");
            txtUsername.setText("");
            txtPassword.setText("");
            mainFrame.onLoginSuccess();
        } else {
            lblError.setText("Invalid credentials or account disabled!");
        }
    }
}
