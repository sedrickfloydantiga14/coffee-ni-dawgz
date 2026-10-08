package com.coffeenidawgz.ui;

import com.coffeenidawgz.dao.CategoryDAO;
import com.coffeenidawgz.dao.ProductDAO;
import com.coffeenidawgz.models.Category;
import com.coffeenidawgz.models.Product;
import com.coffeenidawgz.utils.CurrencyFormatter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

    /**
     * Copies the selected image file into images/products/ with a unique filename.
     * Returns the relative path (e.g. "images/products/latte_1695812345678.jpg").
     */
    private String copyImageToProductsDir(File sourceFile) {
        File productsDir = new File("images/products");
        if (!productsDir.exists()) {
            productsDir.mkdirs();
        }

        String originalName = sourceFile.getName();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? originalName.substring(0, dotIndex) : originalName;
        String extension = dotIndex > 0 ? originalName.substring(dotIndex) : ".jpg";

        // Sanitize the base name
        baseName = baseName.replaceAll("[^a-zA-Z0-9_\\-]", "_");

        // Generate unique filename with timestamp
        String uniqueName = baseName + "_" + System.currentTimeMillis() + extension;
        File destFile = new File(productsDir, uniqueName);

        // Extra safety: if somehow still exists, append counter
        int counter = 1;
        while (destFile.exists()) {
            uniqueName = baseName + "_" + System.currentTimeMillis() + "_" + counter + extension;
            destFile = new File(productsDir, uniqueName);
            counter++;
        }

        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return "images/products/" + uniqueName;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void openProductDialog(Product existingProduct) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existingProduct == null ? "Add New Product" : "Edit Product", true);
        dlg.setSize(480, 680);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

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

        // --- Image picker components ---
        // Holds the File chosen by the user (null if none chosen yet)
        final File[] selectedImageFile = {null};
        // Holds the current image path (for editing existing products)
        final String[] currentImagePath = {existingProduct != null ? existingProduct.getImagePath() : null};

        JTextField txtImagePath = new JTextField();
        txtImagePath.setEditable(false);
        txtImagePath.setBackground(new Color(0xF5, 0xEF, 0xE6));

        StyledButton btnChooseImage = new StyledButton("Choose Image", new Color(0x6D, 0x4C, 0x41), Color.WHITE, 4);
        btnChooseImage.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnChooseImage.setPreferredSize(new Dimension(120, 28));

        // Image preview label
        JLabel lblImagePreview = new JLabel("No image selected", JLabel.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(0, 100));
        lblImagePreview.setMinimumSize(new Dimension(100, 100));
        lblImagePreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xB9), 1, true),
                new EmptyBorder(4, 4, 4, 4)));
        lblImagePreview.setBackground(new Color(0xFF, 0xFA, 0xF0));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblImagePreview.setForeground(new Color(0x8D, 0x6E, 0x63));

        // Load existing image preview if editing
        if (existingProduct != null && existingProduct.getImagePath() != null && !existingProduct.getImagePath().trim().isEmpty()) {
            txtImagePath.setText(existingProduct.getImagePath());
            loadPreviewFromPath(existingProduct.getImagePath(), lblImagePreview);
        }

        btnChooseImage.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Product Image");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "Image Files (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"));

            int result = fileChooser.showOpenDialog(dlg);
            if (result == JFileChooser.APPROVE_OPTION) {
                File chosen = fileChooser.getSelectedFile();
                if (chosen != null && chosen.exists()) {
                    // Validate it's a readable image
                    try {
                        BufferedImage testImg = ImageIO.read(chosen);
                        if (testImg == null) {
                            JOptionPane.showMessageDialog(dlg,
                                    "The selected file is not a valid image.",
                                    "Invalid Image", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        selectedImageFile[0] = chosen;
                        currentImagePath[0] = null; // will be set on save
                        txtImagePath.setText(chosen.getName());

                        // Show preview
                        int scaledWidth = (int) ((double) testImg.getWidth() / testImg.getHeight() * 90);
                        Image scaled;
                        if (scaledWidth > 200) {
                            scaled = testImg.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
                        } else {
                            scaled = testImg.getScaledInstance(-1, 90, Image.SCALE_SMOOTH);
                        }
                        lblImagePreview.setIcon(new ImageIcon(scaled));
                        lblImagePreview.setText("");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dlg,
                                "Could not read the selected file:\n" + ex.getMessage(),
                                "Image Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            // If cancelled, do nothing — keep existing selection
        });

        if (existingProduct != null) {
            txtName.setText(existingProduct.getName());
            txtDesc.setText(existingProduct.getDescription());
            txtSmall.setText(String.valueOf(existingProduct.getSmallPrice()));
            txtMed.setText(String.valueOf(existingProduct.getMediumPrice()));
            txtLarge.setText(String.valueOf(existingProduct.getLargePrice()));
            txtStock.setText(String.valueOf(existingProduct.getStockQuantity()));
            txtThreshold.setText(String.valueOf(existingProduct.getLowStockThreshold()));
        }

        // Row 0: Category
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        pnlForm.add(cboCategory, gbc);
        gbc.gridwidth = 1;

        // Row 1: Product Name
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        pnlForm.add(txtName, gbc);
        gbc.gridwidth = 1;

        // Row 2: Description
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        pnlForm.add(txtDesc, gbc);
        gbc.gridwidth = 1;

        // Row 3: Small Price
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Small Price (\u20B1):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        pnlForm.add(txtSmall, gbc);
        gbc.gridwidth = 1;

        // Row 4: Medium Price
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Medium Price (\u20B1):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        pnlForm.add(txtMed, gbc);
        gbc.gridwidth = 1;

        // Row 5: Large Price
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Large Price (\u20B1):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        pnlForm.add(txtLarge, gbc);
        gbc.gridwidth = 1;

        // Row 6: Stock
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        pnlForm.add(txtStock, gbc);
        gbc.gridwidth = 1;

        // Row 7: Low Stock Threshold
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Low Stock Alert Limit:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        pnlForm.add(txtThreshold, gbc);
        gbc.gridwidth = 1;

        // Row 8: Product Image (label + text field + button)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Product Image:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnlForm.add(txtImagePath, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        pnlForm.add(btnChooseImage, gbc);

        // Row 9: Image Preview
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        pnlForm.add(new JLabel("Preview:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        pnlForm.add(lblImagePreview, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        dlg.add(new JScrollPane(pnlForm), BorderLayout.CENTER);

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

                // Determine image path
                String imagePath;
                if (selectedImageFile[0] != null) {
                    // User chose a new image — copy it to images/products/
                    imagePath = copyImageToProductsDir(selectedImageFile[0]);
                    if (imagePath == null) {
                        JOptionPane.showMessageDialog(dlg,
                                "Failed to copy the selected image. The product will be saved without a new image.",
                                "Image Copy Warning", JOptionPane.WARNING_MESSAGE);
                        // Fall back to existing path or empty
                        imagePath = currentImagePath[0] != null ? currentImagePath[0] : "";
                    }
                } else if (currentImagePath[0] != null) {
                    // Editing and keeping existing image
                    imagePath = currentImagePath[0];
                } else {
                    // No image selected
                    imagePath = "";
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
                p.setImagePath(imagePath);

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

    /**
     * Loads an image preview from a relative image path (e.g. "images/Matchalatte.jpg"
     * or "images/products/latte_123.jpg") and sets it on the preview label.
     */
    private void loadPreviewFromPath(String imagePath, JLabel previewLabel) {
        if (imagePath == null || imagePath.trim().isEmpty()) return;
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                if (img != null) {
                    int scaledWidth = (int) ((double) img.getWidth() / img.getHeight() * 90);
                    Image scaled;
                    if (scaledWidth > 200) {
                        scaled = img.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
                    } else {
                        scaled = img.getScaledInstance(-1, 90, Image.SCALE_SMOOTH);
                    }
                    previewLabel.setIcon(new ImageIcon(scaled));
                    previewLabel.setText("");
                    return;
                }
            }
        } catch (IOException ignored) {}
        previewLabel.setIcon(null);
        previewLabel.setText("Image not found");
    }
}
