package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.CategoryDAO;
import com.coffeenidawgz.dao.ProductDAO;
import com.coffeenidawgz.models.Category;
import com.coffeenidawgz.models.Product;
import com.coffeenidawgz.utils.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductManagementPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private JTable tblProducts;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private List<Product> currentProductList;

    public ProductManagementPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5, 0xEF, 0xE6));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header & Toolbar
        JPanel pnlTop = new JPanel(new BorderLayout(10, 10));
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("PRODUCT MANAGEMENT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0x3E, 0x27, 0x23));
        pnlTop.add(lblTitle, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        pnlActions.add(new JLabel("Search:"));
        txtSearch = new JTextField(15);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterProducts(txtSearch.getText().trim());
            }
        });
        pnlActions.add(txtSearch);

        StyledButton btnAdd = new StyledButton("Add New Product", new Color(0x4E, 0x34, 0x2E), Color.WHITE, 6);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.addActionListener(e -> openProductDialog(null));

        StyledButton btnEdit = new StyledButton("Edit Selected", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 6);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.addActionListener(e -> editSelectedProduct());

        StyledButton btnDelete = new StyledButton("Delete Selected", new Color(0xC6, 0x28, 0x28), Color.WHITE, 6);
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.addActionListener(e -> deleteSelectedProduct());

        pnlActions.add(btnAdd);
        pnlActions.add(btnEdit);
        pnlActions.add(btnDelete);

        pnlTop.add(pnlActions, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Category", "Name", "Description", "Small Price", "Medium Price", "Large Price", "Stock", "Low Threshold", "Image Path"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblProducts = new JTable(tableModel);
        tblProducts.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblProducts.setRowHeight(28);

        JScrollPane scroll = new JScrollPane(tblProducts);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9)));
        add(scroll, BorderLayout.CENTER);

        refreshProducts();
    }

    public void refreshProducts() {
        currentProductList = productDAO.getAllProducts();
        populateTable(currentProductList);
    }

    private void populateTable(List<Product> list) {
        tableModel.setRowCount(0);
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getCategoryName(),
                    p.getName(),
                    p.getDescription(),
                    CurrencyFormatter.format(p.getSmallPrice()),
                    CurrencyFormatter.format(p.getMediumPrice()),
                    CurrencyFormatter.format(p.getLargePrice()),
                    p.getStockQuantity(),
                    p.getLowStockThreshold(),
                    p.getImagePath() != null ? p.getImagePath() : ""
            });
        }
    }

    private void filterProducts(String keyword) {
        if (keyword.isEmpty()) {
            populateTable(currentProductList);
            return;
        }
        String lower = keyword.toLowerCase();
        List<Product> filtered = currentProductList.stream()
                .filter(p -> p.getName().toLowerCase().contains(lower) || p.getCategoryName().toLowerCase().contains(lower))
                .toList();
        populateTable(filtered);
    }

    private void editSelectedProduct() {
        int row = tblProducts.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (Integer) tableModel.getValueAt(row, 0);
        Product product = productDAO.getProductById(id);
        if (product != null) {
            openProductDialog(product);
        }
    }

    private void deleteSelectedProduct() {
        int row = tblProducts.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete/deactivate product: " + name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = productDAO.deleteProduct(id);
            if (success) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openProductDialog(Product existingProduct) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existingProduct == null ? "Add New Product" : "Edit Product", true);
        dlg.setSize(460, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlForm = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlForm.setBorder(new EmptyBorder(15, 20, 15, 20));

        JComboBox<Category> cboCategory = new JComboBox<>();
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category c : categories) cboCategory.addItem(c);

        JTextField txtName = new JTextField();
        JTextField txtDesc = new JTextField();
        JTextField txtSmall = new JTextField("0.00");
        JTextField txtMed = new JTextField("0.00");
        JTextField txtLarge = new JTextField("0.00");
        JTextField txtStock = new JTextField("20");
        JTextField txtThreshold = new JTextField("5");

        JComboBox<String> cboImages = new JComboBox<>();
        cboImages.addItem("images/Americano.jpg");
        cboImages.addItem("images/Latte.jpg");
        cboImages.addItem("images/Mocha.jpg");
        cboImages.addItem("images/IcedMocha.jpg");
        cboImages.addItem("images/Hotchocolate.jpg");
        cboImages.addItem("images/Matchalatte.jpg");
        cboImages.addItem("images/Fudgebrownies.jpg");
        cboImages.setEditable(true);

        if (existingProduct != null) {
            txtName.setText(existingProduct.getName());
            txtDesc.setText(existingProduct.getDescription());
            txtSmall.setText(String.valueOf(existingProduct.getSmallPrice()));
            txtMed.setText(String.valueOf(existingProduct.getMediumPrice()));
            txtLarge.setText(String.valueOf(existingProduct.getLargePrice()));
            txtStock.setText(String.valueOf(existingProduct.getStockQuantity()));
            txtThreshold.setText(String.valueOf(existingProduct.getLowStockThreshold()));
            if (existingProduct.getImagePath() != null) {
                cboImages.setSelectedItem(existingProduct.getImagePath());
            }
        }

        pnlForm.add(new JLabel("Category:")); pnlForm.add(cboCategory);
        pnlForm.add(new JLabel("Product Name:")); pnlForm.add(txtName);
        pnlForm.add(new JLabel("Description:")); pnlForm.add(txtDesc);
        pnlForm.add(new JLabel("Small Price (₱):")); pnlForm.add(txtSmall);
        pnlForm.add(new JLabel("Medium Price (₱):")); pnlForm.add(txtMed);
        pnlForm.add(new JLabel("Large Price (₱):")); pnlForm.add(txtLarge);
        pnlForm.add(new JLabel("Stock Quantity:")); pnlForm.add(txtStock);
        pnlForm.add(new JLabel("Low Stock Alert Limit:")); pnlForm.add(txtThreshold);
        pnlForm.add(new JLabel("Product Image:")); pnlForm.add(cboImages);

        dlg.add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtns.setBorder(new EmptyBorder(5, 20, 15, 20));
        pnlBtns.setOpaque(false);

        StyledButton btnCancel = new StyledButton("Cancel", new Color(0x8D, 0x6E, 0x63), Color.WHITE, 6);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.setPreferredSize(new Dimension(100, 36));
        btnCancel.addActionListener(e -> dlg.dispose());

        StyledButton btnSave = new StyledButton("Save Product", new Color(0x3E, 0x27, 0x23), new Color(0xFF, 0xEC, 0xB3), 6);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setPreferredSize(new Dimension(135, 36));
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Product name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double small = Double.parseDouble(txtSmall.getText().trim());
                double med = Double.parseDouble(txtMed.getText().trim());
                double large = Double.parseDouble(txtLarge.getText().trim());
                int stock = Integer.parseInt(txtStock.getText().trim());
                int threshold = Integer.parseInt(txtThreshold.getText().trim());

                if (small < 0 || med < 0 || large < 0 || stock < 0 || threshold < 0) {
                    JOptionPane.showMessageDialog(dlg, "Prices and stock cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Category selectedCat = (Category) cboCategory.getSelectedItem();

                Product p = existingProduct != null ? existingProduct : new Product();
                p.setCategoryId(selectedCat.getId());
                p.setName(name);
                p.setDescription(txtDesc.getText().trim());
                p.setSmallPrice(small);
                p.setMediumPrice(med);
                p.setLargePrice(large);
                p.setStockQuantity(stock);
                p.setLowStockThreshold(threshold);
                Object imgObj = cboImages.getSelectedItem();
                p.setImagePath(imgObj != null ? imgObj.toString().trim() : "");

                boolean ok = existingProduct == null ? productDAO.addProduct(p) : productDAO.updateProduct(p);
                if (ok) {
                    dlg.dispose();
                    refreshProducts();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Failed to save product!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Please enter valid numeric prices and stock!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
