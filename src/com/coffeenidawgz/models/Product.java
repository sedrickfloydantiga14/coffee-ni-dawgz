package com.coffeenidawgz.models;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private int categoryId;
    private String categoryName;
    private String name;
    private String description;
    private double smallPrice;
    private double mediumPrice;
    private double largePrice;
    private int stockQuantity;
    private int lowStockThreshold;
    private String imagePath;
    private String status; // ACTIVE, INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {}

    public Product(int id, int categoryId, String categoryName, String name, String description,
                   double smallPrice, double mediumPrice, double largePrice,
                   int stockQuantity, int lowStockThreshold, String imagePath, String status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.smallPrice = smallPrice;
        this.mediumPrice = mediumPrice;
        this.largePrice = largePrice;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
        this.imagePath = imagePath;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getSmallPrice() { return smallPrice; }
    public void setSmallPrice(double smallPrice) { this.smallPrice = smallPrice; }

    public double getMediumPrice() { return mediumPrice; }
    public void setMediumPrice(double mediumPrice) { this.mediumPrice = mediumPrice; }

    public double getLargePrice() { return largePrice; }
    public void setLargePrice(double largePrice) { this.largePrice = largePrice; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public double getPriceForSize(String size) {
        if ("Small".equalsIgnoreCase(size)) return smallPrice;
        if ("Medium".equalsIgnoreCase(size)) return mediumPrice;
        if ("Large".equalsIgnoreCase(size)) return largePrice;
        return mediumPrice > 0 ? mediumPrice : smallPrice;
    }

    public boolean isLowStock() {
        return stockQuantity <= lowStockThreshold && stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }
}
