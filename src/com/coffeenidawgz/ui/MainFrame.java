package com.coffeenidawgz.ui;

import com.coffeenidawgz.services.AuthService;
import com.coffeenidawgz.utils.MascotIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout rootCardLayout;
    private JPanel rootCardPanel;

    private CardLayout contentCardLayout;
    private JPanel contentCardPanel;

    private JLabel lblUserInfo;
    private LoginPanel loginPanel;

    private POSPanel posPanel;
    private DashboardPanel dashboardPanel;
    private ProductManagementPanel productPanel;
    private InventoryPanel inventoryPanel;
    private EmployeeManagementPanel employeePanel;
    private ReportsPanel reportsPanel;
    private TransactionsPanel transactionsPanel;
    private SettingsPanel settingsPanel;
    private BackupRestorePanel backupPanel;

    private JPanel pnlSidebar;

    public MainFrame() {
        setTitle("COFFEE NI DAWGZ — Offline Coffee Shop POS");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set Window & Taskbar Logo Icon

        try {
            ImageIcon mascot = MascotIcon.getMascotIcon(128, 128);
            if (mascot != null && mascot.getImage() != null) {
                setIconImage(mascot.getImage());
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar taskbar = Taskbar.getTaskbar();
                    if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        taskbar.setIconImage(mascot.getImage());
                    }
                }
            }
        } catch (Exception ignored) {
        }

        initUI();
    }

    private void initUI() {
        rootCardLayout = new CardLayout();
        rootCardPanel = new JPanel(rootCardLayout);

        // 1. Login View
        loginPanel = new LoginPanel(this);
        rootCardPanel.add(loginPanel, "LOGIN");

        // 2. Main Work View
        JPanel pnlMainApp = new JPanel(new BorderLayout());
        pnlMainApp.setBackground(new Color(0xF5, 0xEF, 0xE6));

        // Top App Bar
        JPanel pnlTopBar = new JPanel(new BorderLayout());
        pnlTopBar.setBackground(new Color(0x3E, 0x27, 0x23));
        pnlTopBar.setBorder(new EmptyBorder(8, 15, 8, 15));

        JPanel pnlBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBrand.setOpaque(false);
        // JLabel lblMascot = new JLabel(MascotIcon.getMascotIcon(42, 42));
        JLabel lblBrandTitle = new JLabel("COFFEE NI DAWGZ");
        lblBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrandTitle.setForeground(new Color(0xFF, 0xEC, 0xB3));
        // pnlBrand.add(lblMascot);
        pnlBrand.add(lblBrandTitle);

        JPanel pnlUserArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlUserArea.setOpaque(false);

        lblUserInfo = new JLabel("User: Guest");
        lblUserInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUserInfo.setForeground(new Color(0xFF, 0xF8, 0xE7));

        StyledButton btnLogout = new StyledButton("Logout", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.addActionListener(e -> performLogout());

        pnlUserArea.add(lblUserInfo);
        pnlUserArea.add(btnLogout);

        pnlTopBar.add(pnlBrand, BorderLayout.WEST);
        pnlTopBar.add(pnlUserArea, BorderLayout.EAST);
        pnlMainApp.add(pnlTopBar, BorderLayout.NORTH);

        // Sidebar Navigation & Content Area
        pnlSidebar = new JPanel();
        pnlSidebar.setLayout(new BoxLayout(pnlSidebar, BoxLayout.Y_AXIS));
        pnlSidebar.setBackground(new Color(0x4E, 0x34, 0x2E));
        pnlSidebar.setPreferredSize(new Dimension(210, 0));
        pnlSidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

        pnlMainApp.add(pnlSidebar, BorderLayout.WEST);

        // Content Area CardLayout
        contentCardLayout = new CardLayout();
        contentCardPanel = new JPanel(contentCardLayout);

        posPanel = new POSPanel(this);
        dashboardPanel = new DashboardPanel();
        productPanel = new ProductManagementPanel();
        inventoryPanel = new InventoryPanel();
        employeePanel = new EmployeeManagementPanel();
        reportsPanel = new ReportsPanel();
        transactionsPanel = new TransactionsPanel();
        settingsPanel = new SettingsPanel();
        backupPanel = new BackupRestorePanel();

        contentCardPanel.add(posPanel, "POS");
        contentCardPanel.add(dashboardPanel, "DASHBOARD");
        contentCardPanel.add(productPanel, "PRODUCTS");
        contentCardPanel.add(inventoryPanel, "INVENTORY");
        contentCardPanel.add(employeePanel, "EMPLOYEES");
        contentCardPanel.add(reportsPanel, "REPORTS");
        contentCardPanel.add(transactionsPanel, "TRANSACTIONS");
        contentCardPanel.add(settingsPanel, "SETTINGS");
        contentCardPanel.add(backupPanel, "BACKUP");

        pnlMainApp.add(contentCardPanel, BorderLayout.CENTER);

        rootCardPanel.add(pnlMainApp, "MAIN");
        add(rootCardPanel);

        // Start at LOGIN screen
        rootCardLayout.show(rootCardPanel, "LOGIN");
    }

    public void onLoginSuccess() {
        var user = AuthService.getInstance().getCurrentUser();
        if (user == null)
            return;

        lblUserInfo.setText(user.getFullName() + " (" + user.getRole() + ")");

        // Rebuild sidebar based on user role
        pnlSidebar.removeAll();

        if (user.isAdmin()) {
            pnlSidebar.add(createNavBtn("Cashier POS", "POS"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("Dashboard", "DASHBOARD"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("Products", "PRODUCTS"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("Inventory", "INVENTORY"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("Employees", "EMPLOYEES"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("Sales Reports", "REPORTS"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("Transactions", "TRANSACTIONS"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("Settings", "SETTINGS"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("Backup / Restore", "BACKUP"));

            contentCardLayout.show(contentCardPanel, "DASHBOARD");
            dashboardPanel.refreshDashboardData();
        } else {
            // Cashier role
            pnlSidebar.add(createNavBtn("Cashier POS", "POS"));
            pnlSidebar.add(Box.createVerticalStrut(8));
            pnlSidebar.add(createNavBtn("My Transactions", "TRANSACTIONS"));

            contentCardLayout.show(contentCardPanel, "POS");
            posPanel.loadProducts(0);
        }

        pnlSidebar.revalidate();
        pnlSidebar.repaint();

        rootCardLayout.show(rootCardPanel, "MAIN");
    }

    private JButton createNavBtn(String title, String cardName) {
        StyledButton btn = new StyledButton(title, new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(190, 40));
        btn.setMaximumSize(new Dimension(190, 40));
        btn.setMinimumSize(new Dimension(190, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addActionListener(e -> {
            contentCardLayout.show(contentCardPanel, cardName);
            if ("POS".equals(cardName))
                posPanel.loadProducts(0);
            else if ("DASHBOARD".equals(cardName))
                dashboardPanel.refreshDashboardData();
            else if ("PRODUCTS".equals(cardName))
                productPanel.refreshProducts();
            else if ("INVENTORY".equals(cardName))
                inventoryPanel.refreshData();
            else if ("EMPLOYEES".equals(cardName))
                employeePanel.refreshUsers();
            else if ("REPORTS".equals(cardName))
                reportsPanel.generateReport(1);
            else if ("TRANSACTIONS".equals(cardName))
                transactionsPanel.refreshTransactions();
            else if ("SETTINGS".equals(cardName))
                settingsPanel.loadSettings();
        });
        return btn;
    }

    private void performLogout() {
        AuthService.getInstance().logout();
        rootCardLayout.show(rootCardPanel, "LOGIN");
    }
}
