package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.CategoryDAO;
import com.coffeenidawgz.dao.DiscountDAO;
import com.coffeenidawgz.dao.ProductDAO;
import com.coffeenidawgz.dao.SettingDAO;
import com.coffeenidawgz.models.Category;
import com.coffeenidawgz.models.Discount;
import com.coffeenidawgz.models.Order;
import com.coffeenidawgz.models.OrderItem;
import com.coffeenidawgz.models.Product;
import com.coffeenidawgz.services.AuthService;
import com.coffeenidawgz.services.POSService;
import com.coffeenidawgz.utils.CurrencyFormatter;
import com.coffeenidawgz.utils.MascotIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class POSPanel extends JPanel {
    private final MainFrame mainFrame;
    private final POSService posService = new POSService();
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final DiscountDAO discountDAO = new DiscountDAO();
    private final SettingDAO settingDAO = new SettingDAO();

    private JPanel pnlProductGrid;
    private JTable tblCart;
    private DefaultTableModel cartTableModel;
    private JLabel lblSubtotal, lblDiscount, lblVat, lblTotal;
    private JComboBox<Discount> cboDiscounts;
    private JRadioButton rdoCash, rdoGCash;
    private JTextField txtCashReceived;
    private JLabel lblChange;
    private JButton btnCheckout;
    private JCheckBox chkGCashVerified;

    public POSPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // LEFT: Products Navigation & Grid
        JPanel pnlLeft = new JPanel(new BorderLayout(5, 5));
        pnlLeft.setOpaque(false);

        // Category Tabs Bar (Top)
        JPanel pnlCatTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pnlCatTabs.setBackground(new Color(0x3E, 0x27, 0x23));
        pnlCatTabs.setBorder(new EmptyBorder(4, 4, 4, 4));

        JButton btnAll = createCategoryButton("ALL", 0);
        pnlCatTabs.add(btnAll);

        List<Category> categories = categoryDAO.getAllCategories();
        for (Category cat : categories) {
            if (!cat.getName().equalsIgnoreCase("ADD-ONS")) {
                pnlCatTabs.add(createCategoryButton(cat.getName(), cat.getId()));
            }
        }
        pnlLeft.add(pnlCatTabs, BorderLayout.NORTH);

        // Product Grid Panel (Up & Down Scrollable)
        pnlProductGrid = new JPanel(new GridLayout(0, 3, 12, 12));
        pnlProductGrid.setOpaque(false);

        JPanel pnlGridWrapper = new JPanel(new BorderLayout());
        pnlGridWrapper.setOpaque(false);
        pnlGridWrapper.add(pnlProductGrid, BorderLayout.NORTH);

        JScrollPane scrollGrid = new JScrollPane(pnlGridWrapper);
        scrollGrid.setBorder(null);
        scrollGrid.setOpaque(false);
        scrollGrid.getViewport().setOpaque(false);
        scrollGrid.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollGrid.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);
        pnlLeft.add(scrollGrid, BorderLayout.CENTER);

        add(pnlLeft, BorderLayout.CENTER);

        // RIGHT: Cart & Checkout Panel
        JPanel pnlCart = new JPanel(new BorderLayout(5, 5));
        pnlCart.setPreferredSize(new Dimension(420, 0));
        pnlCart.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnlCart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9), 2, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Cart Header
        JLabel lblCartHeader = new JLabel("Shopping Cart", JLabel.LEFT);
        lblCartHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCartHeader.setForeground(new Color(0x3E, 0x27, 0x23));
        pnlCart.add(lblCartHeader, BorderLayout.NORTH);

        // Cart Table
        String[] cols = {"Item Details", "Qty", "Price", "Total"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCart = new JTable(cartTableModel);
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblCart.setRowHeight(40);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(180);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(65);
        tblCart.getColumnModel().getColumn(3).setPreferredWidth(70);

        JScrollPane scrollCart = new JScrollPane(tblCart);
        scrollCart.setBorder(BorderFactory.createLineBorder(new Color(0xE0, 0xD0, 0xC0)));
        pnlCart.add(scrollCart, BorderLayout.CENTER);

        // Cart Action Controls (Remove / Quantity / Clear)
        JPanel pnlCartControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlCartControls.setOpaque(false);

        StyledButton btnIncrease = new StyledButton("+", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 4);
        btnIncrease.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnIncrease.addActionListener(e -> updateCartQty(1));

        StyledButton btnDecrease = new StyledButton("-", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 4);
        btnDecrease.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDecrease.addActionListener(e -> updateCartQty(-1));

        StyledButton btnRemove = new StyledButton("Remove Item", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 4);
        btnRemove.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemove.addActionListener(e -> removeSelectedCartItem());

        StyledButton btnClear = new StyledButton("Clear Cart", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 4);
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.addActionListener(e -> {
            posService.clearCart();
            refreshCartDisplay();
        });

        pnlCartControls.add(btnDecrease);
        pnlCartControls.add(btnIncrease);
        pnlCartControls.add(btnRemove);
        pnlCartControls.add(btnClear);

        // Summary & Payment Panel
        JPanel pnlSummary = new JPanel();
        pnlSummary.setLayout(new BoxLayout(pnlSummary, BoxLayout.Y_AXIS));
        pnlSummary.setOpaque(false);
        pnlSummary.setBorder(new EmptyBorder(8, 0, 0, 0));

        pnlSummary.add(pnlCartControls);
        pnlSummary.add(Box.createVerticalStrut(10));

        // Subtotal, Discount, VAT, Total labels
        lblSubtotal = new JLabel("Subtotal: ₱0.00");
        lblSubtotal.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Discount selector
        JPanel pnlDisc = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlDisc.setOpaque(false);
        pnlDisc.add(new JLabel("Discount:"));
        cboDiscounts = new JComboBox<>();
        cboDiscounts.addItem(null); // No discount option
        List<Discount> discounts = discountDAO.getAllDiscounts();
        for (Discount d : discounts) cboDiscounts.addItem(d);
        cboDiscounts.addActionListener(e -> {
            posService.setSelectedDiscount((Discount) cboDiscounts.getSelectedItem());
            refreshCartDisplay();
        });
        pnlDisc.add(cboDiscounts);

        lblDiscount = new JLabel("Discount: -₱0.00");
        lblDiscount.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblVat = new JLabel("VAT (12%): ₱0.00");
        lblVat.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblTotal = new JLabel("TOTAL: ₱0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotal.setForeground(new Color(0x3E, 0x27, 0x23));

        pnlSummary.add(lblSubtotal);
        pnlSummary.add(pnlDisc);
        pnlSummary.add(lblDiscount);
        pnlSummary.add(lblVat);
        pnlSummary.add(Box.createVerticalStrut(5));
        pnlSummary.add(lblTotal);
        pnlSummary.add(Box.createVerticalStrut(10));

        // Payment Method Box
        JPanel pnlPayment = new JPanel(new GridLayout(0, 1, 4, 4));
        pnlPayment.setOpaque(false);
        pnlPayment.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)),
                "Payment Method",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(0x3E, 0x27, 0x23)
        ));

        rdoCash = new JRadioButton("CASH", true);
        rdoGCash = new JRadioButton("GCASH", false);
        ButtonGroup bgPayment = new ButtonGroup();
        bgPayment.add(rdoCash);
        bgPayment.add(rdoGCash);

        JPanel pnlCashInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlCashInput.setOpaque(false);
        pnlCashInput.add(new JLabel("Cash Paid: ₱"));
        txtCashReceived = new JTextField(8);
        txtCashReceived.setText("0.00");
        pnlCashInput.add(txtCashReceived);
        lblChange = new JLabel("Change: ₱0.00");
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlCashInput.add(lblChange);

        chkGCashVerified = new JCheckBox("GCash Payment Verified Offline");
        chkGCashVerified.setVisible(false);

        rdoCash.addActionListener(e -> {
            pnlCashInput.setVisible(true);
            chkGCashVerified.setVisible(false);
        });
        rdoGCash.addActionListener(e -> {
            pnlCashInput.setVisible(false);
            chkGCashVerified.setVisible(true);
            String gcashNum = settingDAO.getSetting("GCASH_NUMBER", "0917-123-4567");
            String gcashName = settingDAO.getSetting("GCASH_NAME", "COFFEE NI DAWGZ POS");
            JOptionPane.showMessageDialog(this,
                    "OFFLINE GCASH INSTRUCTIONS:\n\n" +
                    "Send payment to GCash:\n" +
                    "Account Name: " + gcashName + "\n" +
                    "Account Number: " + gcashNum + "\n" +
                    "Total Amount: " + CurrencyFormatter.format(posService.getTotal()) + "\n\n" +
                    "Cashier must verify customer's reference SMS before completing order.",
                    "GCash Information",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        pnlPayment.add(rdoCash);
        pnlPayment.add(pnlCashInput);
        pnlPayment.add(rdoGCash);
        pnlPayment.add(chkGCashVerified);

        pnlSummary.add(pnlPayment);
        pnlSummary.add(Box.createVerticalStrut(10));

        // Checkout Button
        btnCheckout = new StyledButton("COMPLETE TRANSACTION", new Color(0x3E, 0x27, 0x23), new Color(0xFF, 0xEC, 0xB3), 8);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCheckout.setMaximumSize(new Dimension(380, 48));
        btnCheckout.addActionListener(e -> performCheckout());

        pnlSummary.add(btnCheckout);

        pnlCart.add(pnlSummary, BorderLayout.SOUTH);
        add(pnlCart, BorderLayout.EAST);

        // Load Products initially
        loadProducts(0);
    }

    private JButton createCategoryButton(String label, int catId) {
        StyledButton btn = new StyledButton(label, new Color(0x5D, 0x40, 0x37), Color.WHITE, 6);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.addActionListener(e -> loadProducts(catId));
        return btn;
    }

    public void loadProducts(int categoryId) {
        pnlProductGrid.removeAll();
        List<Product> products = categoryId == 0 ? productDAO.getAllProducts() : productDAO.getProductsByCategory(categoryId);

        for (Product p : products) {
            JPanel card = new JPanel(new BorderLayout(10, 5));
            card.setPreferredSize(new Dimension(0, 92));
            card.setBackground(new Color(0xFF, 0xFA, 0xF0));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(p.isOutOfStock() ? new Color(0xEF, 0x53, 0x50) : (p.isLowStock() ? new Color(0xFF, 0xB7, 0x4D) : new Color(0xD7, 0xCC, 0xB9)), 2, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));

            // Icon / Badge
            JLabel lblIcon = new JLabel(MascotIcon.getProductIcon(p, 60, 60), JLabel.CENTER);

            // Details
            JPanel pnlInfo = new JPanel();
            pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
            pnlInfo.setOpaque(false);

            JLabel lblName = new JLabel(p.getName());
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblName.setForeground(new Color(0x3E, 0x27, 0x23));

            String priceRange = CurrencyFormatter.format(p.getSmallPrice());
            if (p.getLargePrice() > p.getSmallPrice()) {
                priceRange += " - " + CurrencyFormatter.format(p.getLargePrice());
            }
            JLabel lblPrice = new JLabel(priceRange);
            lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblPrice.setForeground(new Color(0x6D, 0x4C, 0x41));

            JLabel lblStock = new JLabel();
            lblStock.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if (p.isOutOfStock()) {
                lblStock.setText("OUT OF STOCK");
                lblStock.setForeground(new Color(0xD3, 0x2F, 0x2F));
            } else if (p.isLowStock()) {
                lblStock.setText("LOW STOCK (" + p.getStockQuantity() + ")");
                lblStock.setForeground(new Color(0xE6, 0x51, 0x00));
            } else {
                lblStock.setText("Stock: " + p.getStockQuantity());
                lblStock.setForeground(new Color(0x38, 0x8E, 0x3C));
            }

            pnlInfo.add(lblName);
            pnlInfo.add(lblPrice);
            pnlInfo.add(lblStock);

            card.add(lblIcon, BorderLayout.WEST);
            card.add(pnlInfo, BorderLayout.CENTER);

            if (!p.isOutOfStock()) {
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        openCustomization(p);
                    }
                });
            }

            pnlProductGrid.add(card);
        }

        pnlProductGrid.revalidate();
        pnlProductGrid.repaint();
    }

    private void openCustomization(Product product) {
        CustomizationDialog dlg = new CustomizationDialog(mainFrame, product);
        dlg.setVisible(true);
        OrderItem item = dlg.getResultItem();
        if (item != null) {
            posService.addItemToCart(item);
            refreshCartDisplay();
        }
    }

    private void refreshCartDisplay() {
        cartTableModel.setRowCount(0);
        for (OrderItem item : posService.getCart()) {
            StringBuilder details = new StringBuilder("<html><b>" + item.getProductName() + "</b> (" + item.getSize() + ")");
            if (!"N/A".equals(item.getTemperature())) details.append("<br>").append(item.getTemperature());
            if (!"N/A".equals(item.getSugarLevel())) details.append(", Sugar: ").append(item.getSugarLevel());
            if (!"N/A".equals(item.getMilkOption())) details.append(", ").append(item.getMilkOption());

            cartTableModel.addRow(new Object[]{
                    details.toString(),
                    item.getQuantity(),
                    CurrencyFormatter.format(item.getUnitPrice()),
                    CurrencyFormatter.format(item.getTotalPrice())
            });
        }

        lblSubtotal.setText("Subtotal: " + CurrencyFormatter.format(posService.getSubtotal()));
        lblDiscount.setText("Discount: -" + CurrencyFormatter.format(posService.getDiscountAmount()));
        lblVat.setText("VAT (12%): " + CurrencyFormatter.format(posService.getVatAmount()));
        lblTotal.setText("TOTAL: " + CurrencyFormatter.format(posService.getTotal()));
    }

    private void updateCartQty(int delta) {
        int row = tblCart.getSelectedRow();
        if (row >= 0) {
            int currentQty = posService.getCart().get(row).getQuantity();
            posService.updateQuantity(row, currentQty + delta);
            refreshCartDisplay();
        }
    }

    private void removeSelectedCartItem() {
        int row = tblCart.getSelectedRow();
        if (row >= 0) {
            posService.removeItemFromCart(row);
            refreshCartDisplay();
        }
    }

    private void performCheckout() {
        if (posService.getCart().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Shopping cart is empty!", "Cart Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paymentMethod = rdoCash.isSelected() ? "CASH" : "GCASH";
        double amountReceived = 0.0;

        if ("CASH".equals(paymentMethod)) {
            try {
                amountReceived = Double.parseDouble(txtCashReceived.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid cash amount!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!posService.validateCashPayment(amountReceived)) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient cash received!\nTotal: " + CurrencyFormatter.format(posService.getTotal()) +
                        "\nReceived: " + CurrencyFormatter.format(amountReceived),
                        "Payment Rejected",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // GCash
            if (!chkGCashVerified.isSelected()) {
                JOptionPane.showMessageDialog(this, "Please confirm offline GCash payment verification first!", "GCash Verification Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            amountReceived = posService.getTotal();
        }

        Order completedOrder = posService.checkout(AuthService.getInstance().getCurrentUser(), paymentMethod, amountReceived);
        if (completedOrder != null) {
            refreshCartDisplay();
            loadProducts(0); // Refresh stock counts in grid
            txtCashReceived.setText("0.00");
            chkGCashVerified.setSelected(false);

            // Show Receipt Dialog
            ReceiptDialog dlg = new ReceiptDialog(mainFrame, completedOrder);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Transaction failed! Please check stock levels.", "Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
