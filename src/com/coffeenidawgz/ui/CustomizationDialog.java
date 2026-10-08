package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.AddOnDAO;
import com.coffeenidawgz.models.AddOn;
import com.coffeenidawgz.models.OrderItem;
import com.coffeenidawgz.models.Product;
import com.coffeenidawgz.utils.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomizationDialog extends JDialog {
    private final Product product;
    private OrderItem resultItem;

    private JRadioButton rdoSmall, rdoMedium, rdoLarge;
    private JRadioButton rdoHot, rdoIced;
    private JComboBox<String> cboSugar;
    private JComboBox<String> cboMilk;
    private List<JCheckBox> chkAddOns = new ArrayList<>();
    private List<AddOn> allAddOns;
    private JSpinner spinQty;

    public CustomizationDialog(Frame owner, Product product) {
        super(owner, "Customize — " + product.getName(), true);
        this.product = product;
        initUI();
    }

    private void initUI() {
        setSize(480, 580);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBorder(new EmptyBorder(15, 20, 15, 20));
        pnlMain.setBackground(new Color(0xFF, 0xFA, 0xF0));

        // Header
        JPanel pnlTitleHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTitleHeader.setOpaque(false);
        // JLabel lblProductImg = new
        // JLabel(com.coffeenidawgz.utils.MascotIcon.getProductIcon(product, 64, 64));

        JPanel pnlTitleText = new JPanel();
        pnlTitleText.setLayout(new BoxLayout(pnlTitleText, BoxLayout.Y_AXIS));
        pnlTitleText.setOpaque(false);

        JLabel lblHeader = new JLabel(product.getName());
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(new Color(0x3E, 0x27, 0x23));

        JLabel lblDesc = new JLabel("<html><i>" + product.getDescription() + "</i></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(0x79, 0x55, 0x48));

        pnlTitleText.add(lblHeader);
        pnlTitleText.add(Box.createVerticalStrut(4));
        pnlTitleText.add(lblDesc);

        // pnlTitleHeader.add(lblProductImg);
        pnlTitleHeader.add(pnlTitleText);

        pnlMain.add(pnlTitleHeader);
        pnlMain.add(Box.createVerticalStrut(15));

        boolean isDrink = !product.getCategoryName().equalsIgnoreCase("SNACKS");

        // 1. Size Options
        JPanel pnlSize = createSectionPanel("Select Size:");
        rdoSmall = new JRadioButton("Small (" + CurrencyFormatter.format(product.getSmallPrice()) + ")");
        rdoMedium = new JRadioButton("Medium (" + CurrencyFormatter.format(product.getMediumPrice()) + ")");
        rdoLarge = new JRadioButton("Large (" + CurrencyFormatter.format(product.getLargePrice()) + ")");

        ButtonGroup bgSize = new ButtonGroup();
        bgSize.add(rdoSmall);
        bgSize.add(rdoMedium);
        bgSize.add(rdoLarge);
        rdoMedium.setSelected(true);

        pnlSize.add(rdoSmall);
        pnlSize.add(rdoMedium);
        pnlSize.add(rdoLarge);
        pnlMain.add(pnlSize);
        pnlMain.add(Box.createVerticalStrut(10));

        if (isDrink) {
            // 2. Temperature
            JPanel pnlTemp = createSectionPanel("Temperature:");
            rdoHot = new JRadioButton("Hot");
            rdoIced = new JRadioButton("Iced");
            ButtonGroup bgTemp = new ButtonGroup();
            bgTemp.add(rdoHot);
            bgTemp.add(rdoIced);

            if (product.getCategoryName().equalsIgnoreCase("HOT DRINKS")) {
                rdoHot.setSelected(true);
            } else {
                rdoIced.setSelected(true);
            }

            pnlTemp.add(rdoHot);
            pnlTemp.add(rdoIced);
            pnlMain.add(pnlTemp);
            pnlMain.add(Box.createVerticalStrut(10));

            // 3. Sugar Level
            JPanel pnlSugar = createSectionPanel("Sugar Level:");
            cboSugar = new JComboBox<>(new String[] { "100% (Normal)", "75% (Less Sweet)", "50% (Half Sweet)",
                    "25% (Mild)", "0% (No Sugar)" });
            cboSugar.setSelectedIndex(0);
            pnlSugar.add(cboSugar);
            pnlMain.add(pnlSugar);
            pnlMain.add(Box.createVerticalStrut(10));

            // 4. Milk Option
            JPanel pnlMilk = createSectionPanel("Milk Choice:");
            cboMilk = new JComboBox<>(
                    new String[] { "Fresh Milk", "Full Cream", "Oat Milk", "Soy Milk", "Almond Milk" });
            cboMilk.setSelectedIndex(0);
            pnlMilk.add(cboMilk);
            pnlMain.add(pnlMilk);
            pnlMain.add(Box.createVerticalStrut(10));

            // 5. Add-Ons
            JPanel pnlAddOns = createSectionPanel("Add-Ons / Extra Toppings:");
            AddOnDAO addonDAO = new AddOnDAO();
            allAddOns = addonDAO.getAllAddOns();
            for (AddOn a : allAddOns) {
                JCheckBox chk = new JCheckBox(a.getName() + " (+ " + CurrencyFormatter.format(a.getPrice()) + ")");
                chkAddOns.add(chk);
                pnlAddOns.add(chk);
            }
            pnlMain.add(pnlAddOns);
            pnlMain.add(Box.createVerticalStrut(10));
        }

        // 6. Quantity
        JPanel pnlQty = createSectionPanel("Quantity:");
        spinQty = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, product.getStockQuantity()), 1));
        spinQty.setPreferredSize(new Dimension(80, 30));
        pnlQty.add(spinQty);
        pnlMain.add(pnlQty);

        JScrollPane scroll = new JScrollPane(pnlMain);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtns.setBackground(new Color(0xEF, 0xE5, 0xD8));
        pnlBtns.setBorder(new EmptyBorder(5, 15, 10, 15));

        StyledButton btnCancel = new StyledButton("Cancel", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.setPreferredSize(new Dimension(100, 36));
        btnCancel.addActionListener(e -> dispose());

        StyledButton btnAdd = new StyledButton("ADD TO CART", new Color(0x3E, 0x27, 0x23), new Color(0xFF, 0xEC, 0xB3), 6);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(140, 36));
        btnAdd.addActionListener(e -> onAddToCart());

        pnlBtns.add(btnCancel);
        pnlBtns.add(btnAdd);
        add(pnlBtns, BorderLayout.SOUTH);
    }

    private JPanel createSectionPanel(String title) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnl.setBackground(new Color(0xFF, 0xFA, 0xF0));
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)),
                title,
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(0x4E, 0x34, 0x2E)));
        return pnl;
    }

    private void onAddToCart() {
        String size = "Medium";
        double unitPrice = product.getMediumPrice();
        if (rdoSmall.isSelected()) {
            size = "Small";
            unitPrice = product.getSmallPrice();
        } else if (rdoLarge.isSelected()) {
            size = "Large";
            unitPrice = product.getLargePrice();
        }

        String temp = "N/A";
        if (rdoHot != null && rdoHot.isSelected())
            temp = "Hot";
        else if (rdoIced != null && rdoIced.isSelected())
            temp = "Iced";

        String sugar = cboSugar != null ? (String) cboSugar.getSelectedItem() : "N/A";
        String milk = cboMilk != null ? (String) cboMilk.getSelectedItem() : "N/A";
        int qty = (Integer) spinQty.getValue();

        List<AddOn> selectedAddOns = new ArrayList<>();
        if (allAddOns != null) {
            for (int i = 0; i < chkAddOns.size(); i++) {
                if (chkAddOns.get(i).isSelected()) {
                    selectedAddOns.add(allAddOns.get(i));
                }
            }
        }

        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setSize(size);
        item.setTemperature(temp);
        item.setSugarLevel(sugar);
        item.setMilkOption(milk);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setAddOns(selectedAddOns);
        item.calculateTotal();

        this.resultItem = item;
        dispose();
    }

    public OrderItem getResultItem() {
        return resultItem;
    }
}
