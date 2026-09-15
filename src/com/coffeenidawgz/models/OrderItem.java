package com.coffeenidawgz.models;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private String productName;
    private String size; // Small, Medium, Large, N/A
    private String temperature; // Hot, Iced, N/A
    private String sugarLevel; // 0%, 25%, 50%, 75%, 100%, N/A
    private String milkOption; // Fresh Milk, Full Cream, etc.
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private List<AddOn> addOns = new ArrayList<>();

    public OrderItem() {}

    public OrderItem(int id, int orderId, int productId, String productName, String size,
                     String temperature, String sugarLevel, String milkOption,
                     int quantity, double unitPrice, double totalPrice) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.size = size;
        this.temperature = temperature;
        this.sugarLevel = sugarLevel;
        this.milkOption = milkOption;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getSugarLevel() { return sugarLevel; }
    public void setSugarLevel(String sugarLevel) { this.sugarLevel = sugarLevel; }

    public String getMilkOption() { return milkOption; }
    public void setMilkOption(String milkOption) { this.milkOption = milkOption; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public List<AddOn> getAddOns() { return addOns; }
    public void setAddOns(List<AddOn> addOns) { this.addOns = addOns; }

    public void calculateTotal() {
        double addOnsPrice = 0.0;
        if (addOns != null) {
            for (AddOn a : addOns) {
                addOnsPrice += a.getPrice();
            }
        }
        this.totalPrice = (this.unitPrice + addOnsPrice) * this.quantity;
    }
}
