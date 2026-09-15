package com.coffeenidawgz.models;

public class Discount {
    private int id;
    private String name;
    private String type; // PERCENTAGE or FIXED
    private double value; // e.g., 20 for 20% or 50 for ₱50
    private String status; // ACTIVE, INACTIVE

    public Discount() {}

    public Discount(int id, String name, String type, double value, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double calculateDiscount(double subtotal) {
        if ("PERCENTAGE".equalsIgnoreCase(type)) {
            return subtotal * (value / 100.0);
        } else {
            return Math.min(subtotal, value);
        }
    }

    @Override
    public String toString() {
        if ("PERCENTAGE".equalsIgnoreCase(type)) {
            return String.format("%s (%.0f%%)", name, value);
        } else {
            return String.format("%s (₱%.2f)", name, value);
        }
    }
}
